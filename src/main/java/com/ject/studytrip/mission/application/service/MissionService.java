package com.ject.studytrip.mission.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.mission.application.dto.MissionInfo;
import com.ject.studytrip.mission.domain.error.MissionErrorCode;
import com.ject.studytrip.mission.domain.factory.MissionFactory;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.policy.MissionPolicy;
import com.ject.studytrip.mission.domain.repository.MissionQueryRepository;
import com.ject.studytrip.mission.domain.repository.MissionRepository;
import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest;
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionOrderRequest;
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest;
import com.ject.studytrip.stamp.domain.model.Stamp;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepository missionRepository;
    private final MissionQueryRepository missionQueryRepository;

    @Transactional
    public Mission createMission(Stamp stamp, CreateMissionRequest request) {
        boolean exists =
                missionRepository.existsByStampIdAndMissionOrderAndDeletedAtIsNull(
                        stamp.getId(), request.order());
        MissionPolicy.validateOrderNotDuplicated(exists);

        Mission mission =
                MissionFactory.create(stamp, request.name(), request.memo(), request.order());

        return missionRepository.save(mission);
    }

    @Transactional
    public void updateMissionNameAndMemoIfPresent(
            Long stampId, Mission mission, UpdateMissionRequest request) {
        validateMissionIsActiveAndBelongsToStamp(stampId, mission);

        mission.update(request.name(), request.memo());
    }

    @Transactional
    public void updateMissionOrders(Long stampId, UpdateMissionOrderRequest request) {
        // 요청된 ID 목록에 해당하는 미션 조회
        List<Long> orderedMissionIds = request.orderedMissionIds();
        List<Mission> missions = missionRepository.findAllByIdIn(orderedMissionIds);

        // 정책 검증
        missions.forEach(mission -> validateMissionIsActiveAndBelongsToStamp(stampId, mission));
        MissionPolicy.validateMissionOrders(orderedMissionIds, missions);

        // ID -> 미션 맵 생성
        Map<Long, Mission> missionMap =
                missions.stream().collect(Collectors.toMap(Mission::getId, Function.identity()));

        // 미션 순서 업데이트
        for (int i = 0; i < orderedMissionIds.size(); i++) {
            Long missionId = orderedMissionIds.get(i);
            Mission mission = missionMap.get(missionId);
            mission.updateMissionOrder(i + 1);
        }
    }

    @Transactional
    public void deleteMission(Long stampId, Mission mission) {
        validateMissionIsActiveAndBelongsToStamp(stampId, mission);

        mission.updateDeletedAt();
    }

    @Transactional(readOnly = true)
    public List<MissionInfo> getMissionsByStamp(Long stampId) {
        List<Mission> missions = missionRepository.findAllByStampIdOrderByMissionOrder(stampId);

        return missions.stream().map(MissionInfo::from).toList();
    }

    @Transactional(readOnly = true)
    public List<Mission> getMissionsByStampId(Long stampId) {
        return missionRepository.findAllByStampIdAndDeletedAtIsNullOrderByMissionOrder(stampId);
    }

    @Transactional(readOnly = true)
    public Mission getValidMission(Long stampId, Long missionId) {
        Mission mission =
                missionRepository
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(MissionErrorCode.MISSION_NOT_FOUND));

        validateMissionIsActiveAndBelongsToStamp(stampId, mission);

        return mission;
    }

    public List<Mission> getValidMissionsWithStamp(List<Long> missionIds) {
        List<Mission> missions = missionQueryRepository.findAllByIdsInFetchJoinStamp(missionIds);

        MissionPolicy.validateExistAll(missions, missionIds);
        missions.forEach(
                mission -> {
                    MissionPolicy.validateNotDeleted(mission);
                    MissionPolicy.validateCompleted(mission);
                });

        return missions;
    }

    public void validateMissionBelongsToStamp(Long stampId, Mission mission) {
        MissionPolicy.validateMissionBelongsToStamp(stampId, mission);
    }

    private void validateMissionIsActiveAndBelongsToStamp(Long stampId, Mission mission) {
        MissionPolicy.validateMissionBelongsToStamp(stampId, mission);
        MissionPolicy.validateNotDeleted(mission);
    }
}
