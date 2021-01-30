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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.cololight.internal.LedStripDriver;
import org.openhab.binding.cololight.internal.LedStripStatus;
import org.openhab.binding.cololight.internal.configuration.ColoLightConfiguration;
import org.openhab.binding.cololight.internal.exception.CommunicationException;
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

    private @Nullable LedStripDriver ledStripDriver;

    private @Nullable ScheduledFuture<?> pollingJob;

    public ColoLightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        assert ledStripDriver != null;
        try {
            if (CHANNEL_POWER.equals(channelUID.getId())) {
                if (command instanceof OnOffType) {
                    logger.debug("Switching Cololight to {}", command);
                    if (command.equals(OnOffType.ON)) {
                        ledStripDriver.setPowerOn();
                        updateState(channelUID, OnOffType.ON);
                    } else {
                        ledStripDriver.setPowerOff();
                        updateState(channelUID, OnOffType.OFF);
                    }
                } else if (command instanceof RefreshType) {
                    LedStripStatus ledStripStatus = ledStripDriver.getPowerStatus();
                    logger.debug("Requesting refresh on Cololight, current state is {}", ledStripStatus);
                    if (ledStripStatus == LedStripStatus.ON) {
                        updateState(channelUID, OnOffType.ON);
                    } else if (ledStripStatus == LedStripStatus.OFF) {
                        updateState(channelUID, OnOffType.OFF);
                    } else {
                        updateState(channelUID, UnDefType.UNDEF);
                    }
                }
            }
        } catch (CommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        ColoLightConfiguration config = getConfigAs(ColoLightConfiguration.class);

        ledStripDriver = new LedStripDriver(config.host, config.port, config.socketTimeout);

        if (ledStripDriver.getStatusIsOk()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }

        pollingJob = scheduler.scheduleWithFixedDelay(this::statusPoll, 0, 30, TimeUnit.SECONDS);

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

    public void statusPoll() {
        assert ledStripDriver != null;
        logger.debug("Polling for Cololight status");
        if (ledStripDriver.getStatusIsOk()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        assert pollingJob != null;
        logger.debug("Stop polling for Cololight status");
        pollingJob.cancel(true);
    }
}
