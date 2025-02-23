package org.finite.Exceptions;

public class IncludeException extends MASMException {
    public IncludeException(String message, int lineNumber, String includePath, String details) {
        super(message, lineNumber, "#include \"" + includePath + "\"", details);
    }
}
