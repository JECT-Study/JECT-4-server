package com.ject.studytrip.trip.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.trip.domain.error.TripReportErrorCode;
import com.ject.studytrip.trip.domain.model.TripReport;
import com.ject.studytrip.trip.domain.policy.TripReportPolicy;
import com.ject.studytrip.trip.domain.repository.TripReportRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripReportQueryService {
    private final TripReportRepository tripReportRepository;

    public TripReport getTripReport(Long tripReportId) {
        return tripReportRepository
                .findById(tripReportId)
                .orElseThrow(() -> new CustomException(TripReportErrorCode.TRIP_REPORT_NOT_FOUND));
    }

    public TripReport getValidTripReport(Long memberId, Long tripReportId) {
        TripReport tripReport =
                tripReportRepository
                        .findById(tripReportId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                TripReportErrorCode.TRIP_REPORT_NOT_FOUND));

        TripReportPolicy.validateOwner(memberId, tripReport);

        return tripReport;
    }

    public List<TripReport> getTripReportsByMemberId(Long memberId) {
        return tripReportRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
    }
}
