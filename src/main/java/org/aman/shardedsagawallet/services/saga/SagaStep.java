package org.aman.shardedsagawallet.services.saga;

public interface SagaStep {

    boolean execute(SagaContext context);
    boolean compensate(SagaContext context);
    String getStepName();

}
