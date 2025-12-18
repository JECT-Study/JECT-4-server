package com.ject.studytrip.mission.infra.jpa

import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.domain.repository.MissionRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class MissionRepositoryAdapter(
    private val missionJpaRepository: MissionJpaRepository,
) : MissionRepository {
    override fun findAllByIdIn(missionIds: List<Long>): List<Mission> = missionJpaRepository.findAllByIdIn(missionIds)

    override fun findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(stampId: Long): List<Mission> =
        missionJpaRepository.findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(stampId)

    override fun findById(missionId: Long): Optional<Mission> = missionJpaRepository.findById(missionId)

    override fun save(mission: Mission): Mission = missionJpaRepository.save(mission)
}
