package com.ject.studytrip.member.fixture;

import com.ject.studytrip.member.application.dto.CreateMemberCommand;

public class CreateMemberCommandFixture {
    private String socialId = "12345";
    private String email = "choi@kakao.com";
    private String profileImage = "https://kakao.com/profile.jpg";
    private String nickname = "민우";
    private String category = "STUDENT";

    public CreateMemberCommandFixture withSocialId(String socialId) {
        this.socialId = socialId;
        return this;
    }

    public CreateMemberCommandFixture withEmail(String email) {
        this.email = email;
        return this;
    }

    public CreateMemberCommandFixture withProfileImage(String profileImage) {
        this.profileImage = profileImage;
        return this;
    }

    public CreateMemberCommandFixture withNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public CreateMemberCommandFixture withCategory(String category) {
        this.category = category;
        return this;
    }

    public CreateMemberCommand build() {
        return new CreateMemberCommand(socialId, email, profileImage, nickname, category);
    }
}
