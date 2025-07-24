package com.ject.studytrip.mission.domain.policy;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.mission.domain.error.MissionErrorCode;
import com.ject.studytrip.mission.domain.model.Mission;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MissionPolicy {

    public static void validateMissionOrders(
            List<Long> orderedMissionIds, List<Mission> savedMissions) {
        // #1: 중복 미션 ID 검증
        Set<Long> uniqueIds = new HashSet<>(orderedMissionIds);

        if (uniqueIds.size() != orderedMissionIds.size()) {
            throw new CustomException(MissionErrorCode.MISSION_ORDER_IDS_DUPLICATED);
        }

        // #2: 미션 개수 검증
        if (orderedMissionIds.size() != savedMissions.size()) {
            throw new CustomException(MissionErrorCode.MISSION_ORDER_SIZE_MISMATCHED);
        }

        // #3: 요청 ID와 실제 저장된 미션 ID가 정확히 일치하는지 확인
        Set<Long> existingIds =
                savedMissions.stream().map(Mission::getId).collect(Collectors.toSet());

        if (!existingIds.equals(uniqueIds)) {
            throw new CustomException(MissionErrorCode.MISSION_ORDER_IDS_NOT_MATCHED);
        }
    }

    public static void validateMissionBelongsToStamp(Long stampId, Mission mission) {
        if (!mission.getStamp().getId().equals(stampId)) {
            throw new CustomException(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP);
        }
    }

    public static void validateNotDeleted(Mission mission) {
        if (mission.getDeletedAt() != null) {
            throw new CustomException(MissionErrorCode.MISSION_ALREADY_DELETED);
        }
    }

    public static void validateOrderNotDuplicated(boolean exists) {
        if (exists) {
            throw new CustomException(MissionErrorCode.MISSION_ORDER_ALREADY_EXISTS);
        }
    }

    public static void validateCompleted(Mission mission) {
        if (mission.isCompleted())
            throw new CustomException(MissionErrorCode.MISSION_ALREADY_COMPLETED);
    }

    public static void validateExistAll(List<Mission> foundMissions, List<Long> requestedIds) {
        boolean isEquals = foundMissions.size() == requestedIds.size();
        if (!isEquals) {
            throw new CustomException(MissionErrorCode.MISSION_NOT_FOUND);
        }
    }
}
