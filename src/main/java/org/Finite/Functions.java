package org.Finite;

import org.Finite.common.*;

import static org.Finite.common.print;

public class Functions {
    public void add(int[] memory, String reg1, String reg2) {
        // read the register hashmap
        int value1 = common.ReadRegister(reg1);
        int value2 = common.ReadRegister(reg2);
        // add the values
        int result = value1 + value2;
        // write the result to the first register
        common.WriteRegister(reg1, result);
    }

    public void sub(int[] memory, String reg1, String reg2) {
        // read the register hashmap
        int value1 = common.ReadRegister(reg1);
        int value2 = common.ReadRegister(reg2);
        // sub the values
        int result = value1 - value2;
        // write the result to the first register
        common.WriteRegister(reg1, result);

    }

    public void mul(int[] memory, String reg1, String reg2) {
        // read the register hashmap
        int value1 = common.ReadRegister(reg1);
        int value2 = common.ReadRegister(reg2);
        // mul the values
        int result = value1 * value2;
        // write the result to the first register
        common.WriteRegister(reg1, result);
    }

    public void div(int[] memory, String reg1, String reg2) {
        // read the register hashmap
        int value1 = common.ReadRegister(reg1);
        int value2 = common.ReadRegister(reg2);
        // div the values
        int result = value1 / value2;
        // write the result to the first register
        common.WriteRegister(reg1, result);
    }

    public void out(int[] memory, String reg1) {
        // check if the thing starts with $, if it does then it is a memory address
        String value = "";
        if (reg1.startsWith("$")) {
        // read a string till the end of the memory address
            // try reading the register as an int else it's raw
            try
            {
                int address = Integer.parseInt(reg1.substring(1));
                int i = 0;

                while (memory[address + i] != 0) {
                    value += (char) memory[address + i];
                    i++;
                }
            } catch (Exception e) {
                // get the value of the register
                value = Integer.toString(common.ReadRegister(reg1));
            }
        } else {
            // get the value of the register
            value = Integer.toString(common.ReadRegister(reg1));
        }
        common.box("Output", String.format("%s", value));
    }
    public void db(int[] memory, String... args) {
        // syntax is
        // db $100 "Hello, World!"

//        // check if the first argument is a memory address
//        if (instr.sop1.startsWith("$")) {
//            // get the memory address
//            int address = Integer.parseInt(instr.sop1.substring(1));
//            // get the string
//            String str = instr.sop2;
//            // write the string to memory
//            for (int i = 0; i < str.length(); i++) {
//                memory[address + i] = str.charAt(i);
//            }
//            // write a null terminator
//            memory[address + str.length()] = 0;
//        } else {
//            common.box("Error", "First argument to db must be a memory address", "error");
//        }
        if (args[0].startsWith("$")) {
            // get the memory address
            int address = Integer.parseInt(args[0].substring(1));
            // get the string
            String str = args[1];
            // write the string to memory
            for (int i = 0; i < str.length(); i++) {
                memory[address + i] = str.charAt(i);
            }
            // write a null terminator
            memory[address + str.length()] = 0;
        } else {
            common.box("Error", "First argument to db must be a memory address", "error");
        }
    }

    public void mov(int[] memory, String reg1, String reg2) {
        // check if the thing is a number or a register
        int value = 0;
        // if the start of the string is a $ then it is a memory address
        if (reg2.startsWith("$")) {
            // get the memory address
            int address = Integer.parseInt(reg2.substring(1));
            // get the value at the memory address
            value = memory[address];
            print("fValue: %d\n", value);
        } else {
           try
           {
                // try to parse the string as an int
                value = Integer.parseInt(reg2);
                print("t1Value: %d\n", value);
              } catch (Exception e) {
                // if it fails, then it is a register
                value = common.ReadRegister(reg2);
                print("c1Value: %d\n", value);
           }
        }
        try {
            // try to parse the string as an int
            value = Integer.parseInt(reg2);
            print("t2Value: %d\n", value);
        } catch (Exception e) {
            // if it fails, then it is a register
            value = common.ReadRegister(reg2);
            print("c2Value: %d\n", value);
        }
        // write the value to the register
        common.WriteRegister(reg1, value);
    }

    public void cmp(int[] memory, String reg1, String reg2) {
        // read the register hashmap or int value
        int value1 = common.ReadRegister(reg1);
        int value2 = 0;
        try {
            value2 = Integer.parseInt(reg2);
        } catch (Exception e) {
            value2 = common.ReadRegister(reg2);
        }
        // compare the values
        if (value1 == value2) {
            // set rflags to 1
            common.WriteRegister("RFLAGS", 1);
        }
        else
        {
            // set rflags to 0
            common.WriteRegister("RFLAGS", 0);
        }
    }
    public void jmp(int[] memory, String reg1) {
        // read the register hashmap
        int value = common.ReadRegister(reg1);
        // jump to the value
        common.WriteRegister("RIP", value);
    }
}

