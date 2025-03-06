#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include "common.h"

#define MAX_CODE 32768

// Remove the opcode_names array as it's now in common.h

void print_operand(enum OperandType type, int value) {
    switch (type) {
        case OP_TYPE_NONE:
            break;
        case OP_TYPE_REGISTER:
            if (value >= 0 && value < 8) {
                printf("%s", registers[value]);
            } else {
                printf("R%d", value);
            }
            break;
        case OP_TYPE_IMMEDIATE:
            printf("%d", value);
            break;
        case OP_TYPE_LABEL:
            printf("#L%d", value); // Generate synthetic label names
            break;
        case OP_TYPE_MEMORY:
            printf("$%d", value);
            break;
    }
}

void disassemble(const char *filename) {
    FILE *fp = fopen(filename, "rb");
    if (!fp) {
        printf("Error: Cannot open file %s\n", filename);
        return;
    }

    // Read and verify header
    struct BytecodeHeader header;
    if (!read_bytecode_header(fp, &header)) {
        printf("Error: Invalid bytecode file format\n");
        fclose(fp);
        return;
    }

    // Verify instruction count is reasonable
    if (header.num_instr > MAX_CODE) {
        printf("Error: Too many instructions (%d)\n", header.num_instr);
        fclose(fp);
        return;
    }

    struct Instruction instr;
    int addr = 0;
    int *label_locations = calloc(header.num_instr, sizeof(int));
    
    if (!label_locations) {
        printf("Error: Memory allocation failed\n");
        fclose(fp);
        return;
    }

    // First pass: identify jump targets for labels
    while (read_instruction(fp, &instr) && addr < header.num_instr) {
        if (instr.opcode >= OP_JMP && instr.opcode <= OP_JL) {
            if (instr.op1_type == OP_TYPE_LABEL && instr.op1 < header.num_instr) {
                label_locations[instr.op1] = 1;
            }
        }
        addr++;
    }

    // Reset file position after header
    fseek(fp, sizeof(struct BytecodeHeader), SEEK_SET);
    addr = 0;

    // Second pass: print disassembly
    printf("; MicroASM disassembly\n\n");
    
    while (read_instruction(fp, &instr) && addr < header.num_instr) {
        // Print label if this address is a jump target
        if (label_locations[addr]) {
            printf("LBL L%d\n", addr);
        }

        // Print instruction with indent
        printf("    %s ", opcode_names[instr.opcode]);
        
        // Print operands
        if (instr.op1_type != OP_TYPE_NONE) {
            print_operand(instr.op1_type, instr.op1);
        }

        if (instr.op2_type != OP_TYPE_NONE) {
            printf(" ");
            print_operand(instr.op2_type, instr.op2);
        }

        printf("\n");
        addr++;
    }

    free(label_locations);
    fclose(fp);
}

int disasm_main(int argc, char *argv[]) {
    if (argc != 2) {
        printf("Usage: %s <input.bin>\n", argv[0]);
        return 1;
    }

    disassemble(argv[1]);
    return 0;
}

#ifdef STANDALONE
int main(int argc, char *argv[]) {
    return disasm_main(argc, argv);
}
#endif
