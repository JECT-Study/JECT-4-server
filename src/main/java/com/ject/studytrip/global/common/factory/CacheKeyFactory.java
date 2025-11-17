package com.ject.studytrip.global.common.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheKeyFactory {
    public static String member(Long memberId) {
        return "member:" + memberId;
    }

    public static String trips(Long memberId, int page, int size) {
        return "member:" + memberId + ":trips" + ":page:" + page + ":size:" + size;
    }

    public static String trip(Long memberId, Long tripId) {
        return "member:" + memberId + ":trip:" + tripId;
    }

    public static String stamps(Long memberId, Long tripId) {
        return "member:" + memberId + ":trip:" + tripId + ":stamps";
    }

    public static String stamp(Long memberId, Long tripId, Long stampId) {
        return "member:" + memberId + ":trip:" + tripId + ":stamp:" + stampId;
    }

    public static String missions(Long memberId, Long tripId, Long stampId) {
        return "member:" + memberId + ":trip:" + tripId + ":stamp:" + stampId + ":missions";
    }

    public static String dailyGoal(Long memberId, Long tripId, Long dailyGoalId) {
        return "member:" + memberId + ":trip:" + tripId + ":dailyGoal:" + dailyGoalId;
    }

    public static String studyLogs(Long memberId, Long tripId, int page, int size, String order) {
        return "member:"
                + memberId
                + ":trip:"
                + tripId
                + ":studyLogs"
                + ":page:"
                + page
                + ":size:"
                + size
                + ":order:"
                + order.toLowerCase();
    }

    public static String tripReports(Long memberId) {
        return "member:" + memberId + ":tripReports";
    }

    public static String tripReport(Long memberId, Long tripReportId, int page, int size) {
        return "member:"
                + memberId
                + ":tripReport:"
                + tripReportId
                + ":page:"
                + page
                + ":size:"
                + size;
    }
}
