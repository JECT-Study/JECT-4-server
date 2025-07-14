package com.ject.studytrip.stamp.presentation.dto.response;

import com.ject.studytrip.stamp.application.dto.StampInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateStampResponse(@Schema(description = "스탬프 ID") Long stampId) {
    public static CreateStampResponse of(StampInfo info) {
        return new CreateStampResponse(info.stampId());
    }
}
