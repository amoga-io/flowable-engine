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
package org.flowable.ui.application;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.variable.api.persistence.entity.VariableInstance;


public class CaseTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask)
    {
        try
        {
            if ("complete".equalsIgnoreCase(delegateTask.getEventName()))
            {
                for (VariableInstance variable : delegateTask.getVariableInstances().values()) {
                    if ("_outcome".equals(variable.getName())) {
                        delegateTask.setVariableLocal("amoTaskLocal_" + delegateTask.getTaskDefinitionKey() + "__status", variable
                                .getValue());
                        delegateTask.setVariable(delegateTask.getTaskDefinitionKey() + "__status", variable
                                .getValue());
                    } else {
                        delegateTask.setVariableLocal("amoTaskLocal_" + variable
                                .getName(), variable.getValue());
                    }
                }
            }
        } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
}
