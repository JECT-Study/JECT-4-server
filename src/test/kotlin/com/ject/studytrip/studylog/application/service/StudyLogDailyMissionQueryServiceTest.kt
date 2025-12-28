package com.ject.studytrip.studylog.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.mission.fixture.DailyMissionFixture
import com.ject.studytrip.mission.fixture.MissionFixture
import com.ject.studytrip.stamp.fixture.StampFixture
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionQueryRepository
import com.ject.studytrip.studylog.fixture.StudyLogDailyMissionFixture
import com.ject.studytrip.studylog.fixture.StudyLogFixture
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.fixture.DailyGoalFixture
import com.ject.studytrip.trip.fixture.TripFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock

@DisplayName("StudyLogDailyMissionQueryService 단위 테스트")
class StudyLogDailyMissionQueryServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var studyLogDailyMissionQueryService: StudyLogDailyMissionQueryService

    @Mock
    private lateinit var studyLogDailyMissionQueryRepository: StudyLogDailyMissionQueryRepository

    private lateinit var studyLog1: StudyLog
    private lateinit var studyLog2: StudyLog
    private lateinit var studyLogDailyMission1: StudyLogDailyMission
    private lateinit var studyLogDailyMission2: StudyLogDailyMission
    private lateinit var studyLogDailyMission3: StudyLogDailyMission

    @BeforeEach
    fun setUp() {
        val member = MemberFixture.createMemberFromKakaoWithId(1L)
        val trip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        val stamp = StampFixture(trip, 1).createWithId(1L)
        val mission = MissionFixture(stamp).createWithId(1L)
        val dailyGoal = DailyGoalFixture(trip).createWithId(1L)
        val dailyMission1 = DailyMissionFixture(mission, dailyGoal).createWithId(1L)
        val dailyMission2 = DailyMissionFixture(mission, dailyGoal).createWithId(2L)
        studyLog1 = StudyLogFixture(member, dailyGoal).createWithId(1L)
        studyLog2 = StudyLogFixture(member, dailyGoal).createWithId(2L)
        studyLogDailyMission1 = StudyLogDailyMissionFixture(studyLog1, dailyMission1).createWithId(1L)
        studyLogDailyMission2 = StudyLogDailyMissionFixture(studyLog1, dailyMission1).createWithId(2L)
        studyLogDailyMission3 = StudyLogDailyMissionFixture(studyLog2, dailyMission2).createWithId(3L)
    }

    @Nested
    @DisplayName("getGroupedStudyLogDailyMissionsByStudyLogIds 메서드는")
    inner class GetGroupedStudyLogDailyMissionsByStudyLogIds {
        @Test
        @DisplayName("학습 로그 ID 목록으로 그룹화된 StudyLogDailyMission Map을 반환한다.")
        fun shouldReturnGroupedStudyLogDailyMissionMapByStudyLogIds() {
            // given
            val studyLogId1 = studyLog1.id
            val studyLogId2 = studyLog2.id
            val studyLogIds = listOf(studyLogId1, studyLogId2)
            val studyLogDailyMissionMap =
                mapOf(
                    studyLogId1 to listOf(studyLogDailyMission1, studyLogDailyMission2),
                    studyLogId2 to listOf(studyLogDailyMission3),
                )
            given(studyLogDailyMissionQueryRepository.findStudyLogDailyMissionsGroupedByStudyLogId(studyLogIds))
                .willReturn(studyLogDailyMissionMap)

            // when
            val result = studyLogDailyMissionQueryService.getGroupedStudyLogDailyMissionsByStudyLogIds(studyLogIds)

            // then
            assertThat(result).isEqualTo(studyLogDailyMissionMap)
        }
    }
}
