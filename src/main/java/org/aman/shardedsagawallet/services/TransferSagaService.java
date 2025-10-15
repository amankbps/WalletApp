package org.aman.shardedsagawallet.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aman.shardedsagawallet.entities.Transaction;
import org.aman.shardedsagawallet.services.saga.SagaContext;
import org.aman.shardedsagawallet.services.saga.steps.SagaOrchestrator;
import org.aman.shardedsagawallet.services.saga.steps.SagaStepFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferSagaService {

    private final TransactionService transactionService;
    private SagaOrchestrator sagaOrchestrator;



    public Long initiateTransfer(Long toWalletId, Long fromWalletId,
                                 BigDecimal amount,String description){
        log.info("Initiating transfer from wallet {} to wallet {} with amount {}" +
                "and description {}",fromWalletId,toWalletId,amount,description);
        Transaction transaction=transactionService.createTransaction(fromWalletId,toWalletId
        ,amount,description);
        SagaContext sagaContext=SagaContext.builder()
                .data(Map.ofEntries(
                        Map.entry("transactionId", transaction.getId()),
                        Map.entry("fromWalletId",fromWalletId),
                        Map.entry("toWalletId",toWalletId),
                        Map.entry("amount",amount),
                        Map.entry("description",description)
                )).build();
        Long sagaInstanceId=sagaOrchestrator.startSaga(sagaContext);
        log.info("saga instance created with id {}",sagaInstanceId);
        transactionService.updateTransactionWithSagaInstanceId(transaction.getId(),sagaInstanceId);
        executeTransferSaga(sagaInstanceId);
        return sagaInstanceId;
    }

    public void executeTransferSaga(Long sagaInstanceId){

        log.info("Executing transfer saga with id {}",sagaInstanceId);

          try {

               for(SagaStepFactory.SagaStepType step:SagaStepFactory.transferMoneySagaSteps){
                    boolean success=sagaOrchestrator.executeStep(sagaInstanceId,step.toString());
                    if(!success){
                        log.error("Failed to execute step {}",step.toString());
                        sagaOrchestrator.failSaga(sagaInstanceId);
                        return;
                    }
                    sagaOrchestrator.compensateSaga(sagaInstanceId);
                    log.info("Transfer saga complete with id {}",sagaInstanceId);
               }

          }catch (Exception e){
              log.error("Failed to execute saga with id {}",sagaInstanceId,e);
              sagaOrchestrator.failSaga(sagaInstanceId);
          }

    }
}
