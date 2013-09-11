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
 * Constants defining the direction of the wind.
 * 
 * @author Guillaume Mazoyer
 * @since 4.2.0
 */
public class WindDirection extends Constant
{
    protected WindDirection(int ordinal, String nickname) {
        super(ordinal, nickname);
    }

    public static final WindDirection INVALID = new WindDirection(GWeatherWindDirection.INVALID,
            "INVALID");

    public static final WindDirection VARIABLE = new WindDirection(GWeatherWindDirection.VARIABLE,
            "VARIABLE");

    public static final WindDirection NORTH = new WindDirection(GWeatherWindDirection.N, "NORTH");

    public static final WindDirection NORTH_NORTH_EAST = new WindDirection(GWeatherWindDirection.NNE,
            "NORTH_NORTH_EAST");

    public static final WindDirection NORTH_EAST = new WindDirection(GWeatherWindDirection.NE,
            "NORTH_EAST");

    public static final WindDirection EAST_NORTH_EAST = new WindDirection(GWeatherWindDirection.ENE,
            "EAST_NORTH_EAST");

    public static final WindDirection EAST = new WindDirection(GWeatherWindDirection.E, "EAST");

    public static final WindDirection EAST_SOUTH_EAST = new WindDirection(GWeatherWindDirection.ESE,
            "EAST_SOUTH_EAST");

    public static final WindDirection SOUTH_EAST = new WindDirection(GWeatherWindDirection.SE,
            "SOUTH_EAST");

    public static final WindDirection SOUTH_SOUTH_EAST = new WindDirection(GWeatherWindDirection.SSE,
            "SOUTH_SOUTH_EAST");

    public static final WindDirection SOUTH = new WindDirection(GWeatherWindDirection.S, "SOUTH");

    public static final WindDirection SOUTH_SOUTH_WEST = new WindDirection(GWeatherWindDirection.SSW,
            "SOUTH_SOUTH_WEST");

    public static final WindDirection SOUTH_WEST = new WindDirection(GWeatherWindDirection.SW,
            "SOUTH_WEST");

    public static final WindDirection WEST_SOUTH_WEST = new WindDirection(GWeatherWindDirection.WSW,
            "WEST_SOUTH_WEST");

    public static final WindDirection WEST = new WindDirection(GWeatherWindDirection.W, "WEST");

    public static final WindDirection WEST_NORTH_WEST = new WindDirection(GWeatherWindDirection.WNW,
            "WEST_NORTH_WEST");

    public static final WindDirection NORTH_WEST = new WindDirection(GWeatherWindDirection.NW,
            "NORTH_WEST");

    public static final WindDirection NORTH_NORTH_WEST = new WindDirection(GWeatherWindDirection.NNW,
            "NORTH_NORTH_WEST");

    public static final WindDirection LAST = new WindDirection(GWeatherWindDirection.LAST, "LAST");

    @Override
    public String toString() {
        return GWeatherMisc.windDirectionToString(this);
    }
}
