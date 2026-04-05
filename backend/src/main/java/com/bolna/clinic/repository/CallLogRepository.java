package com.bolna.clinic.repository;

import com.bolna.clinic.entity.CallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CallLogRepository extends JpaRepository<CallLog, Long> {
    List<CallLog> findAllByOrderByCreatedAtDesc();
    Optional<CallLog> findByBolnaCallId(String bolnaCallId);
    List<CallLog> findByAppointmentId(Long appointmentId);
}
