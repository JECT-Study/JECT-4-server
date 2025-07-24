package com.ject.studytrip.member.fixture;

import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest;

public class UpdateMemberRequestFixture {
    private String nickname = null;
    private String category = null;

    public UpdateMemberRequestFixture withNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public UpdateMemberRequestFixture withCategory(String category) {
        this.category = category;
        return this;
    }

    public UpdateMemberRequest build() {
        return new UpdateMemberRequest(nickname, category);
    }
}
