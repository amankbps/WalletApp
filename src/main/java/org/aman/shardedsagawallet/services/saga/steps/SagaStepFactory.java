package org.aman.shardedsagawallet.services.saga.steps;

import lombok.RequiredArgsConstructor;
import org.aman.shardedsagawallet.services.saga.SagaStepInterface;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SagaStepFactory {

    private final Map<String, SagaStepInterface>sagaStepMap;
    public static final List<SagaStepType> transferMoneySagaSteps=List.of(
            SagaStepFactory.SagaStepType.DEBIT_SOURCE_WALLET_STEP,
            SagaStepFactory.SagaStepType.CREDIT_DESTINATION_WALLET_STEP,
            SagaStepFactory.SagaStepType.UPDATE_TRANSACTION_STATUS_STEP
    );
    public static enum SagaStepType{
        UPDATE_TRANSACTION_STATUS_STEP,
        DEBIT_SOURCE_WALLET_STEP,
        CREDIT_DESTINATION_WALLET_STEP;
    }
    public SagaStepInterface getSagaStep(String stepName){
         return sagaStepMap.get(stepName);
    }
}
