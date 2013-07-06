package introspection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GEnumeration implements ToDefs
{
    private String namespace;

    private String cName;

    private String name;

    private List<String[]> values;

    public GEnumeration(String namespace, String cName, String name) {
        this.namespace = namespace;
        this.cName = cName;
        this.name = name;

        this.values = new ArrayList<String[]>();
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getCName() {
        return this.cName;
    }

    public String getName() {
        return this.name;
    }

    public List<String[]> getValues() {
        return this.values;
    }

    public void addValue(String[] value) {
        this.values.add(value);
    }

    public String toDefs() {
        final StringBuilder builder;

        builder = new StringBuilder();

        builder.append("(define-flags ");
        builder.append(this.name);
        builder.append(NEW_LINE);

        builder.append("  (in-module \"");
        builder.append(this.namespace);
        builder.append("\")");
        builder.append(NEW_LINE);


        builder.append("  (c-name \"");
        builder.append(this.cName);
        builder.append("\")");
        builder.append(NEW_LINE);
        
        builder.append("  (values");
        builder.append(NEW_LINE);
        
        for (String[] value : this.values) {
            builder.append("    '(\"");
            builder.append(value[0]);
            builder.append("\" \"");
            builder.append(value[1]);
            builder.append("\")");
            builder.append(NEW_LINE);
        }
        
        builder.append("  )");
        builder.append(NEW_LINE);

        builder.append(")");

        return builder.toString();
    }

    public void writeToFile(File directory) {
        final File defsFile;
        FileWriter writer;

        defsFile = new File(directory, this.cName + ".defs");
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
