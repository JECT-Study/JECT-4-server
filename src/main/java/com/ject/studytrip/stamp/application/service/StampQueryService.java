package com.ject.studytrip.stamp.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.stamp.domain.error.StampErrorCode;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.policy.StampPolicy;
import com.ject.studytrip.stamp.domain.repository.StampQueryRepository;
import com.ject.studytrip.stamp.domain.repository.StampRepository;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StampQueryService {
    private final StampRepository stampRepository;
    private final StampQueryRepository stampQueryRepository;

    public Stamp getValidStamp(Long tripId, Long stampId) {
        Stamp stamp =
                stampRepository
                        .findById(stampId)
                        .orElseThrow(() -> new CustomException(StampErrorCode.STAMP_NOT_FOUND));

        StampPolicy.validateStampBelongsToTrip(tripId, stamp);
        StampPolicy.validateNotDeleted(stamp);
        StampPolicy.validateCompleted(stamp);

        return stamp;
    }

    public List<Stamp> getStampsByTripId(Long tripId) {
        return stampRepository.findAllByTripIdAndDeletedAtIsNull(tripId);
    }

    public Stamp getFirstInCompleteStampForCourseTrip(Long tripId) {
        return stampQueryRepository
                .findFirstIncompleteStampByTripId(tripId)
                .orElseThrow(() -> new CustomException(StampErrorCode.STAMP_NOT_FOUND));
    }

    public String getStampNameByTripCategory(TripCategory tripCategory, List<Stamp> stamps) {
        // 스탬프 목록이 비어있지 않은지 검증
        StampPolicy.validateStampListNotEmpty(stamps);
        stamps.forEach(StampPolicy::validateNotDeleted);

        if (tripCategory == TripCategory.COURSE) {
            // 코스형 여행은 상위 검증에서 동일한 스탬프인지 검증이 완료된 상태이므로
            // 스탬프 리스트에서 첫 번째 스탬프의 이름을 추출해도 안전
            return stamps.get(0).getName();
        }

        return getExplorationStampName(stamps);
    }

    public int getNextStampOrderByTrip(Trip trip) {
        if (trip.getCategory() != TripCategory.COURSE) {
            return 0;
        }

        return stampQueryRepository.findNextStampOrderByTripId(trip.getId());
    }

    public List<Stamp> getStampsToShiftAfterDeleted(Long tripId, int deletedStampOrder) {
        return stampQueryRepository.findStampsToShiftAfterOrder(tripId, deletedStampOrder);
    }

    private String getExplorationStampName(List<Stamp> stamps) {
        // 스탬프별 개수 집계
        Map<Stamp, Long> stampCountMap =
                stamps.stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 최대 개수를 가진 스탬프들 찾기
        long maxCount = stampCountMap.values().stream().max(Long::compareTo).orElse(0L);

        List<Stamp> maxCountStamps =
                stampCountMap.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(maxCount))
                        .map(Map.Entry::getKey)
                        .toList();

        // 가장 이른 생성 시간을 가진 스탬프 선택
        return maxCountStamps.stream()
                .min(Comparator.comparing(Stamp::getCreatedAt))
                .map(Stamp::getName)
                .orElse("");
    }
}
