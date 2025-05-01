# MASM Debug REPL Documentation

The MASM Debug REPL (Read-Eval-Print Loop) is an interactive debugger for the MASM assembly language interpreter. It provides various commands to inspect and manipulate program state during execution.

## Basic Usage

To start the debug REPL, run MASM with the debug flag:

```bash
java -jar masm.jar --debug
```

The REPL will show a cyan prompt `>` indicating it's ready to accept commands.

## Commands

### Memory Operations

#### `dumpmemory [address] [length]`
Displays memory contents in a hexadecimal and ASCII view.
- `address`: Starting address (hex) (optional, defaults to 0)
- `length`: Number of bytes to display (optional, defaults to 256)

Example:
```
> dumpmemory 100 32
```

#### `inspect memory <address> [length]`
Detailed view of memory at specified address.
- `address`: Memory address to inspect (hex)
- `length`: Number of bytes (optional, defaults to 16)

Example:
```
> inspect memory FF 32
```

### Register Operations

#### `dumpregisters`
Shows all register values in a formatted view, including:
- General purpose registers (RAX, RBX, RCX, RDX)
- Special registers (RIP, RFLAGS)
- Additional registers (R0-R15)

#### `inspect register <name>`
Shows detailed view of a specific register:
- Hexadecimal value
- Decimal value
- Binary representation

Example:
```
> inspect register RAX
```

### Breakpoint Management

#### `break <address>` or `b <address>`
Sets or removes a breakpoint at specified address.

Example:
```
> break FF    # Sets breakpoint at address 0xFF
> b 100      # Sets breakpoint at address 0x100
```

### Execution Control

#### `step` or `s`
Enables single-step mode.

#### `continue` or `c`
Continues normal execution.

### Direct Instructions

The REPL allows executing any MASM instruction directly:

```
> MOV RAX 42    # Moves value 42 into RAX
> ADD RAX RBX   # Adds RBX to RAX
```

### Utility Commands

#### `help`
Shows available commands and their usage.

#### `exit`, `quit`, or `q`
Exits the debugger.

## Features

### Command History
- Use up/down arrow keys to navigate through previous commands
- History is preserved between sessions

### Colored Output
- Error messages in red
- Prompt in cyan
- Breakpoints highlighted in red in memory view

### Hex/ASCII View
Memory dumps show both hexadecimal values and ASCII representation where printable characters are displayed.

## Tips and Tricks

1. Use tab completion for commands (if supported by terminal)
2. Memory addresses can be specified in hexadecimal without '0x' prefix
3. Break points persist until explicitly removed or debugger is closed
4. Use `inspect` for detailed views of specific memory locations or registers
5. The ASCII view in memory dump helps identify string data

## Error Handling

The debugger provides detailed error messages for:
- Invalid commands
- Invalid memory addresses
- Invalid register names
- Execution errors
- Syntax errors

Each error message includes:
- Error description
- Line number (where applicable)
- Instruction context

## Example Session

```
> dumpregisters
RAX: 00000000  RBX: 00000000  RCX: 00000000  RDX: 00000000
...

> mov rax 42
Instruction executed successfully (result: 0)

> inspect register rax
Register RAX:
Hex: 0x0000002A
Dec: 42
Bin: 00000000000000000000000000101010

> break FF
Added breakpoint at 0xFF

> dumpmemory FF 16
00FF: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  ................
```

## Best Practices

1. Use breakpoints strategically at key points in your code
2. Inspect memory and registers frequently to understand program state
3. Use step mode for detailed analysis of program flow
4. Take advantage of the ASCII view in memory dumps to spot string data
5. Use command shortcuts (like 'b' for break) for faster debugging

## Known Limitations

1. Maximum memory size is defined by `common.MAX_MEMORY`
2. Breakpoints are not persistent between program runs
3. No conditional breakpoints
4. No watch points for memory/register changes
