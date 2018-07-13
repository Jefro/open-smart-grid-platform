/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.infra.messaging;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.support.JmsUtils;

import com.alliander.osgp.adapter.protocol.iec61850.device.DeviceResponse;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.responses.EmptyDeviceResponse;
import com.alliander.osgp.adapter.protocol.iec61850.domain.valueobjects.DomainInformation;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.RequestMessageData;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.Iec61850DeviceResponseHandler;
import com.alliander.osgp.adapter.protocol.iec61850.services.DeviceResponseService;
import com.alliander.osgp.shared.exceptionhandling.OsgpException;
import com.alliander.osgp.shared.infra.jms.DeviceMessageMetadata;
import com.alliander.osgp.shared.infra.jms.MessageProcessor;
import com.alliander.osgp.shared.infra.jms.MessageProcessorMap;
import com.alliander.osgp.shared.infra.jms.ProtocolResponseMessage;
import com.alliander.osgp.shared.infra.jms.ResponseMessageResultType;
import com.alliander.osgp.shared.infra.jms.ResponseMessageSender;

public abstract class BaseMessageProcessor implements MessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMessageProcessor.class);

    @Autowired
    protected int maxRedeliveriesForIec61850Requests;

    @Autowired
    protected DeviceResponseMessageSender responseMessageSender;

    @Autowired
    protected DeviceResponseService deviceResponseService;

    @Autowired
    @Qualifier("iec61850DeviceRequestMessageProcessorMap")
    protected MessageProcessorMap iec61850RequestMessageProcessorMap;

    protected DeviceRequestMessageType deviceRequestMessageType;

    protected void printDomainInfo(final RequestMessageData requestMessageData) {
        LOGGER.info("Calling DeviceService function: {} for domain: {} {}", requestMessageData.getMessageType(),
                requestMessageData.getDomain(), requestMessageData.getDomainVersion());
    }

    /**
     * Get the delivery count for a {@link Message} using 'JMSXDeliveryCount'
     * property.
     */
    public Integer getJmsXdeliveryCount(final Message message) {
        try {
            final int jmsXdeliveryCount = message.getIntProperty("JMSXDeliveryCount");
            LOGGER.info("jmsXdeliveryCount: {}", jmsXdeliveryCount);
            return jmsXdeliveryCount;
        } catch (final JMSException e) {
            LOGGER.error("JMSException while reading JMSXDeliveryCount", e);
            return null;
        }
    }

    /**
     * Use 'jmsxDeliveryCount' to determine if a request should be retried using
     * the re-delivery options. In case a JMSException is thrown, the request
     * will be rolled-back to the message-broker and will be re-delivered
     * according to the re-delivery policy set. If the maximum number of
     * re-deliveries have been executed, a protocol response message will be
     * sent to osgp-core.
     */
    public void checkForRedelivery(final DeviceMessageMetadata deviceMessageMetadata, final OsgpException e,
            final DomainInformation domainInformation, final int jmsxDeliveryCount) throws JMSException {
        final int jmsxRedeliveryCount = jmsxDeliveryCount - 1;
        LOGGER.info("jmsxDeliveryCount: {}, jmsxRedeliveryCount: {}, maxRedeliveriesForIec61850Requests: {}",
                jmsxDeliveryCount, jmsxRedeliveryCount, this.maxRedeliveriesForIec61850Requests);
        if (jmsxRedeliveryCount < this.maxRedeliveriesForIec61850Requests) {
            LOGGER.info(
                    "Redelivering message with messageType: {}, correlationUid: {}, for device: {} - jmsxRedeliveryCount: {} is less than maxRedeliveriesForIec61850Requests: {}",
                    deviceMessageMetadata.getMessageType(), deviceMessageMetadata.getCorrelationUid(),
                    deviceMessageMetadata.getDeviceIdentification(), jmsxRedeliveryCount,
                    this.maxRedeliveriesForIec61850Requests);
            final JMSException jmsException = new JMSException(
                    e == null ? "checkForRedelivery() unknown error: OsgpException e is null" : e.getMessage());
            throw JmsUtils.convertJmsAccessException(jmsException);
        } else {
            LOGGER.warn(
                    "All redelivery attempts failed for message with messageType: {}, correlationUid: {}, for device: {}",
                    deviceMessageMetadata.getMessageType(), deviceMessageMetadata.getCorrelationUid(),
                    deviceMessageMetadata.getDeviceIdentification());
            final DeviceResponse deviceResponse = new DeviceResponse(
                    deviceMessageMetadata.getOrganisationIdentification(),
                    deviceMessageMetadata.getDeviceIdentification(), deviceMessageMetadata.getCorrelationUid(),
                    deviceMessageMetadata.getMessagePriority());
            this.handleExpectedError(deviceResponse, e, domainInformation, deviceMessageMetadata.getMessageType());
        }
    }

    /**
     * Handles {@link EmptyDeviceResponse} by default. MessageProcessor
     * implementations can override this function to handle responses containing
     * data.
     */
    public void handleDeviceResponse(final DeviceResponse deviceResponse,
            final ResponseMessageSender responseMessageSender, final DomainInformation domainInformation,
            final String messageType, final int retryCount) {

        ResponseMessageResultType result = ResponseMessageResultType.OK;
        OsgpException ex = null;

        try {
            final EmptyDeviceResponse response = (EmptyDeviceResponse) deviceResponse;
            this.deviceResponseService.handleDeviceMessageStatus(response.getStatus());
        } catch (final OsgpException e) {
            LOGGER.error("Device Response Exception", e);
            result = ResponseMessageResultType.NOT_OK;
            ex = e;
        }

        final DeviceMessageMetadata deviceMessageMetadata = new DeviceMessageMetadata(
                deviceResponse.getDeviceIdentification(), deviceResponse.getOrganisationIdentification(),
                deviceResponse.getCorrelationUid(), messageType, deviceResponse.getMessagePriority());
        final ProtocolResponseMessage protocolResponseMessage = new ProtocolResponseMessage.Builder()
                .domain(domainInformation.getDomain()).domainVersion(domainInformation.getDomainVersion())
                .deviceMessageMetadata(deviceMessageMetadata).result(result).osgpException(ex).retryCount(retryCount)
                .build();
        responseMessageSender.send(protocolResponseMessage);
    }

    protected void handleExpectedError(final DeviceResponse deviceResponse, final OsgpException e,
            final DomainInformation domainInformation, final String messageType) {
        LOGGER.error("Expected error while processing message", e);

        final int retryCount = Integer.MAX_VALUE;

        final DeviceMessageMetadata deviceMessageMetadata = new DeviceMessageMetadata(
                deviceResponse.getDeviceIdentification(), deviceResponse.getOrganisationIdentification(),
                deviceResponse.getCorrelationUid(), messageType, deviceResponse.getMessagePriority());
        final ProtocolResponseMessage protocolResponseMessage = new ProtocolResponseMessage.Builder()
                .domain(domainInformation.getDomain()).domainVersion(domainInformation.getDomainVersion())
                .deviceMessageMetadata(deviceMessageMetadata).result(ResponseMessageResultType.NOT_OK).osgpException(e)
                .retryCount(retryCount).build();
        this.responseMessageSender.send(protocolResponseMessage);
    }

    protected Iec61850DeviceResponseHandler createIec61850DeviceResponseHandler(
            final RequestMessageData requestMessageData, final Message message) {
        final Integer jsmxDeliveryCount = this.getJmsXdeliveryCount(message);
        return new Iec61850DeviceResponseHandler(this, jsmxDeliveryCount, requestMessageData,
                this.responseMessageSender);
    }
}
