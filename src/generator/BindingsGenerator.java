/*
 * java-gnome, a UI library for writing GTK and GNOME programs from Java!
 *
 * Copyright © 2007-2013 Operational Dynamics Consulting, Pty Ltd
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.ParsingException;

import com.operationaldynamics.defsparser.Block;
import com.operationaldynamics.defsparser.DefsLineNumberReader;
import com.operationaldynamics.defsparser.DefsParser;
import com.operationaldynamics.defsparser.FunctionBlock;
import com.operationaldynamics.defsparser.IntrospectionParser;
import com.operationaldynamics.driver.DefsFile;
import com.operationaldynamics.driver.ImproperDefsFileException;

/**
 * The java-gnome code generator.
 * 
 * <p>
 * The biggest problem architecturally is transforming information from the
 * form that the upstream .gir data provides to one we can do something useful
 * with in our context. The code generator, then is essentially two separate
 * pieces that have nothing to do with each other:
 * 
 * <ul>
 * <li>At the front end is the .gir file parser. IntrospectionParser takes an
 * input file of GNOME GIR XML metadata and turns it into an array of Block
 * objects. Blocks are Java objects representing the contents of a given XML
 * description.
 * 
 * <li>Completely independent of the parser is the code generator. A hierarchy
 * of Generator objects exist with the code to output the necessary Java and C
 * code They have constructors which minutely specify the information they
 * require (and with variables names that means something to the task of
 * bindings generation, rather than whatever the origin .gir data might have
 * called it). The types information describing the underlying library is
 * stored in a hash table of which uses the underlying type (as found in the
 * source .gir data) as a key, and a Thing object as the value containing all
 * the necessary mappings of that type to the actual Java or C language type
 * used at each layer of the bindings.
 * </ul>
 * 
 * <p>
 * The link between the two sides are the createThing() and createGenerator()
 * methods in each Block object; this is where we translate from the
 * characteristic key names in the .gir data to the names we use in the
 * generator.
 * 
 * <p>
 * To generate the java-gnome bindings, we therefore do several things:
 * 
 * <ol>
 * <li>register all the type information: We load each .gir file and create
 * arrays of Blocks. We then stash these Blocks in an a class called DefsFile.
 * Along the way we call each Block's createThing() and then Thing.register()
 * to store the the resultant in our lookup table.
 * 
 * <li>parse data to override known .gir data: We load each .defs file and
 * create arrays of Blocks. The .defs file may contain a full description of
 * an object but also a single function, method or signal to add to the
 * original data taken from the Introspection XML.
 * 
 * <li>with a full database of type information in hand, we can then do an
 * iteration over the Block arrays to actually generate the code that goes
 * with each stanza. We get the appropriate Generator object by calling each
 * Block's createGenerator() method and then run its writeTranslationCode()
 * and writeJniCode() methods to finally emit the generated translation layer
 * (Java) and jni layer (C) code.
 * </ol>
 * 
 * @author Andrew Cowie
 * @author Guillaume Mazoyer
 * @since 4.0.3
 */
/*
 * This class's name would seem to imply it is a subclass of Generator, which
 * of course is not the case. It is, however, a good name to have show up when
 * building the library via `make`, so "BindingsGenerator" it is. If someone
 * wants to suggest a better name, go ahead.
 */
public class BindingsGenerator
{
    public static void main(String[] args) throws IOException {
        runGeneratorOutputIntrospectionToFiles(args, new File("generated/bindings/"));
    }

    /**
     * This is building towards the main loop that will drive the .gir file
     * parser and subsequent runs of the bindings code generators, but it is
     * still an intermediate form.
     */
    private static void runGeneratorOutputIntrospectionToFiles(final String[] files, final File outputDir) {
        final File[] overriders;
        final Map<String, DefsFile> introspected;
        final List<DefsFile> all;
        IntrospectionParser parser;
        DefsParser secondParser;
        Block[] blocks;
        PrintWriter typeMapping;
        DefsLineNumberReader in;

        all = new ArrayList<DefsFile>();
        introspected = new HashMap<String, DefsFile>();

        /*
         * Load the all the .gir files into DefsFile objects, one per type.
         * Along the way, this registers the type information.
         */

        for (String file : files) {
            parser = new IntrospectionParser(new File(file));

            try {
                introspected.putAll(parser.parseData());
            } catch (ParsingException e) {
                System.out.println("Malformed XML data in " + file + ":");
                System.out.println(e.getMessage());
                System.out.println("[continuing next file]\n");
            } catch (IOException e) {
                System.out.println("I/O problem when trying to parse " + file);
                System.out.println(e.getMessage());
                System.out.println("[continuing next file]\n");
                continue;
            } finally {
                System.out.flush();
            }
        }

        /*
         * Parse .defs files which are used to override Introspection data.
         */

        overriders = new File("src/overriders").listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".defs");
            }
        });

        /*
         * Load the all the .defs files into DefsFile objects, one per type.
         * Along the way, this registers the type information.
         */

        for (int i = 0; i < overriders.length; i++) {
            try {
                in = new DefsLineNumberReader(new FileReader(overriders[i]), overriders[i].getName());

                secondParser = new DefsParser(in);
                blocks = secondParser.parseData();

                /*
                 * The definition is made to add more things or change what we
                 * know from the introspection data.
                 */

                if (!(blocks[0] instanceof FunctionBlock)) {
                    all.add(new DefsFile(blocks));
                } else {
                    for (Block block : blocks) {
                        final DefsFile toChange;
                        final FunctionBlock function;
                        String object;

                        function = (FunctionBlock) block;

                        /*
                         * We are only interested in the isConstructorOf and
                         * ofObject fields to find the object that we will
                         * need to change.
                         */

                        object = function.getOfObject();
                        if (object == null) {
                            object = function.getIsConstructorOf();
                        }

                        /*
                         * Look for the object and add the new Block.
                         */

                        if (object != null) {
                            toChange = introspected.get(object);

                            if (toChange != null) {
                                toChange.addFunctionBlock(function);
                            }
                        }
                    }
                }

                in.close();
            } catch (IOException ioe) {
                System.out.println("I/O problem when trying to parse " + overriders[i]);
                System.out.println(ioe.getMessage());
                System.out.println("[continuing next file]\n");
                continue;
            } catch (ImproperDefsFileException idfe) {
                System.out.println("Couldn't get sufficient information from " + overriders[i] + ":");
                System.out.println(idfe.getMessage());
                System.out.println("[continuing next file]\n");
                continue;
            } finally {
                System.out.flush();
            }
        }

        /*
         * Add all data from introspection to the list of data to process by
         * the code generator.
         */

        all.addAll(introspected.values());

        /*
         * Now, with the meta data completely loaded, we can generate the
         * bindings code.
         */

        try {
            typeMapping = new PrintWriter(new BufferedWriter(new FileWriter(
                    "generated/bindings/typeMapping.properties")));
        } catch (IOException ie) {
            System.err.println("Can't open typeMapping file for writing!\n" + ie);
            return;
        }

        for (DefsFile data : all) {
            String packageAndClassName;
            File transTarget, jniTarget;
            PrintWriter trans, jni;

            packageAndClassName = data.getType().fullyQualifiedTranslationClassName().replace('.', '/');
            transTarget = new File(outputDir, packageAndClassName + ".java");
            jniTarget = new File(outputDir, packageAndClassName + ".c");

            if (!transTarget.getParentFile().isDirectory()) {
                transTarget.getParentFile().mkdirs();
            }

            try {
                trans = new PrintWriter(new BufferedWriter(new FileWriter(transTarget)));
                jni = new PrintWriter(new BufferedWriter(new FileWriter(jniTarget)));
            } catch (IOException ioe) {
                System.err.println("How come we can't open a file for writing?\n" + ioe);
                return;
            }

            try {
                data.generateTranslationLayer(trans);
            } catch (UnsupportedOperationException uoe) {
                // act to remove that file? Or close it off, or...
            }

            try {
                data.generateJniLayer(jni);
            } catch (UnsupportedOperationException uoe) {
                // act to remove the file in the event there was nothing
                // printed?
            }

            typeMapping.println(data.getType().bareTranslationClassName() + "="
                    + data.getType().fullyQualifiedJavaClassName());

            trans.close();
            jni.close();
        }
        typeMapping.close();
    }
}
