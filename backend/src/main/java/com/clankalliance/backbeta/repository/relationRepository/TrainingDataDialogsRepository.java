package com.clankalliance.backbeta.repository.relationRepository;

import com.clankalliance.backbeta.entity.relation.TrainingDataDialogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingDataDialogsRepository extends JpaRepository<TrainingDataDialogs, String> {
}
