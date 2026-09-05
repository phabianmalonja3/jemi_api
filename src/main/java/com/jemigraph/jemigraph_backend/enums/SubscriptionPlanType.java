package com.jemigraph.jemigraph_backend.enums;
import lombok.Getter;
@Getter
public enum SubscriptionPlanType {
    MONTHLY("Monthly Plan", 30),
    QUARTERLY("Quarterly Plan", 90),
    ANNUAL("Annual Plan", 365);

    private final String displayName;
    private final int durationInDays;

    SubscriptionPlanType(String displayName, int durationInDays) {
        this.displayName = displayName;
        this.durationInDays = durationInDays;
    }

}