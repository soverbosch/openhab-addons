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
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
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
        logger.debug("Handle command {} for channel {}", command, channelUID);
        try {
            if (command instanceof RefreshType) {
                LedStripStatus ledStripStatus = ledStripDriver.getLedStripStatus();
                logger.debug("Requesting refresh on Cololight, current state is {}", ledStripStatus);
                if (CHANNEL_POWER.equals(channelUID.getId())) {
                    updateState(channelUID, ledStripStatus.getPower());
                }
                if (CHANNEL_BRIGHTNESS.equals(channelUID.getId())) {
                    updateState(channelUID, ledStripStatus.getBrightness());
                }
            } else if (CHANNEL_POWER.equals(channelUID.getId())) {
                if (command instanceof OnOffType) {
                    logger.debug("Switching Cololight to {}", command);
                    if (command.equals(OnOffType.ON)) {
                        ledStripDriver.setPowerOn();
                        updateState(channelUID, OnOffType.ON);
                    } else {
                        ledStripDriver.setPowerOff();
                        updateState(channelUID, OnOffType.OFF);
                    }
                }
            } else if (CHANNEL_BRIGHTNESS.equals(channelUID.getId())) {
                logger.debug("Brightness command {} {}", command.toString(), command.getClass());
                if (command instanceof PercentType) {
                    ledStripDriver.setBrightness(((PercentType) command).intValue());
                }
            } else if (CHANNEL_EFFECT.equals(channelUID.getId())) {
                logger.debug("Effect command {} {}", command.toString(), command.getClass());
                ledStripDriver.setEffect(command.toString());
            }
        } catch (CommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN);

        ColoLightConfiguration config = getConfigAs(ColoLightConfiguration.class);
        ledStripDriver = new LedStripDriver(config.host, config.port, config.socketTimeout);

        scheduler.execute(() -> {
            boolean thingReachable = ledStripDriver.getStatusIsOk();
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        pollingJob = scheduler.scheduleWithFixedDelay(this::statusPoll, 30, 30, TimeUnit.SECONDS);
    }

    public void statusPoll() {
        logger.debug("Polling for Cololight status");
        if (ledStripDriver == null) {
            updateStatus(ThingStatus.UNKNOWN);
        } else if (ledStripDriver.getStatusIsOk()) {
            updateStatus(ThingStatus.ONLINE);
            try {
                final LedStripStatus ledStripStatus = ledStripDriver.getLedStripStatus();
                logger.debug(ledStripStatus.toString());
                getThing().getChannels().forEach(channel -> {
                    if (CHANNEL_POWER.equals(channel.getUID().getId())) {
                        updateState(channel.getUID(), ledStripStatus.getPower());
                    } else if (CHANNEL_BRIGHTNESS.equals(channel.getUID().getId())) {
                        // Not interested in updating brightness when LED is off.
                        if (OnOffType.ON.equals(ledStripStatus.getPower())) {
                            updateState(channel.getUID(), ledStripStatus.getBrightness());
                        }
                    }
                });
            } catch (CommunicationException communicationException) {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Stop polling for Cololight status");
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
    }
}
