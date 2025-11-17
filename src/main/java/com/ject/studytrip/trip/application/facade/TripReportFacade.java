package com.ject.studytrip.trip.application.facade;

import static com.ject.studytrip.global.common.constants.CacheNameConstants.TRIP_REPORT;
import static com.ject.studytrip.global.common.constants.CacheNameConstants.TRIP_REPORTS;

import com.ject.studytrip.image.application.dto.PresignedImageInfo;
import com.ject.studytrip.image.application.service.ImageService;
import com.ject.studytrip.member.application.service.MemberQueryService;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.pomodoro.application.service.PomodoroQueryService;
import com.ject.studytrip.studylog.application.dto.StudyLogDetail;
import com.ject.studytrip.studylog.application.dto.StudyLogSliceInfo;
import com.ject.studytrip.studylog.application.service.StudyLogDailyMissionQueryService;
import com.ject.studytrip.studylog.application.service.StudyLogQueryService;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import com.ject.studytrip.trip.application.dto.*;
import com.ject.studytrip.trip.application.service.TripQueryService;
import com.ject.studytrip.trip.application.service.TripReportCommandService;
import com.ject.studytrip.trip.application.service.TripReportQueryService;
import com.ject.studytrip.trip.application.service.TripReportStudyLogCommandService;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripReport;
import com.ject.studytrip.trip.presentation.dto.request.ConfirmTripReportImageRequest;
import com.ject.studytrip.trip.presentation.dto.request.CreateTripReportRequest;
import com.ject.studytrip.trip.presentation.dto.request.PresignTripReportImageRequest;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TripReportFacade {
    private static final String TRIP_REPORT_IMAGE_KEY_PREFIX = "trip-reports";

    private final MemberQueryService memberQueryService;
    private final TripQueryService tripQueryService;
    private final StudyLogQueryService studyLogQueryService;
    private final StudyLogDailyMissionQueryService studyLogDailyMissionQueryService;
    private final PomodoroQueryService pomodoroQueryService;
    private final TripReportQueryService tripReportQueryService;

    private final TripReportCommandService tripReportCommandService;
    private final TripReportStudyLogCommandService tripReportStudyLogCommandService;

    private final ImageService imageService;

    @Transactional(readOnly = true)
    public TripRetrospectDetail getTripRetrospect(Long memberId, Long tripId, int page, int size) {
        Member member = memberQueryService.getValidMember(memberId);
        Trip trip = tripQueryService.getValidCompletedTrip(member.getId(), tripId); // 완료된 여행
        Slice<StudyLog> studyLogSlice =
                studyLogQueryService.getStudyLogsSliceByTripId(trip.getId(), page, size, "LATEST");

        long studyLogCount = studyLogQueryService.getStudyLogCountByTripId(trip.getId());
        long totalFocusHours = pomodoroQueryService.getTotalFocusHoursByTripId(trip.getId());
        long studyDays =
                trip.getEndDate() != null
                        ? Math.max(
                                0,
                                ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1)
                        : 0L;
        List<Long> studyLogIds = studyLogQueryService.getStudyLogIdsByTripId(trip.getId());

        TripRetrospectSummary summary =
                TripRetrospectSummary.of(studyLogCount, totalFocusHours, studyDays, studyLogIds);
        TripInfo tripInfo = TripInfo.from(trip, 0, 100);
        StudyLogSliceInfo studyLogDetailSlice = buildStudyLogDetailsSlice(studyLogSlice);

        return TripRetrospectDetail.from(summary, tripInfo, studyLogDetailSlice);
    }

    @Cacheable(
            cacheNames = TRIP_REPORTS,
            key =
                    "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).tripReports(#memberId)")
    @Transactional(readOnly = true)
    public TripReportsInfo getTripReportsByMember(Long memberId) {
        Member member = memberQueryService.getValidMember(memberId);
        List<TripReport> tripReports =
                tripReportQueryService.getTripReportsByMemberId(member.getId());

        return TripReportsInfo.of(tripReports.stream().map(TripReportInfo::from).toList());
    }

    @Cacheable(
            cacheNames = TRIP_REPORT,
            key =
                    "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).tripReport(#memberId, #tripReportId, #page, #size)")
    @Transactional(readOnly = true)
    public TripReportDetail getTripReport(Long memberId, Long tripReportId, int page, int size) {
        Member member = memberQueryService.getValidMember(memberId);
        TripReport tripReport =
                tripReportQueryService.getValidTripReport(member.getId(), tripReportId);
        Slice<StudyLog> studyLogSlice =
                studyLogQueryService.getStudyLogsSliceByTripReportId(
                        tripReport.getId(), page, size);

        TripReportInfo tripReportInfo = TripReportInfo.from(tripReport);
        StudyLogSliceInfo studyLogDetailSlice = buildStudyLogDetailsSlice(studyLogSlice);

        return TripReportDetail.from(tripReportInfo, studyLogDetailSlice);
    }

    @CacheEvict(
            cacheNames = TRIP_REPORTS,
            key =
                    "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).tripReports(#memberId)")
    @Transactional
    public TripReportInfo createTripReport(Long memberId, CreateTripReportRequest request) {
        Member member = memberQueryService.getValidMember(memberId);
        TripReport tripReport = tripReportCommandService.createTripReport(member, request);
        List<StudyLog> studyLogs = studyLogQueryService.getValidStudyLogs(request.studyLogIds());
        tripReportStudyLogCommandService.createTripReportStudyLogs(tripReport, studyLogs);

        return TripReportInfo.from(tripReport);
    }

    @Caching(
            evict = {
                @CacheEvict(
                        cacheNames = TRIP_REPORTS,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).tripReports(#memberId)"),
                @CacheEvict(cacheNames = TRIP_REPORT, allEntries = true)
            })
    @Transactional
    public void deleteTripReport(Long memberId, Long tripReportId) {
        Member member = memberQueryService.getValidMember(memberId);
        TripReport tripReport =
                tripReportQueryService.getValidTripReport(member.getId(), tripReportId);

        tripReportCommandService.deleteTripReport(tripReport);
    }

    @Transactional(readOnly = true)
    public PresignedTripReportImageInfo issuePresignedUrl(
            Long tripReportId, PresignTripReportImageRequest request) {
        TripReport tripReport = tripReportQueryService.getTripReport(tripReportId);

        PresignedImageInfo info =
                imageService.presign(
                        TRIP_REPORT_IMAGE_KEY_PREFIX,
                        tripReport.getId().toString(),
                        request.originFilename());

        return PresignedTripReportImageInfo.of(
                tripReport.getId(), info.tmpKey(), info.presignedUrl());
    }

    @Caching(
            evict = {
                @CacheEvict(cacheNames = TRIP_REPORTS, allEntries = true),
                @CacheEvict(cacheNames = TRIP_REPORT, allEntries = true)
            })
    @Transactional
    public void confirmImage(Long tripReportId, ConfirmTripReportImageRequest request) {
        TripReport tripReport = tripReportQueryService.getTripReport(tripReportId);
        String imageUrl = imageService.confirm(request.tmpKey());

        tripReportCommandService.updateImageUrl(tripReport, imageUrl);
    }

    private StudyLogSliceInfo buildStudyLogDetailsSlice(Slice<StudyLog> studyLogSlice) {
        List<Long> studyLogIds = studyLogSlice.getContent().stream().map(StudyLog::getId).toList();

        // 학습 로그별 학습 로그 데일리 미션 목록 그룹화
        Map<Long, List<StudyLogDailyMission>> groupedStudyLogDailyMissions =
                studyLogDailyMissionQueryService.getGroupedStudyLogDailyMissionsByStudyLogIds(
                        studyLogIds);

        List<StudyLogDetail> studyLogDetails =
                studyLogSlice.getContent().stream()
                        .map(
                                studyLog ->
                                        StudyLogDetail.from(
                                                studyLog,
                                                groupedStudyLogDailyMissions.get(studyLog.getId())))
                        .toList();

        return StudyLogSliceInfo.of(studyLogDetails, studyLogSlice.hasNext());
    }
}
