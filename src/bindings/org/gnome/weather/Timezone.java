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

import org.gnome.glib.Boxed;

/**
 * A timezone.
 * 
 * <p>
 * There are no public methods for creating timezones; they can only be
 * created by calling the {@link Location#Location(boolean) Location
 * constructor}, and then calling various {@link Location} methods to extract
 * relevant timezones from the location hierarchy.
 * 
 * @author Guillaume Mazoyer
 * @since 4.2.0
 */
public class Timezone extends Boxed
{
    protected Timezone(long pointer) {
        super(pointer);
    }

    @Override
    protected void release() {
        GWeatherTimezone.unref(this);
    }

    /**
     * Gets the UTC timezone.
     * 
     * @return a Timezone for UTC or null if there is an error.
     */
    public static Timezone getUtc() {
        return GWeatherMisc.getUtc();
    }

    /**
     * Gets zone's name; a translated, user-presentable string.
     * 
     * <p>
     * Note that the returned name might not be unique among timezones, and
     * may not make sense to the user unless it is presented along with the
     * timezone's country's name (or in some context where the country is
     * obvious).
     * 
     * @return the zone's name.
     */
    public String getName() {
        return GWeatherTimezone.getName(this);
    }

    /**
     * Gets zone's tzdata identifier, eg "America/New_York".
     * 
     * @return the zone's ID.
     */
    public String getId() {
        return GWeatherTimezone.getTzid(this);
    }

    /**
     * Gets zone's standard offset from UTC, in minutes. Eg, a value of 120
     * would indicate "GMT+2".
     * 
     * @return the zone's standard offset, in minutes.
     */
    public int getOffset() {
        return GWeatherTimezone.getOffset(this);
    }

    /**
     * Checks if zone observes daylight/summer time for part of the year.
     * 
     * @return true if zone observes daylight/summer time.
     */
    public boolean hasDaylightSummerTime() {
        return GWeatherTimezone.hasDst(this);
    }

    /**
     * Gets zone's daylight/summer time offset from UTC, in minutes. Eg, a
     * value of 120 would indicate "GMT+2". This is only meaningful if
     * {@link #hasDaylightSummerTime()} returns true.
     * 
     * @return the zone's daylight/summer time offset, in minutes.
     */
    public int getDaylightSummerTimeOffset() {
        return GWeatherTimezone.getDstOffset(this);
    }
}
