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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that give a way to know what types the {@link IntrospectionParser}
 * can parse and what things do not need parsing inside each type.
 * 
 * @author Guillaume Mazoyer
 */
final class TypesList
{
    private Map<String, String[]> list;

    /**
     * Build and load a types list based on the given filename.
     * 
     * @param filename
     *            the path of the file to load.
     */
    TypesList(String filename) {
        list = new HashMap<String, String[]>();

        load(filename);
    }

    /**
     * Load each type description contained in the given file.
     * 
     * @param filename
     *            the path of the file to load.
     */
    private final void load(String filename) {
        final BufferedReader reader;
        String line, block;
        boolean inBlock;

        inBlock = false;
        block = "";

        try {
            reader = new BufferedReader(new FileReader(filename));

            while ((line = reader.readLine()) != null) {
                /*
                 * This is a comment.
                 */

                if (line.isEmpty() || line.startsWith("#") || line.startsWith(";;")
                        || line.startsWith("//")) {
                    continue;
                }

                if (inBlock) {
                    block += new String(line);
                } else {
                    block = new String(line);
                    inBlock = true;
                }

                /*
                 * We have reached the end of the block.
                 */

                if (block.contains("}")) {
                    final String type;
                    final String[] split, blacklisted;

                    /*
                     * Separate C type name from list of ignored things.
                     */

                    inBlock = false;
                    split = block.split("\\{");
                    type = split[0].trim();
                    blacklisted = split[1].split("\\}")[0].split(",");

                    /*
                     * Remove extra spaces.
                     */

                    for (int i = 0; i < blacklisted.length; i++) {
                        blacklisted[i] = blacklisted[i].trim();
                    }

                    list.put(type,
                            ((blacklisted.length == 1) && blacklisted[0].isEmpty()) ? new String[] {}
                                    : blacklisted);
                }
            }

            reader.close();
        } catch (IOException e) {
            System.err.println("How come we can't open a file for reading?\n" + e);
        }
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
     * Tell if the given thing for the given type can be parsed or not.
     * 
     * @param type
     *            the C type to whch the thing belongs to.
     * @param thing
     *            a constructor, a method, a function or a signal C name.
     * @return true if the thing should be parsed, false otherwise.
     */
    final boolean isThingBlacklisted(String type, String thing) {
        return Arrays.asList(list.get(type)).contains(thing);
    }
}
