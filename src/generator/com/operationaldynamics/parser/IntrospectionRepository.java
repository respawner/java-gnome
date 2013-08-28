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
package com.operationaldynamics.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

/**
 * This class represents a &lt;repository&gt; element that is contained in the
 * Introspection data which are usually take from a .gir file.
 * 
 * @author Guillaume Mazoyer
 */
public class IntrospectionRepository
{
    public static final String CORE_NAMESPACE;

    public static final String C_NAMESPACE;

    public static final String GLIB_NAMESPACE;

    private static final List<String> prefixes;

    private Elements namespaces;

    private String[] headers;

    static {
        CORE_NAMESPACE = "http://www.gtk.org/introspection/core/1.0";
        C_NAMESPACE = "http://www.gtk.org/introspection/c/1.0";
        GLIB_NAMESPACE = "http://www.gtk.org/introspection/glib/1.0";

        prefixes = new ArrayList<String>();
    }

    /**
     * Build a new IntrospectionRepository object.
     * 
     * @param reader
     *            a Reader subclass needed to load the XML data.
     * @throws ParsingException
     *             if an error occurs while parsing the XML file.
     * @throws ValidityException
     *             if the XML file does not seem valid.
     * @throws IOException
     *             if the XML file cannot be read.
     */
    public IntrospectionRepository(Reader reader) throws ValidityException, ParsingException,
            IOException {
        load(reader);
    }

    /**
     * Return the List of registered prefixes that were load from the various
     * namespaces.
     * 
     * @return a List of String, each String is a prefix.
     */
    public static final List<String> getRegisteredPrefixes() {
        return prefixes;
    }

    /**
     * Load the repository that is described in the XML data.
     * 
     * @param reader
     *            a Reader subclass needed to load the XML data.
     * @throws ParsingException
     *             if an error occurs while parsing the XML file.
     * @throws ValidityException
     *             if the XML file does not seem valid.
     * @throws IOException
     *             if the XML file cannot be read.
     */
    private void load(Reader reader) throws ValidityException, ParsingException, IOException {
        final Builder builder;
        final Document document;
        final Element repository;
        final Elements includes;

        builder = new Builder();

        /*
         * Start the parsing of the XML data.
         */

        document = builder.build(reader);

        /*
         * Get the elements that are we need (includes and namespaces).
         */

        repository = document.getRootElement();
        includes = repository.getChildElements("include", C_NAMESPACE);
        namespaces = repository.getChildElements("namespace", CORE_NAMESPACE);

        /*
         * Convert include elements to String.
         */

        headers = new String[includes.size()];

        for (int i = 0; i < headers.length; i++) {
            headers[i] = includes.get(i).getAttributeValue("name");
        }

        /*
         * Load also the C prefix for each namespace.
         */

        for (int i = 0; i < namespaces.size(); i++) {
            final String prefix;

            prefix = namespaces.get(i).getAttributeValue("identifier-prefixes", C_NAMESPACE);

            if (!prefixes.contains(prefix)) {
                prefixes.add(prefix);
            }
        }

    }

    /**
     * Return the elements that contains an XML representation of what this
     * repository contains.
     * 
     * @return XML Elements of namespaces.
     */
    public Elements getAvailableNamespaces() {
        return namespaces;
    }

    /**
     * Return an array of headers that need to be included to use what this
     * repository contains.
     * 
     * @return an array of String.
     */
    public String[] getNeededHeaders() {
        return headers;
    }
}
