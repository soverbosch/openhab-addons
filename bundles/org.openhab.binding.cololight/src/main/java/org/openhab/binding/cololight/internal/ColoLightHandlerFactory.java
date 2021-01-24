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

import static org.openhab.binding.cololight.internal.ColoLightBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.cololight.internal.ColoLightBindingConstants.THING_TYPE_COLOLIGHT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.cololight.internal.handler.ColoLightHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ColoLightHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Sarris Overbosch - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.cololight", service = ThingHandlerFactory.class)
public class ColoLightHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(ColoLightHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.trace("createHandler for {}", thingTypeUID.getAsString());

        if (THING_TYPE_COLOLIGHT.equals(thingTypeUID)) {
            return new ColoLightHandler(thing);
        }

        return null;
    }
}
