package com.ject.studytrip.studylog.application.dto;

public record PresignedStudyLogImageInfo(Long studyLogId, String tmpKey, String presignedUrl) {
    public static PresignedStudyLogImageInfo of(
            Long studyLogId, String tmpKey, String presignedUrl) {
        return new PresignedStudyLogImageInfo(studyLogId, tmpKey, presignedUrl);
    }
}
