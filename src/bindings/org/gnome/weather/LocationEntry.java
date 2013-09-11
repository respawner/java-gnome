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

import org.gnome.gtk.Entry;

/**
 * A subclass of {@link Entry} that provides autocompletion on
 * {@link Location Locations}.
 * 
 * @author Guillaume Mazoyer
 * @since 4.2.0
 */
public class LocationEntry extends Entry
{
    protected LocationEntry(long pointer) {
        super(pointer);
    }

    /**
     * Creates a new {@link LocationEntry}.
     * 
     * @param location
     *            the top-level location for the entry.
     */
    public LocationEntry(Location location) {
        this(GWeatherLocationEntry.createLocationEntry(location));
    }

    /**
     * Sets entry location, and updates the text of the entry accordingly.
     * Note that if the database contains the given location, that will be
     * chosen in place of the location.
     * 
     * @param location
     *            a {@link Location}, or null to clear entry.
     */
    public void setLocation(Location location) {
        GWeatherLocationEntry.setLocation(this, location);
    }

    /**
     * Gets the location that was set by a previous call to
     * {@link LocationEntry#setLocation(Location) setLocation()} or was
     * selected by the user.
     * 
     * @return the selected location, or null if no location is selected.
     */
    public Location getLocation() {
        return GWeatherLocationEntry.getLocation(this);
    }

    /**
     * Checks whether or not entry's text has been modified by the user.
     * {@link LocationEntry#getLocation() getLocation()} should be used for
     * this.
     * 
     * @return : true if entry's text was modified by the user, or false if
     *         it's set to the default text of a location.
     */
    public boolean hasCustomText() {
        return GWeatherLocationEntry.hasCustomText(this);
    }

    /**
     * Sets entry's location to a city with the given code, and given city
     * name, if non-null. If there is no matching city, sets entry's location
     * to null.
     * 
     * @param city
     *            the city name, or null.
     * @param code
     *            the METAR station code
     * @return true if entry's location could be set to a matching city, false
     *         otherwise.
     */
    public boolean setCity(String city, String code) {
        return GWeatherLocationEntry.setCity(this, city, code);
    }
}
