package com.ject.studytrip.image.application.dto;

public record PresignedImageInfo(String tmpKey, String presignedUrl) {
    public static PresignedImageInfo of(String tmpKey, String presignedUrl) {
        return new PresignedImageInfo(tmpKey, presignedUrl);
    }
}
