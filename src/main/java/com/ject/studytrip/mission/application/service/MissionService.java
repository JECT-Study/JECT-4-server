package com.ject.studytrip.mission.application.service;

import com.ject.studytrip.mission.application.dto.MissionInfo;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.repository.MissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepository missionRepository;

    public List<MissionInfo> getMissionsByStamp(Long stampId) {
        List<Mission> missions = missionRepository.findAllByStampIdOrderByMissionOrder(stampId);

        return missions.stream().map(MissionInfo::from).toList();
    }
}
