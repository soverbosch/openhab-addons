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
package org.openhab.binding.cololight.internal.handler;

import static org.openhab.binding.cololight.internal.ColoLightBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.cololight.internal.LedStripDriver;
import org.openhab.binding.cololight.internal.configuration.ColoLightConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ColoLightHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sarris Overbosch - Initial contribution
 */
@NonNullByDefault
public class ColoLightHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ColoLightHandler.class);

    private @Nullable ColoLightConfiguration config;

    private @Nullable LedStripDriver ledStripDriver;

    public ColoLightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_POWER.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {
                    ledStripDriver.setPowerOn();
                } else {
                    ledStripDriver.setPowerOff();
                }
            }

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(ColoLightConfiguration.class);

        updateStatus(ThingStatus.ONLINE);

        ledStripDriver = new LedStripDriver(config.host, config.port);

        // Example for background initialization:
        // scheduler.execute(() -> {
        // boolean thingReachable = true; // <background task with long running initialization here>
        // // when done do:
        // if (thingReachable) {
        // updateStatus(ThingStatus.ONLINE);
        // } else {
        // updateStatus(ThingStatus.OFFLINE);
        // }
        // });

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
