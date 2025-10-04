package org.aman.shardedsagawallet.services.saga.steps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aman.shardedsagawallet.services.saga.SagaContext;
import org.aman.shardedsagawallet.services.saga.SagaStep;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionStatus implements SagaStep {



    @Override
    public boolean execute(SagaContext context) {

        Long transactionId=context.getLong("transactionId");

        log.info("Updating transaction status for transaction {}",transactionId);

        return false;
    }

    @Override
    public boolean compensate(SagaContext context) {
        return false;
    }

    @Override
    public String getStepName() {
        return "UpdateTransactionStatus";
    }
}
