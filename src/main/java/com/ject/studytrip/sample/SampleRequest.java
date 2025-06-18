package com.ject.studytrip.sample;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record SampleRequest(
        @NotEmpty(message = "sample 은 필수입니다.") String sample,
        @Min(value = 1, message = "sampleNum 은 1 이상이여야 합니다.") int sampleNum) {}
