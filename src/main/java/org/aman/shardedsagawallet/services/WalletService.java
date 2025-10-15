package org.aman.shardedsagawallet.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aman.shardedsagawallet.entities.Wallet;
import org.aman.shardedsagawallet.repositories.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Wallet createWallet(Long userId){
           log.info("Creating wallet for user {}",userId);
           Wallet wallet=Wallet.builder()
                   .userId(userId)
                   .isActive(true)
                   .balance(BigDecimal.ZERO)
                   .build();
        wallet= walletRepository.save(wallet);
        log.info("Wallet created with id {}",wallet.getId());
        return wallet;
    }

    public Wallet getWalletById(Long id){
         return walletRepository.findById(id)
                 .orElseThrow(()->new RuntimeException("Wallet not found"));
    }
    public List<Wallet>getWalletByUserId(Long userId){
         return walletRepository.findByUserId(userId);
    }

    @Transactional
    public void debit(Long walletId,BigDecimal amount){
          log.info("Debiting {} from wallet {}",amount,walletId);
          Wallet wallet=getWalletById(walletId);
          wallet.debit(amount);
          walletRepository.save(wallet);
          log.info("Debit successful for wallet {}",walletId);
    }

     @Transactional
    public void credit(Long walletId,BigDecimal amount){
        log.info("crediting {} from wallet {}",amount,walletId);
        Wallet wallet=getWalletById(walletId);
        wallet.credit(amount);
        walletRepository.save(wallet);
        log.info("credit successful for wallet {}",walletId);
    }

    public BigDecimal getWalletBalance(Long walletId){
          log.info("Getting wallet balance {}",walletId);
          BigDecimal balance= getWalletById(walletId).getBalance();
          log.info("Balance for the wallet {} is {}",walletId,balance);
          return balance;
    }

}
