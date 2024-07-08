package org.flowable.ui.application;

import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.Config;
import com.rollbar.notifier.config.ConfigBuilder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.flowable.ui.application.FlowableUiAppEventRegistryCondition.environmentMap;


@ControllerAdvice
public class GlobalControllerExceptionHandler {

    private static Rollbar rollbar = null;
    private static Rollbar getRollbar(){
        return new Rollbar(getConfig2());
    }
    private static Config getConfig2() {
        return ConfigBuilder.withAccessToken(environmentMap.get("rollbarToken")).endpoint("https://api-proxy-rollbar.amoga.workers.dev/").environment(environmentMap.get("environment")).build();
    }
    private static Rollbar getRollbarInstance() {
        if (rollbar == null) {
            rollbar = getRollbar();
        }
        return rollbar;
    }

    @ExceptionHandler(value = Exception.class)
   public void handleExceptions(HttpServletRequest request, HttpServletResponse response, RuntimeException e) {
        getRollbarInstance().error(e,e.getMessage());
        throw e;
   }
}