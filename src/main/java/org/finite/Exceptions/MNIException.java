package org.finite.Exceptions;
public class MNIException extends Exception {
    public MNIException(String message, String module, String method) {
        super("[MNIException]\n" + message + "\nthe operation that was attempted caused an issue with the module (" + module + ")\n the method: " + method + "\nwas the cause of the issue, did you forget to pass arguments or is the module not initialized?");
    }

    public MNIException(String message, String module, int line, String method, String solution) {
        super("[MNIException]\n" + message + "\nthe operation that was attempted caused an issue with the module (" + module + ")\nthe line: " + line + "\nthe method: " + method + "\nwas the cause of the issue, did you forget to pass arguments or is the module not initialized?\n" + solution);
    }
}