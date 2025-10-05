package org.aman.shardedsagawallet.services.saga.steps;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aman.shardedsagawallet.entities.Wallet;
import org.aman.shardedsagawallet.repositories.WalletRepository;
import org.aman.shardedsagawallet.services.saga.SagaContext;
import org.aman.shardedsagawallet.services.saga.SagaStepInterface;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditDestinationWalletStep implements SagaStepInterface {

    private final WalletRepository walletRepository;


    @Override
    @Transactional
    public boolean execute(SagaContext context) {

        //step 1.Get the destination wallet id from the context
        Long toWalletId=context.getLong("toWalletId");
        BigDecimal amount=context.getBigDecimal("amount");
        log.info("Crediting destination wallet {} with amount {}",toWalletId,amount);


        //step 2.Fetch the destination wallet from the database with a lock
        Wallet wallet=walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(()->new RuntimeException("wallet not found"));

        log.info("Wallet fetched with balance {}",wallet.getBalance());
        context.put("originalToWalletBalance",wallet.getBalance());


        //step 3.Credit the destination wallet
        wallet.credit(amount);
        walletRepository.save(wallet);
        log.info("Wallet saved with balance {}",wallet.getBalance());
        context.put("toWalletBalanceAfterCredit",wallet.getBalance());

        log.info("Credit destination wallet step executed successfully");

        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {
        //step 1.Get the destination wallet id from the context
        Long toWalletId=context.getLong("toWalletId");
        BigDecimal amount=context.getBigDecimal("amount");
        log.info("Compensating credit of destination wallet {} with amount {}",toWalletId,amount);


        //step 2.Fetch the destination wallet from the database with a lock
        Wallet wallet=walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(()->new RuntimeException("wallet not found"));

        log.info("Wallet fetched with balance {}",wallet.getBalance());


        //step 3.Credit the destination wallet
        wallet.credit(amount);
        walletRepository.save(wallet);
        log.info("Wallet saved with balance {}",wallet.getBalance());
        context.put("toWalletBalanceAfterCreditCompensating",wallet.getBalance());

        log.info("Credit Compensating of destination wallet step executed successfully");

        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepFactory.SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString();
    }
}
