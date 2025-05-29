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
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("+---------------------------------------------------\n");
        sb.append("| MASM Exception\n");
        sb.append("+---------------------------------------------------\n");
        if (lineNumber > 0) sb.append(String.format("| Line      : %d\n", lineNumber));
    
        sb.append(String.format("┃ Message   : %s\n", super.getMessage()));
        if (details != null && !details.isEmpty()) sb.append(String.format("┃ Details   : %s\n", details));
        sb.append("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getMessage();
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
