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
@Slf4j
@RequiredArgsConstructor
public class DebitSourceWallet implements SagaStepInterface {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        Long fromWalletId=context.getLong("fromWalletId");
        BigDecimal amount=context.getBigDecimal("amount");
        log.info("Debiting source wallet {} with amount {}",fromWalletId,amount);


        Wallet wallet=walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(()->new RuntimeException("wallet not found"));

        log.info("Wallet fetched with balance {}",wallet.getBalance());
        context.put("originalToWalletBalance",wallet.getBalance());


        wallet.debit(amount);
        walletRepository.save(wallet);

        log.info("Wallet saved with balance {}",wallet.getBalance());
        context.put("sourceWalletBalanceAfterDebit",wallet.getBalance());

        log.info("Debit source wallet step executed successfully");
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {
        Long fromWalletId=context.getLong("fromWalletId");
        BigDecimal amount=context.getBigDecimal("amount");
        log.info("Compensating source wallet {} with amount {}",fromWalletId,amount);


        Wallet wallet=walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(()->new RuntimeException("wallet not found"));

        log.info("Wallet fetched with balance {}",wallet.getBalance());
        context.put("sourceWalletBalanceBeforeCompensation",wallet.getBalance());

        wallet.credit(amount);
        walletRepository.save(wallet);

        log.info("Wallet saved with balance {}",wallet.getBalance());
        context.put("sourceWalletBalanceAfterCreditCompensation",wallet.getBalance());

        log.info(" Compensating source wallet step executed successfully");
        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepFactory.SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString();
    }
}
