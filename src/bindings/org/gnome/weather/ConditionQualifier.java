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
 * Constants giving an idea of the intensity of a {@link ConditionPhenomenon
 * phenomenon}.
 * 
 * @author Guillaume Mazoyer
 * @since 4.2.0
 */
public class ConditionQualifier extends Constant
{
    protected ConditionQualifier(int ordinal, String nickname) {
        super(ordinal, nickname);
    }

    public static final ConditionQualifier INVALID = new ConditionQualifier(
            GWeatherConditionQualifier.INVALID, "INVALID");

    public static final ConditionQualifier NONE = new ConditionQualifier(
            GWeatherConditionQualifier.NONE, "NONE");

    public static final ConditionQualifier VICINITY = new ConditionQualifier(
            GWeatherConditionQualifier.VICINITY, "VICINITY");

    public static final ConditionQualifier LIGHT = new ConditionQualifier(
            GWeatherConditionQualifier.LIGHT, "LIGHT");

    public static final ConditionQualifier MODERATE = new ConditionQualifier(
            GWeatherConditionQualifier.MODERATE, "MODERATE");

    public static final ConditionQualifier HEAVY = new ConditionQualifier(
            GWeatherConditionQualifier.HEAVY, "HEAVY");

    public static final ConditionQualifier SHALLOW = new ConditionQualifier(
            GWeatherConditionQualifier.SHALLOW, "SHALLOW");

    public static final ConditionQualifier PATCHES = new ConditionQualifier(
            GWeatherConditionQualifier.PATCHES, "PATCHES");

    public static final ConditionQualifier PARTIAL = new ConditionQualifier(
            GWeatherConditionQualifier.PARTIAL, "PARTIAL");

    public static final ConditionQualifier THUNDERSTORM = new ConditionQualifier(
            GWeatherConditionQualifier.THUNDERSTORM, "THUNDERSTORM");

    public static final ConditionQualifier BLOWING = new ConditionQualifier(
            GWeatherConditionQualifier.BLOWING, "BLOWING");

    public static final ConditionQualifier SHOWERS = new ConditionQualifier(
            GWeatherConditionQualifier.SHOWERS, "SHOWERS");

    public static final ConditionQualifier DRIFTING = new ConditionQualifier(
            GWeatherConditionQualifier.DRIFTING, "DRIFTING");

    public static final ConditionQualifier FREEZING = new ConditionQualifier(
            GWeatherConditionQualifier.FREEZING, "FREEZING");

    public static final ConditionQualifier LAST = new ConditionQualifier(
            GWeatherConditionQualifier.LAST, "LAST");
}
