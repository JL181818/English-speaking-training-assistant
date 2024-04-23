package com.clankalliance.backbeta.repository;

import com.clankalliance.backbeta.entity.Dialog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DialogRepository extends JpaRepository<Dialog, String> {
}
