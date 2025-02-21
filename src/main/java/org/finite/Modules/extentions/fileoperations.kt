package org.finite.kotlin.extentions
import java.io.File
import org.finite.Exceptions.MNIException
import org.finite.ModuleManager.*;
import org.finite.ModuleManager.annotations.*;
@MNIClass("FIleOperations")
class fileoperations {
    // file operations go like this,
    /*
    opening a file puts a file pointer in the memory, which is used to read and write the file.
    MNI FileOperations.openFile R1 ; reads memory at R1 and opens the file with the name in the memory
    MNI FileOperations.readFile R1 R2 ; reads memory at R1 for the file pointer, and R2 for the memory location to start to store the file
     */
    @MNIFunction(module="FileOperations",name="readFile")
    fun readFile(obj: MNIMethodObject){
        var file_name = "";
        var inc = 0;
        var file: File;
        while (obj.readMemory(inc) != 0) {
            file_name += obj.readMemory(inc).toChar();
            inc++;
        }
        try {

        file = File(file_name);
        }
        catch (e: Exception){
            throw MNIException("File not found", "FileOperations","ReadFile");

        }
        var file_content = file.readText();
        for (i in obj.arg2 until file_content.length) {
            obj.writeMemory(i, file_content[i].toInt());
        }


    }

}
