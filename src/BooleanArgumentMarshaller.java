import java.util.Iterator;

public class BooleanArgumentMarshaller implements ArgumentMarshaller {
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
