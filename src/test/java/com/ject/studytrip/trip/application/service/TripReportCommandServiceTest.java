package com.ject.studytrip.trip.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.trip.domain.model.TripReport;
import com.ject.studytrip.trip.domain.repository.TripReportCommandRepository;
import com.ject.studytrip.trip.domain.repository.TripReportRepository;
import com.ject.studytrip.trip.fixture.CreateTripReportRequestFixture;
import com.ject.studytrip.trip.fixture.TripReportFixture;
import com.ject.studytrip.trip.presentation.dto.request.CreateTripReportRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("TripReportCommandService 단위 테스트")
class TripReportCommandServiceTest extends BaseUnitTest {
    @InjectMocks private TripReportCommandService tripReportCommandService;
    @Mock private TripReportRepository tripReportRepository;
    @Mock private TripReportCommandRepository tripReportCommandRepository;

    private Member member;
    private TripReport tripReport;

    @BeforeEach
    void setup() {
        member = MemberFixture.createMemberFromKakaoWithId(1L);
        tripReport = TripReportFixture.createTripReport(member);
    }

    @Nested
    @DisplayName("createTripReport 메서드는")
    class CreateTripReport {

        @Test
        @DisplayName("유효한 요청이 들어오면 여행 리포트를 생성하고 반환한다.")
        void shouldReturnTripReportWhenRequestIsValid() {
            // given
            CreateTripReportRequest request = new CreateTripReportRequestFixture().build();
            given(tripReportRepository.save(any(TripReport.class))).willReturn(tripReport);

            // when
            TripReport result = tripReportCommandService.createTripReport(member, request);

            // then
            assertThat(result).isEqualTo(tripReport);
        }
    }

    @Nested
    @DisplayName("updateImageUrl 메서드는")
    class UpdateImageUrl {
        private static final String NEW_IMAGE_URL =
                "https://cdn.example.com/trip-reports/1/image.jpg";

        @Test
        @DisplayName("유효한 여행 리포트의 이미지 URL을 수정한다.")
        void shouldUpdateImageUrlWhenTripReportIsValid() {
            // given
            String oldImageUrl = tripReport.getImageUrl();

            // when
            tripReportCommandService.updateImageUrl(tripReport, NEW_IMAGE_URL);

            // then
            assertThat(tripReport.getImageUrl()).isEqualTo(NEW_IMAGE_URL);
            assertThat(tripReport.getImageUrl()).isNotEqualTo(oldImageUrl);
        }
    }

    @Nested
    @DisplayName("deleteTripReport 메서드는")
    class DeleteTripReport {

        @Test
        @DisplayName("특정 여행 리포트의 deletedAt 필드를 현재 시간으로 업데이트한다")
        void shouldDeleteTripForUpdateDeletedAt() {
            // when
            tripReportCommandService.deleteTripReport(tripReport);

            // then
            assertThat(tripReport.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("hardDeleteTripReports 메서드는")
    class HardDeleteTripReports {

        @Test
        @DisplayName("삭제된 여행 리포트가 하나라도 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDeletedTripReportDoesNotExist() {
            // given
            given(tripReportCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L);

            // when
            long result = tripReportCommandService.hardDeleteTripReports();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 여행 리포트가 하나라도 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDeletedTripReportExist() {
            // given
            given(tripReportCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L);

            // when
            long result = tripReportCommandService.hardDeleteTripReports();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteTripReportsOwnedByDeletedMember 메서드는")
    class HardDeleteTripReportsOwnedByDeletedMember {

        @Test
        @DisplayName("삭제된 멤버가 소유한 여행 리포트가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenTripReportsOwnedByDeletedMemberDoNotExist() {
            // given
            given(tripReportCommandRepository.deleteAllByDeletedMemberOwner()).willReturn(0L);

            // when
            long result = tripReportCommandService.hardDeleteTripReportsOwnedByDeletedMember();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 멤버가 소유한 여행 리포트가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenTripReportsOwnedByDeletedMemberExist() {
            // given
            given(tripReportCommandRepository.deleteAllByDeletedMemberOwner()).willReturn(5L);

            // when
            long result = tripReportCommandService.hardDeleteTripReportsOwnedByDeletedMember();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteTripReportsByMember 메서드는")
    class HardDeleteTripReportsByMember {

        @Test
        @DisplayName("특정 멤버가 소유한 여행 리포트가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenTripReportsOwnedByMemberDoNotExist() {
            // given
            Long memberId = 1L;
            given(tripReportCommandRepository.deleteAllByMemberId(memberId)).willReturn(0L);

            // when
            long result = tripReportCommandService.hardDeleteTripReportsByMember(memberId);

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("특정 멤버가 소유한 여행 리포트가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenTripReportsOwnedByMemberExist() {
            // given
            Long memberId = 1L;
            given(tripReportCommandRepository.deleteAllByMemberId(memberId)).willReturn(5L);

            // when
            long result = tripReportCommandService.hardDeleteTripReportsByMember(memberId);

            // then
            assertThat(result).isEqualTo(5L);
        }
    }
}
