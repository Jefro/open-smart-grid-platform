/**
 * Copyright 2019 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.simulator.protocol.iec60870.domain;

import org.openmuc.j60870.ASdu;

public class SimpleShortFloatingPointMeasurementAsduGenerator implements Iec60870ASduGenerator {

    @Override
    public ASdu getNextAsdu() {
        final Iec60870ASduFactory factory = new Iec60870ASduFactory();

        return factory.createShortFloatingPointMeasurementAsdu();
    }

}