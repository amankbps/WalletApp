package org.aman.shardedsagawallet.repositories;

import org.aman.shardedsagawallet.entities.SagaStep;
import org.aman.shardedsagawallet.entities.SagaStepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SagaStepRepository extends JpaRepository<SagaStep,Long> {

    List<SagaStep>findBySagaInstanceId(Long sagaInstanceId);

    List<SagaStep>findBySagaInstanceIdAndStatus(Long sagaInstanceId, SagaStepStatus status);

   // @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId= :sagaInstanceId AND s.status= 'COMPLETED'")
   @Query("""
           SELECT s
           FROM SagaStep s
           WHERE s.sagaInstanceId = :sagaInstanceId
             AND s.status = org.aman.shardedsagawallet.entities.SagaStepStatus.COMPLETED
           """)
    List<SagaStep>findCompletedStepsBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);

  //  @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId= :sagaInstanceId AND s.status IN ('COMPLETED','COMPENSATED')")
  @Query("""
           SELECT s
           FROM SagaStep s
           WHERE s.sagaInstanceId = :sagaInstanceId
             AND s.status IN (
                 org.aman.shardedsagawallet.entities.SagaStepStatus.COMPLETED,
                 org.aman.shardedsagawallet.entities.SagaStepStatus.COMPENSATED
             )
           """)
    List<SagaStep>findCompletedOrCompensatedStepsBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);
}
