package org.finite.Exceptions;

public class MASMException extends RuntimeException {
    private final int lineNumber;
    private final String instruction;
    private final String details;

    public MASMException(String message, int lineNumber, String instruction, String details) {
        super(message);
        this.lineNumber = lineNumber;
        this.instruction = instruction;
        this.details = details;
    }

    @Override
    public String getMessage() {
        return String.format("Line %d: %s\nInstruction: %s\nDetails: %s", 
            lineNumber, super.getMessage(), instruction, details);
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getDetails() {
        return details;
    }
}
