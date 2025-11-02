package com.ject.studytrip.trip.application.service;

import com.ject.studytrip.trip.domain.factory.DailyGoalFactory;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.repository.DailyGoalCommandRepository;
import com.ject.studytrip.trip.domain.repository.DailyGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyGoalCommandService {
    public final DailyGoalRepository dailyGoalRepository;
    public final DailyGoalCommandRepository dailyGoalCommandRepository;

    public DailyGoal createDailyGoal(Trip trip, String title) {
        DailyGoal dailyGoal = DailyGoalFactory.create(trip, title);

        return dailyGoalRepository.save(dailyGoal);
    }

    public void deleteDailyGoal(DailyGoal dailyGoal) {
        dailyGoal.updateDeletedAt();
    }

    public long hardDeleteDailyGoals() {
        return dailyGoalCommandRepository.deleteAllByDeletedAtIsNotNull();
    }

    public long hardDeleteDailyGoalsOwnedByDeletedTrip() {
        return dailyGoalCommandRepository.deleteAllByDeletedTripOwner();
    }

    public long hardDeleteDailyGoalsByMember(Long memberId) {
        return dailyGoalCommandRepository.deleteAllByMemberId(memberId);
    }
}
