package com.ject.studytrip.trip.application.facade;

import com.ject.studytrip.member.application.service.MemberService;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.mission.application.dto.DailyMissionInfo;
import com.ject.studytrip.mission.application.service.DailyMissionService;
import com.ject.studytrip.mission.application.service.MissionService;
import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.pomodoro.application.dto.PomodoroInfo;
import com.ject.studytrip.pomodoro.application.service.PomodoroService;
import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.stamp.application.service.StampService;
import com.ject.studytrip.trip.application.dto.DailyGoalDetail;
import com.ject.studytrip.trip.application.dto.DailyGoalInfo;
import com.ject.studytrip.trip.application.service.DailyGoalService;
import com.ject.studytrip.trip.application.service.TripService;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.presentation.dto.request.CreateDailyGoalRequest;
import com.ject.studytrip.trip.presentation.dto.request.UpdateDailyGoalRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyGoalFacade {
    private final MemberService memberService;
    private final TripService tripService;
    private final StampService stampService;
    private final MissionService missionService;
    private final DailyGoalService dailyGoalService;
    private final PomodoroService pomodoroService;
    private final DailyMissionService dailyMissionService;

    @Transactional
    public DailyGoalInfo createDailyGoal(
            Long memberId, Long tripId, CreateDailyGoalRequest request) {
        Trip trip = getValidTripOwnedByMember(memberId, tripId);

        DailyGoal dailyGoal = dailyGoalService.createDailyGoal(trip);

        List<Mission> missions = getValidMissionsByTripCategory(trip, request.missionIds());
        dailyMissionService.createDailyMissions(dailyGoal, missions);

        pomodoroService.createPomodoro(dailyGoal, request.pomodoro());

        return DailyGoalInfo.from(dailyGoal);
    }

    @Transactional
    public void updateDailyGoal(
            Long memberId, Long tripId, Long dailyGoalId, UpdateDailyGoalRequest request) {
        Trip trip = getValidTripOwnedByMember(memberId, tripId);

        DailyGoal dailyGoal = dailyGoalService.getValidDailyGoal(trip.getId(), dailyGoalId);

        // 삭제할 데일리 미션이 있을 경우
        if (request.deleteDailyMissionIds() != null && !request.deleteDailyMissionIds().isEmpty()) {
            List<DailyMission> deleteDailyMissions =
                    dailyMissionService.getValidDailyMissionsByIds(
                            dailyGoal.getId(), request.deleteDailyMissionIds());
            deleteDailyMissions.forEach(dailyMissionService::deleteDailyMission);
        }

        // 새로 추가할 미션이 있을 경우
        if (request.addMissionIds() != null && !request.addMissionIds().isEmpty()) {
            List<Mission> addMissions =
                    getValidMissionsByTripCategory(trip, request.addMissionIds());
            dailyMissionService.createDailyMissions(dailyGoal, addMissions);
        }
    }

    @Transactional
    public void deleteDailyGoal(Long memberId, Long tripId, Long dailyGoalId) {
        Trip trip = getValidTripOwnedByMember(memberId, tripId);

        DailyGoal dailyGoal = dailyGoalService.getValidDailyGoal(trip.getId(), dailyGoalId);

        // 뽀모도로 삭제
        Pomodoro pomodoro = pomodoroService.getValidPomodoroByDailyGoal(dailyGoal.getId());
        pomodoroService.deletePomodoro(pomodoro);

        // 데일리 미션 삭제
        List<DailyMission> dailyMissions =
                dailyMissionService.getDailyMissionsByDailyGoal(dailyGoal.getId());
        for (DailyMission dailyMission : dailyMissions) {
            dailyMissionService.deleteDailyMission(dailyMission);
        }

        // 데일리 목표 삭제
        dailyGoalService.deleteDailyGoal(dailyGoal);
    }

    public DailyGoalDetail getDailyGoal(Long memberId, Long tripId, Long dailyGoalId) {
        Trip trip = getValidTripOwnedByMember(memberId, tripId);

        DailyGoal dailyGoal = dailyGoalService.getValidDailyGoal(trip.getId(), dailyGoalId);

        Pomodoro pomodoro = pomodoroService.getValidPomodoroByDailyGoal(dailyGoal.getId());
        List<DailyMission> dailyMissions =
                dailyMissionService.getDailyMissionsByDailyGoal(dailyGoal.getId());

        return DailyGoalDetail.from(
                DailyGoalInfo.from(dailyGoal),
                PomodoroInfo.from(pomodoro),
                dailyMissions.stream().map(DailyMissionInfo::from).toList());
    }

    private Trip getValidTripOwnedByMember(Long memberId, Long tripId) {
        Member member = memberService.getMember(memberId);

        return tripService.getValidTrip(member.getId(), tripId);
    }

    private List<Mission> getValidMissionsByTripCategory(Trip trip, List<Long> missionIds) {
        List<Mission> missions = missionService.getValidMissionsWithStamp(missionIds);

        for (Mission mission : missions) {
            stampService.validateStampBelongsToTrip(trip.getId(), mission.getStamp());
        }

        // 코스형 여행일 경우, 현재 진행중인 스탬프를 조회하고 요청한 미션들이 해당 스탬프에 속해 있는 미션들인지 검증
        if (trip.getCategory() == TripCategory.COURSE) {
            Long currentStampId =
                    stampService.getFirstInCompleteStampForCourseTrip(trip.getId()).getId();

            for (Mission mission : missions) {
                missionService.validateMissionBelongsToStamp(currentStampId, mission);
            }
        }

        return missions;
    }
}
