package com.ject.studytrip.trip.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.trip.domain.error.TripReportErrorCode;
import com.ject.studytrip.trip.domain.model.TripReport;
import com.ject.studytrip.trip.domain.repository.TripReportQueryRepository;
import com.ject.studytrip.trip.domain.repository.TripReportRepository;
import com.ject.studytrip.trip.fixture.TripReportFixture;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("TripReportQueryService 단위 테스트")
class TripReportQueryServiceTest extends BaseUnitTest {
    @InjectMocks private TripReportQueryService tripReportQueryService;
    @Mock private TripReportRepository tripReportRepository;
    @Mock private TripReportQueryRepository tripReportQueryRepository;

    private Member member;
    private TripReport tripReport1;
    private TripReport tripReport2;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L);
        tripReport1 = TripReportFixture.createTripReportWithId(1L, member);
        tripReport2 = TripReportFixture.createTripReportWithId(2L, member);
    }

    @Nested
    @DisplayName("getTripReport 메서드는")
    class GetTripReport {

        @Test
        @DisplayName("존재하지 않는 여행 리포트로 조회하면 예외가 발생한다.")
        void shouldThrowExceptionWhenTripReportDoNotExist() {
            // given
            Long invalidId = -1L;
            given(tripReportRepository.findById(invalidId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () ->
                                    tripReportQueryService.getValidTripReport(
                                            member.getId(), invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TripReportErrorCode.TRIP_REPORT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("여행 리포트가 이미 삭제되었다면 예외가 발생한다.")
        void shouldThrowExceptionWhenTripReportAlreadyDeleted() {
            // given
            Long tripReportId = tripReport1.getId();
            tripReport1.updateDeletedAt();
            given(tripReportRepository.findById(tripReportId)).willReturn(Optional.of(tripReport1));

            // when & then
            assertThatThrownBy(
                            () ->
                                    tripReportQueryService.getValidTripReport(
                                            member.getId(), tripReportId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TripReportErrorCode.TRIP_REPORT_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("여행 리포트가 존재하면 여행 리포트를 반환한다.")
        void shouldReturnValidTripReportWhenTripReportExist() {
            // given
            Long tripReportId = tripReport1.getId();
            given(tripReportRepository.findById(tripReportId)).willReturn(Optional.of(tripReport1));

            // when
            TripReport result =
                    tripReportQueryService.getValidTripReport(member.getId(), tripReportId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(tripReportId);
            assertThat(result.getMember().getId()).isEqualTo(member.getId());
        }
    }

    @Nested
    @DisplayName("getValidTripReport 메서드는")
    class GetValidTripReport {

        @Test
        @DisplayName("존재하지 않는 여행 리포트로 조회하면 예외가 발생한다.")
        void shouldThrowExceptionWhenTripReportDoNotExist() {
            // given
            Long invalidId = -1L;
            given(tripReportRepository.findById(invalidId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () ->
                                    tripReportQueryService.getValidTripReport(
                                            member.getId(), invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TripReportErrorCode.TRIP_REPORT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("여행 리포트의 소유자가 아니라면 예외가 발생한다.")
        void shouldThrowExceptionWhenNotTripReportOwner() {
            // given
            Member newMember = MemberFixture.createMemberFromKakaoWithId(2L);
            Long tripReportId = tripReport1.getId();
            given(tripReportRepository.findById(tripReportId)).willReturn(Optional.of(tripReport1));

            // When & Then
            AssertionsForClassTypes.assertThatThrownBy(
                            () ->
                                    tripReportQueryService.getValidTripReport(
                                            newMember.getId(), tripReportId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(TripReportErrorCode.NOT_TRIP_REPORT_OWNER.getMessage());
        }

        @Test
        @DisplayName("여행 리포트가 존재하면 여행 리포트를 반환한다.")
        void shouldReturnValidTripReportWhenTripReportExist() {
            // given
            Long tripReportId = tripReport1.getId();
            given(tripReportRepository.findById(tripReportId)).willReturn(Optional.of(tripReport1));

            // when
            TripReport result =
                    tripReportQueryService.getValidTripReport(member.getId(), tripReportId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(tripReportId);
            assertThat(result.getMember().getId()).isEqualTo(member.getId());
        }
    }

    @Nested
    @DisplayName("getTripReportsByMemberId 메서드는")
    class GetTripReportsByMemberId {

        @Test
        @DisplayName("여행 리포트가 존재하지 않으면 빈 리스트를 반환한다.")
        void shouldReturnEmptyListWhenTripReportDoNotExist() {
            // given
            Long memberId = member.getId();
            given(
                            tripReportRepository
                                    .findAllByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                                            memberId))
                    .willReturn(List.of());

            // when
            List<TripReport> result = tripReportQueryService.getTripReportsByMemberId(memberId);

            // then
            assertThat(result.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("여행 리포트가 하나라도 존재하면 특정 멤버가 생성한 여행 리포트 리스트를 반환한다.")
        void shouldReturnTripReportsWhenTripReportExists() {
            // given
            Long memberId = member.getId();
            given(
                            tripReportRepository
                                    .findAllByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                                            memberId))
                    .willReturn(List.of(tripReport1, tripReport2));

            // when
            List<TripReport> result = tripReportQueryService.getTripReportsByMemberId(memberId);

            // then
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.get(0).getId()).isEqualTo(tripReport1.getId());
            assertThat(result.get(1).getId()).isEqualTo(tripReport2.getId());
        }
    }

    @Nested
    @DisplayName("getTripReportImageUrlsByMemberId 메서드는")
    class GetTripReportImageUrlsByMemberId {

        @Test
        @DisplayName("이미지가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoImages() {
            // given
            Long memberId = member.getId();
            given(tripReportQueryRepository.findImageUrlsByMemberId(memberId))
                    .willReturn(List.of());

            // when
            List<String> result = tripReportQueryService.getTripReportImageUrlsByMemberId(memberId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("이미지가 존재하면 URL 리스트를 반환한다")
        void shouldReturnImageUrlsWhenExist() {
            // given
            Long memberId = member.getId();
            List<String> imageUrls =
                    List.of(
                            "https://cdn.example.com/reports/1.jpg",
                            "https://cdn.example.com/reports/2.jpg");
            given(tripReportQueryRepository.findImageUrlsByMemberId(memberId))
                    .willReturn(imageUrls);

            // when
            List<String> result = tripReportQueryService.getTripReportImageUrlsByMemberId(memberId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(imageUrls);
        }
    }
}
