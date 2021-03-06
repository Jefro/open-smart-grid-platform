/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.dto.valueobjects;

import java.io.Serializable;

public enum LightTypeDto implements Serializable {
    RELAY,
    ONE_TO_TEN_VOLT,
    ONE_TO_TEN_VOLT_REVERSE,
    ONE_TO_TWENTY_FOUR_VOLT,
    DALI
}
