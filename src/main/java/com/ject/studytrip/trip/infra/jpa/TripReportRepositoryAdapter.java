package com.ject.studytrip.trip.infra.jpa;

import com.ject.studytrip.trip.domain.model.TripReport;
import com.ject.studytrip.trip.domain.repository.TripReportRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TripReportRepositoryAdapter implements TripReportRepository {
    private final TripReportJpaRepository tripReportJpaRepository;

    @Override
    public Optional<TripReport> findById(Long tripReportId) {
        return tripReportJpaRepository.findById(tripReportId);
    }

    @Override
    public List<TripReport> findAllByMemberIdOrderByCreatedAtDesc(Long memberId) {
        return tripReportJpaRepository.findAllByMember_IdOrderByCreatedAtDesc(memberId);
    }

    @Override
    public TripReport save(TripReport tripReport) {
        return tripReportJpaRepository.save(tripReport);
    }
}
