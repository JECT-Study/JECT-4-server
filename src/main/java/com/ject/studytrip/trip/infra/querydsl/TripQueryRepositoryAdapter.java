package com.ject.studytrip.trip.infra.querydsl;

import com.ject.studytrip.trip.domain.model.QTrip;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.repository.TripQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TripQueryRepositoryAdapter implements TripQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QTrip trip = QTrip.trip;

    @Override
    public Slice<Trip> findSliceByMemberId(Long memberId, Pageable pageable) {
        List<Trip> content =
                queryFactory
                        .selectFrom(trip)
                        .where(trip.member.id.eq(memberId).and(trip.deletedAt.isNull()))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize() + 1)
                        .fetch();

        List<Trip> result = content;
        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            result = content.subList(0, pageable.getPageSize());
        }

        return new SliceImpl<>(result, pageable, hasNext);
    }
}
