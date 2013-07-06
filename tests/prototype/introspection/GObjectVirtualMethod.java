package introspection;

public class GObjectVirtualMethod implements ToDefs
{
    private GObject object;

    private String name;

    private String returnType;

    private String[][] parameters;

    public GObjectVirtualMethod(GObject object, String name, String returnType, String[][] parameters) {
        this.object = object;
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public GObject getObject() {
        return this.object;
    }

    public String getName() {
        return this.name;
    }

    public String getReturnType() {
        return this.returnType;
    }

    public String[][] getParameters() {
        return this.parameters;
    }

    public String toDefs() {
        final StringBuilder builder;

        builder = new StringBuilder();

        builder.append("(define-virtual ");
        builder.append(this.name);
        builder.append(NEW_LINE);

        builder.append("  (of-object \"");
        builder.append(this.object.getCTypeName());
        builder.append("\")");
        builder.append(NEW_LINE);

        builder.append("  (return-type \"");
        builder.append(this.returnType.equals("void") ? "none" : this.returnType);
        builder.append("\")");
        builder.append(NEW_LINE);

        if ((this.parameters != null) && (this.parameters.length > 0)) {
            builder.append("  (parameters");
            builder.append(NEW_LINE);

            for (String[] parameter : this.parameters) {
                builder.append("    '(\"");
                builder.append(parameter[1]);
                builder.append("\" \"");
                builder.append(parameter[0]);
                builder.append("\")");
                builder.append(NEW_LINE);
            }

            builder.append("  )");
            builder.append(NEW_LINE);
        }

        builder.append(")");
        builder.append(NEW_LINE);

        return builder.toString();
    }
}
