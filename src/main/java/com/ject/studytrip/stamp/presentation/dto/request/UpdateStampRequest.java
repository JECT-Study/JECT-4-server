package com.ject.studytrip.stamp.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateStampRequest(@Schema(description = "수정할 스탬프 이름") String name) {}
