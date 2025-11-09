package com.ject.studytrip.dummy.application.dto;

import java.time.LocalDate;

public record CreateDummyStampCommand(String name, int stampOrder, LocalDate endDate) {
    public static CreateDummyStampCommand of(String name, int stampOrder, LocalDate endDate) {
        return new CreateDummyStampCommand(name, stampOrder, endDate);
    }
}
