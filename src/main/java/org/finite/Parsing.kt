package org.finite;

import org.finite.interp.instructions;
import org.finite.Exceptions.MASMException;
import org.finite.common;

// does parsing need to be a object or can it be a class if i call it from java?

// Object is a singleton class, meaning that it can only have one instance
// This is useful for utility classes that don't need to be instantiated multiple times
// It's also useful for static methods, as Kotlin doesn't have static methods
// This is because Kotlin doesn't have static methods, so you can't call a method without an instance of the class
// This is a workaround for that

// when i'm compiling my kotlin code with java in maven, java tells me that cannot find symbol Parsing

// This is because the Kotlin object is compiled to a class with a static method, but Java doesn't recognize it as a static method
// You can call the method as a static method from Java by using the class name, like Parsing.parseAnsiTerminal(input)
// Or you can change the object to a class, and call the method as a normal method, like Parsing parsing = new Parsing(); parsing.parseAnsiTerminal(input);
// The object is a singleton class, so it can only have one instance, and it's useful for utility classes that don't need to be instantiated multiple times

// wouldn't i just use something like Parsing.INSTANCE.parseAnsiTerminal(input) to call the method from java?

// You can do that, but it's not necessary to use an instance of the object, as it's a singleton class

// thanks!

// You're welcome!



object Parsing {
    // Static methods for Java interop
    
    fun parseAnsiTerminal(input: String): String {
        var result = input
        
        // ANSI escape codes for terminal colors
        // Reset
        result = result.replace("[reset]".toRegex(), "\u001B[0m")
        // Bold
        result = result.replace("[bold]".toRegex(), "\u001B[1m")
        // Dim
        result = result.replace("[dim]".toRegex(), "\u001B[2m")
        // Italic
        result = result.replace("[italic]".toRegex(), "\u001B[3m")
        // Underline
        result = result.replace("[underline]".toRegex(), "\u001B[4m")
        // Blink
        result = result.replace("[blink]".toRegex(), "\u001B[5m")
        // Reverse
        result = result.replace("[reverse]".toRegex(), "\u001B[7m")
        // Hidden
        result = result.replace("[hidden]".toRegex(), "\u001B[8m")
        // Strikethrough
        result = result.replace("[strikethrough]".toRegex(), "\u001B[9m")
        // Reset all attributes
        result = result.replace("[resetall]".toRegex(), "\u001B[0m")
        // Reset bold
        result = result.replace("[resetbold]".toRegex(), "\u001B[21m")
        // Clear screen
        result = result.replace("[clear]".toRegex(), "\u001B[2J")
        
        // Foreground colors
        result = result.replace("[black]".toRegex(), "\u001B[30m")
        result = result.replace("[red]".toRegex(), "\u001B[31m")
        result = result.replace("[green]".toRegex(), "\u001B[32m")
        result = result.replace("[yellow]".toRegex(), "\u001B[33m")
        result = result.replace("[blue]".toRegex(), "\u001B[34m")
        result = result.replace("[magenta]".toRegex(), "\u001B[35m")
        result = result.replace("[cyan]".toRegex(), "\u001B[36m")
        result = result.replace("[white]".toRegex(), "\u001B[37m")
        
        // Background colors
        result = result.replace("[bg_black]".toRegex(), "\u001B[40m")
        result = result.replace("[bg_red]".toRegex(), "\u001B[41m")
        result = result.replace("[bg_green]".toRegex(), "\u001B[42m")
        result = result.replace("[bg_yellow]".toRegex(), "\u001B[43m")
        result = result.replace("[bg_blue]".toRegex(), "\u001B[44m")
        result = result.replace("[bg_magenta]".toRegex(), "\u001B[45m")
        result = result.replace("[bg_cyan]".toRegex(), "\u001B[46m")
        result = result.replace("[bg_white]".toRegex(), "\u001B[47m")
        
        return result
    }

    
    fun parseTarget(target: String?, instrs: instructions?): Int {
        if (target == null || instrs == null) {
            return -1
        }

        // If it's a label reference
        if (target.startsWith("#")) {
            val labelName = target.substring(1)
            
            if (instrs.labelMap == null) {
                return -1
            }

            val labelAddress = instrs.labelMap[labelName]
            return labelAddress ?: -1
        }

        // Try parsing as direct number
        return try {
            target.toInt()
        } catch (e: NumberFormatException) {
            // Try as register
            try {
                common.ReadRegister(target)
            } catch (ex: Exception) {
                -1
            }
        }
    }

    fun parseRegister(reg: String?): Int {
        return try {
            reg!!.toInt()
        } catch (e: NumberFormatException) {
            common.ReadRegister(reg)
        }
    }
    fun processEscapeSequences(input: String): String {
        val result = StringBuilder()
        var inEscape = false

        for (c in input) {
            if (inEscape) {
                when (c) {
                    'n' -> result.append('\n')
                    't' -> result.append('\t')
                    'r' -> result.append('\r')
                    'b' -> result.append('\b')
                    'f' -> result.append('\u000C')
                    'a' -> result.append('\u0007')
                    '\\' -> result.append('\\')
                    '0' -> result.append('\u0000')
                    else -> result.append(c)
                }
                inEscape = false
            } else if (c == '\\') {
                inEscape = true
            } else {
                result.append(c)
            }
        }

        return result.toString()
    }

    fun parseAddress(addressPart: String, instrs: instructions): Int {
        if (!addressPart.startsWith("$")) {
            throw MASMException("Address must start with $", instrs.currentLine, instrs.currentlineContents, "Error in parsing address")
        }

        val addressStr = addressPart.substring(1)
        val memoryAddress = try {
            addressStr.toInt()
        } catch (e: NumberFormatException) {
            common.ReadRegister(addressStr)
        }

        if (memoryAddress < 0 || memoryAddress >= common.MAX_MEMORY) {
            throw MASMException("Invalid memory address: $memoryAddress", instrs.currentLine, instrs.currentlineContents, "Error in parsing address")
        }

        return memoryAddress
    }


    
    fun isValidRegister(reg: String?): Boolean {
        return reg != null && arrayOf(
            "RAX", "RBX", "RCX", "RDX", "RSI", "RDI", "RIP", "RSP", "RBP",
            "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9",
            "R10", "R11", "R12", "R13", "R14", "R15", "RFLAGS"
        ).contains(reg)
    }
}