package com.ject.studytrip.trip.infra.jpa;

import com.ject.studytrip.trip.domain.model.TripReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripReportJpaRepository extends JpaRepository<TripReport, Long> {
    List<TripReport> findAllByMember_IdOrderByCreatedAtDesc(Long memberId);
}
