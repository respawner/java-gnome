/*
 * ExampleDrawingInExposeEvent.java
 *
 * Copyright (c) 2007-2008 Operational Dynamics Consulting Pty Ltd
 * 
 * The code in this file, and the library it is a part of, are made available
 * to you by the authors under the terms of the "GNU General Public Licence,
 * version 2" See the LICENCE file for the terms governing usage and
 * redistribution.
 */
package cairo;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.Status;
import org.gnome.gdk.Event;
import org.gnome.gdk.EventExpose;
import org.gnome.gdk.Rectangle;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.Image;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;

/**
 * Exercise drawing with the Cairo API. If you are rendering a custom Widget
 * or otherwise drawing stuff with Cairo that is to be presented by GTK, you
 * are expected to do this in the EXPOSE_EVENT handler for that Widget.
 * 
 * @author Andrew Cowie
 */
/*
 * TODO rename this once more once we actually draw something interesting!
 */
public class ExampleDrawingInExposeEvent
{
    public static void main(String[] args) {
        final Window w;
        final Image i;

        Gtk.init(args);

        w = new Window();
        w.setTitle("Expose");
        w.setDefaultSize(150, 150);

        i = new Image();
        w.add(i);
        w.showAll();

        i.connect(new Widget.EXPOSE_EVENT() {
            public boolean onExposeEvent(Widget source, EventExpose event) {
                final Context cr;
                final Rectangle rect;

                /*
                 * Out of interest, where is this occuring?
                 */

                rect = event.getArea();
                System.out.println("EXPOSE_EVENT bounded by " + rect.getWidth() + "x" + rect.getHeight()
                        + " at " + rect.getX() + "," + rect.getY());

                /*
                 * With that out of the way, we get to the heart of the
                 * matter: creating a Cairo Context based on (and mapped to)
                 * the Drawable underlying the Widget. The key here is that
                 * the Widget is mapped unlike earlier when we were
                 * constructing it. The first EXPOSE_EVENT does not occur
                 * until after the Widget is realized; indeed, that is when it
                 * is triggered.
                 */

                cr = new Context(source.getWindow());

                /*
                 * The Cairo API is built on the notion of fast failure if an
                 * error is encountered. You need to explicitly probe to
                 * determine whether a problem has occured. It is tempting to
                 * put this into every single bound Cairo method.
                 */

                assert (cr.getStatus() == Status.SUCCESS);

                /*
                 * Now, finally do some drawing:
                 */

                cr.setSourceRGBA(0.0, 0.0, 1.0, 0.8);
                cr.moveTo(10, 10);
                cr.lineTo(20, 45);
                cr.stroke();

                cr.setSourceRGBA(0.0, 1.0, 0.0, 0.8);
                cr.rectangle(70, 70, 20, 40);

                cr.fill();

                return false;
            }
        });

        /*
         * And that's it. Conclude with connecting the usual tear-down
         * handler, and then fire up the main loop.
         */

        w.connect(new Window.DELETE_EVENT() {
            public boolean onDeleteEvent(Widget source, Event event) {
                Gtk.mainQuit();
                return false;
            }
        });

        Gtk.main();
    }
}
