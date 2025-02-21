/*
    * This is a test file for the MNI spec, it is not a real file
    * This file is used to test the MNI spec
*/
@MNIClass("functest")
public class functest {
    @MNIFunction(module="functest", name="test")
    public static void test(MNIMethodObject obj) {
        int start = obj.arg1;
        int outputdest = obj.arg2;
        String output = "";
        int i = start;
        int mem = 0;
        // read memory through while, if the memory is not 0, add it to the output
        while (mem != 0) {
            mem = obj.readMemory(i); // links to context and common.readMemory();
            output += (char)mem; // add the memory to the output
            i++; // increment the memory
        }
        // prepend hello to the output
        output = "hello " + output;
        // write the output to the memory
        obj.writeMemory(outputdest, output);
    }
}
