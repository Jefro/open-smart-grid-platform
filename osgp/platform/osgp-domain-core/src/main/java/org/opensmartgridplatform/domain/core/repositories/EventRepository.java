/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.domain.core.repositories;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.opensmartgridplatform.domain.core.entities.Device;
import org.opensmartgridplatform.domain.core.entities.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findByDevice(Device device);

    @Query("SELECT e.device.id as device,max(e.dateTime) as dateTime FROM Event e WHERE e.device IN (?1) GROUP BY e.device.id")
    List<Object> findLatestEventForEveryDevice(Collection<Device> devices);

    List<Event> findTop2ByDeviceOrderByDateTimeDesc(Device device);

    List<Event> findByDateTimeBefore(Date date);

    Slice<Event> findByDateTimeBefore(Date date, Pageable pageable);

}