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
 * Constants describing different condition phenomenon.
 * 
 * @author Guillaume Mazoyer
 * @since 4.2.0
 */
public class ConditionPhenomenon extends Constant
{
    protected ConditionPhenomenon(int ordinal, String nickname) {
        super(ordinal, nickname);
    }

    public static final ConditionPhenomenon INVALID = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.INVALID, "INVALID");

    public static final ConditionPhenomenon NONE = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.NONE, "NONE");

    public static final ConditionPhenomenon DRIZZLE = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.DRIZZLE, "DRIZZLE");

    public static final ConditionPhenomenon RAIN = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.RAIN, "RAIN");

    public static final ConditionPhenomenon SNOW = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.INVALID, "INVALID");

    public static final ConditionPhenomenon SNOW_GRAINS = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.SNOW_GRAINS, "SNOW_GRAINS");

    public static final ConditionPhenomenon ICE_CRYSTALS = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.ICE_CRYSTALS, "ICE_CRYSTALS");

    public static final ConditionPhenomenon ICE_PELLETS = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.ICE_PELLETS, "ICE_PELLETS");

    public static final ConditionPhenomenon HAIL = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.HAIL, "HAIL");

    public static final ConditionPhenomenon SMALL_HAIL = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.SMALL_HAIL, "SMALL_HAIL");

    public static final ConditionPhenomenon UNKNOWN_PRECIPITATION = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.UNKNOWN_PRECIPITATION, "UNKNOWN_PRECIPITATION");

    public static final ConditionPhenomenon MIST = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.MIST, "MIST");

    public static final ConditionPhenomenon FOG = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.FOG, "FOG");

    public static final ConditionPhenomenon SMOKE = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.SMOKE, "SMOKE");

    public static final ConditionPhenomenon VOLCANIC_ASH = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.VOLCANIC_ASH, "VOLCANIC_ASH");

    public static final ConditionPhenomenon SAND = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.SAND, "SAND");

    public static final ConditionPhenomenon HAZE = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.HAZE, "HAZE");

    public static final ConditionPhenomenon SPRAY = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.SPRAY, "SPRAY");

    public static final ConditionPhenomenon DUST = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.DUST, "DUST");

    public static final ConditionPhenomenon SQUALL = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.SQUALL, "SQUALL");

    public static final ConditionPhenomenon SANDSTORM = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.SANDSTORM, "SANDSTORM");

    public static final ConditionPhenomenon DUSTSTORM = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.DUSTSTORM, "DUSTSTORM");

    public static final ConditionPhenomenon FUNNEL_CLOUD = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.FUNNEL_CLOUD, "FUNNEL_CLOUD");

    public static final ConditionPhenomenon TORNADO = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.TORNADO, "TORNADO");

    public static final ConditionPhenomenon DUST_WHIRLS = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.DUST_WHIRLS, "DUST_WHIRLS");

    public static final ConditionPhenomenon LAST = new ConditionPhenomenon(
            GWeatherConditionPhenomenon.LAST, "LAST");
}
