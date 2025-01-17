/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.CallActivity;
import org.flowable.bpmn.model.CompensateEventDefinition;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.SubProcess;
import org.flowable.bpmn.model.Transaction;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.common.engine.impl.util.CollectionUtil;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.bpmn.helper.ErrorPropagation;
import org.flowable.engine.impl.bpmn.helper.ScopeUtil;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.delegate.ActivityBehavior;
import org.flowable.engine.impl.jobexecutor.ParallelMultiInstanceActivityCompletionJobHandler;
import org.flowable.engine.impl.jobexecutor.ParallelMultiInstanceWithNoWaitStatesAsyncLeaveJobHandler;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.impl.util.JobUtil;
import org.flowable.engine.impl.variable.ParallelMultiInstanceLoopVariable;
import org.flowable.engine.impl.variable.ParallelMultiInstanceLoopVariableType;
import org.flowable.job.service.JobService;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.flowable.variable.api.persistence.entity.VariableInstance;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 * @author Filip Hrisafov
 */
public class ParallelMultiInstanceBehavior extends MultiInstanceActivityBehavior {

    private static final long serialVersionUID = 1L;

    public ParallelMultiInstanceBehavior(Activity activity, AbstractBpmnActivityBehavior originalActivityBehavior) {
        super(activity, originalActivityBehavior);
    }

    /**
     * Handles the parallel case of spawning the instances. Will create child executions accordingly for every instance needed.
     */
    @Override
    protected int createInstances(DelegateExecution multiInstanceRootExecution) {
        int nrOfInstances = resolveNrOfInstances(multiInstanceRootExecution);
        if (nrOfInstances < 0) {
            throw new FlowableIllegalArgumentException("Invalid number of instances: must be non-negative integer value" + ", but was " + nrOfInstances);
        } else if (nrOfInstances == 0) {
            // This is the same logic as with the Sequential multi instance behaviour.
            // Variables will not be created
            return nrOfInstances;
        }

        setLoopVariable(multiInstanceRootExecution, NUMBER_OF_INSTANCES, nrOfInstances);
        setLoopVariable(multiInstanceRootExecution, NUMBER_OF_COMPLETED_INSTANCES, ParallelMultiInstanceLoopVariable.completed(multiInstanceRootExecution.getId()));
        setLoopVariable(multiInstanceRootExecution, NUMBER_OF_ACTIVE_INSTANCES, ParallelMultiInstanceLoopVariable.active(multiInstanceRootExecution.getId()));

        List<ExecutionEntity> concurrentExecutions = new ArrayList<>();
        for (int loopCounter = 0; loopCounter < nrOfInstances; loopCounter++) {
            ExecutionEntity concurrentExecution = CommandContextUtil.getExecutionEntityManager()
                    .createChildExecution((ExecutionEntity) multiInstanceRootExecution);
            concurrentExecution.setCurrentFlowElement(activity);
            concurrentExecution.setActive(true);
            concurrentExecution.setScope(false);

            concurrentExecutions.add(concurrentExecution);
            logLoopDetails(concurrentExecution, "initialized", loopCounter, 0, nrOfInstances, nrOfInstances);
            
            //CommandContextUtil.getHistoryManager().recordActivityStart(concurrentExecution);
        }

        // Before the activities are executed, all executions MUST be created up front
        // Do not try to merge this loop with the previous one, as it will lead
        // to bugs, due to possible child execution pruning.
        for (int loopCounter = 0; loopCounter < nrOfInstances; loopCounter++) {
            ExecutionEntity concurrentExecution = concurrentExecutions.get(loopCounter);
            // executions can be inactive, if instances are all automatics
            // (no-waitstate) and completionCondition has been met in the meantime
            if (concurrentExecution.isActive() 
                    && !concurrentExecution.isEnded() 
                    && !concurrentExecution.getParent().isEnded()) {
                executeOriginalBehavior(concurrentExecution, (ExecutionEntity) multiInstanceRootExecution, loopCounter);
            } 
        }

        // See ACT-1586: ExecutionQuery returns wrong results when using multi
        // instance on a receive task The parent execution must be set to false, so it wouldn't show up in
        // the execution query when using .activityId(something). Do not we cannot nullify the
        // activityId (that would have been a better solution), as it would break boundary event behavior.
        if (!concurrentExecutions.isEmpty()) {
            multiInstanceRootExecution.setActive(false);
        }

        ProcessEngineConfigurationImpl processEngineConfiguration = CommandContextUtil.getProcessEngineConfiguration();
        // No need to check the completed variable for backwards compatibility(like below),
        // as this is for new instances and not for existing ones that get completed and might be started in the 'old way'
        if (isAsyncWithoutWaitStates(processEngineConfiguration)) {

            // The difference with the ParallelMultiInstanceActivityCompletionJobHandler approach is that here the job gets created up front
            // (as the flag for no wait states has been set, this won't create an ever-recreating job).

            JobEntity job = JobUtil.createJob(concurrentExecutions.get(0), ParallelMultiInstanceWithNoWaitStatesAsyncLeaveJobHandler.TYPE, processEngineConfiguration);
            JobService jobService = processEngineConfiguration.getJobServiceConfiguration().getJobService();
            jobService.createAsyncJobNoTriggerAsyncExecutor(job, true);
            jobService.insertJob(job);
        }

        return nrOfInstances;
    }

    public boolean isAsyncWithoutWaitStates(ProcessEngineConfigurationImpl processEngineConfiguration) {
        return activity.isAsynchronous()
            && activity.getLoopCharacteristics().isNoWaitStatesAsyncLeave()
            && processEngineConfiguration.isParallelMultiInstanceAsyncLeave();
    }

    /**
     * Called when the wrapped {@link ActivityBehavior} calls the {@link AbstractBpmnActivityBehavior#leave(DelegateExecution)} method. Handles the completion of one of the parallel instances
     */
    @Override
    public void leave(DelegateExecution execution) {

        boolean zeroNrOfInstances = false;
        if (resolveNrOfInstances(execution) == 0) {
            // Empty collection, just leave.
            zeroNrOfInstances = true;
            super.leave(execution); // Plan the default leave
        }

        ProcessEngineConfigurationImpl processEngineConfiguration = CommandContextUtil.getProcessEngineConfiguration();
        if (isAsyncWithoutWaitStates(processEngineConfiguration)) {

            // Optimization: if there are active child executions in the database, the childExecutions definitely must not be fetched.
            // If there are none in the database, the child executions need to be fetched: either the multi instance is completed
            // or this logic is called at a time when things have not yet been flushed.

            ExecutionEntityManager executionEntityManager = processEngineConfiguration.getExecutionEntityManager();
            DelegateExecution miRootExecution = getMultiInstanceRootExecution(execution);
            long activeChildExecutionsCount = executionEntityManager.countActiveExecutionsByParentId(miRootExecution.getId());

            if (activeChildExecutionsCount > 0) { // there are active ones, nothing to do

                // In this 'no wait state' mode, all executions until the multi instance root need to be inactivated, to make the async job work.
                inactivateExecutionAndParentExecutions(execution, processEngineConfiguration);

                callActivityEndListeners(execution);
                aggregateVariablesForChildExecution(execution, miRootExecution);

            } else {
                internalLeave(execution, zeroNrOfInstances);
            }

        } else {
            internalLeave(execution, zeroNrOfInstances);

        }

    }

    protected void internalLeave(DelegateExecution execution, boolean zeroNrOfInstances) {
        int loopCounter = getLoopVariable(execution, getCollectionElementIndexVariable());
        int nrOfInstances = getLoopVariable(execution, NUMBER_OF_INSTANCES);
        VariableInstance nrOfCompletedInstancesVariable = getLoopVariableInstance(execution, NUMBER_OF_COMPLETED_INSTANCES);
        boolean usesParallelMultiInstanceLoopVariable =
            nrOfCompletedInstancesVariable != null && ParallelMultiInstanceLoopVariableType.TYPE_NAME.equals(nrOfCompletedInstancesVariable.getTypeName());

        int nrOfCompletedInstances = getLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES) + 1;
        int nrOfActiveInstances = getLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES) - 1;

        DelegateExecution miRootExecution = getMultiInstanceRootExecution(execution);
        if (miRootExecution != null && !usesParallelMultiInstanceLoopVariable) { // will be null in case of empty collection
            // only need to update the variables if it doesn't use the new mechanism, i.e. backwards compatibility for already running instances
            setLoopVariable(miRootExecution, NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances);
            setLoopVariable(miRootExecution, NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances);
        }

        ProcessEngineConfigurationImpl processEngineConfiguration = CommandContextUtil.getProcessEngineConfiguration();
        inactivateExecution(execution, processEngineConfiguration);

        try {
            callActivityEndListeners(execution);
        } catch (BpmnError bpmnError) {
            ErrorPropagation.propagateError(bpmnError, execution);
            return;
        }

        logLoopDetails(execution, "instance completed", loopCounter, nrOfCompletedInstances, nrOfActiveInstances, nrOfInstances);

        if (zeroNrOfInstances) {
            return;
        }

        ExecutionEntity executionEntity = (ExecutionEntity) execution;
        if (executionEntity.getParent() != null) {

            // If usesParallelMultiInstanceLoopVariable is true, it means that this instance is running
            // with the old logic and we can't async leave.
            boolean asyncLeave = usesParallelMultiInstanceLoopVariable
                && processEngineConfiguration.isParallelMultiInstanceAsyncLeave()
                && !isAsyncWithoutWaitStates(processEngineConfiguration); // when using the 'no wait states' flag, the regular leave logic should be executed.

            if (!asyncLeave) {
                // If we are not leaving async then we have to lock the parent scope
                lockFirstParentScope(executionEntity);
            }

            // When leaving one of the child executions we need to aggregate the information for it
            // Aggregation of all variables will be done in MultiInstanceActivityBehavior#leave()
            aggregateVariablesForChildExecution(execution, miRootExecution);

            boolean isCompletionConditionSatisfied = completionConditionSatisfied(execution.getParent());
            if (nrOfCompletedInstances >= nrOfInstances || isCompletionConditionSatisfied) {
                leave(executionEntity, nrOfInstances, nrOfCompletedInstances, isCompletionConditionSatisfied);

            } else if (asyncLeave) {
                JobService jobService = processEngineConfiguration.getJobServiceConfiguration().getJobService();
                JobEntity job = JobUtil.createJob(executionEntity, ParallelMultiInstanceActivityCompletionJobHandler.TYPE, processEngineConfiguration);

                jobService.createAsyncJob(job, true);
                jobService.scheduleAsyncJob(job);

            }

        } else {
            sendCompletedEvent(execution);
            super.leave(execution);
        }
    }

    protected void inactivateExecutionAndParentExecutions(DelegateExecution execution, ProcessEngineConfigurationImpl processEngineConfiguration) {
        inactivateExecution(execution, processEngineConfiguration);

        ExecutionEntity parentExecution = (ExecutionEntity) execution.getParent();
        while (!parentExecution.isMultiInstanceRoot()) {
            inactivateExecution(parentExecution, processEngineConfiguration);
            parentExecution = parentExecution.getParent();
        }
    }

    protected ExecutionEntity inactivateExecution(DelegateExecution execution, ProcessEngineConfigurationImpl processEngineConfiguration) {
        processEngineConfiguration.getActivityInstanceEntityManager().recordActivityEnd((ExecutionEntity) execution, null);
        ExecutionEntity executionEntity = (ExecutionEntity) execution;
        if (execution.getParent() != null) {
            executionEntity.inactivate();
        }
        return executionEntity;
    }

    public boolean leaveAsync(ExecutionEntity execution) {
        int nrOfInstances = getLoopVariable(execution, NUMBER_OF_INSTANCES);
        int nrOfCompletedInstances = getLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES);
        boolean isCompletionConditionSatisfied = completionConditionSatisfied(execution.getParent());
        if (nrOfCompletedInstances >= nrOfInstances || isCompletionConditionSatisfied) {
            leave(execution, nrOfInstances, nrOfCompletedInstances, isCompletionConditionSatisfied);
            return true;
        }
        return false;
    }

    protected void leave(ExecutionEntity execution, int nrOfInstances, int nrOfCompletedInstances, boolean isCompletionConditionSatisfied) {
        DelegateExecution miRootExecution = getMultiInstanceRootExecution(execution);
        ExecutionEntity leavingExecution = null;
        if (nrOfInstances > 0) {
            leavingExecution = execution.getParent();
        } else {
            CommandContextUtil.getActivityInstanceEntityManager().recordActivityEnd((ExecutionEntity) execution, null);
            leavingExecution = execution;
        }

        Activity activity = (Activity) execution.getCurrentFlowElement();
        verifyCompensation(execution, leavingExecution, activity);
        verifyCallActivity(leavingExecution, activity);

        // When we complete the Multi Instance Root execution we need to explicitly set the number of completed / active instances
        // as the ParallelMultiInstanceLoopVariable can only handle the runtime information
        setLoopVariable(miRootExecution, NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances);
        // For backwards compatibility we set the current value of the number of active instances,
        // since the execution might be completed with a completion event.
        // In this case the active instances is the number of active instances when the execution completed
        setLoopVariable(miRootExecution, NUMBER_OF_ACTIVE_INSTANCES, getLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES));

        if (isCompletionConditionSatisfied) {
            LinkedList<DelegateExecution> toVerify = new LinkedList<>(miRootExecution.getExecutions());
            while (!toVerify.isEmpty()) {
                DelegateExecution childExecution = toVerify.pop();
                if (((ExecutionEntity) childExecution).isInserted()) {
                    childExecution.inactivate();
                }

                List<? extends DelegateExecution> childExecutions = childExecution.getExecutions();
                if (childExecutions != null && !childExecutions.isEmpty()) {
                    toVerify.addAll(childExecutions);
                }
            }
            sendCompletedWithConditionEvent(leavingExecution);
        }
        else {
            sendCompletedEvent(leavingExecution);
        }

        super.leave(leavingExecution);
    }

    protected Activity verifyCompensation(DelegateExecution execution, ExecutionEntity executionToUse, Activity activity) {
        boolean hasCompensation = false;
        if (activity instanceof Transaction) {
            hasCompensation = true;
        } else if (activity instanceof SubProcess) {
            SubProcess subProcess = (SubProcess) activity;
            for (FlowElement subElement : subProcess.getFlowElements()) {
                if (subElement instanceof Activity) {
                    Activity subActivity = (Activity) subElement;
                    if (CollectionUtil.isNotEmpty(subActivity.getBoundaryEvents())) {
                        for (BoundaryEvent boundaryEvent : subActivity.getBoundaryEvents()) {
                            if (CollectionUtil.isNotEmpty(boundaryEvent.getEventDefinitions()) &&
                                    boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {

                                hasCompensation = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (hasCompensation) {
            ScopeUtil.createCopyOfSubProcessExecutionForCompensation(executionToUse);
        }
        return activity;
    }

    protected void verifyCallActivity(ExecutionEntity executionToUse, Activity activity) {
        if (activity instanceof CallActivity) {
            ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();
            if (executionToUse != null) {
                List<String> callActivityExecutionIds = new ArrayList<>();

                // Find all execution entities that are at the call activity
                List<ExecutionEntity> childExecutions = executionEntityManager.collectChildren(executionToUse);
                if (childExecutions != null) {
                    for (ExecutionEntity childExecution : childExecutions) {
                        if (activity.getId().equals(childExecution.getCurrentActivityId())) {
                            callActivityExecutionIds.add(childExecution.getId());
                        }
                    }

                    // Now all call activity executions have been collected, loop again and check which should be removed
                    for (int i = childExecutions.size() - 1; i >= 0; i--) {
                        ExecutionEntity childExecution = childExecutions.get(i);
                        if (StringUtils.isNotEmpty(childExecution.getSuperExecutionId())
                                && callActivityExecutionIds.contains(childExecution.getSuperExecutionId())) {

                            executionEntityManager.deleteProcessInstanceExecutionEntity(childExecution.getId(), activity.getId(),
                                    "call activity completion condition met", true, false, true);
                        }
                    }

                }
            }
        }
    }


    protected void lockFirstParentScope(DelegateExecution execution) {

        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();

        boolean found = false;
        ExecutionEntity parentScopeExecution = null;
        ExecutionEntity currentExecution = (ExecutionEntity) execution;
        while (!found && currentExecution != null && currentExecution.getParentId() != null) {
            parentScopeExecution = executionEntityManager.findById(currentExecution.getParentId());
            if (parentScopeExecution != null && parentScopeExecution.isScope()) {
                found = true;
            }
            currentExecution = parentScopeExecution;
        }

        parentScopeExecution.forceUpdate();
    }
}
