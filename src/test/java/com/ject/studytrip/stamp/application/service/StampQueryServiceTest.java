package com.ject.studytrip.stamp.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.stamp.domain.error.StampErrorCode;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.repository.StampQueryRepository;
import com.ject.studytrip.stamp.domain.repository.StampRepository;
import com.ject.studytrip.stamp.fixture.StampFixture;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.TripFixture;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("StampQueryService 단위 테스트")
class StampQueryServiceTest extends BaseUnitTest {
    @InjectMocks private StampQueryService stampQueryService;
    @Mock private StampRepository stampRepository;
    @Mock private StampQueryRepository stampQueryRepository;

    private Trip courseTrip;
    private Trip exploreTrip;
    private Stamp courseStamp1;
    private Stamp courseStamp2;

    @BeforeEach
    void setup() {
        Member member = MemberFixture.createMemberFromKakao();
        courseTrip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        exploreTrip = TripFixture.createTripWithId(2L, member, TripCategory.EXPLORE);
        courseStamp1 = StampFixture.createStampWithId(1L, courseTrip, 1);
        courseStamp2 = StampFixture.createStampWithId(2L, courseTrip, 2);
    }

    @Nested
    @DisplayName("getValidStamp 메서드는")
    class GetValidStamp {

        @Test
        @DisplayName("유효하지 않은 스탬프 ID일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampNotFoundById() {
            // given
            Long stampId = 1000L;

            // when & then
            assertThatThrownBy(() -> stampQueryService.getValidStamp(courseTrip.getId(), stampId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("여행에 속한 스탬프가 아닐 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampDoesNotBelongToTrip() {
            // given
            given(stampRepository.findById(any())).willReturn(Optional.ofNullable(courseStamp1));

            // when & then
            assertThatThrownBy(
                            () ->
                                    stampQueryService.getValidStamp(
                                            exploreTrip.getId(), courseStamp1.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_NOT_BELONG_TO_TRIP.getMessage());
        }

        @Test
        @DisplayName("삭제된 스탬프일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampAlreadyDeleted() {
            // given
            courseStamp1.updateDeletedAt();
            Long tripId = courseTrip.getId();
            Long stampId = courseStamp1.getId();
            given(stampRepository.findById(stampId)).willReturn(Optional.ofNullable(courseStamp1));

            // when & then
            assertThatThrownBy(() -> stampQueryService.getValidStamp(tripId, stampId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("완료된 스탬프일 경우 예외가 발생한다.")
        void shouldThrowExceptionWhenStampAlreadyCompleted() {
            // given
            courseStamp1.updateCompleted();
            Long tripId = courseTrip.getId();
            Long stampId = courseStamp1.getId();
            given(stampRepository.findById(stampId)).willReturn(Optional.ofNullable(courseStamp1));

            // when & then
            assertThatThrownBy(() -> stampQueryService.getValidStamp(tripId, stampId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_ALREADY_COMPLETED.getMessage());
        }

        @Test
        @DisplayName("스탬프 ID로 스탬프를 조회하고, 여행 소속 및 삭제 여부를 검증하고 반환한다")
        void shouldGetStampReturnValidStamp() {
            // given
            given(stampRepository.findById(any())).willReturn(Optional.ofNullable(courseStamp1));

            // when
            Stamp stamp = stampQueryService.getValidStamp(courseTrip.getId(), courseStamp1.getId());

            // then
            assertThat(stamp.getId()).isEqualTo(courseStamp1.getId());
        }
    }

    @Nested
    @DisplayName("getStampsByTripId 메서드는")
    class GetStampsByTripId {

        @Test
        @DisplayName("유효한 여행 ID로 삭제된 스탬프를 제외한 스탬프 목록을 조회한다")
        void shouldGetStampsByTripId() {
            // given
            given(stampRepository.findAllByTripIdAndDeletedAtIsNull(courseTrip.getId()))
                    .willReturn(List.of(courseStamp1, courseStamp2));

            // when
            List<Stamp> stamps = stampQueryService.getStampsByTripId(courseTrip.getId());

            // then
            verify(stampRepository).findAllByTripIdAndDeletedAtIsNull(any());

            assertThat(stamps.isEmpty()).isFalse();
            assertThat(stamps.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getFirstInCompleteStampForCourseTrip 메서드는")
    class GetFirstInCompleteStampForCourseTrip {

        @Test
        @DisplayName("코스형 여행의 현재 진행중인 스탬프가 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenNoIncompleteStampExistsForCourseTrip() {
            // given
            given(stampQueryRepository.findFirstIncompleteStampByTripId(any()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () ->
                                    stampQueryService.getFirstInCompleteStampForCourseTrip(
                                            courseTrip.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("코스형 여행 ID로 현재 진행중인 스탬프를 조회하고 반한환다")
        void shouldReturnFirstIncompleteStampForCourseTrip() {
            // given
            given(stampQueryRepository.findFirstIncompleteStampByTripId(any()))
                    .willReturn(Optional.ofNullable(courseStamp1));

            // when
            Stamp stamp =
                    stampQueryService.getFirstInCompleteStampForCourseTrip(courseTrip.getId());

            // then
            assertThat(stamp.getId()).isEqualTo(courseStamp1.getId());
            assertThat(stamp.isCompleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("getStampNameByTripCategory 메서드는")
    class GetStampNameByTripCategory {

        @Test
        @DisplayName("스탬프 목록이 비어있을 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampListIsEmpty() {
            // given
            List<Stamp> emptyStamps = List.of();

            // when & then
            assertThatThrownBy(
                            () ->
                                    stampQueryService.getStampNameByTripCategory(
                                            TripCategory.COURSE, emptyStamps))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_LIST_CANNOT_BE_EMPTY.getMessage());
        }

        @Test
        @DisplayName("스탬프가 삭제된 상태일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampIsDeleted() {
            // given
            courseStamp1.updateDeletedAt();
            List<Stamp> stamps = List.of(courseStamp1);

            // when & then
            assertThatThrownBy(
                            () ->
                                    stampQueryService.getStampNameByTripCategory(
                                            TripCategory.COURSE, stamps))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("코스형 여행일 경우 선택한 미션들이 하나의 스탬프에 속하므로 해당 스탬프의 이름을 반환한다")
        void shouldReturnStampNameForCourseTrip() {
            // given
            Stamp stamp = StampFixture.createStamp(courseTrip, 1);
            List<Stamp> stamps = List.of(stamp);

            // when
            String result =
                    stampQueryService.getStampNameByTripCategory(TripCategory.COURSE, stamps);

            // then
            assertThat(result).isEqualTo(stamp.getName());
        }

        @Test
        @DisplayName("탐험형 여행일 경우 선택한 미션들의 스탬프 중 가장 많이 포함된 스탬프의 이름을 제목으로 반환한다")
        void shouldReturnMostFrequentStampNameForExplorationTrip() {
            // given
            Stamp stamp1 = StampFixture.createStamp(exploreTrip, 0);
            Stamp stamp2 = StampFixture.createStamp(exploreTrip, 0);
            List<Stamp> stamps = List.of(stamp1, stamp1, stamp2);

            // then
            String result =
                    stampQueryService.getStampNameByTripCategory(TripCategory.EXPLORE, stamps);

            // when
            assertThat(result).isEqualTo(stamp1.getName());
        }

        @Test
        @DisplayName("탐험형 여행이면서 가장 많이 포함된 스탬프가 2개 이상일 경우 createdAt이 가장 빠른 스탬프의 이름을 반환한다")
        void shouldReturnEarliestStampNameWhenFrequencyIsSame() {
            // given
            Stamp stamp1 = StampFixture.createStamp(exploreTrip, 0);
            ReflectionTestUtils.setField(stamp1, "createdAt", LocalDateTime.now());
            Stamp stamp2 = StampFixture.createStamp(exploreTrip, 0);
            ReflectionTestUtils.setField(stamp2, "createdAt", LocalDateTime.now().minusDays(1));
            List<Stamp> stamps = List.of(stamp1, stamp1, stamp2, stamp2);

            // when
            String result =
                    stampQueryService.getStampNameByTripCategory(TripCategory.EXPLORE, stamps);

            // then
            assertThat(result).isEqualTo(stamp2.getName());
        }
    }

    @Nested
    @DisplayName("getNextStampOrderByTrip 메서드는")
    class GetNextStampOrderByTrip {

        @Test
        @DisplayName("탐험형 여행일 경우 0을 반환한다.")
        void shouldReturnZeroForExploreTrip() {
            // when
            int result = stampQueryService.getNextStampOrderByTrip(exploreTrip);

            // then
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("코스형 여행일 경우 다음 스탬프 순서를 반환한다.")
        void shouldReturnNextStampOrderForCourseTrip() {
            // given
            given(stampQueryRepository.findNextStampOrderByTripId(courseTrip.getId()))
                    .willReturn(3);

            // when
            int result = stampQueryService.getNextStampOrderByTrip(courseTrip);

            // then
            assertThat(result).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getStampsToShiftAfterDeleted 메서드는")
    class GetStampsToShiftAfterDeleted {

        @Test
        @DisplayName("시프트할 스탬프가 존재하지 않으면 빈 리스트를 반환한다.")
        void shouldReturnEmptyListWhenStampsToShiftDoNotExist() {
            // given
            Long tripId = courseTrip.getId();
            int deletedOrder = courseStamp2.getStampOrder();
            given(stampQueryRepository.findStampsToShiftAfterOrder(tripId, deletedOrder))
                    .willReturn(List.of());

            // when
            List<Stamp> result =
                    stampQueryService.getStampsToShiftAfterDeleted(tripId, deletedOrder);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(List.of());
        }

        @Test
        @DisplayName("시프트할 스탬프가 존재하면 스탬프 리스트를 반환한다.")
        void shouldReturnStampsWhenStampsToShiftExist() {
            // given
            Long tripId = courseTrip.getId();
            int deletedOrder = courseStamp1.getStampOrder();
            given(stampQueryRepository.findStampsToShiftAfterOrder(tripId, deletedOrder))
                    .willReturn(List.of(courseStamp2));

            // when
            List<Stamp> result =
                    stampQueryService.getStampsToShiftAfterDeleted(tripId, deletedOrder);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(List.of(courseStamp2));
        }
    }
}
