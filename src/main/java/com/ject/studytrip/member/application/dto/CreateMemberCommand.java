package com.ject.studytrip.member.application.dto;

public record CreateMemberCommand(
        String socialId, String email, String profileImage, String nickname, String category) {
    public static CreateMemberCommand of(
            String socialId, String email, String profileImage, String nickname, String category) {
        return new CreateMemberCommand(socialId, email, profileImage, nickname, category);
    }
}
