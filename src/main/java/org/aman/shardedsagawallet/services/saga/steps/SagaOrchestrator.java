package org.aman.shardedsagawallet.services.saga.steps;

import org.aman.shardedsagawallet.entities.SagaInstance;
import org.aman.shardedsagawallet.services.saga.SagaContext;

public interface SagaOrchestrator {

    Long startSaga(SagaContext context);

    boolean executeStep(Long sagaInstanceId,String stepName);

    boolean compensateStep(Long sagaInstanceId,String stepName);

    SagaInstance getSagaInstance(Long sagaInstanceId);

    void compensateSaga(Long sagaInstanceId);

    void failSaga(Long sagaInstanceId);

    void completeSaga(Long sagaInstanceId);

}
