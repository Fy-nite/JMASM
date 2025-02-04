package org.Finite;
import java.util.Arrays;
import java.util.Scanner;
import static org.Finite.common.*;
import static org.Finite.debug.*;
import org.Finite.Functions.*;
public class interp {
    public class instruction {
        String name;
        int opcode;
        int iop1;
        int iop2;
        String sop1;
        String sop2;
    }
    Functions functions = new Functions();

    public  int ExecuteSingleInstruction(instruction instr) {
        switch (instr.name) {
            case "mov":
                functions.mov(instr);
                break;
            case "add":
                functions.add(instr);
                break;
            case "sub":
                functions.sub(instr);
                break;
            case "mul":
                functions.mul(instr);
                break;
            case "div":
                functions.div(instr);
                break;
            case "out":
                functions.out(instr);
                break;

        }

        return 0;
    }

}