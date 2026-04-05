package com.bolna.clinic.repository;

import com.bolna.clinic.entity.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {

    List<DoctorSlot> findByDoctorIdAndAvailableTrueOrderBySlotTimeAsc(Long doctorId);

    List<DoctorSlot> findByDoctorIdAndSlotTimeBetweenAndAvailableTrueOrderBySlotTimeAsc(
            Long doctorId, LocalDateTime from, LocalDateTime to);

    Optional<DoctorSlot> findByDoctorIdAndSlotTimeAndAvailableTrue(Long doctorId, LocalDateTime slotTime);

    Optional<DoctorSlot> findByDoctorIdAndSlotTime(Long doctorId, LocalDateTime slotTime);
}
