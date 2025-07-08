package com.ject.studytrip.stamp.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.entity.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.stamp.domain.error.StampErrorCode;
import com.ject.studytrip.stamp.domain.factory.StampFactory;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.repository.StampRepository;
import com.ject.studytrip.stamp.fixture.CreateStampRequestFixture;
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.TripFixture;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("StampService 단위 테스트")
public class StampServiceTest extends BaseUnitTest {
    private static final String STAMP_NAME = "STAMP NAME";
    private static final LocalDate STAMP_DEAD_LINE = LocalDate.now().plusDays(7);

    @InjectMocks private StampService stampService;
    @Mock private StampRepository stampRepository;

    private Trip courseTrip;
    private Trip exploreTrip;

    @BeforeEach
    void setup() {
        Member member = MemberFixture.createMemberFromKakao();
        courseTrip = TripFixture.createTrip(member, TripCategory.COURSE);
        exploreTrip = TripFixture.createTrip(member, TripCategory.EXPLORE);
    }

    @Nested
    @DisplayName("스탬프를 생성한다")
    class CreateStamp {

        @Test
        @DisplayName("코스형 여행의 유효한 스탬프 리스트를 넘기면 저장된다")
        void shouldCreateStampsForCourseTrip() {
            // given
            List<CreateStampRequest> requests = List.of(new CreateStampRequestFixture().build());

            // when
            stampService.createStamps(courseTrip, requests);

            // then
            verify(stampRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("탐험형 여행의 유효한 스탬프 리스트를 넘기면 저장된다")
        void shouldCreateStampsForExploreTrip() {
            // given
            List<CreateStampRequest> requests =
                    List.of(new CreateStampRequestFixture().withStampOrder(0).build());

            // when
            stampService.createStamps(exploreTrip, requests);

            // then
            verify(stampRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("스탬프의 마감일이 과거라면 예외가 발생한다")
        void shouldThrowExceptionWhenStampDeadlineCannotBeInPast() {
            // given
            List<CreateStampRequest> requests =
                    List.of(
                            new CreateStampRequestFixture()
                                    .withDeadline(LocalDate.now().minusDays(1))
                                    .build());

            // when & then
            assertThatThrownBy(() -> stampService.createStamps(courseTrip, requests))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_DEADLINE_CANNOT_BE_IN_PAST.getMessage());
        }

        @Test
        @DisplayName("스탬프의 마감일이 여행 종료일보다 이후일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampDeadlineIsAfterTripEndDate() {
            // given
            List<CreateStampRequest> requests =
                    List.of(
                            new CreateStampRequestFixture()
                                    .withDeadline(courseTrip.getEndDate().plusDays(1))
                                    .build());

            // when & then
            assertThatThrownBy(() -> stampService.createStamps(courseTrip, requests))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_DEADLINE_EXCEEDS_TRIP_END_DATE.getMessage());
        }

        @Test
        @DisplayName("탐험형 여행에서 순서가 1 이상이면 예외가 발생한다")
        void shouldThrowExceptionWhenOrderExistsInExploreTrip() {
            // given
            List<CreateStampRequest> requests =
                    List.of(new CreateStampRequestFixture().withStampOrder(1).build());

            // when & then
            assertThatThrownBy(() -> stampService.createStamps(exploreTrip, requests))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(
                            StampErrorCode.INVALID_STAMP_ORDER_FOR_EXPLORATION_TRIP.getMessage());
        }

        @Test
        @DisplayName("코스형 여행에서 순서가 1 미만 또는 총 개수 초과라면 예외가 발생한다")
        void shouldThrowExceptionWhenStampOrderIsOutOfRangeForCourseTrip() {
            // given
            List<CreateStampRequest> requests =
                    List.of(new CreateStampRequestFixture().withStampOrder(2).build());

            // when & then
            assertThatThrownBy(() -> stampService.createStamps(courseTrip, requests))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(
                            StampErrorCode.INVALID_STAMP_ORDER_RANGE_FOR_COURSE_TRIP.getMessage());
        }

        @Test
        @DisplayName("코스형 여행에서 순서가 중복되면 예외가 발생한다")
        void shouldThrowExceptionWhenDuplicateOrderInCourseTrip() {
            // given
            List<CreateStampRequest> requests =
                    List.of(
                            new CreateStampRequestFixture().withStampOrder(1).build(),
                            new CreateStampRequestFixture().withStampOrder(1).build());

            // when & then
            assertThatThrownBy(() -> stampService.createStamps(courseTrip, requests))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.DUPLICATE_STAMP_ORDER_FOR_COURSE_TRIP.getMessage());
        }
    }

    @Nested
    @DisplayName("스탬프를 수정한다")
    class UpdateStamp {

        @Test
        @DisplayName("여행의 카테고리가 탐험형으로 수정되면 소속된 모든 스탬프의 순서를 0으로 수정한다")
        void shouldSetAllStampOrdersToZeroWhenCategoryChangesToExplore() {
            // given
            Stamp stamp = spy(StampFactory.create(courseTrip, STAMP_NAME, 1, STAMP_DEAD_LINE));
            List<Stamp> stamps = List.of(stamp);

            given(stampRepository.findAllByTripIdOrderByDeadlineAsc(courseTrip.getId()))
                    .willReturn(stamps);

            // when
            stampService.updateStampsOrderByTripCategoryChange(
                    courseTrip.getId(), TripCategory.EXPLORE);

            // then
            assertThat(stamp.getStampOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("여행의 카테고리가 코스형으로 수정되면 소속된 모든 스탬프의 순서를 마감일이 이른 순으로 1부터 순차적으로 순서를 수정한다")
        void shouldSetSequentialStampOrdersWhenCategoryChangesToCourse() {
            // given
            Stamp stamp1 =
                    spy(
                            StampFactory.create(
                                    exploreTrip, STAMP_NAME, 0, STAMP_DEAD_LINE.plusDays(1)));
            Stamp stamp2 = spy(StampFactory.create(exploreTrip, STAMP_NAME, 0, STAMP_DEAD_LINE));
            List<Stamp> stamps =
                    Stream.of(stamp1, stamp2)
                            .sorted(Comparator.comparing(Stamp::getDeadline))
                            .collect(Collectors.toList());

            given(stampRepository.findAllByTripIdOrderByDeadlineAsc(exploreTrip.getId()))
                    .willReturn(stamps);

            // when
            stampService.updateStampsOrderByTripCategoryChange(
                    exploreTrip.getId(), TripCategory.COURSE);

            // then
            assertThat(stamp2.getStampOrder()).isEqualTo(1);
            assertThat(stamp1.getStampOrder()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("스탬프 목록을 조회한다")
    class ListStamps {

        @Test
        @DisplayName("유효한 여행 ID로 스탬프 목록을 조회한다")
        void shouldGetStampsByTripId() {
            // when
            stampService.getStampsByTripId(courseTrip.getId());

            // then
            verify(stampRepository).findAllByTripId(any());
        }
    }
}
