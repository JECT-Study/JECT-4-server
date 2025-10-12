package com.ject.studytrip.trip.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.trip.domain.model.TripReport;
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
}
