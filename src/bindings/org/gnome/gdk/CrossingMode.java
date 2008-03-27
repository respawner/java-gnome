/*
 * CrossingMode.java
 *
 * Copyright (c) 2007-2008 Operational Dynamics Consulting Pty Ltd
 *
 * The code in this file, and the library it is a part of, are made available
 * to you by the authors under the terms of the "GNU General Public Licence,
 * version 2" plus the "Classpath Exception" (you may link to this code as a
 * library into other programs provided you don't make a derivation of it).
 * See the LICENCE file for the terms governing usage and redistribution.
 */
package org.gnome.gdk;

import org.freedesktop.bindings.Constant;

/**
 * Constants relating to the nature of the event when a mouse enters or leaves
 * a GDK Window.
 * 
 * @author Andrew Cowie
 * @since 4.0.7
 * @see <a
 *      href="http://xorg.freedesktop.org/releases/X11R7.0/doc/PDF/xlib.pdf">The
 *      XLib programming manual</a>, section 10.6
 */
/*
 * FIXME Improve the explanation of these occurrences.
 */
public final class CrossingMode extends Constant
{
    private CrossingMode(int ordinal, String nickname) {
        super(ordinal, nickname);
    }

    /**
     * The EventCrossing occurred because of pointer motion (by the user).
     */
    public static final CrossingMode NORMAL = new CrossingMode(GdkCrossingMode.NORMAL, "NORMAL");

    /**
     * Event occurred because a grab was activated.
     */
    public static final CrossingMode GRAB = new CrossingMode(GdkCrossingMode.GRAB, "GRAB");

    /**
     * Event occurred because an ungrab happened.
     */
    public static final CrossingMode UNGRAB = new CrossingMode(GdkCrossingMode.UNGRAB, "UNGRAB");
}
