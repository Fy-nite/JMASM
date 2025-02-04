package org.Finite;
import org.Finite.common.*;
public class Functions {
    public void add(int[] memory, String reg1 , String reg2) {
    // read the register hashmap
    int value1 = common.ReadRegister(reg1);
    int value2 = common.ReadRegister(reg2);
    // add the values
    int result = value1 + value2;
    // write the result to the first register
    common.WriteRegister(Integer.parseInt(reg1), result);
    }
}
