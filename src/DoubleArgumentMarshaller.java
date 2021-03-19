import java.util.Iterator;
import java.util.NoSuchElementException;

public class DoubleArgumentMarshaller implements ArgumentMarshaller {
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