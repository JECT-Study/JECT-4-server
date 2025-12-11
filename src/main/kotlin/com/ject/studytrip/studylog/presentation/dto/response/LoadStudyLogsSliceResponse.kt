package com.ject.studytrip.studylog.presentation.dto.response

import com.ject.studytrip.mission.application.dto.MissionInfo
import com.ject.studytrip.studylog.application.dto.StudyLogDailyMissionInfo
import com.ject.studytrip.studylog.application.dto.StudyLogDetail
import com.ject.studytrip.studylog.application.dto.StudyLogInfo
import io.swagger.v3.oas.annotations.media.Schema

data class LoadStudyLogsSliceResponse(
    @field:Schema(description = "학습 로그 목록")
    val studyLogs: List<StudyLogResponse>,
    @field:Schema(description = "다음 데이터 존재 여부")
    val hasNext: Boolean,
) {
    companion object {
        @JvmStatic
        fun of(
            studyLogDetails: List<StudyLogDetail>,
            hasNext: Boolean,
        ): LoadStudyLogsSliceResponse =
            LoadStudyLogsSliceResponse(
                studyLogs =
                    studyLogDetails.map { detail ->
                        StudyLogResponse.of(
                            studyLogInfo = detail.studyLogInfo,
                            studyLogDailyMissionInfos = detail.studyLogDailyMissionInfos,
                        )
                    },
                hasNext = hasNext,
            )
    }

    data class StudyLogResponse(
        @field:Schema(description = "학습 로그 ID")
        val studyLogId: Long,
        @field:Schema(description = "학습 로그에서 선택한 미션 목록")
        val dailyMissions: List<StudyLogDailyMissionResponse>,
        @field:Schema(description = "학습 로그 제목")
        val title: String,
        @field:Schema(description = "학습 로그 내용")
        val content: String,
        @field:Schema(description = "학습 로그 이미지 URL")
        val imageUrl: String?,
        @field:Schema(description = "학습 로그 생성날짜")
        val createdAt: String,
    ) {
        companion object {
            fun of(
                studyLogInfo: StudyLogInfo,
                studyLogDailyMissionInfos: List<StudyLogDailyMissionInfo>,
            ): StudyLogResponse =
                StudyLogResponse(
                    studyLogId = studyLogInfo.studyLogId,
                    dailyMissions =
                        studyLogDailyMissionInfos.map { info ->
                            StudyLogDailyMissionResponse.of(
                                studyLogDailyMissionInfo = info,
                                missionInfo = info.dailyMissionInfo.missionInfo,
                            )
                        },
                    title = studyLogInfo.title,
                    content = studyLogInfo.content,
                    imageUrl = studyLogInfo.imageUrl,
                    createdAt = studyLogInfo.createdAt,
                )
        }

        data class StudyLogDailyMissionResponse(
            @field:Schema(description = "학습 로그 데일리 미션 ID")
            val studyLogDailyMissionId: Long,
            @field:Schema(description = "미션 이름")
            val missionName: String,
        ) {
            companion object {
                fun of(
                    studyLogDailyMissionInfo: StudyLogDailyMissionInfo,
                    missionInfo: MissionInfo,
                ): StudyLogDailyMissionResponse =
                    StudyLogDailyMissionResponse(
                        studyLogDailyMissionId = studyLogDailyMissionInfo.studyLogDailyMissionId,
                        missionName = missionInfo.missionName,
                    )
            }
        }
    }
}
