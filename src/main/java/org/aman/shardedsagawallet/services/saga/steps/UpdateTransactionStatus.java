package org.aman.shardedsagawallet.services.saga.steps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aman.shardedsagawallet.entities.Transaction;
import org.aman.shardedsagawallet.entities.TransactionStatus;
import org.aman.shardedsagawallet.repositories.TransactionRepository;
import org.aman.shardedsagawallet.services.saga.SagaContext;
import org.aman.shardedsagawallet.services.saga.SagaStep;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionStatus implements SagaStep {

    private final TransactionRepository transactionRepository;

    @Override
    public boolean execute(SagaContext context) {

        Long transactionId=context.getLong("transactionId");
        log.info("Updating transaction status for transaction {}",transactionId);

        Transaction transaction=transactionRepository.findById(transactionId)
                .orElseThrow(()->new RuntimeException("transaction not found"));

        context.put("originalTransactionStatus",transaction.getStatus());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        log.info("Transaction status updated for transaction {}",transactionId);
        context.put("transactionStatusAfterUpdate",transaction.getStatus());

        log.info("Update transaction status step executed successfully");

        return true;
    }

    @Override
    public boolean compensate(SagaContext context) {
        Long transactionId=context.getLong("transactionId");
        TransactionStatus originalTransactionStatus= TransactionStatus.valueOf(context.getString("originalTransactionStatus"));
        log.info("Compensating transaction status for transaction {}",transactionId);


        Transaction transaction=transactionRepository.findById(transactionId)
                .orElseThrow(()->new RuntimeException("transaction not found"));

        transaction.setStatus(originalTransactionStatus);
        transactionRepository.save(transaction);

        log.info("Transaction status compensated for transaction {}",transactionId);
        return true;
    }

    @Override
    public String getStepName() {
        return "UpdateTransactionStatus";
    }
}
