package com.ject.studytrip.dummy.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.trip.domain.model.Trip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

@DisplayName("DummyTripCommandService 단위 테스트")
class DummyTripCommandServiceTest extends BaseUnitTest {
    private static final int COUNT = 10;

    @InjectMocks private DummyTripCommandService dummyTripCommandService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMemberFromKakao();
    }

    @Nested
    @DisplayName("createDummyTrip 메서드는")
    class CreateDummyTrip {

        @Test
        @DisplayName("COURSE 카테고리가 들어오면 코스형 더미 여행을 생성하고 반환한다.")
        void shouldReturnDummyCourseTripWhenCategoryIsCourse() {
            // given
            String category = "COURSE";

            // when
            Trip result = dummyTripCommandService.createDummyTrip(member, category, COUNT);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isNotNull();
            assertThat(result.getMemo()).isNotNull();
            assertThat(result.getCategory()).isNotNull();
            assertThat(result.getStartDate()).isNotNull();
            assertThat(result.getEndDate()).isNotNull();
        }

        @Test
        @DisplayName("EXPLORE 카테고리가 들어오면 탐험형 더미 여행을 생성하고 반환한다.")
        void shouldReturnDummyExploreTripWhenCategoryIsExplore() {
            // given
            String category = "EXPLORE";

            // when
            Trip result = dummyTripCommandService.createDummyTrip(member, category, COUNT);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isNotNull();
            assertThat(result.getMemo()).isNotNull();
            assertThat(result.getCategory()).isNotNull();
            assertThat(result.getStartDate()).isNotNull();
            assertThat(result.getEndDate()).isNull();
        }
    }
}
