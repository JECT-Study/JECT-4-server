package com.ject.studytrip.dummy.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.fixture.StampFixture;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.TripFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

@DisplayName("DummyMissionCommandService 단위 테스트")
class DummyMissionCommandServiceTest extends BaseUnitTest {
    private static final int COUNT = 10;

    @InjectMocks private DummyMissionCommandService dummyMissionCommandService;

    private Trip courseTrip;

    @BeforeEach
    void setUp() {
        Member member = MemberFixture.createMemberFromKakao();
        courseTrip = new TripFixture(member, TripCategory.COURSE).create();
    }

    @Nested
    @DisplayName("createDummyMission 메서드는")
    class CreateDummyMission {

        @Test
        @DisplayName("특정 스탬프가 들어오면 더미 미션을 생성하고 리턴한다.")
        void shouldReturnDummyMissionForStamp() {
            // given
            Stamp stamp = new StampFixture(courseTrip, COUNT).create();

            // when
            Mission result = dummyMissionCommandService.createDummyMission(stamp);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isNotNull();
        }
    }
}
