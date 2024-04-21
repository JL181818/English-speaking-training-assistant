package com.clankalliance.backbeta.repository;

import com.clankalliance.backbeta.entity.TrainingData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingDataRepository extends JpaRepository<TrainingData, String> {

    @Query("from TrainingData t where t.user.id = ?1")
    Page<TrainingData> findByUserId(Long userId, PageRequest p);

}
