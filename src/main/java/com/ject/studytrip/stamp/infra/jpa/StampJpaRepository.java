package com.ject.studytrip.stamp.infra.jpa;

import com.ject.studytrip.stamp.domain.model.Stamp;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StampJpaRepository extends JpaRepository<Stamp, Long> {
    List<Stamp> findAllByIdIn(List<Long> ids);

    List<Stamp> findAllByTripIdAndDeletedAtIsNull(Long tripId);

    List<Stamp> findAllByTripIdOrderByDeadlineAsc(Long tripId);
}
