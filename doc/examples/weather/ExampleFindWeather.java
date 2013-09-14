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
 */
package weather;

import org.gnome.gdk.Event;
import org.gnome.gtk.Button;
import org.gnome.gtk.Entry;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.Label;
import org.gnome.gtk.VBox;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;
import org.gnome.weather.ForecastType;
import org.gnome.weather.Info;
import org.gnome.weather.Location;
import org.gnome.weather.LocationEntry;

/**
 * A simple program that shows a use case of weather related classes.
 * 
 * @author Guillaume Mazoyer
 * @since 4.2.0
 */
public class ExampleFindWeather
{
    public static void main(String[] args) {
        final Window window;
        final VBox vbox;
        final Button button;
        final LocationEntry entry;
        final Location world;
        final Info info;
        final Label summary, update, conditions, temperature;

        /*
         * Initialize GTK.
         */

        Gtk.init(args);

        /*
         * Create a top level Window.
         */

        window = new Window();

        /*
         * Connect the signal to close the window
         */

        window.connect(new Window.DeleteEvent() {
            public boolean onDeleteEvent(Widget source, Event event) {
                Gtk.mainQuit();
                return false;
            }
        });

        /*
         * Create a VBox which will contain everything.
         */

        vbox = new VBox(false, 3);

        /*
         * Create a Info that will contain data about the forecast.
         */

        world = new Location(true);
        info = new Info(world, ForecastType.ZONE);

        /*
         * Add labels that will be updated.
         */

        summary = new Label("Weather summary");
        vbox.add(summary);
        update = new Label("Update summary");
        vbox.add(update);
        conditions = new Label("Conditions summary");
        vbox.add(conditions);
        temperature = new Label("Temperature summary");
        vbox.add(temperature);

        /*
         * Do the work when info is updated.
         */

        info.connect(new Info.Updated() {
            public void onUpdated(Info source) {
                if (source.getWeatherSummary() != null) {
                    summary.setLabel(source.getWeatherSummary());
                }

                if (source.getUpdate() != null) {
                    update.setLabel(source.getUpdate());
                }

                if (source.getConditions() != null) {
                    conditions.setLabel(source.getConditions());
                }

                if (source.getTemperatureSummary() != null) {
                    temperature.setLabel(source.getTemperatureSummary());
                }
            }
        });

        /*
         * Create the Entry which will display the location.
         */

        entry = new LocationEntry(world);
        vbox.add(entry);

        /*
         * Change the location when a valid one has been found.
         */

        entry.connect(new Entry.Changed() {
            public void onChanged(Entry source) {
                if (entry.getLocation() != null) {
                    info.setLocation(entry.getLocation());
                }
            }
        });

        /*
         * Create the Button to update the forecast.
         */

        button = new Button("Update");
        vbox.add(button);

        /*
         * Force the update when clicked.
         */

        button.connect(new Button.Clicked() {
            public void onClicked(Button source) {
                info.update();
            }
        });

        /*
         * Finally pack the VBox into our Window and set a title.
         */

        window.add(vbox);
        window.setTitle("Does it rain?");
        window.showAll();

        /*
         * Run the main loop.
         */

        Gtk.main();
    }
}
