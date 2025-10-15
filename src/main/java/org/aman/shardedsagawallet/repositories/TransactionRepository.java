package org.aman.shardedsagawallet.repositories;

import org.aman.shardedsagawallet.entities.Transaction;
import org.aman.shardedsagawallet.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    //all the debit
    List<Transaction>findByFromWalletId(Long fromWalletId);
    //all the credit
    List<Transaction>findByToWalletId(Long toWalletId);

    //all the transaction
    @Query("SELECT t FROM Transaction t WHERE t.fromWalletId= :walletId OR t.toWalletId= :walletId")
    List<Transaction>findByWalletId(@Param("walletId") Long walletId);

    List<Transaction>findByStatus(TransactionStatus status);

    List<Transaction>findBySagaInstanceId(Long sagaInstanceId);


}
