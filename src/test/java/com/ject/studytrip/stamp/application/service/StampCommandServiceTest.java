package com.ject.studytrip.stamp.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.stamp.domain.error.StampErrorCode;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.repository.StampCommandRepository;
import com.ject.studytrip.stamp.domain.repository.StampRepository;
import com.ject.studytrip.stamp.fixture.CreateStampRequestFixture;
import com.ject.studytrip.stamp.fixture.StampFixture;
import com.ject.studytrip.stamp.fixture.UpdateStampOrderRequestFixture;
import com.ject.studytrip.stamp.fixture.UpdateStampRequestFixture;
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.TripFixture;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("StampCommandService 단위 테스트")
class StampCommandServiceTest extends BaseUnitTest {
    @InjectMocks private StampCommandService stampCommandService;
    @Mock private StampRepository stampRepository;
    @Mock private StampCommandRepository stampCommandRepository;

    private Member member;
    private Trip courseTrip;
    private Trip exploreTrip;
    private Stamp courseStamp1;
    private Stamp courseStamp2;
    private Stamp exploreStamp1;

    @BeforeEach
    void setup() {
        member = MemberFixture.createMemberFromKakao();
        courseTrip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        exploreTrip = TripFixture.createTripWithId(2L, member, TripCategory.EXPLORE);
        courseStamp1 = StampFixture.createStampWithId(1L, courseTrip, 1);
        courseStamp2 = StampFixture.createStampWithId(2L, courseTrip, 2);
        exploreStamp1 = StampFixture.createStampWithId(3L, exploreTrip, 0);
    }

    @Nested
    @DisplayName("createStamp 메서드는")
    class CreateStamp {
        private final CreateStampRequestFixture fixture = new CreateStampRequestFixture();

        @Test
        @DisplayName("스탬프 종료일이 과거 날짜라면 예외가 발생한다")
        void shouldThrowExceptionWhenEndDateIsInPast() {
            // given
            int nextOrder = courseStamp2.getStampOrder() + 1;
            CreateStampRequest request = fixture.withEndDateInPast().build();

            // when & then
            assertThatThrownBy(
                            () -> stampCommandService.createStamp(courseTrip, nextOrder, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_END_DATE_CANNOT_BE_IN_PAST.getMessage());
        }

        @Test
        @DisplayName("스탬프 종료일이 여행 종료일보다 이후라면 예외가 발생한다")
        void shouldThrowExceptionWhenEndDateIsAfterTripEndDate() {
            // given
            int nextOrder = courseStamp2.getStampOrder() + 1;
            CreateStampRequest request = fixture.withEndDateAfterTripEndDate().build();

            // when & then
            assertThatThrownBy(
                            () -> stampCommandService.createStamp(courseTrip, nextOrder, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(
                            StampErrorCode.STAMP_END_DATE_AFTER_TRIP_END_DATE_NOT_ALLOWED
                                    .getMessage());
        }

        @Test
        @DisplayName("탐험형 여행에서는 order가 항상 0으로 저장된다")
        void shouldCreateExploreStampWithOrderZero() {
            // given
            CreateStampRequest request = fixture.build();
            Stamp exploreStamp2 = StampFixture.createStampWithName(exploreTrip, request.name(), 0);
            given(stampRepository.save(any(Stamp.class))).willReturn(exploreStamp2);

            // when
            exploreStamp2 = stampCommandService.createStamp(exploreTrip, 0, request);

            // then
            assertThat(exploreStamp2.getTrip()).isEqualTo(exploreTrip);
            assertThat(exploreStamp2.getName()).isEqualTo(request.name());
            assertThat(exploreStamp2.getStampOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("코스형 여행에서는 마지막 order 다음 값으로 저장된다")
        void shouldCreateCourseStampWithNextSequentialOrder() {
            // given
            int nextOrder = courseStamp2.getStampOrder() + 1;
            CreateStampRequest request = fixture.build();
            Stamp courseStamp3 =
                    StampFixture.createStampWithName(courseTrip, request.name(), nextOrder);
            given(stampRepository.save(any(Stamp.class))).willReturn(courseStamp3);

            // when
            courseStamp3 = stampCommandService.createStamp(courseTrip, nextOrder, request);

            // then
            assertThat(courseStamp3.getStampOrder()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("createStamps 메서드는")
    class CreateStamps {
        private final CreateStampRequestFixture fixture = new CreateStampRequestFixture();

        @Test
        @DisplayName("탐험형 여행에서는 전달한 모든 스탬프의 order가 0으로 저장된다")
        void shouldCreateExploreStampsWithOrderZero() {
            // given
            List<CreateStampRequest> requests = List.of(fixture.build(), fixture.build());

            // when
            stampCommandService.createStamps(exploreTrip, 0, requests);

            // then
            verify(stampRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("코스형 여행에서는 마지막 order 다음 값부터 순차적으로 저장된다")
        void shouldCreateCourseStampsSequentiallyFromNextOrder() {
            // given
            int nextOrder = courseStamp2.getStampOrder() + 1;
            List<CreateStampRequest> requests = List.of(fixture.build(), fixture.build());

            // when
            stampCommandService.createStamps(courseTrip, nextOrder, requests);

            // then
            verify(stampRepository).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("updateStamp 메서드는")
    class UpdateStamp {
        private final UpdateStampRequestFixture fixture = new UpdateStampRequestFixture();

        @Test
        @DisplayName("유효한 정보로 스탬프의 이름을 수정하면 스탬프가 업데이트된다")
        void shouldUpdateStampName() {

            // given
            UpdateStampRequest request = fixture.buildUpdateName();

            // when
            stampCommandService.updateStamp(courseTrip, courseStamp1, request);

            // then
            assertThat(courseStamp1.getName()).isEqualTo(request.name());
        }

        @Test
        @DisplayName("유효한 정보로 스탬프의 종료일을 수정하면 스탬프가 업데이트된다")
        void shouldUpdateStampEndDate() {
            // given
            UpdateStampRequest request = fixture.buildUpdateEndDate();

            // when
            stampCommandService.updateStamp(courseTrip, courseStamp1, request);

            // then
            assertThat(courseStamp1.getEndDate()).isEqualTo(request.endDate());
        }

        @Test
        @DisplayName("과거 날짜로 스탬프의 종료일을 수정하면 예외가 발생한다")
        void shouldThrowExceptionWhenEndDateIsInPast() {
            // given
            UpdateStampRequest request = fixture.withEndDateInPast().buildUpdateEndDate();

            // when & then
            assertThatThrownBy(
                            () ->
                                    stampCommandService.updateStamp(
                                            courseTrip, courseStamp1, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_END_DATE_CANNOT_BE_IN_PAST.getMessage());
        }

        @Test
        @DisplayName("여행 종료일보다 이후 날짜로 스탬프의 종료일을 수정하면 예외가 발생한다")
        void shouldThrowExceptionWhenEndDateIsAfterTripEndDate() {
            // given
            UpdateStampRequest request = fixture.withEndDateAfterTripEndDate().buildUpdateEndDate();

            // when & then
            assertThatThrownBy(
                            () ->
                                    stampCommandService.updateStamp(
                                            courseTrip, courseStamp1, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(
                            StampErrorCode.STAMP_END_DATE_AFTER_TRIP_END_DATE_NOT_ALLOWED
                                    .getMessage());
        }
    }

    @Nested
    @DisplayName("updateStampOrders 메서드는")
    class UpdateStampOrders {
        private final UpdateStampOrderRequestFixture fixture = new UpdateStampOrderRequestFixture();

        @Test
        @DisplayName("탐험형 여행의 스탬프 순서를 수정하면 예외가 발생한다")
        void shouldThrowExceptionWhenTripIsExplorationType() {
            // given
            UpdateStampOrderRequest request = fixture.buildUpdateOrders();

            // when & then
            assertThatThrownBy(() -> stampCommandService.updateStampOrders(exploreTrip, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(
                            StampErrorCode.CANNOT_UPDATE_ORDER_FOR_EXPLORATION_TRIP.getMessage());
        }

        @Test
        @DisplayName("유효하지 않는 스탬프 ID일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampNotFoundById() {
            // given
            UpdateStampOrderRequest request =
                    fixture.withOrderedStampIds(List.of(1000L, 1001L)).buildUpdateOrders();

            // when & then
            assertThatThrownBy(() -> stampCommandService.updateStampOrders(courseTrip, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.INVALID_STAMP_ID_IN_REQUEST.getMessage());
        }

        @Test
        @DisplayName("여행에 속한 스탬프가 아닐 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampDoesNotBelongToTrip() {
            // given
            UpdateStampOrderRequest request = fixture.buildUpdateOrders();

            Trip newTrip = TripFixture.createTripWithId(3L, member, TripCategory.COURSE);
            given(stampRepository.findAllByIdIn(request.orderedStampIds()))
                    .willReturn(List.of(courseStamp1, courseStamp2));

            // when & then
            assertThatThrownBy(() -> stampCommandService.updateStampOrders(newTrip, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.getMessage());
        }

        @Test
        @DisplayName("삭제된 스탬프일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampAlreadyDeleted() {
            // given
            UpdateStampOrderRequest request = fixture.buildUpdateOrders();

            courseStamp1.updateDeletedAt();
            given(stampRepository.findAllByIdIn(request.orderedStampIds()))
                    .willReturn(List.of(courseStamp1, courseStamp2));

            // when & then
            assertThatThrownBy(() -> stampCommandService.updateStampOrders(courseTrip, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("코스형 여행에서 클라이언트가 전달한 스탬프 ID 리스트 순서에 따라 스탬프의 순서를 수정한다")
        void shouldUpdateStampOrderForCourseTrip() {
            // given
            UpdateStampOrderRequest request =
                    fixture.withOrderedStampIds(List.of(2L, 1L)).buildUpdateOrders();

            given(stampRepository.findAllByIdIn(request.orderedStampIds()))
                    .willReturn(List.of(courseStamp1, courseStamp2));

            // when
            stampCommandService.updateStampOrders(courseTrip, request);

            // then
            assertThat(courseStamp2.getStampOrder()).isEqualTo(1);
            assertThat(courseStamp1.getStampOrder()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("updateStampOrdersForUpdateTripCategory 메서드는")
    class UpdateStampOrdersForUpdateTripCategory {

        @Test
        @DisplayName("여행의 카테고리가 탐험형으로 수정되면 소속된 모든 스탬프의 순서를 0으로 수정한다")
        void shouldSetAllStampOrdersToZeroWhenCategoryChangesToExplore() {
            // given
            given(stampRepository.findAllByTripIdOrderByCreatedAtAsc(courseTrip.getId()))
                    .willReturn(List.of(courseStamp1, courseStamp2));

            // when
            stampCommandService.updateStampOrdersByTripCategoryChange(
                    courseTrip.getId(), TripCategory.EXPLORE);

            // then
            assertThat(courseStamp1.getStampOrder()).isEqualTo(0);
            assertThat(courseStamp2.getStampOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("여행의 카테고리가 코스형으로 수정되면 소속된 모든 스탬프의 순서를 생성일이 이른 순으로 1부터 순차적으로 순서를 수정한다")
        void shouldSetSequentialStampOrdersWhenCategoryChangesToCourse() {
            // given
            courseStamp1.updateStampOrder(0);
            courseStamp2.updateStampOrder(0);

            given(stampRepository.findAllByTripIdOrderByCreatedAtAsc(exploreTrip.getId()))
                    .willReturn(List.of(courseStamp1, courseStamp2));

            // when
            stampCommandService.updateStampOrdersByTripCategoryChange(
                    exploreTrip.getId(), TripCategory.COURSE);

            // then
            assertThat(courseStamp1.getStampOrder()).isEqualTo(1);
            assertThat(courseStamp2.getStampOrder()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("deleteStamp 메서드는")
    class DeleteStamp {

        @Test
        @DisplayName("코스형 여행의 스탬프 삭제 시 deletedAt 필드를 현재 시각으로 설정한다.")
        void shouldDeleteCourseTripStamp() {
            // when
            stampCommandService.deleteStamp(courseStamp1);

            // then
            assertThat(courseStamp1.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("탐험형 여행의 스탬프 삭제 시 deletedAt 필드를 현재 시각으로 설정한다.")
        void shouldDeleteExploreTripStamp() {
            // when
            stampCommandService.deleteStamp(exploreStamp1);

            // then
            assertThat(exploreStamp1.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("completeStamp 메서드는")
    class CompleteStamp {

        @Test
        @DisplayName("이미 완료된 스탬프이면 예외가 발생한다.")
        void shouldThrowExceptionWhenStampIsAlreadyCompleted() {
            // given
            exploreStamp1.updateCompleted();

            // when & then
            assertThatThrownBy(() -> stampCommandService.completeStamp(exploreStamp1))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_ALREADY_COMPLETED.getMessage());
        }

        @Test
        @DisplayName("유효한 스탬프가 들어오면, completed 필드를 true로 업데이트한다.")
        void shouldCompleteStamp() {
            // when
            stampCommandService.completeStamp(exploreStamp1);

            // then
            assertThat(exploreStamp1.isCompleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("shiftStampOrders 메서드는")
    class ShiftStampOrders {

        @Test
        @DisplayName("시프트할 스탬프가 존재하지 않으면 아무 동작도 수행하지 않는다.")
        void shouldDoNothingWhenStampsToShiftDoNotExist() {
            // when
            stampCommandService.shiftStampOrders(List.of());

            // then
            assertThat(courseStamp2.getStampOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("시프트할 스탬프가 존재하면 각 스탬프의 order를 1씩 감소시킨다.")
        void shouldDecreaseOrdersByOneWhenStampsToShiftExist() {
            // when
            stampCommandService.shiftStampOrders(List.of(courseStamp2));

            // then
            assertThat(courseStamp2.getStampOrder()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("validateAllStampsCompletedByTripId 메서드는")
    class ValidateAllStampsCompletedByTripId {

        @Test
        @DisplayName("특정 여행 하위의 스탬프가 하나라도 완료되지 않았다면 예외가 발생한다.")
        void shouldThrowExceptionWhenAnyStampIsNotCompleted() {
            // given
            Long tripId = courseTrip.getId();
            given(
                            stampCommandRepository
                                    .existsByTripIdAndCompletedIsFalseAndDeletedAtIsNull(tripId))
                    .willReturn(true);

            // when & then
            Assertions.assertThatThrownBy(
                            () -> stampCommandService.validateAllStampsCompletedByTripId(tripId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.ALL_STAMPS_NOT_COMPLETED.getMessage());
        }

        @Test
        @DisplayName("특정 여행 하위의 모든 스탬프가 완료되면 예외가 발생하지 않는다.")
        void shouldPassWhenAllStampsAreCompleted() {
            // given
            Long tripId = courseTrip.getId();
            given(
                            stampCommandRepository
                                    .existsByTripIdAndCompletedIsFalseAndDeletedAtIsNull(tripId))
                    .willReturn(false);

            // when & then
            assertDoesNotThrow(
                    () -> stampCommandService.validateAllStampsCompletedByTripId(tripId));
        }
    }

    @Nested
    @DisplayName("hardDeleteStamps 메서드는")
    class HardDeleteStamps {

        @Test
        @DisplayName("삭제된 스탬프가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDeletedStampsDoNotExist() {
            // given
            given(stampCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L);

            // when
            long result = stampCommandService.hardDeleteStamps();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 스탬프가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDeletedStampsExist() {
            // given
            given(stampCommandRepository.deleteAllByDeletedTripOwner()).willReturn(5L);

            // when
            long result = stampCommandService.hardDeleteStampsOwnedByDeletedTrip();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteStampsOwnedByDeletedTrip 메서드는")
    class HardDeleteStampsOwnedByDeletedTrip {

        @Test
        @DisplayName("삭제된 여행이 소유한 스탬프가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenStampsOwnedByDeletedTripDoNotExist() {
            // given
            given(stampCommandRepository.deleteAllByDeletedTripOwner()).willReturn(0L);

            // when
            long result = stampCommandService.hardDeleteStampsOwnedByDeletedTrip();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 여행이 소유한 스탬프가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenStampsOwnedByDeletedTripExist() {
            // given
            given(stampCommandRepository.deleteAllByDeletedTripOwner()).willReturn(5L);

            // when
            long result = stampCommandService.hardDeleteStampsOwnedByDeletedTrip();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("increaseTotalMissions 메서드는")
    class IncreaseTotalMissions {

        @Test
        @DisplayName("스탬프의 총 미션 수를 1 증가시킨다")
        void shouldIncreaseTotalMissions() {
            // given
            int initialTotalMissions = courseStamp1.getTotalMissions();

            // when
            stampCommandService.increaseTotalMissions(courseStamp1);

            // then
            assertThat(courseStamp1.getTotalMissions()).isEqualTo(initialTotalMissions + 1);
        }
    }

    @Nested
    @DisplayName("decreaseTotalMissions 메서드는")
    class DecreaseTotalMissions {

        @Test
        @DisplayName("스탬프의 총 미션 수를 1 감소시킨다")
        void shouldDecreaseTotalMissions() {
            // given
            courseStamp1.increaseTotalMissions();
            courseStamp1.increaseTotalMissions();
            int initialTotalMissions = courseStamp1.getTotalMissions();

            // when
            stampCommandService.decreaseTotalMissions(courseStamp1);

            // then
            assertThat(courseStamp1.getTotalMissions()).isEqualTo(initialTotalMissions - 1);
        }
    }

    @Nested
    @DisplayName("increaseCompletedMissions 메서드는")
    class IncreaseCompletedMissions {

        @Test
        @DisplayName("스탬프의 완료된 미션 수를 지정된 개수만큼 증가시킨다")
        void shouldIncreaseCompletedMissions() {
            // given
            int initialCompletedMissions = courseStamp1.getCompletedMissions();
            int increaseCount = 3;

            // when
            stampCommandService.increaseCompletedMissions(courseStamp1, increaseCount);

            // then
            assertThat(courseStamp1.getCompletedMissions())
                    .isEqualTo(initialCompletedMissions + increaseCount);
        }
    }

    @Nested
    @DisplayName("hardDeleteStampsByMember 메서드는")
    class HardDeleteStampsByMember {

        @Test
        @DisplayName("특정 멤버가 소유한 스탬프가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenStampsOwnedByMemberDoNotExist() {
            // given
            Long memberId = 1L;
            given(stampCommandRepository.deleteAllByMemberId(memberId)).willReturn(0L);

            // when
            long result = stampCommandService.hardDeleteStampsByMember(memberId);

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("특정 멤버가 소유한 스탬프가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenStampsOwnedByMemberExist() {
            // given
            Long memberId = 1L;
            given(stampCommandRepository.deleteAllByMemberId(memberId)).willReturn(5L);

            // when
            long result = stampCommandService.hardDeleteStampsByMember(memberId);

            // then
            assertThat(result).isEqualTo(5L);
        }
    }
}
