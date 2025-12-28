package com.ject.studytrip.stamp.domain.repository

import com.ject.studytrip.stamp.domain.model.Stamp
import java.util.Optional

interface StampRepository {
    fun save(stamp: Stamp): Stamp

    fun saveAll(stamps: List<Stamp>): List<Stamp>

    fun findById(stampId: Long): Optional<Stamp>

    fun findAllByIdIn(stampIds: List<Long>): List<Stamp>

    fun findAllByTripIdAndDeletedAtIsNull(tripId: Long): List<Stamp>

    fun findAllByTripIdOrderByCreatedAtAsc(tripId: Long): List<Stamp>
}
