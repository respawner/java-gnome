/*
 * java-gnome, a UI library for writing GTK and GNOME programs from Java!
 *
 * Copyright © 2013 Operational Dynamics Consulting, Pty Ltd and Others
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted through http://java-gnome.sourceforge.net/.
 *
 * Linking this library statically or dynamically with other modules is making
 * a combined work based on this library. Thus, the terms and conditions of
 * the GPL cover the whole combination. As a special exception (the
 * "Classpath Exception"), the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your
 * choice, provided that you also meet, for each linked independent module,
 * the terms and conditions of the license of that module. An independent
 * module is a module which is not derived from or based on this library. If
 * you modify this library, you may extend the Classpath Exception to your
 * version of the library, but you are not obligated to do so. If you do not
 * wish to do so, delete this exception statement from your version.
 */
package org.gnome.weather;

import org.freedesktop.bindings.Constant;

/**
 * The size/scope of a particular {@link Location}.
 * 
 * <p>
 * Locations form a hierarchy, with a {@link LocationLevel#WORLD WORLD}
 * location at the top, divided into regions or countries, and so on.
 * Countries may or may not be divided into "adm1"s, and "adm1"s may or may
 * not be divided into "adm2"s. A city will have at least one, and possibly
 * several, weather stations inside it. Weather stations will never appear
 * outside of cities.
 * 
 * @author Guillaume Mazoyer
 * @since 4.2.0
 */
public class LocationLevel extends Constant
{
    protected LocationLevel(int ordinal, String nickname) {
        super(ordinal, nickname);
    }

    /**
     * A location representing the entire world.
     */
    public static final LocationLevel WORLD = new LocationLevel(GWeatherLocationLevel.WORLD, "WORLD");

    /**
     * A location representing a continent or other top-level region.
     */
    public static final LocationLevel REGION = new LocationLevel(GWeatherLocationLevel.REGION, "REGION");

    /**
     * A location representing a "country" (or other geographic unit that has
     * an ISO-3166 country code).
     */
    public static final LocationLevel COUNTRY = new LocationLevel(GWeatherLocationLevel.COUNTRY,
            "COUNTRY");

    /**
     * A location representing a "first-level administrative division"; ie, a
     * state, province, or similar division.
     */
    public static final LocationLevel ADM1 = new LocationLevel(GWeatherLocationLevel.ADM1, "ADM1");

    /**
     * A location representing a subdivision of a {@link LocationLevel#ADM1
     * ADM1} location, or a direct subdivision of a country that is not
     * represented in a {@link LocationEntry}.
     */
    public static final LocationLevel ADM2 = new LocationLevel(GWeatherLocationLevel.ADM2, "ADM2");

    /**
     * A location representing a city.
     */
    public static final LocationLevel CITY = new LocationLevel(GWeatherLocationLevel.CITY, "CITY");

    /**
     * A location representing a weather station.
     */
    public static final LocationLevel STATION = new LocationLevel(
            GWeatherLocationLevel.WEATHER_STATION, "STATION");
}
