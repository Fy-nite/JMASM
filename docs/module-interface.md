# Module Native Interface (MNI) Documentation

## Overview
The Module Native Interface (MNI) is a system that allows Micro-Assembly to interface with Java libraries and custom modules at runtime. It provides a bridge between the assembly language and native Java code through annotations and a module registry system.

## Key Components

### Annotations
- `@MNIClass(value)`: Marks a Java class as an MNI module
- `@MNIFunction(module, name)`: Marks methods that can be called from Micro-Assembly

### Core Classes
- `ModuleRegistry`: Singleton that manages module registration and lookup
- `MNIMethodObject`: Contains execution context and utilities for memory/register access
- `ModuleInit`: Handles loading and initialization of modules

## Usage Example

### Java Module Definition
```java
@MNIClass("math")
public class MathModule {
    @MNIFunction(module = "math", name = "add")
    public static void add(MNIMethodObject obj) {
        int value1 = obj.getRegister(obj.reg1);
        int value2 = obj.getRegister(obj.reg2);
        obj.setRegister(obj.reg1, value1 + value2);
    }
}
```

### Micro-Assembly Usage
```wasm
MNI math.add R1 R2  ; Calls the math module's add function
```

## Memory and Register Access
- Modules can read/write memory using `MNIMethodObject.readMemory()` and `writeMemory()`
- Register access through `getRegister()` and `setRegister()`
- Stack operations available via `push()` and `pop()`

more operations should be coming in the near future.

## Module Loading
- Modules can be loaded from JAR files in the modules directory
- Built-in modules can be registered programmatically
- Module methods are discovered through reflection and annotations

## Project Directory Structure
The project automatically creates and manages several directories under the root folder:

```
MASM_ROOT/
├── logs/         # Log files and execution traces
├── modules/      # JAR files containing MNI modules 
├── config/       # Configuration files
└── temp/         # Temporary files during execution
```

### Directory Initialization
The system automatically creates these directories on startup if they don't exist. Default paths can be configured through system properties:

```properties
masm.root.dir=/path/to/root      # Root directory for all MASM files
masm.modules.dir=${root}/modules  # Modules directory
masm.logs.dir=${root}/logs       # Logs directory
masm.config.dir=${root}/config   # Config directory
masm.temp.dir=${root}/temp       # Temporary files
```

If no root directory is specified, the system defaults to the user's home directory under `.masm/`.
