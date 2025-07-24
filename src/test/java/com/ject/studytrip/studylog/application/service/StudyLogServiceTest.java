package com.ject.studytrip.studylog.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.studylog.domain.repository.StudyLogQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class StudyLogServiceTest extends BaseUnitTest {

    @InjectMocks private StudyLogService studyLogService;
    @Mock private StudyLogQueryRepository studyLogQueryRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L);
    }

    @Nested
    @DisplayName("getActiveStudyLogCountByMemberId 메서드는")
    class GetActiveStudyLogCountByMemberId {

        @Test
        @DisplayName("해당 멤버의 학습 기록이 존재하지 않으면 0을 반환한다.")
        void shouldReturnZeroWhenStudyLogDoesNotExistForMember() {
            // given
            given(studyLogQueryRepository.countActiveStudyLogsByMemberId(member.getId()))
                    .willReturn(0L);

            // when
            long result = studyLogService.getActiveStudyLogCountByMemberId(member.getId());

            // then
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("해당 멤버의 학습 기록이 존재하면 그 개수를 반환한다.")
        void shouldReturnCountWhenStudyLogExistsForMember() {
            // given
            given(studyLogQueryRepository.countActiveStudyLogsByMemberId(member.getId()))
                    .willReturn(3L);

            // when
            long result = studyLogService.getActiveStudyLogCountByMemberId(member.getId());

            // then
            assertThat(result).isEqualTo(3L);
        }
    }
}
