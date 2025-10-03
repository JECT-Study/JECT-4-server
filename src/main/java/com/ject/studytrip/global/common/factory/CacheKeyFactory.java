package com.ject.studytrip.global.common.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheKeyFactory {
    public static String member(Long memberId) {
        return "member:" + memberId;
    }

    public static String trips(Long memberId, int page, int size) {
        return "member:" + memberId + ":page:" + page + ":size:" + size;
    }

    public static String trip(Long memberId, Long tripId) {
        return "member:" + memberId + ":trip:" + tripId;
    }

    public static String stamps(Long memberId, Long tripId) {
        return "member:" + memberId + ":trip:" + tripId;
    }

    public static String stamp(Long memberId, Long tripId, Long stampId) {
        return "member:" + memberId + ":trip:" + tripId + ":stamp:" + stampId;
    }

    public static String missions(Long memberId, Long tripId, Long stampId) {
        return "member:" + memberId + ":trip:" + tripId + ":stamp:" + stampId;
    }

    public static String dailyGoal(Long memberId, Long tripId, Long dailyGoalId) {
        return "member:" + memberId + ":trip:" + tripId + ":dailyGoal:" + dailyGoalId;
    }

    public static String studyLogs(Long memberId, Long tripId, int page, int size) {
        return "member:" + memberId + ":trip:" + tripId + ":page:" + page + ":size:" + size;
    }
}
