package com.clankalliance.backbeta.repository;

import com.clankalliance.backbeta.entity.TrainingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingDataRepository extends JpaRepository<TrainingData, String> {



}
