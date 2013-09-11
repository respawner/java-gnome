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

/**
 * Implements methods that cannot be use right from generated code.
 * 
 * @author Guillaume Mazoyer
 */
final class GWeatherInfoOverride extends Plumbing
{
    private GWeatherInfoOverride() {
        /*
         * No instantiation.
         */
    }

    static final long getValueUpdate(Info self) {
        long result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_update(pointerOf(self));

            return result;
        }
    }

    private static native final long weather_info_get_value_update(long self);

    static final Sky getValueSky(Info self) {
        int result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_sky(pointerOf(self));

            return (Sky) enumFor(Sky.class, result);
        }
    }

    private static native final int weather_info_get_value_sky(long self);

    static final ConditionPhenomenon getValueConditionPhenomenon(Info self) {
        int result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_condition_phenomenon(pointerOf(self));

            return (ConditionPhenomenon) enumFor(ConditionPhenomenon.class, result);
        }
    }

    private static native final int weather_info_get_value_condition_phenomenon(long self);

    static final ConditionQualifier getValueConditionQualifier(Info self) {
        int result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_condition_qualifier(pointerOf(self));

            return (ConditionQualifier) enumFor(ConditionQualifier.class, result);
        }
    }

    private static native final int weather_info_get_value_condition_qualifier(long self);

    static final double getValueTemperature(Info self, TemperatureUnit unit) {
        double result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        if (unit == null) {
            throw new IllegalArgumentException("unit can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_temperature(pointerOf(self), unit.ordinal);

            return result;
        }
    }

    private static native final double weather_info_get_value_temperature(long self, int unit);

    static final double getValueTemperatureMin(Info self, TemperatureUnit unit) {
        double result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        if (unit == null) {
            throw new IllegalArgumentException("unit can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_temperature_min(pointerOf(self), unit.ordinal);

            return result;
        }
    }

    private static native final double weather_info_get_value_temperature_min(long self, int unit);

    static final double getValueTemperatureMax(Info self, TemperatureUnit unit) {
        double result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        if (unit == null) {
            throw new IllegalArgumentException("unit can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_temperature_max(pointerOf(self), unit.ordinal);

            return result;
        }
    }

    private static native final double weather_info_get_value_temperature_max(long self, int unit);

    static final double getValueDew(Info self, TemperatureUnit unit) {
        double result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        if (unit == null) {
            throw new IllegalArgumentException("unit can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_dew(pointerOf(self), unit.ordinal);

            return result;
        }
    }

    private static native final double weather_info_get_value_dew(long self, int unit);

    static final double getValueApparent(Info self, TemperatureUnit unit) {
        double result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        if (unit == null) {
            throw new IllegalArgumentException("unit can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_apparent(pointerOf(self), unit.ordinal);

            return result;
        }
    }

    private static native final double weather_info_get_value_apparent(long self, int unit);

    static final double getValueWind(Info self, SpeedUnit unit) {
        double result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        if (unit == null) {
            throw new IllegalArgumentException("unit can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_wind(pointerOf(self), unit.ordinal);

            return result;
        }
    }

    private static native final double weather_info_get_value_wind(long self, int unit);

    static final WindDirection getValueWindDirection(Info self) {
        int result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_wind_direction(pointerOf(self));

            return (WindDirection) enumFor(WindDirection.class, result);
        }
    }

    private static native final int weather_info_get_value_wind_direction(long self);

    static final double getValuePressure(Info self, PressureUnit unit) {
        double result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        if (unit == null) {
            throw new IllegalArgumentException("unit can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_pressure(pointerOf(self), unit.ordinal);

            return result;
        }
    }

    private static native final double weather_info_get_value_pressure(long self, int unit);

    static final double getValueVisibility(Info self, DistanceUnit unit) {
        double result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        if (unit == null) {
            throw new IllegalArgumentException("unit can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_visibility(pointerOf(self), unit.ordinal);

            return result;
        }
    }

    private static native final double weather_info_get_value_visibility(long self, int unit);

    static final long getValueSunrise(Info self) {
        long result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_sunrise(pointerOf(self));

            return result;
        }
    }

    private static native final long weather_info_get_value_sunrise(long self);

    static final long getValueSunset(Info self) {
        long result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_sunrise(pointerOf(self));

            return result;
        }
    }

    private static native final long weather_info_get_value_sunset(long self);

    static final double[] getValueMoonphase(Info self) {
        double[] result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_value_moonphase(pointerOf(self));

            return result;
        }
    }

    private static native final double[] weather_info_get_value_moonphase(long self);

    static final long[] getUpcomingMoonphases(Info self) {
        long[] result;

        if (self == null) {
            throw new IllegalArgumentException("self can't be null");
        }

        synchronized (lock) {
            result = weather_info_get_upcoming_moonphases(pointerOf(self));

            return result;
        }
    }

    private static native final long[] weather_info_get_upcoming_moonphases(long self);
}
