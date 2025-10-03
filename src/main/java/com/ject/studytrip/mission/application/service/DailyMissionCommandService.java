package com.ject.studytrip.mission.application.service;

import com.ject.studytrip.mission.domain.factory.DailyMissionFactory;
import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.repository.DailyMissionQueryRepository;
import com.ject.studytrip.mission.domain.repository.DailyMissionRepository;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyMissionCommandService {
    private final DailyMissionRepository dailyMissionRepository;
    private final DailyMissionQueryRepository dailyMissionQueryRepository;

    public List<DailyMission> createDailyMissions(DailyGoal dailyGoal, List<Mission> missions) {
        List<DailyMission> dailyMissions =
                missions.stream()
                        .map(mission -> DailyMissionFactory.create(mission, dailyGoal))
                        .toList();

        return dailyMissionRepository.saveAll(dailyMissions);
    }

    public void deleteDailyMission(DailyMission dailyMission) {
        dailyMission.updateDeletedAt();
    }

    public long hardDeleteDailyMissions() {
        return dailyMissionQueryRepository.deleteAllByDeletedAtIsNotNull();
    }

    public long hardDeleteDailyMissionsOwnedByDeletedMission() {
        return dailyMissionQueryRepository.deleteAllByDeletedMissionOwner();
    }

    public long hardDeleteDailyMissionsOwnedByDeletedDailyGoal() {
        return dailyMissionQueryRepository.deleteAllByDeletedDailyGoalOwner();
    }
}
