package introspection;

public class GObjectMethod implements ToDefs
{
    private GObject object;

    private String cName;

    private String name;

    private String returnType;

    private boolean callerOwnsReturn;

    private String[][] parameters;

    public GObjectMethod(GObject object, String cName, String name, String returnType,
            boolean callerOwnsReturn, String[]... parameters) {
        this.object = object;
        this.cName = cName;
        this.name = name;
        this.returnType = returnType;
        this.callerOwnsReturn = callerOwnsReturn;
        this.parameters = parameters;
    }

    public GObject getObject() {
        return this.object;
    }

    public String getcName() {
        return this.cName;
    }

    public String getName() {
        return this.name;
    }

    public String getReturnType() {
        return this.returnType;
    }

    public boolean getCallerOwnsReturn() {
        return this.callerOwnsReturn;
    }

    public String[][] getParameters() {
        return this.parameters;
    }

    public String toDefs() {
        final StringBuilder builder;

        builder = new StringBuilder();

        builder.append("(define-method ");
        builder.append(this.name);
        builder.append(NEW_LINE);

        builder.append("  (of-object \"");
        builder.append(this.object.getCTypeName());
        builder.append("\")");
        builder.append(NEW_LINE);

        builder.append("  (c-name \"");
        builder.append(this.cName);
        builder.append("\")");
        builder.append(NEW_LINE);

        if (this.callerOwnsReturn) {
            builder.append("  (caller-owns-return #t)");
            builder.append(NEW_LINE);
        }

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
