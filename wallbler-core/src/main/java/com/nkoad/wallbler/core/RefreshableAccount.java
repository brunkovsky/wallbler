package com.nkoad.wallbler.core;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class RefreshableAccount extends Account<RefreshableValidator> {
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private ScheduledFuture scheduledFuture;

    protected void activate(Map<String, Object> properties) {
        super.activate(properties);
        int delayInHours = (int) properties.get("config.refresh") * 24;
        if (delayInHours > 0 ) {
            if (validator.isAccept()) {
                scheduledFuture = executorService.schedule(() -> {
                    setAccessToken(properties, refreshAccessToken());
                }, delayInHours, TimeUnit.HOURS);
            }
        }
    }

    protected abstract String refreshAccessToken();

    protected void modified(Map<String, Object> properties) {
        super.modified(properties);
    }

    protected void deactivate(Map<String, Object> properties) {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

}
