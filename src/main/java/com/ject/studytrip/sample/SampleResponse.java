package com.ject.studytrip.sample;

public record SampleResponse(String sample, int sampleNum) {

    public static SampleResponse of(String sample, int sampleNum) {
        return new SampleResponse(sample, sampleNum);
    }
}
