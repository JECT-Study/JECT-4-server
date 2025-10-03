package com.ject.studytrip.trip.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.trip.domain.error.DailyGoalErrorCode;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.policy.DailyGoalPolicy;
import com.ject.studytrip.trip.domain.repository.DailyGoalRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyGoalQueryService {
    public final DailyGoalRepository dailyGoalRepository;

    public DailyGoal getValidDailyGoal(Long tripId, Long dailyGoalId) {
        DailyGoal dailyGoal =
                dailyGoalRepository
                        .findById(dailyGoalId)
                        .orElseThrow(
                                () -> new CustomException(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND));

        DailyGoalPolicy.validateBelongsToTrip(dailyGoal, tripId);
        DailyGoalPolicy.validateNotDeleted(dailyGoal);

        return dailyGoal;
    }

    public List<DailyGoal> getCompleteDailyGoalsByTripId(Long tripId) {
        return dailyGoalRepository.findAllByTripIdAndCompletedIsTrue(tripId);
    }
}
