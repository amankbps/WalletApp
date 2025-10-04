package org.aman.shardedsagawallet.entities;

public enum SagaStepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED,
    SKIPPED,
}
