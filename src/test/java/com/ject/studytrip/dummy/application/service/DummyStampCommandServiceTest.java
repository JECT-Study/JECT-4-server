package com.ject.studytrip.dummy.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.TripFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

@DisplayName("DummyStampCommandService 단위 테스트")
class DummyStampCommandServiceTest extends BaseUnitTest {
    private static final int COUNT = 10;

    @InjectMocks private DummyStampCommandService dummyStampCommandService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMemberFromKakao();
    }

    @Nested
    @DisplayName("createDummyStamp 메서드는")
    class CreateDummyStamp {

        @Test
        @DisplayName("코스형 여행이 들어오면 코스형 더미 스탬프를 생성하고 반환한다.")
        void shouldReturnDummyCourseStampForCourseTrip() {
            // given
            Trip courseTrip = new TripFixture(member, TripCategory.COURSE).create();

            // when
            Stamp result = dummyStampCommandService.createDummyStamp(courseTrip, COUNT);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isNotNull();
            assertThat(result.getStampOrder()).isPositive(); // 양수
            assertThat(result.getEndDate()).isNotNull();
        }

        @Test
        @DisplayName("탐험형 여행이 들어오면 탐험형 더미 스탬프를 생성하고 반환한다.")
        void shouldReturnDummyExploreStampForExploreTrip() {
            // given
            Trip exploreTrip = new TripFixture(member, TripCategory.EXPLORE).create();

            // when
            Stamp result = dummyStampCommandService.createDummyStamp(exploreTrip, COUNT);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isNotNull();
            assertThat(result.getStampOrder()).isZero();
            assertThat(result.getEndDate()).isNull();
        }
    }
}
