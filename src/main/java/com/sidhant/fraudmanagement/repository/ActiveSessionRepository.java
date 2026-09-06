package com.sidhant.fraudmanagement.repository;

import com.sidhant.fraudmanagement.entity.ActiveSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActiveSessionRepository extends JpaRepository<ActiveSession, Long> {

 
    Optional<ActiveSession> findByUser_Id(Long userId);

    Optional<ActiveSession> findByUser_IdAndActiveTrue(Long userId);
     
     
}