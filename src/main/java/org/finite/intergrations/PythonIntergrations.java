package org.finite.intergrations;
import org.python.*;
import org.python.util.PythonInterpreter;
import org.python.core.*;
public class PythonIntergrations {
    private PythonInterpreter interpreter;

    public PythonIntergrations() {
        interpreter = new PythonInterpreter();
    }

    public void executeScript(String script) {
        interpreter.exec(script);
    }

    public void closeInterpreter() {
        interpreter.close();
    }
    public Boolean RegisterJavaClasses(String[] classes) {
        try {
            for (String className : classes) {
                interpreter.exec("from " + className + " import *");
            }
            return true;
        } catch (PyException e) {
            return false;
        }
    }
    public PyObject getVariable(String varName) {
        return interpreter.get(varName);
    }
    public void setVariable(String varName, PyObject value) {
        interpreter.set(varName, value);
    }


}