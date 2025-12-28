package com.ject.studytrip.stamp.infra.jpa

import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.domain.repository.StampRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class StampRepositoryAdapter(
    private val stampJpaRepository: StampJpaRepository,
) : StampRepository {
    override fun save(stamp: Stamp): Stamp = stampJpaRepository.save(stamp)

    override fun saveAll(stamps: List<Stamp>): List<Stamp> = stampJpaRepository.saveAll(stamps)

    override fun findById(stampId: Long): Optional<Stamp> = stampJpaRepository.findById(stampId)

    override fun findAllByIdIn(stampIds: List<Long>): List<Stamp> = stampJpaRepository.findAllByIdIn(stampIds)

    override fun findAllByTripIdAndDeletedAtIsNull(tripId: Long): List<Stamp> = stampJpaRepository.findAllByTripIdAndDeletedAtIsNull(tripId)

    override fun findAllByTripIdOrderByCreatedAtAsc(tripId: Long): List<Stamp> =
        stampJpaRepository.findAllByTripIdOrderByCreatedAtAsc(tripId)
}
