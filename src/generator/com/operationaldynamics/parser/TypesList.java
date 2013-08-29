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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

/**
 * A class that give a way to know what types the {@link IntrospectionParser}
 * can parse and what things do not need parsing inside each type.
 * 
 * @author Guillaume Mazoyer
 */
final class TypesList
{
    /**
     * Class that contains attributes for a whitelisted type.
     * 
     * @author Guillaume Mazoyer
     */
    private class TypeAttributes
    {
        private String javaName;

        private List<String> headers;

        private List<String> ignores;

        private TypeAttributes() {
            headers = new ArrayList<String>();
            ignores = new ArrayList<String>();
        }
    }

    private Map<String, TypeAttributes> list;

    private Map<String, String> packages;

    /**
     * Build and load a types list based on the given filename.
     * 
     * @param filename
     *            the path of the file to load.
     */
    TypesList(String filename) {
        list = new HashMap<String, TypeAttributes>();
        packages = new HashMap<String, String>();

        load(filename);
    }

    /**
     * Load each type description contained in the given file.
     * 
     * @param filename
     *            the path of the file to load.
     */
    private final void load(String filename) {
        final Builder builder;
        final Document document;
        final Elements modules, types;

        builder = new Builder();

        /*
         * Start the parsing of the XML data.
         */

        try {
            document = builder.build(new File(filename));
        } catch (ParsingException e) {
            System.err.println("Malformed XML data in " + filename + ":");
            System.err.println(e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("I/O problem when trying to parse " + filename);
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        } finally {
            System.err.flush();
        }

        /*
         * Get all modules and types listed in the file.
         */

        modules = document.getRootElement().getChildElements("module");
        types = document.getRootElement().getChildElements("type");

        /*
         * Load modules exceptions.
         */

        for (int i = 0; i < modules.size(); i++) {
            final Element module;

            module = modules.get(i);

            packages.put(module.getAttributeValue("name"), module.getAttributeValue("java-package"));
        }

        /*
         * Load types list.
         */

        for (int i = 0; i < types.size(); i++) {
            final Element type;
            final Elements headers;
            final Elements things;
            final String typeName;
            final TypeAttributes attributes;

            type = types.get(i);
            headers = type.getChildElements("header");
            things = type.getChildElements("thing");
            typeName = type.getAttributeValue("name");
            attributes = new TypeAttributes();

            /*
             * Get the Java name if specified.
             */

            attributes.javaName = type.getAttributeValue("java-name");

            /*
             * Load the needed headers.
             */

            for (int j = 0; j < headers.size(); j++) {
                final Element header;

                header = headers.get(j);

                attributes.headers.add(header.getAttributeValue("name"));
            }

            /*
             * Load the things to ignore.
             */

            for (int j = 0; j < things.size(); j++) {
                final Element thing;

                thing = things.get(j);

                if (thing.getAttribute("ignore") != null) {
                    attributes.ignores.add(thing.getAttributeValue("name"));
                }
            }

            list.put(typeName, attributes);
        }
    }

    /**
     * Return the Java package name to use for the given module.
     * 
     * @param module
     *            the GObject Introspection namespace.
     * @return the Java package name to use.
     */
    final String getActualJavaPackage(String module) {
        final String javaPackage;

        javaPackage = packages.get(module);

        return ((javaPackage == null) ? module : javaPackage);
    }

    /**
     * Tell if the given type can be parsed or not.
     * 
     * @param type
     *            the C type to check.
     * @return true if the type should be parsed, false otherwise.
     */
    final boolean isTypeWhitelisted(String type) {
        return list.keySet().contains(type);
    }

    /**
     * Return the Java name to use for the given object name.
     * 
     * @param type
     *            the C type for which we need the Java name.
     * @param name
     *            the name to use if not overridden.
     * @return the Java name to use.
     */
    final String getActualJavaName(String type, String name) {
        final String javaName;

        javaName = list.get(type).javaName;

        return (javaName == null) ? name : javaName;
    }

    /**
     * Return an array containing all the headers that need to be imported in
     * the generated C code.
     * 
     * @param type
     *            the C type that we need the headers for.
     * @return an array of String with each String being a header or null if
     *         there is no headers to include.
     */
    final String[] getHeadersForType(String type) {
        final List<String> headers;

        headers = list.get(type).headers;

        if (headers.size() < 1) {
            return null;
        } else {
            return (String[]) headers.toArray(new String[headers.size()]);
        }
    }

    /**
     * Tell if the given thing for the given type can be parsed or not.
     * 
     * @param type
     *            the C type to which the thing belongs to.
     * @param thing
     *            a constructor, a method, a function or a signal C name.
     * @return true if the thing should be parsed, false otherwise.
     */
    final boolean isThingBlacklisted(String type, String thing) {
        return list.get(type).ignores.contains(thing);
    }
}
