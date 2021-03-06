/**
 * Copyright 2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.domain.smartmetering.application.mapping.customconverters;

import org.opensmartgridplatform.domain.core.valueobjects.smartmetering.ReadAlarmRegisterData;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ReadAlarmRegisterDataDto;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

public class ReadAlarmRegisterDataConverter extends CustomConverter<ReadAlarmRegisterData, ReadAlarmRegisterDataDto> {

    @Override
    public ReadAlarmRegisterDataDto convert(final ReadAlarmRegisterData source,
            final Type<? extends ReadAlarmRegisterDataDto> destinationType, final MappingContext context) {
        return new ReadAlarmRegisterDataDto();
    }

}
