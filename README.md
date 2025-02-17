# JMASM - Micro-Assembly Interpreter

JMASM is an interpreter for MASM (Micro-Assembly), a simplified assembly language. This interpreter provides a straightforward way to execute MASM code, making it ideal for learning assembly programming concepts and writing complex programs.

## Features

- Interprets MASM (Micro-Assembly) code
- Simple and intuitive syntax
- Cross-platform compatibility

## Installation

ethier download the binary from the release page or build it from source

```bash
# Clone the repository
git clone https://github.com/Fy-nite/JMASM.git

# Change the working directory  
cd JMASM

# Build the project
mvn clean package -P <windows|linux>

```

after building the project you can find the executable in the target folder.

for linux builds it's in target/linux (2 files)
for windows builds it's just MASM.exe inside the target folder

we don't know why our binaries that get created make the linux build have 2 files but it works so we are not complaining.

we are not too sure about the windows build so if you have any issues please let us know


## Usage

```bash
# Basic usage
jmasm -f <filename.masm>

# Example
jmasm -f example.masm
```

## MASM Syntax

MASM uses a simplified assembly syntax. Here's a basic example:

```masm
#include "stdio.io"
lbl main
    ;; set the FD to 1 for stdout
    mov RAX 1
    ;; set the memory address
    mov RBX 10
    ;; call printf 
    call #printf
    hlt
```


## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

[MIT](LICENSE)

## Author

Charlie santana
