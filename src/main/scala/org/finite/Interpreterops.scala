package org.finite

import org.finite.ModuleManager.MNIMethodObject
import org.finite.ModuleManager.annotations.{MNIClass, MNIFunction}
import org.finite.Exceptions.MNIException

@MNIClass("InterpreterOps")
object Interpreterops {
  // Keep track of virtual memory instances
  private var virtualMemories = Map[Int, Array[Int]]()
  private var virtualRegisters = Map[Int, Array[Int]]()
  private var nextHandle = 1

  @MNIFunction(module = "InterpreterOps", name = "createVirtualMemory")
  def createVirtualMemory(obj: MNIMethodObject): Unit = {
    val size = obj.getArgument(0)
    if (size <= 0) {
      throw new MNIException("Invalid memory size", "InterpreterOps", "createVirtualMemory")
    }
    
    val handle = nextHandle
    nextHandle += 1
    virtualMemories += (handle -> new Array[Int](size))
    common.WriteRegister("RFLAGS", handle)
  }

  @MNIFunction(module = "InterpreterOps", name = "readVirtual")
  def readVirtual(obj: MNIMethodObject): Unit = {
    val handle = obj.getArgument(0)
    val address = obj.getArgument(1)
    val destReg = obj.getArgumentRegister(2)
    
    virtualMemories.get(handle) match {
      case Some(memory) if address >= 0 && address < memory.length =>
        common.WriteRegister(destReg, memory(address))
      case _ =>
        throw new MNIException("Invalid memory handle or address", "InterpreterOps", "readVirtual")
    }
  }

  @MNIFunction(module = "InterpreterOps", name = "writeVirtual")
  def writeVirtual(obj: MNIMethodObject): Unit = {
    val handle = obj.getArgument(0)
    val address = obj.getArgument(1)
    val value = obj.getArgument(2)
    
    virtualMemories.get(handle) match {
      case Some(memory) if address >= 0 && address < memory.length =>
        memory(address) = value
      case _ =>
        throw new MNIException("Invalid memory handle or address", "InterpreterOps", "writeVirtual")
    }
  }

  @MNIFunction(module = "InterpreterOps", name = "createRegisterBank")
  def createRegisterBank(obj: MNIMethodObject): Unit = {
    val numRegisters = obj.getArgument(0)
    if (numRegisters <= 0) {
      throw new MNIException("Invalid number of registers", "InterpreterOps", "createRegisterBank")
    }
    
    val handle = nextHandle
    nextHandle += 1
    virtualRegisters += (handle -> new Array[Int](numRegisters))
    common.WriteRegister("RFLAGS", handle)
  }

  @MNIFunction(module = "InterpreterOps", name = "readRegister")
  def readRegister(obj: MNIMethodObject): Unit = {
    val handle = obj.getArgument(0)
    val regNum = obj.getArgument(1)
    val destReg = obj.getArgumentRegister(2)
    
    virtualRegisters.get(handle) match {
      case Some(registers) if regNum >= 0 && regNum < registers.length =>
        common.WriteRegister(destReg, registers(regNum))
      case _ =>
        throw new MNIException("Invalid register bank handle or register number", "InterpreterOps", "readRegister")
    }
  }

  @MNIFunction(module = "InterpreterOps", name = "writeRegister")
  def writeRegister(obj: MNIMethodObject): Unit = {
    val handle = obj.getArgument(0)
    val regNum = obj.getArgument(1)
    val value = obj.getArgument(2)
    
    virtualRegisters.get(handle) match {
      case Some(registers) if regNum >= 0 && regNum < registers.length =>
        registers(regNum) = value
      case _ =>
        throw new MNIException("Invalid register bank handle or register number", "InterpreterOps", "writeRegister")
    }
  }

  @MNIFunction(module = "InterpreterOps", name = "parseInstruction")
  def parseInstruction(obj: MNIMethodObject): Unit = {
    val instrStr = obj.readString(obj.getArgument(0))
    val resultAddr = obj.getArgument(1)
    
    // Parse instruction into components
    val parts = instrStr.trim.split("\\s+", 4)
    if (parts.isEmpty) {
      throw new MNIException("Empty instruction", "InterpreterOps", "parseInstruction")
    }
    
    // Write instruction parts to memory structure
    obj.writeMemory(resultAddr, parts(0)) // opcode
    if (parts.length > 1) obj.writeMemory(resultAddr + 1, parts(1))
    if (parts.length > 2) obj.writeMemory(resultAddr + 2, parts(2))
    if (parts.length > 3) obj.writeMemory(resultAddr + 3, parts(3))
  }

  @MNIFunction(module = "InterpreterOps", name = "isValidOpcode")
  def isValidOpcode(obj: MNIMethodObject): Unit = {
    val opcode = obj.readString(obj.getArgument(0))
    val isValid = Set(
      "MOV", "ADD", "SUB", "MUL", "DIV", "CMP",
      "JMP", "JE", "JNE", "CALL", "RET", "HLT",
      "PUSH", "POP", "AND", "OR", "XOR", "NOT"
    ).contains(opcode.toUpperCase)
    
    common.WriteRegister("RFLAGS", if (isValid) 1 else 0)
  }
}
