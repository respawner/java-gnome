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
package com.operationaldynamics.defsparser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import junit.framework.TestCase;
import nu.xom.ParsingException;

import com.operationaldynamics.codegen.Thing;

/**
 * Evaluate the internal methods in the IntrospectionParser class.
 * 
 * @author Guillaume Mazoyer
 */
public class ValidateIntrospectionParsing extends TestCase
{
    private static final String inputGirData;

    static {
        final StringBuffer buffer;
        final String[] raw;

        raw = new String[] {
            "<repository version=\"1.2\"",
            "            xmlns=\"http://www.gtk.org/introspection/core/1.0\"",
            "            xmlns:c=\"http://www.gtk.org/introspection/c/1.0\"",
            "            xmlns:glib=\"http://www.gtk.org/introspection/glib/1.0\">",
            "  <namespace name=\"Gtk\"",
            "             version=\"3.0\"",
            "             shared-library=\"libgtk-3.so.0,libgdk-3.so.0\"",
            "             c:identifier-prefixes=\"Gtk\"",
            "             c:symbol-prefixes=\"gtk\">",
            "    <class name=\"Button\"",
            "             c:symbol-prefix=\"button\"",
            "             c:type=\"GtkButton\"",
            "             parent=\"Bin\"",
            "             glib:type-name=\"GtkButton\"",
            "             glib:get-type=\"gtk_button_get_type\"",
            "             glib:type-struct=\"ButtonClass\">",
            "      <constructor name=\"new\" c:identifier=\"gtk_button_new\">",
            "        <return-value transfer-ownership=\"none\">",
            "          <type name=\"Widget\" c:type=\"GtkWidget*\"/>",
            "        </return-value>",
            "      </constructor>",
            "      <method name=\"set_label\" c:identifier=\"gtk_button_set_label\">",
            "        <return-value transfer-ownership=\"none\">",
            "          <type name=\"none\" c:type=\"void\"/>",
            "        </return-value>",
            "        <parameters>",
            "          <instance-parameter name=\"button\" transfer-ownership=\"none\">",
            "            <type name=\"Button\" c:type=\"GtkButton*\"/>",
            "          </instance-parameter>",
            "          <parameter name=\"label\" transfer-ownership=\"none\">",
            "            <type name=\"utf8\" c:type=\"const gchar*\"/>",
            "          </parameter>",
            "        </parameters>",
            "      </method>",
            "      <glib:signal name=\"clicked\" when=\"first\" action=\"1\">",
            "        <return-value transfer-ownership=\"none\">",
            "          <type name=\"none\" c:type=\"void\"/>",
            "        </return-value>",
            "      </glib:signal>",
            "    </class>",
            "  </namespace>",
            "</repository>"
        };

        buffer = new StringBuffer();
        for (String line : raw) {
            buffer.append(line);
            buffer.append("\n");
        }

        inputGirData = buffer.toString();
    }

    protected IntrospectionParser parser;

    StringReader in;

    public void setUp() throws IOException {
        in = new StringReader(inputGirData);

        parser = new IntrospectionParser(in);
    }

    public void tearDown() throws IOException {
        in.close();
    }

    public final void testObjectBlockCreated() throws ParsingException, IOException {
        Map<String, Block[]> results;
        Block[] blocks;
        ObjectBlock o;

        results = parser.parseData();

        assertEquals(1, results.size());
        assertNotNull(results.get("GtkButton"));

        blocks = results.get("GtkButton");

        assertTrue(blocks[0] instanceof ObjectBlock);

        o = (ObjectBlock) blocks[0];

        assertEquals("Gtk", o.inModule);
        assertEquals("GtkBin", o.parent);
        assertEquals("GtkButton", o.cName);
        assertEquals("Button", o.blockName);
    }

    public final void testCantCreateThingFromNonTypeBlock() throws ParsingException, IOException {
        Block[] blocks;
        Thing t;

        blocks = parser.parseData().get("GtkButton");
        t = null;

        assertFalse(blocks[1] instanceof TypeBlock);
        try {
            t = blocks[1].createThing();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
            // good
        }
        assertNull(t);
    }

    public final void testMethodReferenceToSelfInsertion() throws ParsingException, IOException {
        Block[] blocks;
        MethodBlock block;

        blocks = parser.parseData().get("GtkButton");

        assertTrue(blocks[2] instanceof MethodBlock);

        block = (MethodBlock) blocks[2];

        assertTrue(block.parameters.length == 2);
        assertEquals("GtkButton*", block.parameters[0][0]);
        assertEquals("self", block.parameters[0][1]);
        assertEquals("const-gchar*", block.parameters[1][0]);
        assertEquals("label", block.parameters[1][1]);
    }
}
