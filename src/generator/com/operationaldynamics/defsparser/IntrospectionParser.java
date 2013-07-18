package com.operationaldynamics.defsparser;

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

/*
 * This code started life as prototype written during the Google Summer of Code
 * 2013. It is based on the code made by Serkan Kaba from an old branch about
 * GObject Introspection. This parser aims to replace the old but well working
 * .defs parser being used by java-gnome for several years. 
 */

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
import nu.xom.ValidityException;

import com.operationaldynamics.driver.DefsFile;
import com.operationaldynamics.driver.ImproperDefsFileException;

/**
 * A .gir file parser: convert XML data into an array of Block objects
 * suitable to be used to instantiate code generators.
 * 
 * <p>
 * Observation: we can only use this parser to parse .gir files containing
 * data in XML.
 * 
 * @author Guillaume Mazoyer
 */
public class IntrospectionParser
{
    public static final String CORE_NAMESPACE;

    public static final String C_NAMESPACE;

    public static final String GLIB_NAMESPACE;

    private File introspectionFile;

    static {
        CORE_NAMESPACE = "http://www.gtk.org/introspection/core/1.0";
        C_NAMESPACE = "http://www.gtk.org/introspection/c/1.0";
        GLIB_NAMESPACE = "http://www.gtk.org/introspection/glib/1.0";
    }

    /**
     * Initialize the parser for a given file containg Introspection data.
     * 
     * @param introspectionFile
     *            a file object that is a reference to a .gir file to be
     *            parsed.
     */
    public IntrospectionParser(final File introspectionFile) {
        this.introspectionFile = introspectionFile;
    }

    /**
     * Return a String representation of the parent of the given object.
     * 
     * @param object
     *            the XML reference to the object.
     * @param module
     *            the name of the module the object belongs to.
     * @return the parent class of the given object,
     */
    private static final String guessParent(Element object, String module) {
        String parent;

        parent = object.getAttributeValue("parent");

        if (parent == null) {
            parent = "";
        } else if (parent.equals("GObject.Object") || parent.equals("GObject.InitiallyUnowned")) {
            /*
             * The parent in a GObject.
             */

            parent = "GObject";
        } else if (parent.contains(".")) {
            /*
             * The parent is in another module (but there are exceptions to
             * handle).
             */

            if (parent.startsWith("Gio.")) {
                parent = parent.replace("Gio.", "G");
            } else {
                parent = parent.replace(".", "");
            }
        } else if (!parent.contains(".")) {
            /*
             * The parent is in the same module so we append the module name.
             */

            if (module.equals("GdkPixbuf")) {
                parent = "Gdk" + parent;
            } else {
                parent = module + parent;
            }
        }

        return parent;
    }

    /**
     * Return the return-type of the given function.
     * 
     * @param function
     *            the XML representation of the function.
     * @return an array containing the return type of the given function.
     */
    private static final String[] getReturnType(Element function) {
        Element type;
        String typeString;

        /*
         * Get the type of the return value.
         */

        type = function.getFirstChildElement("return-value", CORE_NAMESPACE).getFirstChildElement(
                "type", CORE_NAMESPACE);

        if (type == null) {
            /*
             * The return type is an array.
             */

            type = function.getFirstChildElement("return-value", CORE_NAMESPACE).getFirstChildElement(
                    "array", CORE_NAMESPACE);
        }

        typeString = type.getAttributeValue("type", C_NAMESPACE);

        /*
         * Handle the case where the function does not return anything.
         */

        if (typeString == null) {
            typeString = type.getAttributeValue("name");
        } else if (typeString.equals("void")) {
            typeString = "none";
        }

        return new String[] {
            "return-type",
            typeString.replace(" ", "-")
        };
    }

    /**
     * Return whether the caller owns the return of the function.
     * 
     * @param function
     *            the XML representation of the function.
     * @return an array containing the definition of the ownership transfer or
     *         null.
     */
    private static final String[] getCallerOwnsReturn(Element function) {
        final String callerOwnsReturn;

        callerOwnsReturn = function.getFirstChildElement("return-value", CORE_NAMESPACE)
                .getAttributeValue("transfer-ownership");

        return ((callerOwnsReturn != null) && callerOwnsReturn.equals("full") ? new String[] {
            "caller-owns-return",
            "#t"
        } : null);
    }

    /**
     * Check if the function has var args.
     * 
     * @param function
     *            the XML representation of the function.
     * @return a boolean value telling if there are var args (true) or not
     *         (false).
     */
    private static final boolean hasVarArgs(Element function) {
        return (function.toXML().indexOf("varargs") > 0);
    }

    /**
     * Return a list containing all the parameters for the given function.
     * 
     * @param function
     *            the XML representation of the function.
     * @return a list of String array, one String array contains the name, the
     *         type of a parameter and if it can be null.
     */
    private static final List<String[]> getParameters(Element function) {
        final List<String[]> parameters;
        final Element element;
        final Elements list;

        parameters = new ArrayList<String[]>();
        element = function.getFirstChildElement("parameters", CORE_NAMESPACE);

        if (element == null) {
            return parameters;
        }

        list = element.getChildElements("parameter", CORE_NAMESPACE);

        /*
         * Get all parameters for the given function.
         */

        for (int parameterIndex = 0; parameterIndex < list.size(); parameterIndex++) {
            final Element parameter;
            final String name;
            Element type;
            String typeString;

            parameter = list.get(parameterIndex);
            name = parameter.getAttributeValue("name");

            if (name != null) {
                type = parameter.getFirstChildElement("type", CORE_NAMESPACE);

                /*
                 * If the parameter is an array go deeper.
                 */

                if (type == null) {
                    type = parameter.getFirstChildElement("array", CORE_NAMESPACE);
                }

                /*
                 * This is not a useful parameter for us.
                 */

                if (type == null) {
                    continue;
                }

                if (type.getAttributeValue("type", C_NAMESPACE) == null) {
                    typeString = type.getAttributeValue("name").replace(".", "");
                } else {
                    typeString = type.getAttributeValue("type", C_NAMESPACE).replace(" ", "-");
                }

                /*
                 * Add the parameter to the parameters list and tell if 'null'
                 * can be used.
                 */

                parameters.add(new String[] {
                    typeString,
                    name,
                    ((parameter.getAttributeValue("allow-none") != null)
                            && parameter.getAttributeValue("allow-none").equals("1") ? "yes" : "no")
                });
            }
        }

        return parameters;
    }

    /**
     * Run the parser across the XML data and return a map of DefsFile objects
     * representing the blocks found there.
     * 
     * @return a map of DefsFile objects identified by a String which is the
     *         name of the object.
     * @throws ParsingException
     *             if an error occurs while parsing the XML file.
     * @throws ValidityException
     *             if the XML file does not seem valid.
     * @throws IOException
     *             if the XML file cannot be read.
     */
    public Map<String, DefsFile> parseData() throws ValidityException, ParsingException, IOException {
        final Map<String, DefsFile> defs;
        final Builder builder;
        Document document;
        Element repository;
        Elements namespaces;

        defs = new HashMap<String, DefsFile>();
        builder = new Builder();
        document = null;

        /*
         * Start the parsing of the XML data.
         */

        document = builder.build(this.introspectionFile);

        /*
         * Get the first elements that are available (namespaces).
         */

        repository = document.getRootElement();
        namespaces = repository.getChildElements("namespace", CORE_NAMESPACE);

        /*
         * For each namespace go deep.
         */

        for (int namespaceIndex = 0; namespaceIndex < namespaces.size(); namespaceIndex++) {
            final Element namespace;
            final Elements objects, interfaces, enumerations, flags, boxeds;
            final String namespaceName;

            /*
             * Retrieve namespace attributes and what it contains (classes and
             * stuff).
             */

            namespace = namespaces.get(namespaceIndex);
            namespaceName = namespace.getAttributeValue("name");
            objects = namespace.getChildElements("class", CORE_NAMESPACE);
            interfaces = namespace.getChildElements("interface", CORE_NAMESPACE);
            enumerations = namespace.getChildElements("enumeration", CORE_NAMESPACE);
            flags = namespace.getChildElements("bitfield", CORE_NAMESPACE);
            boxeds = namespace.getChildElements("record", CORE_NAMESPACE);

            /*
             * Examine each class.
             */

            for (int objectIndex = 0; objectIndex < objects.size(); objectIndex++) {
                final Element object;
                final Elements constructors, methods, signals;
                final List<Block> blocks;
                final List<String[]> characteristics;

                object = objects.get(objectIndex);
                blocks = new ArrayList<Block>();
                characteristics = new ArrayList<String[]>();

                /*
                 * Retrieve constructors, methods and signals.
                 */

                constructors = object.getChildElements("constructor", CORE_NAMESPACE);
                methods = object.getChildElements("method", CORE_NAMESPACE);
                signals = object.getChildElements("virtual-method", CORE_NAMESPACE);

                /*
                 * Get object first characteristics: module it belongs to and
                 * its C name.
                 */

                characteristics.add(new String[] {
                    "in-module",
                    namespaceName
                });
                characteristics.add(new String[] {
                    "parent",
                    guessParent(object, namespaceName)
                });
                characteristics.add(new String[] {
                    "c-name",
                    object.getAttributeValue("type", C_NAMESPACE) == null ? object.getAttributeValue(
                            "type-name", GLIB_NAMESPACE) : object.getAttributeValue("type", C_NAMESPACE)
                });

                System.out.println(object.getAttributeValue("type", C_NAMESPACE) + " <-- "
                        + guessParent(object, namespaceName) + " | "
                        + object.getAttributeValue("parent"));

                /*
                 * Build the object blocks based on the info we have.
                 */

                blocks.add(new ObjectBlock(object.getAttributeValue("name"), characteristics, null));

                /*
                 * Parse all constructors data.
                 */

                for (int constructorIndex = 0; constructorIndex < constructors.size(); constructorIndex++) {
                    final Element constructor;
                    final List<String[]> constructorCharacteristics;
                    final String[] callerOwnsReturn;

                    constructor = constructors.get(constructorIndex);
                    constructorCharacteristics = new ArrayList<String[]>();

                    /*
                     * Get the class that this constructor belongs to and the
                     * C name of this constructor.
                     */

                    constructorCharacteristics.add(new String[] {
                        "is-constructor-of",
                        object.getAttributeValue("type", C_NAMESPACE)
                    });
                    constructorCharacteristics.add(new String[] {
                        "c-name",
                        constructor.getAttributeValue("identifier", C_NAMESPACE)
                    });

                    /*
                     * Handle return value and its owner.
                     */

                    constructorCharacteristics.add(getReturnType(constructor));
                    callerOwnsReturn = getCallerOwnsReturn(constructor);

                    if (callerOwnsReturn != null) {
                        constructorCharacteristics.add(callerOwnsReturn);
                    }

                    if (hasVarArgs(constructor)) {
                        constructorCharacteristics.add(new String[] {
                            "varargs",
                            "#t"
                        });
                    }

                    blocks.add(new FunctionBlock(
                            constructor.getAttributeValue("identifier", C_NAMESPACE),
                            constructorCharacteristics, getParameters(constructor)));
                }

                /*
                 * Parse all methods data.
                 */

                for (int methodIndex = 0; methodIndex < methods.size(); methodIndex++) {
                    final Element method;
                    final List<String[]> methodCharacteristics;
                    final String[] callerOwnsReturn;
                    String ofObject;

                    method = methods.get(methodIndex);
                    methodCharacteristics = new ArrayList<String[]>();

                    /*
                     * Get the class that this method belongs to and the C
                     * name of this method.
                     */

                    ofObject = object.getAttributeValue("type", C_NAMESPACE);

                    if (ofObject == null) {
                        ofObject = object.getAttributeValue("type-name", GLIB_NAMESPACE);
                    }

                    methodCharacteristics.add(new String[] {
                        "of-object",
                        ofObject
                    });
                    methodCharacteristics.add(new String[] {
                        "c-name",
                        method.getAttributeValue("identifier", C_NAMESPACE)
                    });

                    /*
                     * Handle return value and its owner.
                     */

                    methodCharacteristics.add(getReturnType(method));
                    callerOwnsReturn = getCallerOwnsReturn(method);

                    if (callerOwnsReturn != null) {
                        methodCharacteristics.add(callerOwnsReturn);
                    }

                    if (hasVarArgs(method)) {
                        methodCharacteristics.add(new String[] {
                            "varargs",
                            "#t"
                        });
                    }

                    blocks.add(new MethodBlock(method.getAttributeValue("name"), methodCharacteristics,
                            getParameters(method)));
                }

                /*
                 * Parse all signals data.
                 */

                for (int signalIndex = 0; signalIndex < signals.size(); signalIndex++) {
                    final Element signal;
                    final List<String[]> signalCharacteristics;

                    signal = signals.get(signalIndex);
                    signalCharacteristics = new ArrayList<String[]>();

                    /*
                     * Get the class that this method belongs to.
                     */

                    signalCharacteristics.add(new String[] {
                        "of-object",
                        object.getAttributeValue("type", C_NAMESPACE)
                    });

                    /*
                     * Handle return value.
                     */

                    signalCharacteristics.add(getReturnType(signal));

                    blocks.add(new VirtualBlock(signal.getAttributeValue("name").replace("-", "_"),
                            signalCharacteristics, getParameters(signal)));
                }

                try {
                    /*
                     * Generate the defs file definition for the given object.
                     */

                    defs.put(object.getAttributeValue("name"),
                            new DefsFile(blocks.toArray(new Block[0])));
                } catch (ImproperDefsFileException e) {
                    e.printStackTrace();
                }
            }

            /*
             * Examine each interface.
             */

            for (int interfaceIndex = 0; interfaceIndex < interfaces.size(); interfaceIndex++) {
                final Element interfaze;
                final Elements methods, signals;
                final List<Block> blocks;
                final List<String[]> characteristics;

                interfaze = interfaces.get(interfaceIndex);
                blocks = new ArrayList<Block>();
                characteristics = new ArrayList<String[]>();

                /*
                 * Retrieve constructors, methods and signals.
                 */

                methods = interfaze.getChildElements("method", CORE_NAMESPACE);
                signals = interfaze.getChildElements("virtual-method", CORE_NAMESPACE);

                /*
                 * Get object first characteristics: module it belongs to and
                 * its C name.
                 */

                characteristics.add(new String[] {
                    "in-module",
                    namespaceName
                });
                characteristics.add(new String[] {
                    "c-name",
                    interfaze.getAttributeValue("type", C_NAMESPACE)
                });

                /*
                 * Build the object blocks based on the info we have.
                 */

                blocks.add(new InterfaceBlock(interfaze.getAttributeValue("name"), characteristics));

                /*
                 * Parse all methods data.
                 */

                for (int methodIndex = 0; methodIndex < methods.size(); methodIndex++) {
                    final Element method;
                    final List<String[]> methodCharacteristics;
                    final String[] callerOwnsReturn;

                    method = methods.get(methodIndex);
                    methodCharacteristics = new ArrayList<String[]>();

                    /*
                     * Get the class that this method belongs to and the C
                     * name of this method.
                     */

                    methodCharacteristics.add(new String[] {
                        "of-object",
                        interfaze.getAttributeValue("type", C_NAMESPACE)
                    });
                    methodCharacteristics.add(new String[] {
                        "c-name",
                        method.getAttributeValue("identifier", C_NAMESPACE)
                    });

                    /*
                     * Handle return value and its owner.
                     */

                    methodCharacteristics.add(getReturnType(method));
                    callerOwnsReturn = getCallerOwnsReturn(method);

                    if (callerOwnsReturn != null) {
                        methodCharacteristics.add(callerOwnsReturn);
                    }

                    if (hasVarArgs(method)) {
                        methodCharacteristics.add(new String[] {
                            "varargs",
                            "#t"
                        });
                    }

                    blocks.add(new MethodBlock(method.getAttributeValue("name"), methodCharacteristics,
                            getParameters(method)));
                }

                /*
                 * Parse all signals data.
                 */

                for (int signalIndex = 0; signalIndex < signals.size(); signalIndex++) {
                    final Element signal;
                    final List<String[]> signalCharacteristics;

                    signal = signals.get(signalIndex);
                    signalCharacteristics = new ArrayList<String[]>();

                    /*
                     * Get the class that this method belongs to.
                     */

                    signalCharacteristics.add(new String[] {
                        "of-object",
                        interfaze.getAttributeValue("type", C_NAMESPACE)
                    });

                    /*
                     * Handle return value.
                     */

                    signalCharacteristics.add(getReturnType(signal));

                    blocks.add(new VirtualBlock(signal.getAttributeValue("name").replace("-", "_"),
                            signalCharacteristics, getParameters(signal)));
                }

                try {
                    /*
                     * Generate the defs file definition for the given object.
                     */

                    defs.put(interfaze.getAttributeValue("name"),
                            new DefsFile(blocks.toArray(new Block[0])));
                } catch (ImproperDefsFileException e) {
                    e.printStackTrace();
                }
            }

            /*
             * Examine each enumeration.
             */

            for (int enumerationIndex = 0; enumerationIndex < enumerations.size(); enumerationIndex++) {
                final Element enumeration;
                final Elements valuesList;
                final List<Block> blocks;
                final List<String[]> characteristics, values;

                enumeration = enumerations.get(enumerationIndex);
                valuesList = enumeration.getChildElements("member", CORE_NAMESPACE);
                blocks = new ArrayList<Block>();
                characteristics = new ArrayList<String[]>();
                values = new ArrayList<String[]>();

                /*
                 * Get object first characteristics: module it belongs to and
                 * its C name.
                 */

                characteristics.add(new String[] {
                    "in-module",
                    namespaceName
                });
                characteristics.add(new String[] {
                    "c-name",
                    enumeration.getAttributeValue("type", C_NAMESPACE)
                });

                for (int valueIndex = 0; valueIndex < valuesList.size(); valueIndex++) {
                    final Element value;
                    String nick;

                    value = valuesList.get(valueIndex);
                    nick = value.getAttributeValue("nick", GLIB_NAMESPACE);

                    if (nick == null) {
                        nick = value.getAttributeValue("name");
                    }

                    values.add(new String[] {
                        nick,
                        value.getAttributeValue("identifier", C_NAMESPACE)
                    });
                }

                blocks.add(new EnumBlock(enumeration.getAttributeValue("name"), characteristics, values));

                try {
                    /*
                     * Generate the defs file definition for the given object.
                     */

                    defs.put(enumeration.getAttributeValue("name"),
                            new DefsFile(blocks.toArray(new Block[0])));
                } catch (ImproperDefsFileException e) {
                    e.printStackTrace();
                }
            }

            /*
             * Examine each flags.
             */

            for (int flagIndex = 0; flagIndex < flags.size(); flagIndex++) {
                final Element flag;
                final Elements valuesList;
                final List<Block> blocks;
                final List<String[]> characteristics, values;

                flag = flags.get(flagIndex);
                valuesList = flag.getChildElements("member", CORE_NAMESPACE);
                blocks = new ArrayList<Block>();
                characteristics = new ArrayList<String[]>();
                values = new ArrayList<String[]>();

                /*
                 * Get object first characteristics: module it belongs to and
                 * its C name.
                 */

                characteristics.add(new String[] {
                    "in-module",
                    namespaceName
                });
                characteristics.add(new String[] {
                    "c-name",
                    flag.getAttributeValue("type", C_NAMESPACE)
                });

                for (int valueIndex = 0; valueIndex < valuesList.size(); valueIndex++) {
                    final Element value;
                    String valueName;

                    value = valuesList.get(valueIndex);
                    valueName = value.getAttributeValue("nick", GLIB_NAMESPACE);

                    if (valueName == null) {
                        valueName = value.getAttributeValue("name");
                    }

                    if (valueName == null) {
                        continue;
                    }

                    values.add(new String[] {
                        valueName,
                        value.getAttributeValue("identifier", C_NAMESPACE)
                    });
                }

                if (values.size() > 0) {
                    blocks.add(new FlagsBlock(flag.getAttributeValue("name"), characteristics, values));

                    try {
                        /*
                         * Generate the defs file definition for the given
                         * object.
                         */

                        defs.put(flag.getAttributeValue("name"),
                                new DefsFile(blocks.toArray(new Block[0])));
                    } catch (ImproperDefsFileException e) {
                        e.printStackTrace();
                    }
                }
            }

            /*
             * Examine each boxed.
             */

            for (int boxedIndex = 0; boxedIndex < boxeds.size(); boxedIndex++) {
                final Element boxed;
                final Elements methods;
                final List<Block> blocks;
                final List<String[]> characteristics;
                final String boxedName;

                boxed = boxeds.get(boxedIndex);
                boxedName = boxed.getAttributeValue("name");

                /*
                 * If this boxed thing is internal just ignore it.
                 */

                if (boxedName.endsWith("Class") || boxedName.endsWith("Private")) {
                    continue;
                }

                methods = boxed.getChildElements("method", CORE_NAMESPACE);
                blocks = new ArrayList<Block>();
                characteristics = new ArrayList<String[]>();

                /*
                 * Get object first characteristics: module it belongs to and
                 * its C name.
                 */

                characteristics.add(new String[] {
                    "in-module",
                    namespaceName
                });
                characteristics.add(new String[] {
                    "c-name",
                    boxed.getAttributeValue("type", C_NAMESPACE)
                });

                blocks.add(new BoxedBlock(boxedName, characteristics));

                /*
                 * Retrieve all info about methods.
                 */

                for (int methodIndex = 0; methodIndex < methods.size(); methodIndex++) {
                    final Element method;
                    final List<String[]> methodCharacteristics;
                    final String[] callerOwnsReturn;

                    method = methods.get(methodIndex);
                    methodCharacteristics = new ArrayList<String[]>();

                    /*
                     * Get the boxed that this method belongs to and the C
                     * name of this method.
                     */

                    methodCharacteristics.add(new String[] {
                        "of-object",
                        boxed.getAttributeValue("type", C_NAMESPACE)
                    });
                    methodCharacteristics.add(new String[] {
                        "c-name",
                        method.getAttributeValue("identifier", C_NAMESPACE)
                    });

                    /*
                     * Handle return value and its owner.
                     */

                    methodCharacteristics.add(getReturnType(method));
                    callerOwnsReturn = getCallerOwnsReturn(method);

                    if (callerOwnsReturn != null) {
                        methodCharacteristics.add(callerOwnsReturn);
                    }

                    if (hasVarArgs(method)) {
                        methodCharacteristics.add(new String[] {
                            "varargs",
                            "#t"
                        });
                    }

                    blocks.add(new MethodBlock(method.getAttributeValue("name"), methodCharacteristics,
                            getParameters(method)));
                }

                try {
                    /*
                     * Generate the defs file definition for the given boxed.
                     */

                    defs.put(boxedName, new DefsFile(blocks.toArray(new Block[0])));
                } catch (ImproperDefsFileException e) {
                    e.printStackTrace();
                }
            }
        }

        return defs;
    }
}
