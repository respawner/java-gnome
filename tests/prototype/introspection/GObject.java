package introspection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GObject implements ToDefs
{
    private String namespace;

    private String parent;

    private String cTypeName;

    private String className;

    private List<GObjectConstructor> constructors;

    private List<GObjectMethod> methods;

    private List<GObjectVirtualMethod> virtuals;

    public GObject(String namespace, String parent, String cTypeName, String className) {
        this.namespace = namespace;
        this.parent = parent;
        this.cTypeName = cTypeName;
        this.className = className;

        this.constructors = new ArrayList<GObjectConstructor>();
        this.methods = new ArrayList<GObjectMethod>();
        this.virtuals = new ArrayList<GObjectVirtualMethod>();
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getParent() {
        return this.parent;
    }

    public String getCTypeName() {
        return this.cTypeName;
    }

    public String getClassName() {
        return this.className;
    }

    public List<GObjectConstructor> getConstructors() {
        return constructors;
    }

    public void addConstructor(GObjectConstructor constructor) {
        this.constructors.add(constructor);
    }

    public List<GObjectMethod> getMethods() {
        return methods;
    }

    public void addMethod(GObjectMethod method) {
        this.methods.add(method);
    }

    public List<GObjectVirtualMethod> getVirtualMethods() {
        return virtuals;
    }

    public void addVirtualMethod(GObjectVirtualMethod virtual) {
        this.virtuals.add(virtual);
    }

    public String toDefs() {
        final StringBuilder builder;

        builder = new StringBuilder();

        builder.append("(define-object ");
        builder.append(this.className);
        builder.append(NEW_LINE);

        builder.append("  (in-module \"");
        builder.append(this.namespace);
        builder.append("\")");
        builder.append(NEW_LINE);

        builder.append("  (parent \"");
        builder.append(this.namespace);
        builder.append(this.parent);
        builder.append("\")");
        builder.append(NEW_LINE);

        builder.append("  (c-name \"");
        builder.append(this.cTypeName);
        builder.append("\")");
        builder.append(NEW_LINE);

        builder.append(")");
        builder.append(NEW_LINE);
        builder.append(NEW_LINE);

        for (GObjectConstructor constructor : this.constructors) {
            builder.append(constructor.toDefs());
        }

        builder.append(NEW_LINE);

        for (GObjectMethod method : this.methods) {
            builder.append(method.toDefs());
        }

        builder.append(NEW_LINE);

        for (GObjectVirtualMethod virtual : this.virtuals) {
            builder.append(virtual.toDefs());
        }

        return builder.toString();
    }

    public void writeToFile(File directory) {
        final File defsFile;
        FileWriter writer;

        defsFile = new File(directory, this.cTypeName + ".defs");
        writer = null;

        try {
            writer = new FileWriter(defsFile);

            writer.write(this.toDefs());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
