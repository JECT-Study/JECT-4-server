package com.ject.studytrip.trip.application.facade

import com.ject.studytrip.global.common.constants.CacheNameConstants.TRIP_REPORT
import com.ject.studytrip.global.common.constants.CacheNameConstants.TRIP_REPORTS
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.image.application.service.ImageService
import com.ject.studytrip.member.application.service.MemberQueryService
import com.ject.studytrip.pomodoro.application.service.PomodoroQueryService
import com.ject.studytrip.studylog.application.dto.StudyLogDetail
import com.ject.studytrip.studylog.application.dto.StudyLogSliceInfo
import com.ject.studytrip.studylog.application.service.StudyLogDailyMissionQueryService
import com.ject.studytrip.studylog.application.service.StudyLogQueryService
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.trip.application.dto.PresignedTripReportImageInfo
import com.ject.studytrip.trip.application.dto.TripInfo
import com.ject.studytrip.trip.application.dto.TripReportDetail
import com.ject.studytrip.trip.application.dto.TripReportInfo
import com.ject.studytrip.trip.application.dto.TripReportsInfo
import com.ject.studytrip.trip.application.dto.TripRetrospectDetail
import com.ject.studytrip.trip.application.dto.TripRetrospectSummary
import com.ject.studytrip.trip.application.service.TripQueryService
import com.ject.studytrip.trip.application.service.TripReportCommandService
import com.ject.studytrip.trip.application.service.TripReportQueryService
import com.ject.studytrip.trip.application.service.TripReportStudyLogCommandService
import com.ject.studytrip.trip.presentation.dto.request.ConfirmTripReportImageRequest
import com.ject.studytrip.trip.presentation.dto.request.CreateTripReportRequest
import com.ject.studytrip.trip.presentation.dto.request.PresignTripReportImageRequest
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.temporal.ChronoUnit

@Component
class TripReportFacade(
    // Query Service
    private val memberQueryService: MemberQueryService,
    private val tripQueryService: TripQueryService,
    private val studyLogQueryService: StudyLogQueryService,
    private val studyLogDailyMissionQueryService: StudyLogDailyMissionQueryService,
    private val pomodoroQueryService: PomodoroQueryService,
    private val tripReportQueryService: TripReportQueryService,
    // Command Service
    private val tripReportCommandService: TripReportCommandService,
    private val tripReportStudyLogCommandService: TripReportStudyLogCommandService,
    // Image Service
    private val imageService: ImageService,
) {
    companion object {
        private const val TRIP_REPORT_IMAGE_KEY_PREFIX = "trip-reports"
    }

    @CacheEvict(cacheNames = [TRIP_REPORTS], key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).tripReports(#memberId)")
    @Transactional
    fun createTripReport(
        memberId: Long,
        request: CreateTripReportRequest,
    ): TripReportInfo {
        val member = memberQueryService.getValidMember(memberId)
        val tripReport = tripReportCommandService.createTripReport(member, request)
        val studyLogs = studyLogQueryService.getValidStudyLogs(request.studyLogIds)

        tripReportStudyLogCommandService.createTripReportStudyLogs(tripReport, studyLogs)

        return TripReportInfo.from(tripReport)
    }

    @Caching(
        evict = [
            CacheEvict(
                cacheNames = [TRIP_REPORTS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).tripReports(#memberId)",
            ),
            CacheEvict(
                cacheNames = [TRIP_REPORT],
                allEntries = true,
            ),
        ],
    )
    @Transactional
    fun deleteTripReport(
        memberId: Long,
        tripReportId: Long,
    ) {
        val tripReport = tripReportQueryService.getValidTripReport(memberId, tripReportId)

        tripReportCommandService.deleteTripReport(tripReport)
    }

    @Transactional(readOnly = true)
    fun getTripRetrospect(
        memberId: Long,
        tripId: Long,
        page: Int,
        size: Int,
    ): TripRetrospectDetail {
        val trip = tripQueryService.getValidCompletedTrip(memberId, tripId)
        val studyLogSlice = studyLogQueryService.getStudyLogsSliceByTripId(trip.id.requireId(), page, size, "LATEST")
        val studyLogCount = studyLogQueryService.getStudyLogCountByTripId(trip.id.requireId())
        val totalFocusHours = pomodoroQueryService.getTotalFocusHoursByTripId(trip.id.requireId())
        val studyDays = trip.endDate?.let { maxOf(0, ChronoUnit.DAYS.between(trip.startDate, it) + 1) } ?: 0L
        val studyLogIds = studyLogQueryService.getStudyLogIdsByTripId(trip.id.requireId())

        val summary = TripRetrospectSummary(studyLogCount, totalFocusHours, studyDays, studyLogIds)
        val tripInfo = TripInfo.from(trip, 0, 100)
        val studyLogSliceInfo = buildStudyLogSliceInfo(studyLogSlice)

        return TripRetrospectDetail(summary, tripInfo, studyLogSliceInfo)
    }

    @Cacheable(cacheNames = [TRIP_REPORTS], key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).tripReports(#memberId)")
    @Transactional(readOnly = true)
    fun getTripReportsByMember(memberId: Long): TripReportsInfo {
        val tripReports = tripReportQueryService.getTripReportsByMemberId(memberId)

        return TripReportsInfo(tripReports.map { TripReportInfo.from(it) })
    }

    @Cacheable(
        cacheNames = [TRIP_REPORT],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).tripReport(#memberId, #tripReportId, #page, #size)",
    )
    @Transactional(readOnly = true)
    fun getTripReport(
        memberId: Long,
        tripReportId: Long,
        page: Int,
        size: Int,
    ): TripReportDetail {
        val tripReport = tripReportQueryService.getValidTripReport(memberId, tripReportId)
        val studyLogSlice = studyLogQueryService.getStudyLogsSliceByTripReportId(tripReport.id.requireId(), page, size)

        val tripReportInfo = TripReportInfo.from(tripReport)
        val studyLogSliceInfo = buildStudyLogSliceInfo(studyLogSlice)

        return TripReportDetail(tripReportInfo, studyLogSliceInfo)
    }

    @Transactional(readOnly = true)
    fun issuePresignedUrl(
        tripReportId: Long,
        request: PresignTripReportImageRequest,
    ): PresignedTripReportImageInfo {
        val tripReport = tripReportQueryService.getTripReport(tripReportId)
        val info = imageService.presign(TRIP_REPORT_IMAGE_KEY_PREFIX, tripReport.id.toString(), request.originFilename)

        return PresignedTripReportImageInfo(tripReport.id.requireId(), info.tmpKey, info.presignedUrl)
    }

    @Transactional
    fun confirmImage(
        tripReportId: Long,
        request: ConfirmTripReportImageRequest,
    ) {
        val tripReport = tripReportQueryService.getTripReport(tripReportId)
        val imageUrl = imageService.confirm(request.tmpKey)

        tripReportCommandService.updateImageUrl(tripReport, imageUrl)
    }

    private fun buildStudyLogSliceInfo(studyLogSlice: Slice<StudyLog>): StudyLogSliceInfo {
        val studyLogIds = studyLogSlice.content.map { it.id.requireId() }

        // 학습 로그별 학습 로그 데일리 미션 목록 그룹화
        val groupedStudyLogDailyMissions = studyLogDailyMissionQueryService.getGroupedStudyLogDailyMissionsByStudyLogIds(studyLogIds)
        val studyLogDetails = studyLogSlice.content.map { StudyLogDetail.from(it, groupedStudyLogDailyMissions[it.id]) }

        return StudyLogSliceInfo(studyLogDetails, studyLogSlice.hasNext())
    }
}
