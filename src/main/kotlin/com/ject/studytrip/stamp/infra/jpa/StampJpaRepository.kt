package com.ject.studytrip.stamp.infra.jpa

import com.ject.studytrip.stamp.domain.model.Stamp
import org.springframework.data.jpa.repository.JpaRepository

interface StampJpaRepository : JpaRepository<Stamp, Long> {
    fun findAllByIdIn(stampIds: List<Long>): List<Stamp>

    fun findAllByTripIdAndDeletedAtIsNull(tripId: Long): List<Stamp>

    fun findAllByTripIdOrderByCreatedAtAsc(tripId: Long): List<Stamp>
}
