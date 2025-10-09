package com.ject.studytrip.studylog.presentation.dto.response;

import com.ject.studytrip.mission.application.dto.MissionInfo;
import com.ject.studytrip.studylog.application.dto.StudyLogDailyMissionInfo;
import com.ject.studytrip.studylog.application.dto.StudyLogDetail;
import com.ject.studytrip.studylog.application.dto.StudyLogInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Slice;

public record LoadStudyLogsSliceResponse(
        @Schema(description = "학습 로그 목록") List<StudyLogResponse> studyLogs,
        @Schema(description = "다음 데이터 존재 여부") boolean hasNext) {
    public static LoadStudyLogsSliceResponse of(Slice<StudyLogDetail> results) {
        return new LoadStudyLogsSliceResponse(
                results.getContent().stream()
                        .map(
                                result ->
                                        StudyLogResponse.of(
                                                result.studyLogInfo(),
                                                result.studyLogDailyMissionInfos()))
                        .toList(),
                results.hasNext());
    }

    private record StudyLogResponse(
            @Schema(description = "학습 로그 ID") Long studyLogId,
            @Schema(description = "학습 로그에서 선택한 미션 목록")
                    List<StudyLogDailyMissionResponse> dailyMissions,
            @Schema(description = "학습 로그 제목") String title,
            @Schema(description = "학습 로그 내용") String content,
            @Schema(description = "학습 로그 이미지 URL") String imageUrl,
            @Schema(description = "학습 로그 생성날짜") String createdAt) {
        private static StudyLogResponse of(
                StudyLogInfo studyLogInfo,
                List<StudyLogDailyMissionInfo> studyLogDailyMissionInfos) {
            return new StudyLogResponse(
                    studyLogInfo.studyLogId(),
                    studyLogDailyMissionInfos.stream()
                            .map(
                                    studyLogDailyMissionInfo ->
                                            StudyLogDailyMissionResponse.of(
                                                    studyLogDailyMissionInfo,
                                                    studyLogDailyMissionInfo
                                                            .dailyMissionInfo()
                                                            .missionInfo()))
                            .toList(),
                    studyLogInfo.title(),
                    studyLogInfo.content(),
                    studyLogInfo.imageUrl(),
                    studyLogInfo.createdAt());
        }

        private record StudyLogDailyMissionResponse(
                @Schema(description = "학습 로그 데일리 미션 ID") Long studyLogDailyMissionId,
                @Schema(description = "미션 이름") String missionName) {
            private static StudyLogDailyMissionResponse of(
                    StudyLogDailyMissionInfo studyLogDailyMissionInfo, MissionInfo missionInfo) {
                return new StudyLogDailyMissionResponse(
                        studyLogDailyMissionInfo.studyLogDailyMissionId(),
                        missionInfo.missionName());
            }
        }
    }
}
