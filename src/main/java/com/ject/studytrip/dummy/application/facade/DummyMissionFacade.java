package com.ject.studytrip.dummy.application.facade;

import com.ject.studytrip.dummy.application.dto.DummyMissionInfo;
import com.ject.studytrip.dummy.application.dto.DummyMissionsInfo;
import com.ject.studytrip.dummy.application.service.DummyMissionCommandService;
import com.ject.studytrip.dummy.application.service.DummyStampCommandService;
import com.ject.studytrip.dummy.application.service.DummyTripCommandService;
import com.ject.studytrip.member.application.service.MemberQueryService;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.domain.model.Trip;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DummyMissionFacade {
    private final MemberQueryService memberQueryService;

    private final DummyTripCommandService dummyTripCommandService;
    private final DummyStampCommandService dummyStampCommandService;
    private final DummyMissionCommandService dummyMissionCommandService;

    public DummyMissionsInfo generateDummyMissions(Long memberId, String category, int count) {
        Member member = memberQueryService.getValidMember(memberId);

        Trip trip = dummyTripCommandService.createDummyTrip(member, category, count);
        Stamp stamp = dummyStampCommandService.createDummyStamp(trip, count);
        List<Mission> missions =
                Stream.generate(() -> dummyMissionCommandService.createDummyMission(stamp))
                        .limit(count)
                        .toList();

        return DummyMissionsInfo.of(missions.stream().map(DummyMissionInfo::from).toList());
    }
}
