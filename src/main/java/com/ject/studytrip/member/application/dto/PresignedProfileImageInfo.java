package com.ject.studytrip.member.application.dto;

public record PresignedProfileImageInfo(Long memberId, String tmpKey, String presignedUrl) {
    public static PresignedProfileImageInfo of(Long memberId, String tmpKey, String presignedUrl) {
        return new PresignedProfileImageInfo(memberId, tmpKey, presignedUrl);
    }
}
