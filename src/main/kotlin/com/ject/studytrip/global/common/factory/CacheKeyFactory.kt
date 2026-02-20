package com.ject.studytrip.global.common.factory

object CacheKeyFactory {
    @JvmStatic
    fun member(memberId: Long): String = "member:$memberId"

    @JvmStatic
    fun trips(
        memberId: Long,
        page: Int,
        size: Int,
    ): String = "member:$memberId:trips:page:$page:size:$size"

    @JvmStatic
    fun trip(
        memberId: Long,
        tripId: Long,
    ): String = "member:$memberId:trip:$tripId"

    @JvmStatic
    fun stamps(
        memberId: Long,
        tripId: Long,
    ): String = "member:$memberId:trip:$tripId:stamps"

    @JvmStatic
    fun stamp(
        memberId: Long,
        tripId: Long,
        stampId: Long,
    ): String = "member:$memberId:trip:$tripId:stamp:$stampId"

    @JvmStatic
    fun missions(
        memberId: Long,
        tripId: Long,
        stampId: Long,
    ): String = "member:$memberId:trip:$tripId:stamp:$stampId:missions"

    @JvmStatic
    fun dailyGoal(
        memberId: Long,
        tripId: Long,
        dailyGoalId: Long,
    ): String = "member:$memberId:trip:$tripId:dailyGoal:$dailyGoalId"

    @JvmStatic
    fun studyLogs(
        memberId: Long,
        tripId: Long,
        page: Int,
        size: Int,
        order: String,
    ): String = "member:$memberId:trip:$tripId:studyLogs:page:$page:size:$size:order:${order.lowercase()}"

    @JvmStatic
    fun tripReports(memberId: Long): String = "member:$memberId:tripReports"

    @JvmStatic
    fun tripReport(
        memberId: Long,
        tripReportId: Long,
        page: Int,
        size: Int,
    ): String = "member:$memberId:tripReport:$tripReportId:page:$page:size:$size"
}
