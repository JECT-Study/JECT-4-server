package com.ject.studytrip.stamp.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.stamp.domain.error.StampErrorCode;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.repository.StampQueryRepository;
import com.ject.studytrip.stamp.domain.repository.StampRepository;
import com.ject.studytrip.stamp.fixture.CreateStampRequestFixture;
import com.ject.studytrip.stamp.fixture.StampFixture;
import com.ject.studytrip.stamp.fixture.UpdateStampRequestFixture;
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.TripFixture;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("StampService 단위 테스트")
public class StampServiceTest extends BaseUnitTest {
    private static final LocalDate PAST_DATE = LocalDate.now().minusDays(1);

    @InjectMocks private StampService stampService;
    @Mock private StampRepository stampRepository;
    @Mock private StampQueryRepository stampQueryRepository;

    private Member member;
    private Trip courseTrip;
    private Trip exploreTrip;
    private Stamp courseStamp1;
    private Stamp courseStamp2;

    @BeforeEach
    void setup() {
        member = MemberFixture.createMemberFromKakao();
        courseTrip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        exploreTrip = TripFixture.createTripWithId(2L, member, TripCategory.EXPLORE);
        courseStamp1 = StampFixture.createStampWithId(1L, courseTrip, 1);
        courseStamp2 = StampFixture.createStampWithId(2L, courseTrip, 2);
    }

    @Nested
    @DisplayName("스탬프를 생성한다")
    class CreateStamp {
        private final CreateStampRequestFixture fixture = new CreateStampRequestFixture();

        @Nested
        @DisplayName("단일 스탬프 생성")
        class CreateSingleStamp {

            @Test
            @DisplayName("유효한 요청으로 스탬프를 생성하면 스탬프가 저장되고 반환된다")
            void shouldCreateValidStamp() {
                // given
                CreateStampRequest request = fixture.build();

                Stamp saved = Stamp.of(courseTrip, request.name(), request.order());
                given(stampRepository.save(any())).willReturn(saved);

                // when
                Stamp stamp = stampService.createStamp(courseTrip, request);

                // then
                verify(stampRepository).save(any());
                assertThat(stamp.getName()).isEqualTo(saved.getName());
                assertThat(stamp.getStampOrder()).isEqualTo(saved.getStampOrder());
            }

            @Test
            @DisplayName("탐험형 여행에 순서가 지정된 스탬프를 등록하면 예외가 발생한다")
            void shouldThrowExceptionWhenOrderSpecifiedForExploreTrip() {
                // given
                CreateStampRequest request = fixture.withStampOrder(1).build();

                // when & then
                assertThatThrownBy(() -> stampService.createStamp(exploreTrip, request))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(
                                StampErrorCode.INVALID_STAMP_ORDER_FOR_EXPLORATION_TRIP
                                        .getMessage());
            }

            @Test
            @DisplayName("코스형 여행에 순서가 유효 범위를 벗어난 경우 예외가 발생한다")
            void shouldThrowExceptionWhenOrderOutOfRange() {
                // given
                CreateStampRequest request = fixture.withStampOrder(1000).build();

                // when & then
                assertThatThrownBy(() -> stampService.createStamp(courseTrip, request))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(
                                StampErrorCode.INVALID_STAMP_ORDER_RANGE_FOR_COURSE_TRIP
                                        .getMessage());
            }

            @Test
            @DisplayName("코스형 여행에 중복된 순서의 스탬프를 등록하면 예외가 발생한다")
            void shouldThrowExceptionWhenDuplicateOrderForCourseTrip() {
                // given
                CreateStampRequest request = fixture.withStampOrder(1).build();
                given(stampRepository.findAllByTripIdAndDeletedAtIsNull(courseTrip.getId()))
                        .willReturn(List.of(courseStamp1));

                // when & then
                assertThatThrownBy(() -> stampService.createStamp(courseTrip, request))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(
                                StampErrorCode.DUPLICATE_STAMP_ORDER_FOR_COURSE_TRIP.getMessage());
            }
        }

        @Nested
        @DisplayName("여러개 스탬프 생성")
        class CreateStamps {
            @Test
            @DisplayName("코스형 여행의 유효한 스탬프 리스트를 넘기면 저장된다")
            void shouldCreateStampsForCourseTrip() {
                // given
                List<CreateStampRequest> requests = List.of(fixture.build());

                // when
                stampService.createStamps(courseTrip, requests);

                // then
                verify(stampRepository).saveAll(anyList());
            }

            @Test
            @DisplayName("탐험형 여행의 유효한 스탬프 리스트를 넘기면 저장된다")
            void shouldCreateStampsForExploreTrip() {
                // given
                List<CreateStampRequest> requests = List.of(fixture.withStampOrder(0).build());

                // when
                stampService.createStamps(exploreTrip, requests);

                // then
                verify(stampRepository).saveAll(anyList());
            }

            @Test
            @DisplayName("탐험형 여행에서 순서가 1 이상이면 예외가 발생한다")
            void shouldThrowExceptionWhenOrderExistsInExploreTrip() {
                // given
                List<CreateStampRequest> requests = List.of(fixture.withStampOrder(1).build());

                // when & then
                assertThatThrownBy(() -> stampService.createStamps(exploreTrip, requests))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(
                                StampErrorCode.INVALID_STAMP_ORDER_FOR_EXPLORATION_TRIP
                                        .getMessage());
            }

            @Test
            @DisplayName("코스형 여행에서 순서가 1 미만 또는 총 개수 초과라면 예외가 발생한다")
            void shouldThrowExceptionWhenStampOrderIsOutOfRangeForCourseTrip() {
                // given
                List<CreateStampRequest> requests = List.of(fixture.withStampOrder(2).build());

                // when & then
                assertThatThrownBy(() -> stampService.createStamps(courseTrip, requests))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(
                                StampErrorCode.INVALID_STAMP_ORDER_RANGE_FOR_COURSE_TRIP
                                        .getMessage());
            }

            @Test
            @DisplayName("코스형 여행에서 순서가 중복되면 예외가 발생한다")
            void shouldThrowExceptionWhenDuplicateOrderInCourseTrip() {
                // given
                List<CreateStampRequest> requests =
                        List.of(
                                fixture.withStampOrder(1).build(),
                                fixture.withStampOrder(1).build());

                // when & then
                assertThatThrownBy(() -> stampService.createStamps(courseTrip, requests))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(
                                StampErrorCode.DUPLICATE_STAMP_ORDER_FOR_COURSE_TRIP.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("스탬프를 수정한다")
    class UpdateStamp {
        private final UpdateStampRequestFixture fixture = new UpdateStampRequestFixture();

        @Nested
        @DisplayName("스탬프 이름 수정")
        class UpdateStampName {

            @Test
            @DisplayName("유효한 정보로 스탬프의 이름을 수정하면 스탬프가 업데이트된다")
            void shouldUpdateStampNameOrDeadline() {
                // given
                UpdateStampRequest request = fixture.buildUpdateName();

                // when
                stampService.updateStampName(courseStamp1, request);

                // then
                assertThat(courseStamp1.getName()).isEqualTo(request.name());
            }
        }

        @Nested
        @DisplayName("스탬프 순서 수정")
        class UpdateStampOrders {

            @Test
            @DisplayName("코스형 여행에서 클라이언트가 전달한 스탬프 ID 리스트 순서에 따라 스탬프의 순서를 수정한다")
            void shouldUpdateStampOrderForCourseTrip() {
                // given
                UpdateStampOrderRequest request =
                        fixture.withOrderedStampIds(List.of(2L, 1L)).buildUpdateOrders();

                given(stampRepository.findAllByIdIn(request.orderedStampIds()))
                        .willReturn(List.of(courseStamp1, courseStamp2));

                // when
                stampService.updateStampOrders(courseTrip, request);

                // then
                assertThat(courseStamp2.getStampOrder()).isEqualTo(1);
                assertThat(courseStamp1.getStampOrder()).isEqualTo(2);
            }

            @Test
            @DisplayName("탐험형 여행의 스탬프 순서를 수정하면 예외가 발생한다")
            void shouldThrowExceptionWhenTripIsExplorationType() {
                // given
                UpdateStampOrderRequest request = fixture.buildUpdateOrders();

                // when & then
                assertThatThrownBy(() -> stampService.updateStampOrders(exploreTrip, request))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(
                                StampErrorCode.CANNOT_UPDATE_ORDER_FOR_EXPLORATION_TRIP
                                        .getMessage());
            }

            @Test
            @DisplayName("유효하지 않는 스탬프 ID일 경우 예외가 발생한다")
            void shouldThrowExceptionWhenStampNotFoundById() {
                // given
                UpdateStampOrderRequest request =
                        fixture.withOrderedStampIds(List.of(1000L, 1001L)).buildUpdateOrders();

                // when & then
                assertThatThrownBy(() -> stampService.updateStampOrders(courseTrip, request))
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
                assertThatThrownBy(() -> stampService.updateStampOrders(newTrip, request))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(StampErrorCode.STAMP_NOT_BELONG_TO_TRIP.getMessage());
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
                assertThatThrownBy(() -> stampService.updateStampOrders(courseTrip, request))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(StampErrorCode.STAMP_ALREADY_DELETED.getMessage());
            }
        }

        @Nested
        @DisplayName("여행 카테고리에 변경사항에 따른 스탬프 순서 수정")
        class UpdateStampOrdersForUpdateTripCategory {

            @Test
            @DisplayName("여행의 카테고리가 탐험형으로 수정되면 소속된 모든 스탬프의 순서를 0으로 수정한다")
            void shouldSetAllStampOrdersToZeroWhenCategoryChangesToExplore() {
                // given
                given(stampRepository.findAllByTripIdOrderByCreatedAtAsc(courseTrip.getId()))
                        .willReturn(List.of(courseStamp1, courseStamp2));

                // when
                stampService.updateStampOrdersByTripCategoryChange(
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
                stampService.updateStampOrdersByTripCategoryChange(
                        exploreTrip.getId(), TripCategory.COURSE);

                // then
                assertThat(courseStamp1.getStampOrder()).isEqualTo(1);
                assertThat(courseStamp2.getStampOrder()).isEqualTo(2);
            }
        }
    }

    @Nested
    @DisplayName("스탬프를 삭제한다")
    class DeleteStamp {

        @Test
        @DisplayName("코스형 여행의 스탬프 삭제 시 deletedAt 필드를 현재 시각으로 설정하고, 삭제된 스탬프 이후 순서들을 하나씩 앞당긴다")
        void shouldDeleteCourseTripStamp() {
            // given
            given(
                            stampQueryRepository.findStampsToShiftAfterOrder(
                                    courseTrip.getId(), courseStamp1.getStampOrder()))
                    .willReturn(List.of(courseStamp2));

            // when
            stampService.deleteStamp(courseTrip.getId(), courseTrip.getCategory(), courseStamp1);

            // then
            assertThat(courseStamp1.getDeletedAt()).isNotNull();
            assertThat(courseStamp2.getStampOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("탐험형 여행의 스탬프 삭제 시 deletedAt 필드를 현재 시각으로 설정한다")
        void shouldDeleteExploreTripStamp() {
            // given
            Stamp exploreStamp = StampFixture.createStamp(exploreTrip, 0);

            // when
            stampService.deleteStamp(exploreTrip.getId(), exploreTrip.getCategory(), exploreStamp);

            // then
            assertThat(exploreStamp.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("스탬프 목록을 조회한다")
    class ListStamps {

        @Test
        @DisplayName("유효한 여행 ID로 삭제된 스탬프를 제외한 스탬프 목록을 조회한다")
        void shouldGetStampsByTripId() {
            // given
            given(stampRepository.findAllByTripIdAndDeletedAtIsNull(courseTrip.getId()))
                    .willReturn(List.of(courseStamp1, courseStamp2));

            // when
            List<Stamp> stamps = stampService.getStampsByTripId(courseTrip.getId());

            // then
            verify(stampRepository).findAllByTripIdAndDeletedAtIsNull(any());

            assertThat(stamps.isEmpty()).isFalse();
            assertThat(stamps.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("스탬프를 조회한다")
    class GetStamp {

        @Test
        @DisplayName("스탬프 ID로 스탬프를 조회하고, 여행 소속 및 삭제 여부를 검증하고 반환한다")
        void shouldGetStampReturnValidStamp() {
            // given
            given(stampRepository.findById(any())).willReturn(Optional.ofNullable(courseStamp1));

            // when
            Stamp stamp = stampService.getValidStamp(courseTrip.getId(), courseStamp1.getId());

            // then
            verify(stampRepository).findById(any());

            assertThat(stamp.getId()).isEqualTo(courseStamp1.getId());
        }

        @Test
        @DisplayName("유효하지 않은 스탬프 ID일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampNotFoundById() {
            // given
            Long stampId = 1000L;

            // when & then
            assertThatThrownBy(() -> stampService.getValidStamp(courseTrip.getId(), stampId))
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
                                    stampService.getValidStamp(
                                            exploreTrip.getId(), courseStamp1.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_NOT_BELONG_TO_TRIP.getMessage());
        }

        @Test
        @DisplayName("삭제된 스탬프일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampAlreadyDeleted() {
            // given
            courseStamp1.updateDeletedAt();

            given(stampRepository.findById(any())).willReturn(Optional.ofNullable(courseStamp1));

            // when & then
            assertThatThrownBy(
                            () ->
                                    stampService.getValidStamp(
                                            courseStamp1.getId(), courseStamp1.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("코스형 여행 ID로 현재 진행중인 스탬프를 조회하고 반한환다")
        void shouldReturnFirstIncompleteStampForCourseTrip() {
            // given
            given(stampQueryRepository.findFirstIncompleteStampByTripId(any()))
                    .willReturn(Optional.ofNullable(courseStamp1));

            // when
            Stamp stamp = stampService.getFirstInCompleteStampForCourseTrip(courseTrip.getId());

            // then
            assertThat(stamp.getId()).isEqualTo(courseStamp1.getId());
            assertThat(stamp.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("코스형 여행의 현재 진행중인 스탬프가 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenNoIncompleteStampExistsForCourseTrip() {
            // given
            given(stampQueryRepository.findFirstIncompleteStampByTripId(any()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () ->
                                    stampService.getFirstInCompleteStampForCourseTrip(
                                            courseTrip.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("getStampNameByTripCategory 메서드는")
    class GetStampNameByTripCategory {

        @Test
        @DisplayName("코스형 여행일 경우 선택한 미션들이 하나의 스탬프에 속하므로 해당 스탬프의 이름을 반환한다")
        void shouldReturnStampNameForCourseTrip() {
            // given
            Stamp stamp = StampFixture.createStamp(courseTrip, 1);
            List<Stamp> stamps = List.of(stamp);

            // when
            String result = stampService.getStampNameByTripCategory(TripCategory.COURSE, stamps);

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
            String result = stampService.getStampNameByTripCategory(TripCategory.EXPLORE, stamps);

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
            String result = stampService.getStampNameByTripCategory(TripCategory.EXPLORE, stamps);

            // then
            assertThat(result).isEqualTo(stamp2.getName());
        }

        @Test
        @DisplayName("스탬프 목록이 비어있을 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampListIsEmpty() {
            // given
            List<Stamp> emptyStamps = List.of();

            // when & then
            assertThatThrownBy(
                            () ->
                                    stampService.getStampNameByTripCategory(
                                            TripCategory.COURSE, emptyStamps))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_LIST_CANNOT_BE_EMPTY.getMessage());
        }

        @Test
        @DisplayName("스탬프가 삭제된 상태일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenStampIsDeleted() {
            // given
            Stamp stamp = StampFixture.createStamp(courseTrip, 1);
            ReflectionTestUtils.setField(stamp, "deletedAt", LocalDateTime.now());

            List<Stamp> stamps = List.of(stamp);

            // when & then
            assertThatThrownBy(
                            () ->
                                    stampService.getStampNameByTripCategory(
                                            TripCategory.COURSE, stamps))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_ALREADY_DELETED.getMessage());
        }
    }

    @Nested
    @DisplayName("completeStamp 메서드는")
    class CompleteStamp {

        @Test
        @DisplayName("이미 완료된 스탬프이면 예외가 발생한다.")
        void shouldThrowExceptionWhenStampIsAlreadyCompleted() {
            // given
            ReflectionTestUtils.setField(courseStamp1, "completed", true);

            // when & then
            assertThatThrownBy(() -> stampService.completeStamp(courseStamp1))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.STAMP_ALREADY_COMPLETED.getMessage());
        }

        @Test
        @DisplayName("유효한 스탬프가 들어오면, completed 필드를 true로 업데이트하고, 여행의 완료된 스탬프 수를 1 증가시킨다.")
        void shouldCompleteStamp() {
            // when
            stampService.completeStamp(courseStamp1);

            // then
            assertThat(courseStamp1.isCompleted()).isTrue();
            assertThat(courseStamp1.getTrip().getCompletedStamps()).isEqualTo(1);
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
            given(stampQueryRepository.existsByTripIdAndCompletedIsFalseAndDeletedAtIsNull(tripId))
                    .willReturn(true);

            // when & then
            Assertions.assertThatThrownBy(
                            () -> stampService.validateAllStampsCompletedByTripId(tripId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StampErrorCode.ALL_STAMPS_NOT_COMPLETED.getMessage());
        }

        @Test
        @DisplayName("특정 여행 하위의 모든 스탬프가 완료되면 예외가 발생하지 않는다.")
        void shouldPassWhenAllStampsAreCompleted() {
            // given
            Long tripId = courseTrip.getId();
            given(stampQueryRepository.existsByTripIdAndCompletedIsFalseAndDeletedAtIsNull(tripId))
                    .willReturn(false);

            // when & then
            assertDoesNotThrow(() -> stampService.validateAllStampsCompletedByTripId(tripId));
        }
    }
}
