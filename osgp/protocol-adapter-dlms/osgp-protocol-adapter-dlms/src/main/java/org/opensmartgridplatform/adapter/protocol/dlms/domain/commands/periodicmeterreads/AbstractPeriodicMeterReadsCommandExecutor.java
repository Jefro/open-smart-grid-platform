/**
 * Copyright 2016 Smart Society Services B.V.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.periodicmeterreads;

import org.joda.time.DateTime;
import org.openmuc.jdlms.datatypes.DataObject;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.AbstractCommandExecutor;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.AttributeAddressForProfile;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.DlmsObjectConfigService;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.DlmsObjectType;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.model.DlmsObject;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.model.DlmsProfile;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.model.Medium;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.model.ProfileCaptureTime;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.AmrProfileStatusCodeHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.DlmsHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.Protocol;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.BufferedDateTimeValidationException;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.*;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;

public abstract class AbstractPeriodicMeterReadsCommandExecutor<T, R> extends AbstractCommandExecutor<T, R> {

    private final AmrProfileStatusCodeHelper amrProfileStatusCodeHelper;

    AbstractPeriodicMeterReadsCommandExecutor(final Class<? extends PeriodicMeterReadsRequestDataDto> clazz,
                                              final AmrProfileStatusCodeHelper amrProfileStatusCodeHelper) {
        super(clazz);
        this.amrProfileStatusCodeHelper = amrProfileStatusCodeHelper;
    }

    /**
     * Calculates/derives the date of the read buffered DataObject.
     *
     * @param periodicMeterReadsQuery    the request query
     * @param bufferedObjects            the
     * @param attributeAddressForProfile the attribute address
     * @param previousLogTime            the log time of the previous meter read
     * @param intervalTime               the interval time for this device
     * @param dlmsHelper                 dlms helper object
     * @return the date of the buffered {@link DataObject} or null if it cannot be determined
     * @throws ProtocolAdapterException
     * @throws BufferedDateTimeValidationException
     */
    Date readClock(final PeriodicMeterReadsRequestDto periodicMeterReadsQuery,
                   final List<DataObject> bufferedObjects,
                   final AttributeAddressForProfile attributeAddressForProfile,
                   final Date previousLogTime,
                   final ProfileCaptureTime intervalTime,
                   final DlmsHelper dlmsHelper)
            throws ProtocolAdapterException, BufferedDateTimeValidationException {

        final PeriodTypeDto queryPeriodType = periodicMeterReadsQuery.getPeriodType();
        final DateTime from = new DateTime(periodicMeterReadsQuery.getBeginDate());
        final DateTime to = new DateTime(periodicMeterReadsQuery.getEndDate());

        final Integer clockIndex = attributeAddressForProfile.getIndex(DlmsObjectType.CLOCK, null);

        CosemDateTimeDto cosemDateTime = null;

        if (clockIndex != null) {
            cosemDateTime = dlmsHelper.readDateTime(bufferedObjects.get(clockIndex),
                    "Clock from " + queryPeriodType + " buffer gas");
        }

        final DateTime bufferedDateTime = cosemDateTime == null ? null : cosemDateTime.asDateTime();

        if (bufferedDateTime != null) {
            dlmsHelper.validateBufferedDateTime(bufferedDateTime, cosemDateTime, from, to);
            return bufferedDateTime.toDate();
        } else {

            // no date was returned, calculate date based on previous value
            return calculateIntervalDate(periodicMeterReadsQuery.getPeriodType(), previousLogTime, intervalTime);
        }
    }

    /**
     * Calculates/derives the next interval date in case it was not present in the current meter read record.
     *
     * @param periodTypeDto   the time interval period.
     * @param previousLogTime the logTime of the previous meter read record
     * @param intervalTime    the interval time for this device to be taken into account when the periodTypeDto is INTERVAL
     * @return the derived date based on the previous meter read record, or null if it cannot be determined
     */
    private Date calculateIntervalDate(final PeriodTypeDto periodTypeDto,
                                       final Date previousLogTime,
                                       final ProfileCaptureTime intervalTime) throws BufferedDateTimeValidationException {

        if (previousLogTime == null) {
            return null;
        }

        switch (periodTypeDto) {
            case DAILY:
                return Date.from(previousLogTime.toInstant().plus(Duration.ofDays(1)));
            case MONTHLY:
                LocalDateTime localDateTime = LocalDateTime.ofInstant(previousLogTime.toInstant(), ZoneId.systemDefault()).plusMonths(1);

                return Date.from(localDateTime.atZone(ZoneId.systemDefault())
                        .toInstant());
            case INTERVAL:

                int intervalTimeMinutes = 0;
                if (intervalTime == ProfileCaptureTime.QUARTER_HOUR) {
                    intervalTimeMinutes = 15;
                } else if (intervalTime == ProfileCaptureTime.HOUR) {
                    intervalTimeMinutes = 60;
                }

                return Date.from(previousLogTime.toInstant().plus(Duration.ofMinutes(intervalTimeMinutes)));
            default:
                throw new BufferedDateTimeValidationException("Invalid PeriodTypeDto given: " + periodTypeDto);
        }
    }

    /**
     * Get the interval time for given device and medium.
     *
     * @param device                  the device for which the interval should be determined
     * @param dlmsObjectConfigService service which holds the configuration for this device
     * @param medium                  the type of device
     * @return the derived ProfileCaptureTime for this device, or null if it cannot be determined
     */
    ProfileCaptureTime getProfileCaptureTime(DlmsDevice device, DlmsObjectConfigService dlmsObjectConfigService, Medium medium) {
        DlmsObject dlmsObject =
                dlmsObjectConfigService.findDlmsObject(Protocol.withNameAndVersion(device.getProtocol(),
                        device.getProtocolVersion()),
                        DlmsObjectType.INTERVAL_VALUES,
                        medium)
                        .orElse(null);

        if (dlmsObject instanceof DlmsProfile) {
            DlmsProfile profile = (DlmsProfile) dlmsObject;

            getLogger().info("Capture time of this device is {} ", profile.getCaptureTime());
            return profile.getCaptureTime();
        }

        return null;
    }

    AmrProfileStatusCodeDto readStatus(final List<DataObject> bufferedObjects,
                                       final AttributeAddressForProfile attributeAddressForProfile) throws ProtocolAdapterException {

        final Integer statusIndex = attributeAddressForProfile.getIndex(DlmsObjectType.AMR_STATUS, null);

        AmrProfileStatusCodeDto amrProfileStatusCode = null;

        if (statusIndex != null) {
            amrProfileStatusCode = this.readAmrProfileStatusCode(bufferedObjects.get(statusIndex));
        }

        return amrProfileStatusCode;
    }

    /**
     * Reads AmrProfileStatusCode from DataObject holding a bitvalue in a numeric
     * datatype.
     *
     * @param amrProfileStatusData AMR profile register value.
     * @return AmrProfileStatusCode object holding status enum values.
     * @throws ProtocolAdapterException on invalid register data.
     */
    private AmrProfileStatusCodeDto readAmrProfileStatusCode(final DataObject amrProfileStatusData)
            throws ProtocolAdapterException {

        if (!amrProfileStatusData.isNumber()) {
            throw new ProtocolAdapterException("Could not read AMR profile register data. Invalid data type.");
        }

        final Set<AmrProfileStatusCodeFlagDto> flags = this.amrProfileStatusCodeHelper.toAmrProfileStatusCodeFlags(
                amrProfileStatusData.getValue());
        return new AmrProfileStatusCodeDto(flags);
    }

    protected abstract Logger getLogger();

}
