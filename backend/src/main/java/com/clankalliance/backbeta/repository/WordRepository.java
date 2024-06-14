package com.clankalliance.backbeta.repository;

import com.clankalliance.backbeta.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends JpaRepository<Word, String> {
}
