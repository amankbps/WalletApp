package org.aman.shardedsagawallet.repositories;

import org.aman.shardedsagawallet.entities.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance,Long> {
}
