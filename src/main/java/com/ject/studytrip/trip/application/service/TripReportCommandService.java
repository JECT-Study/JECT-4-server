package com.ject.studytrip.trip.application.service;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.trip.domain.factory.TripReportFactory;
import com.ject.studytrip.trip.domain.model.TripReport;
import com.ject.studytrip.trip.domain.repository.TripReportCommandRepository;
import com.ject.studytrip.trip.domain.repository.TripReportRepository;
import com.ject.studytrip.trip.presentation.dto.request.CreateTripReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripReportCommandService {
    private final TripReportRepository tripReportRepository;
    private final TripReportCommandRepository tripReportCommandRepository;

    public TripReport createTripReport(Member member, CreateTripReportRequest request) {
        TripReport tripReport =
                TripReportFactory.create(
                        member,
                        request.title(),
                        request.content(),
                        request.startDate(),
                        request.endDate(),
                        request.studyLogCount(),
                        request.totalFocusHours(),
                        request.studyDays(),
                        request.imageTitle());

        return tripReportRepository.save(tripReport);
    }

    public void updateImageUrl(TripReport tripReport, String imageUrl) {
        tripReport.updateImageUrl(imageUrl);
    }

    public void deleteTripReport(TripReport tripReport) {
        tripReport.updateDeletedAt();
    }

    public long hardDeleteTripReports() {
        return tripReportCommandRepository.deleteAllByDeletedAtIsNotNull();
    }

    public long hardDeleteTripReportsOwnedByDeletedMember() {
        return tripReportCommandRepository.deleteAllByDeletedMemberOwner();
    }

    public long hardDeleteTripReportsByMember(Long memberId) {
        return tripReportCommandRepository.deleteAllByMemberId(memberId);
    }
}
