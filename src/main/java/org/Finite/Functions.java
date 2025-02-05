package org.Finite;

import org.Finite.common.*;

public class Functions {
    public  void add(int[] memory, String reg1, String reg2) {
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
        // read the register hashmap
        int value = common.ReadRegister(reg1);
        // print the value
        common.box("Output", String.format("%d", value));
    }
    public void mov(int[] memory, String reg1, String reg2) {
        // check if the thing is a number or a register
        int value = 0;
        try {
            value = Integer.parseInt(reg2);
        } catch (Exception e) {
            value = common.ReadRegister(reg2);
        }
        // write the value to the first register
        common.print("Writing %d to %s\n", value, reg1);
        common.WriteRegister(reg1, value);
    }
}
