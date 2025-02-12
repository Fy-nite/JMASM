package org.Finite;

import org.Finite.common.*;

import static org.Finite.common.print;
import static org.Finite.common.printerr;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.Finite.interp.instructions;

public class Functions {
    public static void include(String filename, instructions instrs) {
        // check if the file exists
        File file = new File(filename);
        if (!file.exists()) {
            common.box("Error", "file " + filename + " does not exist", "error");
            return;
        }

        // read the file
        try {
            BufferedReader reader = new BufferedReader
                    (new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {

            }
            reader.close();
        } catch (IOException e) {
            common.printerr("Error reading file: " + filename);
        }
    }
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


    public void out(int[] memory, String fd, String source) {
        String value = "";
        int fileDescriptor;
        if (source == null) {
            return;
        }
        try {
            fileDescriptor = Integer.parseInt(fd);
        } catch (Exception e) {
            // If fd is a register, get its value
            fileDescriptor = common.ReadRegister(fd);
        }

        // Handle memory address starting with $
        if (source.startsWith("$")) {
            String addr = source.substring(1);
            try {
                // Check if it's a direct memory address
                int address = Integer.parseInt(addr);
                int i = 0;
                while (memory[address + i] != 0) {
                    value += (char) memory[address + i];
                    i++;
                }
            } catch (Exception e) {
                // If not direct address, it's a register containing address
                int regAddr = common.ReadRegister(addr);
                int i = 0;
                while (memory[regAddr + i] != 0) {
                    value += (char) memory[regAddr + i];
                    i++;
                }
            }
        } else {
            // Direct register value
            value = Integer.toString(common.ReadRegister(source));
        }

        if (fileDescriptor == 1) {
            print("%s", value);
        } else if (fileDescriptor == 2) {
            common.printerr("%s", value);
        }
    }

    public void in(int[] memory, String fd, String dest) {
        // Read from stdin
        String value = common.inbox("stdin");
        int fileDescriptor;
        
        try {
            fileDescriptor = Integer.parseInt(fd);
        } catch (Exception e) {
            // If fd is a register, get its value
            fileDescriptor = common.ReadRegister(fd);
        }

        if (fileDescriptor == 0) {
            // Write to memory
            if (dest.startsWith("$")) {
                // Handle memory address starting with $
                String addr = dest.substring(1);
                try {
                    // Check if it's a direct memory address
                    int address = Integer.parseInt(addr);
                    for (int i = 0; i < value.length(); i++) {
                        memory[address + i] = value.charAt(i);
                    }
                } catch (Exception e) {
                    // If not direct address, it's a register containing address
                    int regAddr = common.ReadRegister(addr);
                    for (int i = 0; i < value.length(); i++) {
                        memory[regAddr + i] = value.charAt(i);
                    }
                }
            } else {
                common.printerr("Error: Invalid destination address: " + dest);
            }
        }
    }
    
    public void db(int[] memory, String... argz) {
        // split argz into two parts
        String[] args = argz[0].split(" ");
        // check if the first argument is a memory address
        if (args[0].matches("\\$\\d+")) {
            int address = Integer.parseInt(args[0].substring(1));
            String str = args[1].replaceAll("^\"|\"$", "");
            for (int i = 0; i < str.length(); i++) {
                memory[address + i] = str.charAt(i);
            }
        } else {
            print("Error: %s\n", args[0]);
            int address = Integer.parseInt(args[0]);
            String str = args[1].replaceAll("^\"|\"$", "");
            for (int i = 0; i < str.length(); i++) {
                memory[address + i] = str.charAt(i);
            }
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

        } else {
           try
           {
                // try to parse the string as an int
                value = Integer.parseInt(reg2);
 
              } catch (Exception e) {
                // if it fails, then it is a register
                value = common.ReadRegister(reg2);
  
           }
        }
        try {
            // try to parse the string as an int
            value = Integer.parseInt(reg2);
    
        } catch (Exception e) {
            // if it fails, then it is a register
            value = common.ReadRegister(reg2);
     
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

    public void cout(int[] memory, String fd, String reg) {
        // read the register hashmap
        int value = common.ReadRegister(reg);
        // print the value
        if (fd.equals("1")) {
            // convert to character
            char c = (char) value;
            print("%c", c);
        } else if (fd.equals("2")) {
            // convert to character
            char c = (char) value;
            printerr("%c", c);
        }
    }

    public void jmp(int[] memory, String target) {
        int value;
        try {
            value = Integer.parseInt(target);
        } catch (NumberFormatException e) {
            // If not a number, try to read as register
            value = common.ReadRegister(target);
        }
        common.WriteRegister("RIP", value);
    }
}

