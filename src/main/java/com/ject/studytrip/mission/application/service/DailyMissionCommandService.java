package com.ject.studytrip.mission.application.service;

import com.ject.studytrip.mission.domain.factory.DailyMissionFactory;
import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.repository.DailyMissionCommandRepository;
import com.ject.studytrip.mission.domain.repository.DailyMissionRepository;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyMissionCommandService {
    private final DailyMissionRepository dailyMissionRepository;
    private final DailyMissionCommandRepository dailyMissionCommandRepository;

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
        return dailyMissionCommandRepository.deleteAllByDeletedAtIsNotNull();
    }

    public long hardDeleteDailyMissionsOwnedByDeletedMission() {
        return dailyMissionCommandRepository.deleteAllByDeletedMissionOwner();
    }

    public long hardDeleteDailyMissionsOwnedByDeletedDailyGoal() {
        return dailyMissionCommandRepository.deleteAllByDeletedDailyGoalOwner();
    }

    public long hardDeleteDailyMissionsByMember(Long memberId) {
        return dailyMissionCommandRepository.deleteAllByMemberId(memberId);
    }
}
