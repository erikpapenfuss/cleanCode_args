import java.util.*;

public class Args {
    private String schema;
    private Map<Character, ArgumentMarshaller> marshallers = new HashMap<Character, ArgumentMarshaller>();
    private Set<Character> argsFound = new HashSet<Character>();
    private Iterator<String> currentArgument;
    private List<String> argsList;

    public Args(String schema, String[] args) throws ArgsException {
        this.schema = schema;
        argsList = Arrays.asList(args);
        parse();
    }

    private void parse() throws ArgsException {
        parseSchema();
        parseArguments();
    }

    private boolean parseSchema() throws ArgsException {
        for (String element : schema.split(",")) {
            if (element.length() > 0) {
                parseSchemaElement(element.trim());
            }
        }
        return true;
    }

    private void parseSchemaElement(String element) throws ArgsException {
        char elementId = element.charAt(0);
        String elementTail = element.substring(1);
        validateSchemaElementId(elementId);
        if(elementTail.length() == 0)
            marshallers.put(elementId, new BooleanArgumentMarshaller());
        else if(elementTail.equals("*"))
            marshallers.put(elementId, new StringArgumentMarshaller());
        else if(elementTail.equals("#"))
            marshallers.put(elementId, new IntegerArgumentMarshaller());
        else if(elementTail.equals("##"))
            marshallers.put(elementId, new DoubleArgumentMarshaller());
        else {
            throw new ArgsException(ArgsException.ErrorCode.INVALID_FORMAT, elementId, elementTail);
        }
    }

    private void validateSchemaElementId(char elementId) throws ArgsException {
        if(!Character.isLetter(elementId)) {
            throw new ArgsException(ArgsException.ErrorCode.INVALID_ARGUMENT_NAME, elementId, null);
        }
    }

    private boolean parseArguments() throws  ArgsException {
        for (currentArgument = argsList.iterator(); currentArgument.hasNext();) {
            String arg = currentArgument.next();
            parseArgument(arg);
        }
        return true;
    }

    private void parseArgument(String arg) throws ArgsException {
        if(arg.startsWith("-"))
            parseElements(arg);
    }

    private void parseElements(String arg) throws ArgsException {
        for (int i = 1; i < arg.length(); i++) {
            parseElement(arg.charAt(i));
        }
    }

    private void parseElement(char argChar) throws ArgsException {
        if (setArgument(argChar))
            argsFound.add(argChar);
        else {
            throw new ArgsException(ArgsException.ErrorCode.UNEXPECTED_ARGUMENT, argChar, null);
        }
    }

    private boolean setArgument(char argChar) throws ArgsException {
        ArgumentMarshaller m = marshallers.get(argChar);
        if(m == null)
            return false;
        try {
            m.set(currentArgument);
            return true;
        }
        catch (ArgsException e) {
            e.setErrorArgumentId(argChar);
            throw e;
        }
    }

    public int cardinality() {
        return argsFound.size();
    }

    public String usage() {
        if (schema.length() > 0)
            return "- [" + schema + "]";
        else return "";
    }

    public boolean getBoolean(char arg) {
        Args.ArgumentMarshaller am = marshallers.get(arg);
        boolean b = false;
        try {
            b = am != null &&  (Boolean) am.get();
        }
        catch (ClassCastException e) {
            b = false;
        }
        return b;
    }

    public String getString(char arg) {
        Args.ArgumentMarshaller am = marshallers.get(arg);
        try {
            return am == null ? "" : (String)am.get();
        }
        catch (ClassCastException e) {
            return "";
        }
    }

    public int getInt(char arg) {
        Args.ArgumentMarshaller am = marshallers.get(arg);
        try {
            return am == null ? 0 : (Integer) am.get();
        }
        catch (Exception e) {
            return 0;
        }
    }

    public double getDouble(char arg) {
        Args.ArgumentMarshaller am = marshallers.get(arg);
        try {
            return am == null ? 0 : (Double) am.get();
        }
        catch (Exception e) {
            return 0.0;
        }
    }

    public boolean has(char arg) {
        return argsFound.contains(arg);
    }

    private interface ArgumentMarshaller {
        void set(Iterator<String> currentArgument) throws ArgsException;
        Object get();
    }

    private class BooleanArgumentMarshaller implements ArgumentMarshaller {
        private boolean booleanValue = false;

        public void set(Iterator<String> currentArgument) throws ArgsException {
            booleanValue = true;
        }

        public void set(String s) {

        }

        public Object get() {
            return booleanValue;
        }
    }

    private class StringArgumentMarshaller implements ArgumentMarshaller {
        private String stringValue = "";

        public void set(Iterator<String> currentArgument) throws ArgsException {
            try {
                stringValue = currentArgument.next();
            }
            catch (NoSuchElementException e) {
                throw new ArgsException(ArgsException.ErrorCode.MISSING_STRING);
            }
        }

        public void set(String s) {
            stringValue = s;
        }

        public Object get() {
            return stringValue;
        }
    }

    private class IntegerArgumentMarshaller implements ArgumentMarshaller {
        private int intValue = 0;

        public void set(Iterator<String> currentArgument) throws ArgsException {
            String parameter = null;
            try {
                parameter = currentArgument.next();
                intValue = Integer.parseInt(parameter);
            }
            catch(NoSuchElementException e) {
                throw new ArgsException(ArgsException.ErrorCode.MISSING_INTEGER);
            }
            catch (NumberFormatException e) {
                throw new ArgsException(ArgsException.ErrorCode.INVALID_INTEGER);
            }
        }

        public Object get() {
            return intValue;
        }
    }

    private class DoubleArgumentMarshaller implements ArgumentMarshaller {
        private double doubleValue = 0;

        public void set(Iterator<String> currentArgument) throws ArgsException {
            String parameter = null;
            try {
                parameter = currentArgument.next();
                doubleValue = Double.parseDouble(parameter);
            }
            catch (NoSuchElementException e) {
                throw new ArgsException(ArgsException.ErrorCode.MISSING_DOUBLE);
            }
            catch (NumberFormatException e) {
                throw new ArgsException(ArgsException.ErrorCode.INVALID_DOUBLE);
            }
        }

        public Object get() {
            return doubleValue;
        }
    }
}


