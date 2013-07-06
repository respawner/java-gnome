package introspection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Generates .defs files from GObject Introspection data.
 * 
 * @author Guillaume Mazoyer
 */
public class IntrospectionToDefs
{
    public static final String NEW_LINE = System.lineSeparator();

    private static final File dataDirectory = new File("tests/prototype/introspection/data/");

    private static final File defsDirectory = new File("tests/prototype/introspection/defs/");

    private static final IntrospectionNamespaceContext introspectionNamespaceContext = new IntrospectionNamespaceContext();

    private static GObject generateObject(String namespace, String className, String parent,
            String cTypeName) {
        return new GObject(namespace, parent, cTypeName, className);
    }

    private static GObjectConstructor generateConstructor(GObject object, String name,
            String returnType, boolean callerOwnsReturn, String[]... parameters) {
        return new GObjectConstructor(object, name, returnType, callerOwnsReturn, parameters);
    }

    private static GObjectMethod generateMethod(GObject object, String name, String cName,
            String returnType, boolean callerOwnsReturn, String[]... parameters) {
        return new GObjectMethod(object, cName, name, returnType, callerOwnsReturn, parameters);
    }

    private static GObjectVirtualMethod generateVirtualMethod(GObject object, String name,
            String returnType, String[]... parameters) {
        return new GObjectVirtualMethod(object, name, returnType, parameters);
    }

    private static GEnumeration generateEnumeration(String namespace, String cName, String name) {
        return new GEnumeration(namespace, cName, name);
    }

    private static void processIntrospectionData(String introspectionFile) {
        final DocumentBuilderFactory factory;
        final Document document;
        final NodeList classes, enumerations;
        final String namespace;
        DocumentBuilder builder;

        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return;
        }

        try {
            document = builder.parse(introspectionFile);
            document.getDocumentElement().normalize();
        } catch (SAXException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        /*
         * Retrieve the namespace defined in this file.
         */

        namespace = document.getElementsByTagName("namespace")
                .item(0)
                .getAttributes()
                .getNamedItem("c:identifier-prefixes")
                .getNodeValue();

        /*
         * Get all classes defined in this introspection data file.
         */

        classes = document.getElementsByTagName("class");
        for (int i = 0; i < classes.getLength(); i++) {
            final String className, cTypeName, parent;
            final NodeList nodes;
            final GObject object;

            /*
             * Retrieve class data and create the .defs file that will contain
             * its definition.
             */

            className = classes.item(i).getAttributes().getNamedItem("name").getNodeValue();
            cTypeName = classes.item(i).getAttributes().getNamedItem("c:type").getNodeValue();
            parent = classes.item(i).getAttributes().getNamedItem("parent").getNodeValue();

            object = generateObject(namespace, className, parent, cTypeName);

            nodes = classes.item(i).getChildNodes();

            /*
             * Retrieve data for the different functions (constructors,
             * methods and virtuals).
             */

            for (int j = 0; j < nodes.getLength(); j++) {
                final NodeList details;
                final String nodeType, functionName, cFunctionName;
                final ArrayList<String[]> parameters;
                NodeList returnDetails, parametersDetails;
                String functionReturnType;
                boolean callerOwnsReturn;

                nodeType = nodes.item(j).getNodeName().toString();
                parameters = new ArrayList<String[]>();
                functionReturnType = null;
                callerOwnsReturn = false;

                if (nodeType.equals("constructor")) {
                    final GObjectConstructor constructor;

                    details = nodes.item(j).getChildNodes();

                    cFunctionName = nodes.item(j)
                            .getAttributes()
                            .getNamedItem("c:identifier")
                            .getNodeValue();

                    for (int k = 0; k < details.getLength(); k++) {
                        if (details.item(k).getNodeName().equals("return-value")) {
                            callerOwnsReturn = details.item(k)
                                    .getAttributes()
                                    .getNamedItem("transfer-ownership")
                                    .getNodeValue()
                                    .equals("full");
                            returnDetails = details.item(k).getChildNodes();

                            for (int l = 0; l < returnDetails.getLength(); l++) {
                                if (returnDetails.item(l).getNodeName().equals("type")) {
                                    functionReturnType = returnDetails.item(l)
                                            .getAttributes()
                                            .getNamedItem("c:type")
                                            .getNodeValue();
                                }
                            }
                        } else if (details.item(k).getNodeName().equals("parameters")) {
                            parametersDetails = details.item(k).getChildNodes();

                            for (int m = 0; m < parametersDetails.getLength(); m++) {
                                final String[] parameter;
                                final NodeList parameterDetails;

                                if (!parametersDetails.item(m).getNodeName().equals("parameter")) {
                                    continue;
                                }

                                parameter = new String[2];

                                parameter[0] = parametersDetails.item(m)
                                        .getAttributes()
                                        .getNamedItem("name")
                                        .getNodeValue();

                                parameterDetails = parametersDetails.item(m).getChildNodes();
                                for (int n = 0; n < parameterDetails.getLength(); n++) {
                                    if (parameterDetails.item(n).getNodeName().equals("type")) {
                                        parameter[1] = parameterDetails.item(n)
                                                .getAttributes()
                                                .getNamedItem("c:type")
                                                .getNodeValue()
                                                .replaceAll(" ", "-");
                                    }
                                }

                                parameters.add(parameter);
                            }
                        }
                    }

                    constructor = generateConstructor(object, cFunctionName, functionReturnType,
                            callerOwnsReturn, parameters.toArray(new String[parameters.size()][2]));
                    object.addConstructor(constructor);
                } else if (nodeType.equals("method")) {
                    final GObjectMethod method;

                    details = nodes.item(j).getChildNodes();

                    functionName = nodes.item(j).getAttributes().getNamedItem("name").getNodeValue();
                    cFunctionName = nodes.item(j)
                            .getAttributes()
                            .getNamedItem("c:identifier")
                            .getNodeValue();

                    for (int k = 0; k < details.getLength(); k++) {
                        if (details.item(k).getNodeName().equals("return-value")) {
                            callerOwnsReturn = (details.item(k)
                                    .getAttributes()
                                    .getNamedItem("transfer-ownership") != null)
                                    && details.item(k)
                                            .getAttributes()
                                            .getNamedItem("transfer-ownership")
                                            .getNodeValue()
                                            .equals("full");
                            returnDetails = details.item(k).getChildNodes();

                            for (int l = 0; l < returnDetails.getLength(); l++) {
                                if (returnDetails.item(l).getNodeName().equals("type")) {
                                    functionReturnType = returnDetails.item(l)
                                            .getAttributes()
                                            .getNamedItem("c:type")
                                            .getNodeValue();
                                } else if (returnDetails.item(l).getNodeName().equals("array")) {
                                    functionReturnType = returnDetails.item(l)
                                            .getAttributes()
                                            .getNamedItem("c:type")
                                            .getNodeValue()
                                            .replaceAll(" ", "-");
                                }
                            }
                        } else if (details.item(k).getNodeName().equals("parameters")) {
                            parametersDetails = details.item(k).getChildNodes();

                            for (int m = 0; m < parametersDetails.getLength(); m++) {
                                final String[] parameter;
                                final NodeList parameterDetails;

                                if (!parametersDetails.item(m).getNodeName().equals("parameter")) {
                                    continue;
                                }

                                parameter = new String[2];

                                parameter[0] = parametersDetails.item(m)
                                        .getAttributes()
                                        .getNamedItem("name")
                                        .getNodeValue();

                                parameterDetails = parametersDetails.item(m).getChildNodes();
                                for (int n = 0; n < parameterDetails.getLength(); n++) {
                                    if (parameterDetails.item(n).getNodeName().equals("type")) {
                                        parameter[1] = parameterDetails.item(n)
                                                .getAttributes()
                                                .getNamedItem("c:type")
                                                .getNodeValue()
                                                .replaceAll(" ", "-");
                                    } else if (parameterDetails.item(n).getNodeName().equals("array")) {
                                        parameter[1] = parameterDetails.item(n)
                                                .getAttributes()
                                                .getNamedItem("c:type")
                                                .getNodeValue()
                                                .replaceAll(" ", "-");
                                    }
                                }

                                parameters.add(parameter);
                            }
                        }
                    }

                    method = generateMethod(object, functionName, cFunctionName, functionReturnType,
                            callerOwnsReturn, parameters.toArray(new String[parameters.size()][2]));
                    object.addMethod(method);
                } else if (nodeType.endsWith("virtual-method")) {
                    final GObjectVirtualMethod virtual;

                    details = nodes.item(j).getChildNodes();

                    functionName = nodes.item(j).getAttributes().getNamedItem("name").getNodeValue();

                    for (int k = 0; k < details.getLength(); k++) {
                        if (details.item(k).getNodeName().equals("return-value")) {
                            returnDetails = details.item(k).getChildNodes();

                            for (int l = 0; l < returnDetails.getLength(); l++) {
                                if (returnDetails.item(l).getNodeName().equals("type")) {
                                    functionReturnType = returnDetails.item(l)
                                            .getAttributes()
                                            .getNamedItem("c:type")
                                            .getNodeValue();
                                } else if (returnDetails.item(l).getNodeName().equals("array")) {
                                    functionReturnType = returnDetails.item(l)
                                            .getAttributes()
                                            .getNamedItem("c:type")
                                            .getNodeValue()
                                            .replaceAll(" ", "-");
                                }
                            }
                        } else if (details.item(k).getNodeName().equals("parameters")) {
                            parametersDetails = details.item(k).getChildNodes();

                            for (int m = 0; m < parametersDetails.getLength(); m++) {
                                final String[] parameter;
                                final NodeList parameterDetails;

                                if (!parametersDetails.item(m).getNodeName().equals("parameter")) {
                                    continue;
                                }

                                parameter = new String[2];

                                parameter[0] = parametersDetails.item(m)
                                        .getAttributes()
                                        .getNamedItem("name")
                                        .getNodeValue();

                                parameterDetails = parametersDetails.item(m).getChildNodes();
                                for (int n = 0; n < parameterDetails.getLength(); n++) {
                                    if (parameterDetails.item(n).getNodeName().equals("type")) {
                                        parameter[1] = parameterDetails.item(n)
                                                .getAttributes()
                                                .getNamedItem("c:type")
                                                .getNodeValue()
                                                .replaceAll(" ", "-");
                                    } else if (parameterDetails.item(n).getNodeName().equals("array")) {
                                        parameter[1] = parameterDetails.item(n)
                                                .getAttributes()
                                                .getNamedItem("c:type")
                                                .getNodeValue()
                                                .replaceAll(" ", "-");
                                    }
                                }

                                parameters.add(parameter);
                            }
                        }
                    }

                    virtual = generateVirtualMethod(object, functionName, functionReturnType,
                            parameters.toArray(new String[parameters.size()][2]));
                    object.addVirtualMethod(virtual);
                } else {
                    // System.out.println("Unhandled node type: " + nodeType);
                }
            }

            object.writeToFile(defsDirectory);
        }

        /*
         * Get all enumerations defined in this introspection data file.
         */

        enumerations = document.getElementsByTagName("enumeration");
        for (int i = 0; i < enumerations.getLength(); i++) {
            final String name, cName;
            final NodeList nodes;
            final GEnumeration enumeration;

            /*
             * Retrieve enumeration data and create the .defs file that will
             * contain its definition.
             */

            name = enumerations.item(i).getAttributes().getNamedItem("name").getNodeValue();
            cName = enumerations.item(i).getAttributes().getNamedItem("c:type").getNodeValue();

            enumeration = generateEnumeration(namespace, cName, name);

            nodes = enumerations.item(i).getChildNodes();

            /*
             * Retrieve data for the different functions (constructors,
             * methods and virtuals).
             */

            for (int j = 0; j < nodes.getLength(); j++) {
                final String nodeType;

                nodeType = nodes.item(j).getNodeName().toString();

                if (nodeType.equals("member")) {
                    enumeration.addValue(new String[] {
                        nodes.item(j).getAttributes().getNamedItem("glib:nick").getNodeValue(),
                        nodes.item(j).getAttributes().getNamedItem("c:identifier").getNodeValue()
                    });
                } else {
                    // System.out.println("Unhandled node type: " + nodeType);
                }
            }

            enumeration.writeToFile(defsDirectory);
        }
    }

    public static void main(String[] args) {
        final File[] introspectionFiles;

        introspectionFiles = dataDirectory.listFiles();

        if ((introspectionFiles == null) || (introspectionFiles.length < 1)) {
            System.out.println("No data introspection file in " + dataDirectory.getAbsolutePath());
            return;
        }

        /*
         * Process introspection for each files.
         */

        for (File file : introspectionFiles) {
            processIntrospectionData(file.getAbsolutePath());
        }
    }

    private static class IntrospectionNamespaceContext implements NamespaceContext
    {
        public String getNamespaceURI(String prefix) {
            if (prefix.equals("ns")) {
                return "http://www.gtk.org/introspection/core/1.0";
            }

            if (prefix.equals("c")) {
                return "http://www.gtk.org/introspection/c/1.0";
            }

            if (prefix.equals("glib")) {
                return "http://www.gtk.org/introspection/glib/1.0";
            }

            return null;
        }

        public String getPrefix(String namespaceURI) {
            return null;
        }

        public Iterator<?> getPrefixes(String namespaceURI) {
            return null;
        }
    }
}
