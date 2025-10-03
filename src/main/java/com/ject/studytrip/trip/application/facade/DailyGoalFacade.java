package com.ject.studytrip.trip.application.facade;

import static com.ject.studytrip.global.common.constants.CacheNameConstants.DAILY_GOAL;

import com.ject.studytrip.member.application.service.MemberQueryService;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.mission.application.dto.DailyMissionInfo;
import com.ject.studytrip.mission.application.service.DailyMissionCommandService;
import com.ject.studytrip.mission.application.service.DailyMissionQueryService;
import com.ject.studytrip.mission.application.service.MissionCommandService;
import com.ject.studytrip.mission.application.service.MissionQueryService;
import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.pomodoro.application.dto.PomodoroInfo;
import com.ject.studytrip.pomodoro.application.service.PomodoroCommandService;
import com.ject.studytrip.pomodoro.application.service.PomodoroQueryService;
import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.stamp.application.service.StampCommandService;
import com.ject.studytrip.stamp.application.service.StampQueryService;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.application.dto.DailyGoalDetail;
import com.ject.studytrip.trip.application.dto.DailyGoalInfo;
import com.ject.studytrip.trip.application.service.DailyGoalCommandService;
import com.ject.studytrip.trip.application.service.DailyGoalQueryService;
import com.ject.studytrip.trip.application.service.TripQueryService;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.presentation.dto.request.CreateDailyGoalRequest;
import com.ject.studytrip.trip.presentation.dto.request.UpdateDailyGoalRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DailyGoalFacade {
    private final MemberQueryService memberQueryService;
    private final TripQueryService tripQueryService;
    private final StampQueryService stampQueryService;
    private final DailyMissionQueryService dailyMissionQueryService;
    private final DailyGoalQueryService dailyGoalQueryService;
    private final PomodoroQueryService pomodoroQueryService;

    private final StampCommandService stampCommandService;
    private final MissionQueryService missionQueryService;
    private final MissionCommandService missionCommandService;
    private final DailyMissionCommandService dailyMissionCommandService;
    private final DailyGoalCommandService dailyGoalCommandService;
    private final PomodoroCommandService pomodoroCommandService;

    @Transactional
    public DailyGoalInfo createDailyGoal(
            Long memberId, Long tripId, CreateDailyGoalRequest request) {
        Trip trip = getValidTripOwnedByMember(memberId, tripId);
        List<Mission> missions = getValidMissionsByTripCategory(trip, request.missionIds());

        // 스탬프 이름을 추출해 title 설정
        String title = determineTitleByStamps(trip.getCategory(), missions);
        DailyGoal dailyGoal = dailyGoalCommandService.createDailyGoal(trip, title);

        dailyMissionCommandService.createDailyMissions(dailyGoal, missions);
        pomodoroCommandService.createPomodoro(dailyGoal, request.pomodoro());

        return DailyGoalInfo.from(dailyGoal);
    }

    @CacheEvict(
            cacheNames = DAILY_GOAL,
            key =
                    "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).dailyGoal(#memberId, #tripId, #dailyGoalId)")
    @Transactional
    public void updateDailyGoal(
            Long memberId, Long tripId, Long dailyGoalId, UpdateDailyGoalRequest request) {
        Trip trip = getValidTripOwnedByMember(memberId, tripId);
        DailyGoal dailyGoal = dailyGoalQueryService.getValidDailyGoal(trip.getId(), dailyGoalId);

        // 삭제할 데일리 미션이 있을 경우
        if (request.deleteDailyMissionIds() != null && !request.deleteDailyMissionIds().isEmpty()) {
            List<DailyMission> deleteDailyMissions =
                    dailyMissionQueryService.getValidDailyMissionsByIds(
                            dailyGoal.getId(), request.deleteDailyMissionIds());
            deleteDailyMissions.forEach(dailyMissionCommandService::deleteDailyMission);
        }

        // 새로 추가할 미션이 있을 경우
        if (request.addMissionIds() != null && !request.addMissionIds().isEmpty()) {
            List<Mission> addMissions =
                    getValidMissionsByTripCategory(trip, request.addMissionIds());
            dailyMissionCommandService.createDailyMissions(dailyGoal, addMissions);
        }
    }

    @CacheEvict(
            cacheNames = DAILY_GOAL,
            key =
                    "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).dailyGoal(#memberId, #tripId, #dailyGoalId)")
    @Transactional
    public void deleteDailyGoal(Long memberId, Long tripId, Long dailyGoalId) {
        Trip trip = getValidTripOwnedByMember(memberId, tripId);
        DailyGoal dailyGoal = dailyGoalQueryService.getValidDailyGoal(trip.getId(), dailyGoalId);
        Pomodoro pomodoro = pomodoroQueryService.getValidPomodoroByDailyGoal(dailyGoal.getId());

        // 뽀모도로 삭제
        pomodoroCommandService.deletePomodoro(pomodoro);

        // 데일리 미션 삭제
        List<DailyMission> dailyMissions =
                dailyMissionQueryService.getDailyMissionsByDailyGoal(dailyGoal.getId());
        for (DailyMission dailyMission : dailyMissions) {
            dailyMissionCommandService.deleteDailyMission(dailyMission);
        }

        // 데일리 목표 삭제
        dailyGoalCommandService.deleteDailyGoal(dailyGoal);
    }

    @Cacheable(
            cacheNames = DAILY_GOAL,
            key =
                    "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).dailyGoal(#memberId, #tripId, #dailyGoalId)")
    @Transactional(readOnly = true)
    public DailyGoalDetail getDailyGoal(Long memberId, Long tripId, Long dailyGoalId) {
        Trip trip = getValidTripOwnedByMember(memberId, tripId);
        DailyGoal dailyGoal = dailyGoalQueryService.getValidDailyGoal(trip.getId(), dailyGoalId);
        Pomodoro pomodoro = pomodoroQueryService.getValidPomodoroByDailyGoal(dailyGoal.getId());
        List<DailyMission> dailyMissions =
                dailyMissionQueryService.getDailyMissionsByDailyGoal(dailyGoal.getId());

        return DailyGoalDetail.from(
                DailyGoalInfo.from(dailyGoal),
                PomodoroInfo.from(pomodoro),
                dailyMissions.stream().map(DailyMissionInfo::from).toList());
    }

    private Trip getValidTripOwnedByMember(Long memberId, Long tripId) {
        Member member = memberQueryService.getValidMember(memberId);

        return tripQueryService.getValidTrip(member.getId(), tripId);
    }

    private List<Mission> getValidMissionsByTripCategory(Trip trip, List<Long> missionIds) {
        List<Mission> missions = missionQueryService.getValidMissionsWithStamp(missionIds);

        for (Mission mission : missions) {
            stampCommandService.validateStampBelongsToTrip(trip.getId(), mission.getStamp());
        }

        // 코스형 여행일 경우, 현재 진행중인 스탬프를 조회하고 요청한 미션들이 해당 스탬프에 속해 있는 미션들인지 검증
        if (trip.getCategory() == TripCategory.COURSE) {
            Long currentStampId =
                    stampQueryService.getFirstInCompleteStampForCourseTrip(trip.getId()).getId();

            missionCommandService.validateMissionsBelongsToStamp(currentStampId, missions);
        }

        return missions;
    }

    private String determineTitleByStamps(TripCategory tripCategory, List<Mission> missions) {
        List<Stamp> stamps = missions.stream().map(Mission::getStamp).toList();

        return stampQueryService.getStampNameByTripCategory(tripCategory, stamps);
    }
}
