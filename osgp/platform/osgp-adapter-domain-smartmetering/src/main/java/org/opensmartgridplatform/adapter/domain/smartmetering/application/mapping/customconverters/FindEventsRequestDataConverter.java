/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.domain.smartmetering.application.mapping.customconverters;

import org.opensmartgridplatform.domain.core.valueobjects.smartmetering.EventLogCategory;
import org.opensmartgridplatform.domain.core.valueobjects.smartmetering.FindEventsRequestData;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.EventLogCategoryDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.FindEventsRequestDto;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

public class FindEventsRequestDataConverter
        extends BidirectionalConverter<FindEventsRequestDto, FindEventsRequestData> {

    @Override
    public FindEventsRequestData convertTo(final FindEventsRequestDto source,
            final Type<FindEventsRequestData> destinationType, final MappingContext context) {
        if (source == null) {
            return null;
        }

        final EventLogCategory eventLogCategory = EventLogCategory.valueOf(source.getEventLogCategory().toString());

        return new FindEventsRequestData(eventLogCategory, source.getFrom(), source.getUntil());
    }

    @Override
    public FindEventsRequestDto convertFrom(final FindEventsRequestData source,
            final Type<FindEventsRequestDto> destinationType, final MappingContext context) {
        if (source == null) {
            return null;
        }

        final EventLogCategoryDto eventLogCategory = EventLogCategoryDto
                .valueOf(source.getEventLogCategory().toString());

        return new FindEventsRequestDto(eventLogCategory, source.getFrom(), source.getUntil());
    }

}
