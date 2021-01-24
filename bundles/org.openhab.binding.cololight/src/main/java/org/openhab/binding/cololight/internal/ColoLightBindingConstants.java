/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.cololight.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ColoLightBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sarris Overbosch - Initial contribution
 */
@NonNullByDefault
public class ColoLightBindingConstants {

    public static final String BINDING_ID = "cololight";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_COLOLIGHT = new ThingTypeUID(BINDING_ID, "cololight");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_COLOLIGHT);

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
}
