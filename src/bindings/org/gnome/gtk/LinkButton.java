/*
 * LinkButton.java
 *
 * Copyright (c) 2009 Operational Dynamics Consulting Pty Ltd, and Others
 *
 * The code in this file, and the library it is a part of, are made available
 * to you by the authors under the terms of the "GNU General Public Licence,
 * version 2" plus the "Classpath Exception" (you may link to this code as a
 * library into other programs provided you don't make a derivation of it).
 * See the LICENCE file for the terms governing usage and redistribution.
 */
package org.gnome.gtk;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * LinkButton is a specialized subclass of Button for linking to URIs. The
 * default action is to open the URI with its associated application
 * determined by the desktop environment. <img src="LinkButton.png"
 * class="snapshot">
 * 
 * @author Serkan Kaba
 * @author Guillaume Mazoyer
 * @since 4.0.14
 */
public class LinkButton extends Button
{
    protected LinkButton(long pointer) {
        super(pointer);
    }

    /**
     * Create a button pointing to given URI. The <code>uri</code> will also
     * be used as the label.
     * 
     * @since 4.0.14
     */
    public LinkButton(URI uri) {
        super(GtkLinkButton.createLinkButton(uri.toString()));
    }

    /**
     * Create a button pointing to given URI and the given <code>label</code>
     * as you would expect from a normal hyperlink.
     * 
     * @since 4.0.14
     */
    public LinkButton(URI uri, String label) {
        super(GtkLinkButton.createLinkButtonWithLabel(uri.toString(), label));
    }

    /**
     * Return the button's URI.
     * 
     * @since 4.0.14
     */
    /*
     * Naming convention same as FileChooser's getURI().
     */
    public URI getURI() {
        try {
            return new URI(GtkLinkButton.getUri(this));
        } catch (URISyntaxException e) {
            throw new RuntimeException("We shouldn't be throwing this exception", e);
        }
    }

    /**
     * Modify the button's {@link URI}.
     * 
     * @since 4.0.14
     */
    public void setURI(URI uri) {
        GtkLinkButton.setUri(this, uri.toString());
    }

    /**
     * Return the button's visited state. Note that this might be modified
     * programatically by {@link #setVisited(boolean)}.
     * 
     * @since 4.0.14
     */
    public boolean getVisited() {
        return GtkLinkButton.getVisited(this);
    }

    /**
     * Modify the button's visited state.
     * 
     * @since 4.0.14
     */
    public void setVisited(boolean visited) {
        GtkLinkButton.setVisited(this, visited);
    }
}
