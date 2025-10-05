package org.aman.shardedsagawallet.config;

import org.aman.shardedsagawallet.services.saga.SagaStepInterface;
import org.aman.shardedsagawallet.services.saga.steps.CreditDestinationWalletStep;
import org.aman.shardedsagawallet.services.saga.steps.DebitSourceWallet;
import org.aman.shardedsagawallet.services.saga.steps.SagaStepFactory;
import org.aman.shardedsagawallet.services.saga.steps.UpdateTransactionStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SagaConfiguration {

    @Bean
    public Map<String, SagaStepInterface>sagaStepMap(
            DebitSourceWallet debitSourceWallet,
            CreditDestinationWalletStep creditDestinationWalletStep,
            UpdateTransactionStatus updateTransactionStatus
    ){
         Map<String, SagaStepInterface>sagaStepMap=new HashMap<>();
         sagaStepMap.put(SagaStepFactory.SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString(),debitSourceWallet);
         sagaStepMap.put(SagaStepFactory.SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString(),creditDestinationWalletStep);
         sagaStepMap.put(SagaStepFactory.SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString(),updateTransactionStatus);
         return sagaStepMap;
    }

}
