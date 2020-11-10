package com.nkoad.wallbler.core;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class RefreshableAccount extends Account<RefreshableValidator> {
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private ScheduledFuture scheduledFuture;

    protected void activate(Map<String, Object> accountProperties) {
        super.activate(accountProperties);
        int delayInDays = (int) accountProperties.get("config.refresh");
        if (delayInDays > 0) {
            if (validator.isAccept()) {
                scheduledFuture = executorService.schedule(() -> {
                    setAccessToken(accountProperties, refreshAccessToken());
                    refreshLinkedFeeds(accountProperties);
                }, delayInDays, TimeUnit.MINUTES);
            }
        }
    }

    protected abstract String refreshAccessToken();

    protected void deactivate(Map<String, Object> accountProperties) {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

}
