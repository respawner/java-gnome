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

/*
 * This code started life as prototype written during the Google Summer of Code
 * 2013. It is based on the code made by Serkan Kaba from an old branch about
 * GObject Introspection. This parser aims to replace the old but well working
 * .defs parser being used by java-gnome for several years. 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

    private static final String[] modules;

    private static final Properties packageOverrides;

    private static final Properties nameOverrides;

    private static final List<String> blacklist;

    private File introspectionFile;

    static {
        CORE_NAMESPACE = "http://www.gtk.org/introspection/core/1.0";
        C_NAMESPACE = "http://www.gtk.org/introspection/c/1.0";
        GLIB_NAMESPACE = "http://www.gtk.org/introspection/glib/1.0";

        modules = new String[] {
            "Atk",
            "Gdk",
            "Gtk",
            "G",
            "Notify",
            "Pango",
            "Rsvg"
        };

        packageOverrides = new Properties();
        try {
            packageOverrides.load(new FileInputStream("src/generator/packages-override.properties"));
        } catch (IOException e) {
            System.err.println("How come we can't open a file for reading?\n" + e);
        }

        nameOverrides = new Properties();
        try {
            nameOverrides.load(new FileInputStream("src/generator/names-override.properties"));
        } catch (IOException e) {
            System.err.println("How come we can't open a file for reading?\n" + e);
        }

        blacklist = new ArrayList<String>();
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
     * Load the whitelist of objects/enumerations/flags/boxeds/interfaces that
     * we want to use.
     */
    private static final List<String> loadWhitelist() {
        final List<String> whitelist;
        final BufferedReader reader;
        String line;

        whitelist = new ArrayList<String>();

        try {
            reader = new BufferedReader(new FileReader("src/generator/whitelist.txt"));

            while ((line = reader.readLine()) != null) {
                whitelist.add(line);
            }

            reader.close();
        } catch (IOException e) {
            System.err.println("How come we can't open a file for reading?\n" + e);
        }

        return whitelist;
    }

    /**
     * Browse all the Introspection data to find gtypes to blacklist (because
     * we don't handle them yet).
     * 
     * @param namespaces
     *            the Introspection namespaces to browse.
     */
    private static final void loadBlacklist(Elements namespaces) {
        final List<String> whitelist;

        whitelist = loadWhitelist();

        for (int namespaceIndex = 0; namespaceIndex < namespaces.size(); namespaceIndex++) {
            final Element namespace;
            final Elements objects, interfaces, enumerations, flags, boxeds, unions;

            namespace = namespaces.get(namespaceIndex);
            objects = namespace.getChildElements("class", CORE_NAMESPACE);
            interfaces = namespace.getChildElements("interface", CORE_NAMESPACE);
            enumerations = namespace.getChildElements("enumeration", CORE_NAMESPACE);
            flags = namespace.getChildElements("bitfield", CORE_NAMESPACE);
            boxeds = namespace.getChildElements("record", CORE_NAMESPACE);
            unions = namespace.getChildElements("union", CORE_NAMESPACE);

            for (int objectIndex = 0; objectIndex < objects.size(); objectIndex++) {
                final Element object;
                final String cName;

                object = objects.get(objectIndex);
                cName = (object.getAttributeValue("type", C_NAMESPACE) != null) ? object.getAttributeValue(
                        "type", C_NAMESPACE) : object.getAttributeValue("type-name", GLIB_NAMESPACE);

                if (!whitelist.contains(cName)) {
                    blacklist.add(cName);
                    continue;
                }
            }

            for (int interfaceIndex = 0; interfaceIndex < interfaces.size(); interfaceIndex++) {
                final Element interfaze;
                final String cName;

                interfaze = interfaces.get(interfaceIndex);
                cName = (interfaze.getAttributeValue("type", C_NAMESPACE) != null) ? interfaze.getAttributeValue(
                        "type", C_NAMESPACE) : interfaze.getAttributeValue("type-name", GLIB_NAMESPACE);

                if (!whitelist.contains(cName)) {
                    blacklist.add(cName);
                    continue;
                }

            }

            for (int enumerationIndex = 0; enumerationIndex < enumerations.size(); enumerationIndex++) {
                final Element enumeration;
                final String cName;

                enumeration = enumerations.get(enumerationIndex);
                cName = (enumeration.getAttributeValue("type", C_NAMESPACE) != null) ? enumeration.getAttributeValue(
                        "type", C_NAMESPACE)
                        : enumeration.getAttributeValue("type-name", GLIB_NAMESPACE);

                if (!whitelist.contains(cName)) {
                    blacklist.add(cName);
                    continue;
                }
            }

            for (int flagIndex = 0; flagIndex < flags.size(); flagIndex++) {
                final Element flag;
                final String cName;

                flag = flags.get(flagIndex);
                cName = (flag.getAttributeValue("type", C_NAMESPACE) != null) ? flag.getAttributeValue(
                        "type", C_NAMESPACE) : flag.getAttributeValue("type-name", GLIB_NAMESPACE);

                if (!whitelist.contains(cName)) {
                    blacklist.add(cName);
                    continue;
                }
            }

            for (int boxedIndex = 0; boxedIndex < boxeds.size(); boxedIndex++) {
                final Element boxed;
                final String cName;

                boxed = boxeds.get(boxedIndex);
                cName = (boxed.getAttributeValue("type", C_NAMESPACE) != null) ? boxed.getAttributeValue(
                        "type", C_NAMESPACE) : boxed.getAttributeValue("type-name", GLIB_NAMESPACE);

                if (!whitelist.contains(cName)) {
                    blacklist.add(cName);
                    continue;
                }
            }

            for (int unionIndex = 0; unionIndex < unions.size(); unionIndex++) {
                final Element union;
                final String cName;

                union = unions.get(unionIndex);
                cName = (union.getAttributeValue("type", C_NAMESPACE) != null) ? union.getAttributeValue(
                        "type", C_NAMESPACE) : union.getAttributeValue("type-name", GLIB_NAMESPACE);

                if (!whitelist.contains(cName)) {
                    blacklist.add(cName);
                    continue;
                }
            }
        }
    }

    /**
     * Return whether the given C type is blacklisted.
     * 
     * @param gtype
     *            the C type of the object.
     * @return true if the C type is blacklisted.
     */
    private static final boolean isBlacklisted(String gtype) {
        return blacklist.contains(gtype);
    }

    /**
     * Return the Java package name to use for the given namespace.
     * 
     * @param introspectionNamespace
     *            the GObject Introspection namespace.
     * @return the Java package name to use.
     */
    private static final String getActualJavaPackage(String introspectionNamespace) {
        final String javaPackage;

        javaPackage = packageOverrides.getProperty(introspectionNamespace);

        return ((javaPackage == null) ? introspectionNamespace : javaPackage);
    }

    /**
     * Return the Java name to use for the given object name.
     * 
     * @param cType
     *            the C type.
     * @param name
     *            the GObject name.
     * @return the Java name to use.
     */
    private static final String getActualJavaName(String cType, String name) {
        final String javaName;

        javaName = nameOverrides.getProperty(cType);

        return ((javaName == null) ? name : javaName);
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
     * Try to guess the real type for a given type with a name that is not
     * enough for us.
     * 
     * @param type
     *            the type to inspect.
     * @param module
     *            the namespace currently inspected.
     * @return a usable type.
     */
    private static final String guessUsableType(String type, String module) {
        String usable;

        if (type.startsWith("GList-") || type.startsWith("GSList-")) {
            return type.split("-")[0] + "-" + guessUsableType(type.split("-")[1], module) + "*";
        }

        if ((type.length() > 0) && !Character.isUpperCase(type.charAt(0))) {
            /*
             * Not a GObject thing.
             */

            return type;
        }

        if (type.equals("GObject.Object") || type.equals("GObject.InitiallyUnowned")) {
            /*
             * This is a basic GObject.
             */

            return "GObject";
        }

        if (type.equals("GdkPixbuf.Pixbuf")) {
            /*
             * This is a basic GdkPixbuf object.
             */

            return "GdkPixbuf";
        }

        if (type.startsWith("GObject.")) {
            return type.replace("GObject.", "G");
        }

        if (type.startsWith("Gio.")) {
            return type.replace("Gio.", "G");
        }

        for (String m : modules) {
            if (type.startsWith(m)) {
                return type;
            }
        }

        usable = new String(type);

        if (usable.contains(".")) {
            /*
             * The type is in another module (but there are exceptions to
             * handle).
             */

            if (usable.startsWith("Gio.")) {
                return type.replace("Gio.", "G");
            } else {
                return type.replace(".", "");
            }
        }

        if (!usable.contains(".")) {
            /*
             * The type is in the same module so we append the module name.
             */

            if (module.equals("GdkPixbuf")) {
                usable = "Gdk" + type;
            } else {
                usable = module + type;
            }
        } else {
            usable = type;
        }

        return usable;
    }

    /**
     * Return the return-type of the given function.
     * 
     * @param function
     *            the XML representation of the function.
     * @param module
     *            the namespace the function belongs to.
     * @return an array containing the return type of the given function.
     */
    private static final String[] getReturnType(Element function, String module) {
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

            type = function.getFirstChildElement("return-value", CORE_NAMESPACE);

            if (type.getAttributeValue("type", C_NAMESPACE) == null) {
                type = type.getFirstChildElement("array", CORE_NAMESPACE);
            }
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

        /*
         * FIXME: this is ugly.
         */

        if (typeString.startsWith("GList") || typeString.startsWith("GSList")) {
            typeString = typeString.replaceFirst("\\*", "-")
                    + type.getChildElements().get(0).getAttributeValue("name");
            typeString.replace("*", "");
        }

        typeString = guessUsableType(typeString, module);

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
     * @param module
     *            the namespace the function belongs to.
     * @return a list of String array, one String array contains the name, the
     *         type of a parameter and if it can be null.
     */
    private static final List<String[]> getParameters(Element function, String module) {
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
            Element type;
            String name, typeString;

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
                 * FIXME: this is ugly.
                 */

                if (typeString.startsWith("GList") || typeString.startsWith("GSList")) {
                    typeString = typeString.replaceFirst("\\*", "-")
                            + type.getChildElements().get(0).getAttributeValue("name");
                    typeString.replace("*", "");
                }

                typeString = guessUsableType(typeString, module);

                /*
                 * Strange case where gconstpointer is given as parameter
                 * which is a pointer to an object that cannot be modified.
                 */

                if (typeString.equals("gconstpointer")) {
                    typeString = guessUsableType(type.getAttributeValue("name").replace(".", ""), module)
                            + "*";
                }

                /*
                 * FIXME: In signal we already use the "handler" and "result"
                 * variables so we need to generate another name (basically we
                 * just append the parameter number).
                 */

                if ((function.getQualifiedName().equals("virtual-method") || function.getQualifiedName()
                        .equals("signal")) && (name.equals("handler") || name.equals("result"))) {
                    name += parameterIndex;
                }

                /*
                 * Add the parameter to the parameters list and tell if 'null'
                 * can be used.
                 */

                parameters.add(new String[] {
                    typeString,
                    name,
                    (parameter.getAttributeValue("allow-none") != null) ? "yes" : "no"
                });
            }
        }

        return parameters;
    }

    /**
     * Return a FunctionBlock which represents the constructor of an object
     * based on what we have found in the .gir file.
     * 
     * @param object
     *            the XML representation of the object the constructor belongs
     *            to.
     * @param constructor
     *            the XML representation of the constructor.
     * @param namespace
     *            the namespace that we are inspecting.
     * @return a block usable by the code generator.
     */
    private static final FunctionBlock parseConstructor(Element object, Element constructor,
            String namespace) {
        final List<String[]> constructorCharacteristics;
        final String[] callerOwnsReturn;

        constructorCharacteristics = new ArrayList<String[]>();

        /*
         * Get the class that this constructor belongs to and the C name of
         * this constructor.
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

        constructorCharacteristics.add(getReturnType(constructor, namespace));
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

        return new FunctionBlock(constructor.getAttributeValue("identifier", C_NAMESPACE),
                constructorCharacteristics, getParameters(constructor, namespace));
    }

    /**
     * Return a FunctionBlock which represents the function of an object based
     * on what we have found in the .gir file.
     * 
     * @param object
     *            the XML representation of the object the constructor belongs
     *            to.
     * @param function
     *            the XML representation of the function.
     * @param namespace
     *            the namespace that we are inspecting.
     * @return a block usable by the code generator.
     */
    private static final FunctionBlock parseFunction(Element object, Element function, String namespace) {
        final List<String[]> functionCharacteristics;
        final String[] callerOwnsReturn;

        functionCharacteristics = new ArrayList<String[]>();

        /*
         * Get the class that this method belongs to and the C name of this
         * method.
         */

        functionCharacteristics.add(new String[] {
            "of-object",
            object.getAttributeValue("type", C_NAMESPACE)
        });
        functionCharacteristics.add(new String[] {
            "c-name",
            function.getAttributeValue("identifier", C_NAMESPACE)
        });

        /*
         * Handle return value and its owner.
         */

        functionCharacteristics.add(getReturnType(function, namespace));
        callerOwnsReturn = getCallerOwnsReturn(function);

        if (callerOwnsReturn != null) {
            functionCharacteristics.add(callerOwnsReturn);
        }

        if (hasVarArgs(function)) {
            functionCharacteristics.add(new String[] {
                "varargs",
                "#t"
            });
        }

        return new FunctionBlock(function.getAttributeValue("name"), functionCharacteristics,
                getParameters(function, namespace));
    }

    /**
     * Return a MethodBlock which represents the method of an object based on
     * what we have found in the .gir file.
     * 
     * @param object
     *            the XML representation of the object the method belongs to.
     * @param method
     *            the XML representation of the method.
     * @param objectCharacteristics
     *            the list of the of the object this method belongs to.
     * @param namespace
     *            the namespace that we are inspecting.
     * @return a block usable by the code generator.
     */
    private static final MethodBlock parseMethod(Element object, Element method,
            List<String[]> objectCharacteristics, String namespace) throws IllegalStateException {
        final List<String[]> methodCharacteristics;
        final String[] callerOwnsReturn;
        String ofObject;

        if (method.getAttributeValue("name").startsWith("_")) {
            throw new IllegalStateException(
                    "The method name starts with an _ which is an illegal character.");
        }

        methodCharacteristics = new ArrayList<String[]>();

        /*
         * Get the class that this method belongs to and the C name of this
         * method.
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
         * Handle specials methods to copy and release resources.
         */

        if (method.getAttributeValue("name").equals("copy")) {
            objectCharacteristics.add(new String[] {
                "copy-func",
                method.getAttributeValue("identifier", C_NAMESPACE)
            });
        }

        if (method.getAttributeValue("name").equals("free")
                || method.getAttributeValue("name").equals("destroy")) {
            objectCharacteristics.add(new String[] {
                "release-func",
                method.getAttributeValue("identifier", C_NAMESPACE)
            });
        }

        /*
         * Handle return value and its owner.
         */

        methodCharacteristics.add(getReturnType(method, namespace));
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

        return new MethodBlock(method.getAttributeValue("name"), methodCharacteristics, getParameters(
                method, namespace));
    }

    /**
     * Return a VirtualBlock which represents the virtual method of an object
     * based on what we have found in the .gir file.
     * 
     * @param object
     *            the XML representation of the object the method belongs to.
     * @param virtual
     *            the XML representation of the virtual method.
     * @param namespace
     *            the namespace that we are inspecting.
     * @param signalNames
     *            a list of signal names already known to avoid duplicates.
     * @return a block usable by the code generator.
     */
    private static final VirtualBlock parseVirtual(Element object, Element virtual, String namespace,
            List<String> signalNames) {
        final List<String[]> virtualCharacteristics;
        final String virtualName;

        virtualName = virtual.getAttributeValue("name").replace("-", "_");
        virtualCharacteristics = new ArrayList<String[]>();

        /*
         * We need a list of signals names to avoid duplicates between signals
         * and virtuals.
         */

        signalNames.add(virtualName);

        /*
         * Get the class that this method belongs to.
         */

        virtualCharacteristics.add(new String[] {
            "of-object",
            object.getAttributeValue("type", C_NAMESPACE)
        });

        /*
         * Handle return value.
         */

        virtualCharacteristics.add(getReturnType(virtual, namespace));

        return new VirtualBlock(virtualName, virtualCharacteristics, getParameters(virtual, namespace));
    }

    /**
     * Return a VirtualBlock which represents the signal of an object based on
     * what we have found in the .gir file.
     * 
     * @param object
     *            the XML representation of the object the method belongs to.
     * @param signal
     *            the XML representation of the signal method.
     * @param namespace
     *            the namespace that we are inspecting.
     * @param signalNames
     *            a list of signal names already known to avoid duplicates.
     * @return a block usable by the code generator.
     */
    private static final VirtualBlock parseSignal(Element object, Element signal, String namespace,
            List<String> signalNames) throws IllegalStateException {
        final List<String[]> signalCharacteristics;
        final String signalName;

        signalName = signal.getAttributeValue("name").replace("-", "_");
        signalCharacteristics = new ArrayList<String[]>();

        /*
         * Signal already handled with a virtual method.
         */

        if (signalNames.contains(signalName)) {
            throw new IllegalStateException("The signal has already been handled.");
        }

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

        signalCharacteristics.add(getReturnType(signal, namespace));

        return new VirtualBlock(signalName, signalCharacteristics, getParameters(signal, namespace));
    }

    /**
     * Run the parser across the XML data and return a map of DefsFile objects
     * representing the blocks found there.
     * 
     * @return a list of DefsFile objects.
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
            final Elements objects, interfaces, enumerations, flags, boxeds, unions;
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
            unions = namespace.getChildElements("union", CORE_NAMESPACE);

            /*
             * First pass, blacklist what need to be blacklisted.
             */

            loadBlacklist(namespaces);

            /*
             * Examine each class.
             */

            for (int objectIndex = 0; objectIndex < objects.size(); objectIndex++) {
                final Element object;
                final Elements constructors, methods, functions, virtuals, signals;
                final List<Block> blocks;
                final List<String[]> characteristics;
                final List<String> signalNames;
                final String cName;
                final boolean introspectable;

                object = objects.get(objectIndex);
                blocks = new ArrayList<Block>();
                characteristics = new ArrayList<String[]>();
                signalNames = new ArrayList<String>();
                cName = (object.getAttributeValue("type", C_NAMESPACE) != null) ? object.getAttributeValue(
                        "type", C_NAMESPACE) : object.getAttributeValue("type-name", GLIB_NAMESPACE);
                introspectable = (object.getAttribute("foreign") == null);

                if (isBlacklisted(cName) || !introspectable) {
                    continue;
                }

                /*
                 * Retrieve constructors, methods and signals.
                 */

                constructors = object.getChildElements("constructor", CORE_NAMESPACE);
                methods = object.getChildElements("method", CORE_NAMESPACE);
                functions = object.getChildElements("function", CORE_NAMESPACE);
                virtuals = object.getChildElements("virtual-method", CORE_NAMESPACE);
                signals = object.getChildElements("signal", GLIB_NAMESPACE);

                /*
                 * Get object first characteristics: module it belongs to and
                 * its C name.
                 */

                characteristics.add(new String[] {
                    "in-module",
                    getActualJavaPackage(namespaceName)
                });
                characteristics.add(new String[] {
                    "parent",
                    guessParent(object, namespaceName)
                });
                characteristics.add(new String[] {
                    "c-name",
                    cName
                });

                /*
                 * System.out.println(object.getAttributeValue("type",
                 * C_NAMESPACE) + " <-- " + guessParent(object, namespaceName)
                 * + " | " + object.getAttributeValue("parent"));
                 */

                /*
                 * Build the object blocks based on the info we have.
                 */

                blocks.add(new ObjectBlock(getActualJavaName(cName, object.getAttributeValue("name")),
                        characteristics, null));

                /*
                 * Parse all constructors data.
                 */

                for (int constructorIndex = 0; constructorIndex < constructors.size(); constructorIndex++) {
                    blocks.add(parseConstructor(object, constructors.get(constructorIndex),
                            namespaceName));
                }

                /*
                 * Parse all methods data.
                 */

                for (int methodIndex = 0; methodIndex < methods.size(); methodIndex++) {
                    blocks.add(parseMethod(object, methods.get(methodIndex), characteristics,
                            namespaceName));
                }

                /*
                 * Parse all functions data.
                 */

                for (int functionIndex = 0; functionIndex < functions.size(); functionIndex++) {
                    blocks.add(parseFunction(object, functions.get(functionIndex), namespaceName));
                }

                /*
                 * Parse all virtuals data.
                 */

                for (int virtualIndex = 0; virtualIndex < virtuals.size(); virtualIndex++) {
                    blocks.add(parseVirtual(object, virtuals.get(virtualIndex), namespaceName,
                            signalNames));
                }

                /*
                 * Parse all signals data.
                 */

                for (int signalIndex = 0; signalIndex < signals.size(); signalIndex++) {
                    try {
                        blocks.add(parseSignal(object, signals.get(signalIndex), namespaceName,
                                signalNames));
                    } catch (IllegalStateException e) {
                        continue;
                    }
                }

                try {
                    /*
                     * Generate the defs file definition for the given object.
                     */

                    defs.put(cName, new DefsFile(blocks.toArray(new Block[blocks.size()])));
                } catch (ImproperDefsFileException e) {
                    e.printStackTrace();
                }
            }

            /*
             * Examine each interface.
             */

            for (int interfaceIndex = 0; interfaceIndex < interfaces.size(); interfaceIndex++) {
                final Element interfaze;
                final Elements methods, functions, virtuals, signals;
                final List<Block> blocks;
                final List<String[]> characteristics;
                final List<String> signalNames;
                final String cName;
                final boolean introspectable;

                interfaze = interfaces.get(interfaceIndex);
                blocks = new ArrayList<Block>();
                characteristics = new ArrayList<String[]>();
                signalNames = new ArrayList<String>();
                cName = (interfaze.getAttributeValue("type", C_NAMESPACE) != null) ? interfaze.getAttributeValue(
                        "type", C_NAMESPACE) : interfaze.getAttributeValue("type-name", GLIB_NAMESPACE);
                introspectable = (interfaze.getAttribute("foreign") == null);

                if (isBlacklisted(cName) || !introspectable) {
                    continue;
                }

                /*
                 * Retrieve constructors, methods and signals.
                 */

                methods = interfaze.getChildElements("method", CORE_NAMESPACE);
                functions = interfaze.getChildElements("function", CORE_NAMESPACE);
                virtuals = interfaze.getChildElements("virtual-method", CORE_NAMESPACE);
                signals = interfaze.getChildElements("signal", GLIB_NAMESPACE);

                /*
                 * Get object first characteristics: module it belongs to and
                 * its C name.
                 */

                characteristics.add(new String[] {
                    "in-module",
                    getActualJavaPackage(namespaceName)
                });
                characteristics.add(new String[] {
                    "c-name",
                    cName
                });

                /*
                 * Build the object blocks based on the info we have.
                 */

                blocks.add(new InterfaceBlock(getActualJavaName(cName,
                        interfaze.getAttributeValue("name")), characteristics));

                /*
                 * Parse all methods data.
                 */

                for (int methodIndex = 0; methodIndex < methods.size(); methodIndex++) {
                    blocks.add(parseMethod(interfaze, methods.get(methodIndex), characteristics,
                            namespaceName));
                }

                /*
                 * Parse all functions data.
                 */

                for (int functionIndex = 0; functionIndex < functions.size(); functionIndex++) {
                    blocks.add(parseFunction(interfaze, functions.get(functionIndex), namespaceName));
                }

                /*
                 * Parse all virtuals data.
                 */

                for (int virtualIndex = 0; virtualIndex < virtuals.size(); virtualIndex++) {
                    blocks.add(parseVirtual(interfaze, virtuals.get(virtualIndex), namespaceName,
                            signalNames));
                }

                /*
                 * Parse all signals data.
                 */

                for (int signalIndex = 0; signalIndex < signals.size(); signalIndex++) {
                    try {
                        blocks.add(parseSignal(interfaze, signals.get(signalIndex), namespaceName,
                                signalNames));
                    } catch (IllegalStateException e) {
                        continue;
                    }
                }

                try {
                    /*
                     * Generate the defs file definition for the given object.
                     */

                    defs.put(cName, new DefsFile(blocks.toArray(new Block[blocks.size()])));
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
                final String cName;
                final boolean introspectable;

                enumeration = enumerations.get(enumerationIndex);
                valuesList = enumeration.getChildElements("member", CORE_NAMESPACE);
                blocks = new ArrayList<Block>();
                characteristics = new ArrayList<String[]>();
                values = new ArrayList<String[]>();
                cName = (enumeration.getAttributeValue("type", C_NAMESPACE) != null) ? enumeration.getAttributeValue(
                        "type", C_NAMESPACE)
                        : enumeration.getAttributeValue("type-name", GLIB_NAMESPACE);
                introspectable = (enumeration.getAttribute("foreign") == null);

                if (isBlacklisted(cName) || !introspectable) {
                    continue;
                }

                /*
                 * Get object first characteristics: module it belongs to and
                 * its C name.
                 */

                characteristics.add(new String[] {
                    "in-module",
                    getActualJavaPackage(namespaceName)
                });
                characteristics.add(new String[] {
                    "c-name",
                    cName
                });

                for (int valueIndex = 0; valueIndex < valuesList.size(); valueIndex++) {
                    final Element value;
                    String nick, identifier;

                    value = valuesList.get(valueIndex);
                    nick = value.getAttributeValue("nick", GLIB_NAMESPACE);
                    identifier = value.getAttributeValue("identifier", C_NAMESPACE);

                    if (nick == null) {
                        nick = value.getAttributeValue("name");
                    }

                    /*
                     * FIXME: Exception for GdkEventType cases (but there are
                     * valid aliases which are available.
                     */

                    if (identifier.startsWith(namespaceName.toUpperCase() + "_2")
                            || identifier.startsWith(namespaceName.toUpperCase() + "_3")) {
                        continue;
                    }

                    values.add(new String[] {
                        nick,
                        identifier
                    });
                }

                blocks.add(new EnumBlock(
                        getActualJavaName(cName, enumeration.getAttributeValue("name")),
                        characteristics, values));

                try {
                    /*
                     * Generate the defs file definition for the given object.
                     */

                    defs.put(cName, new DefsFile(blocks.toArray(new Block[blocks.size()])));
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
                final String cName;
                final boolean introspectable;

                flag = flags.get(flagIndex);
                valuesList = flag.getChildElements("member", CORE_NAMESPACE);
                blocks = new ArrayList<Block>();
                characteristics = new ArrayList<String[]>();
                values = new ArrayList<String[]>();
                cName = (flag.getAttributeValue("type", C_NAMESPACE) != null) ? flag.getAttributeValue(
                        "type", C_NAMESPACE) : flag.getAttributeValue("type-name", GLIB_NAMESPACE);
                introspectable = (flag.getAttribute("foreign") == null);

                if (isBlacklisted(cName) || !introspectable) {
                    continue;
                }

                /*
                 * Get object first characteristics: module it belongs to and
                 * its C name.
                 */

                characteristics.add(new String[] {
                    "in-module",
                    getActualJavaPackage(namespaceName)
                });
                characteristics.add(new String[] {
                    "c-name",
                    cName
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
                    blocks.add(new FlagsBlock(getActualJavaName(cName, flag.getAttributeValue("name")),
                            characteristics, values));

                    try {
                        /*
                         * Generate the defs file definition for the given
                         * object.
                         */

                        defs.put(cName, new DefsFile(blocks.toArray(new Block[blocks.size()])));
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
                final Elements fields, constructors, methods;
                final List<Block> blocks;
                final List<String[]> characteristics;
                final String boxedName, cName;
                final BoxedBlock boxedBlock;
                final boolean introspectable;

                boxed = boxeds.get(boxedIndex);
                boxedName = boxed.getAttributeValue("name");
                cName = (boxed.getAttributeValue("type", C_NAMESPACE) != null) ? boxed.getAttributeValue(
                        "type", C_NAMESPACE) : boxed.getAttributeValue("type-name", GLIB_NAMESPACE);
                introspectable = (boxed.getAttribute("foreign") == null);

                /*
                 * If this boxed thing is internal or must be skipped just
                 * ignore it.
                 */

                if (isBlacklisted(cName) || !introspectable || boxedName.startsWith("_")
                        || boxedName.endsWith("Class") || boxedName.endsWith("Private")) {
                    continue;
                }

                fields = boxed.getChildElements("field", CORE_NAMESPACE);
                constructors = boxed.getChildElements("constructor", CORE_NAMESPACE);
                methods = boxed.getChildElements("method", CORE_NAMESPACE);
                blocks = new ArrayList<Block>();
                characteristics = new ArrayList<String[]>();

                /*
                 * Get object first characteristics: module it belongs to and
                 * its C name.
                 */

                characteristics.add(new String[] {
                    "in-module",
                    getActualJavaPackage(namespaceName)
                });
                characteristics.add(new String[] {
                    "c-name",
                    cName
                });

                boxedBlock = new BoxedBlock(getActualJavaName(cName, boxedName), characteristics);
                blocks.add(boxedBlock);

                /*
                 * Parse all fields.
                 */

                for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
                    final Element field;
                    final String name;
                    final boolean writable;
                    String type;

                    field = fields.get(fieldIndex);
                    type = field.getChildElements().get(0).getAttributeValue("type", C_NAMESPACE);
                    name = field.getAttributeValue("name");
                    writable = (field.getAttributeValue("writable") != null)
                            && field.getAttributeValue("writable").equals("1");

                    /*
                     * No C type just a name.
                     */

                    if (type == null) {
                        type = getActualJavaPackage(namespaceName)
                                + field.getChildElements().get(0).getAttributeValue("name");
                    }

                    /*
                     * Generate block to read the field.
                     */

                    blocks.add(new GetterBlock(boxedBlock, type, name));

                    /*
                     * If the field is also writable, generate the block to
                     * write it.
                     */

                    if (writable) {
                        blocks.add(new SetterBlock(boxedBlock, type, name));
                    }
                }

                /*
                 * Parse all constructors data.
                 */

                for (int constructorIndex = 0; constructorIndex < constructors.size(); constructorIndex++) {
                    blocks.add(parseConstructor(boxed, constructors.get(constructorIndex), namespaceName));
                }

                /*
                 * Retrieve all info about methods.
                 */

                for (int methodIndex = 0; methodIndex < methods.size(); methodIndex++) {
                    try {
                        blocks.add(parseMethod(boxed, methods.get(methodIndex), characteristics,
                                namespaceName));
                    } catch (IllegalStateException e) {
                        continue;
                    }
                }

                try {
                    /*
                     * Generate the defs file definition for the given boxed.
                     */

                    defs.put(cName, new DefsFile(blocks.toArray(new Block[blocks.size()])));
                } catch (ImproperDefsFileException e) {
                    e.printStackTrace();
                }
            }

            /*
             * Examine each union.
             */

            for (int unionIndex = 0; unionIndex < unions.size(); unionIndex++) {
                final Element union;
                final Elements fields, constructors, methods;
                final List<Block> blocks;
                final List<String[]> characteristics;
                final String unionName, cName;
                final BoxedBlock unionBlock;
                final boolean introspectable;

                union = unions.get(unionIndex);
                unionName = union.getAttributeValue("name");
                cName = (union.getAttributeValue("type", C_NAMESPACE) != null) ? union.getAttributeValue(
                        "type", C_NAMESPACE) : union.getAttributeValue("type-name", GLIB_NAMESPACE);
                introspectable = (union.getAttribute("foreign") == null);

                /*
                 * If this boxed thing is internal or must be skipped just
                 * ignore it.
                 */

                if (isBlacklisted(cName) || !introspectable || unionName.startsWith("_")
                        || unionName.endsWith("Class") || unionName.endsWith("Private")) {
                    continue;
                }

                fields = union.getChildElements("field", CORE_NAMESPACE);
                constructors = union.getChildElements("constructor", CORE_NAMESPACE);
                methods = union.getChildElements("method", CORE_NAMESPACE);
                blocks = new ArrayList<Block>();
                characteristics = new ArrayList<String[]>();

                /*
                 * Get object first characteristics: module it belongs to and
                 * its C name.
                 */

                characteristics.add(new String[] {
                    "in-module",
                    getActualJavaPackage(namespaceName)
                });
                characteristics.add(new String[] {
                    "c-name",
                    cName
                });

                unionBlock = new BoxedBlock(getActualJavaName(cName, unionName), characteristics);
                blocks.add(unionBlock);

                /*
                 * Parse all fields.
                 */

                for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
                    final Element field;
                    final String name;
                    final boolean writable;
                    String type;

                    field = fields.get(fieldIndex);
                    type = field.getChildElements().get(0).getAttributeValue("type", C_NAMESPACE);
                    name = field.getAttributeValue("name");
                    writable = (field.getAttributeValue("writable") != null)
                            && field.getAttributeValue("writable").equals("1");

                    /*
                     * No C type just a name.
                     */

                    if (type == null) {
                        type = getActualJavaPackage(namespaceName)
                                + field.getChildElements().get(0).getAttributeValue("name");
                    }

                    /*
                     * Generate block to read the field.
                     */

                    blocks.add(new GetterBlock(unionBlock, type, name));

                    /*
                     * If the field is also writable, generate the block to
                     * write it.
                     */

                    if (writable) {
                        blocks.add(new SetterBlock(unionBlock, type, name));
                    }
                }

                /*
                 * Parse all constructors data.
                 */

                for (int constructorIndex = 0; constructorIndex < constructors.size(); constructorIndex++) {
                    blocks.add(parseConstructor(union, constructors.get(constructorIndex), namespaceName));
                }

                /*
                 * Retrieve all info about methods.
                 */

                for (int methodIndex = 0; methodIndex < methods.size(); methodIndex++) {
                    try {
                        blocks.add(parseMethod(union, methods.get(methodIndex), characteristics,
                                namespaceName));
                    } catch (IllegalStateException e) {
                        continue;
                    }
                }

                try {
                    /*
                     * Generate the defs file definition for the given boxed.
                     */

                    defs.put(cName, new DefsFile(blocks.toArray(new Block[blocks.size()])));
                } catch (ImproperDefsFileException e) {
                    e.printStackTrace();
                }
            }
        }

        return defs;
    }
}
