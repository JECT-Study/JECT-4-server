package com.ject.studytrip.mission.application.service;

import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.policy.DailyMissionPolicy;
import com.ject.studytrip.mission.domain.repository.DailyMissionQueryRepository;
import com.ject.studytrip.mission.domain.repository.DailyMissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyMissionQueryService {
    private final DailyMissionRepository dailyMissionRepository;
    private final DailyMissionQueryRepository dailyMissionQueryRepository;

    public List<DailyMission> getValidDailyMissionsByIds(
            Long dailyGoalId, List<Long> dailyMissionIds) {
        List<DailyMission> dailyMissions = dailyMissionRepository.findAllByIdIn(dailyMissionIds);
        validateDailyMissions(dailyMissions, dailyMissionIds, dailyGoalId);

        return dailyMissions;
    }

    public List<DailyMission> getValidDailyMissionsWithMissionAndStampByIds(
            Long dailyGoalId, List<Long> dailyMissionIds) {
        List<DailyMission> dailyMissions =
                dailyMissionQueryRepository.findAllWithMissionAndStampByIds(dailyMissionIds);
        validateDailyMissions(dailyMissions, dailyMissionIds, dailyGoalId);

        return dailyMissions;
    }

    public List<DailyMission> getDailyMissionsByDailyGoal(Long dailyGoalId) {
        return dailyMissionQueryRepository.findAllByDailyGoalIdFetchJoinMission(dailyGoalId);
    }

    private void validateDailyMissions(
            List<DailyMission> dailyMissions, List<Long> dailyMissionIds, Long dailyGoalId) {
        DailyMissionPolicy.validateExistAll(dailyMissions, dailyMissionIds);
        dailyMissions.forEach(
                dailyMission -> {
                    DailyMissionPolicy.validateBelongsToDailyGoal(dailyMission, dailyGoalId);
                    DailyMissionPolicy.validateNotDeleted(dailyMission);
                });
    }
}
