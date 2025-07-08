package com.ject.studytrip.trip.infra.jpa;

import com.ject.studytrip.trip.domain.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripJpaRepository extends JpaRepository<Trip, Long> {}
