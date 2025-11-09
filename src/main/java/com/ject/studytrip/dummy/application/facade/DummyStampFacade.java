package com.ject.studytrip.dummy.application.facade;

import com.ject.studytrip.dummy.application.dto.DummyStampInfo;
import com.ject.studytrip.dummy.application.dto.DummyStampsInfo;
import com.ject.studytrip.dummy.application.service.DummyStampCommandService;
import com.ject.studytrip.dummy.application.service.DummyTripCommandService;
import com.ject.studytrip.member.application.service.MemberQueryService;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.domain.model.Trip;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DummyStampFacade {
    private final MemberQueryService memberQueryService;

    private final DummyTripCommandService dummyTripCommandService;
    private final DummyStampCommandService dummyStampCommandService;

    public DummyStampsInfo generateDummyStamps(Long memberId, String category, int count) {
        Member member = memberQueryService.getValidMember(memberId);

        Trip trip = dummyTripCommandService.createDummyTrip(member, category, count);
        List<Stamp> stamps =
                IntStream.rangeClosed(1, count)
                        .mapToObj(order -> dummyStampCommandService.createDummyStamp(trip, order))
                        .toList();

        return DummyStampsInfo.of(stamps.stream().map(DummyStampInfo::from).toList());
    }
}
