package com.ject.studytrip.member.application.facade;

import com.ject.studytrip.member.application.dto.MemberDetail;
import com.ject.studytrip.member.application.dto.MemberInfo;
import com.ject.studytrip.member.application.service.MemberService;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest;
import com.ject.studytrip.studylog.application.service.StudyLogService;
import com.ject.studytrip.trip.application.dto.TripCount;
import com.ject.studytrip.trip.application.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberFacade {
    private final MemberService memberService;
    private final TripService tripService;
    private final StudyLogService studyLogService;

    public void updateNicknameAndCategoryIfPresent(Long memberId, UpdateMemberRequest request) {
        Member member = memberService.getActiveMemberById(memberId);

        memberService.updateNicknameAndCategoryIfPresent(member, request);
    }

    public void deleteMember(Long memberId) {
        Member member = memberService.getActiveMemberById(memberId);

        memberService.deleteMember(member);
    }

    public MemberDetail getMemberDetail(Long memberId) {
        Member member = memberService.getActiveMemberById(memberId);
        TripCount tripCount = tripService.getActiveTripCountsByMemberId(memberId);
        long studyLogCount = studyLogService.getActiveStudyLogCountByMemberId(memberId);

        MemberInfo memberInfo = MemberInfo.from(member);

        return MemberDetail.from(memberInfo, tripCount, studyLogCount);
    }
}
