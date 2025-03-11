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
}