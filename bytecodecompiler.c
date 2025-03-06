#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <stdbool.h>
#include "common.h"
#include <stdint.h>

#define MAX_LINE 256
#define CODE_START MEMORY_REGION_CODE
#define MAX_LABELS 1000
#define MAX_CODE 32768

// Ensure BYTECODE_MAGIC is correctly defined as "MASM"
#define BYTECODE_MAGIC "MASM"

// Global variables
struct Label labels[MAX_LABELS];
int label_count = 0;
struct Instruction bytecode[MAX_CODE];
int bytecode_pos = 0;


#ifdef STANDALONE
int main(int argc, char *argv[]) {
    return compiler_main(argc, argv);
}
#endif

int compiler_main(int argc, char *argv[]) {
    if (argc != 3) {
        printf("Usage: %s <input.masm> <output.bin>\n", argv[0]);
        return 1;
    }

    write_bytecode(argv[1], argv[2]);
    return 0;
}

int add_label(const char *name, int address) {
    if (label_count >= MAX_LABELS) return -1;
    strcpy(labels[label_count].name, name);
    labels[label_count].address = address;
    return label_count++;
}

int find_label(const char *name) {
    for (int i = 0; i < label_count; i++) {
        if (strcmp(labels[i].name, name) == 0) {
            return labels[i].address;
        }
    }
    return -1;
}

void parse_operand(char *operand, enum OperandType *type, int *value) {
    if (operand == NULL || operand[0] == '\0') {
        *type = OP_TYPE_NONE;
        *value = 0;
        return;
    }

    // Check for label reference
    if (operand[0] == '#') {
        *type = OP_TYPE_LABEL;
        *value = find_label(operand + 1);
        return;
    }

    // Check for memory reference
    if (operand[0] == '$') {
        *type = OP_TYPE_MEMORY;
        // Use strtol for better number parsing
        char *endptr;
        *value = strtol(operand + 1, &endptr, 10);
        if (*endptr != '\0') {
            printf("Warning: Invalid memory address: %s\n", operand);
        }
        return;
    }

    // Check for register
    int reg = get_register(operand);
    if (reg >= 0) {
        *type = OP_TYPE_REGISTER;
        *value = reg;
        return;
    }

    // Must be immediate value
    // Use strtol for better number parsing
    char *endptr;
    *value = strtol(operand, &endptr, 10);
    *type = OP_TYPE_IMMEDIATE;
    if (*endptr != '\0') {
        printf("Warning: Invalid immediate value: %s\n", operand);
    }
}


void write_bytecode(const char *input_file, const char *output_file) {
    FILE *in = fopen(input_file, "r");
    if (!in) {
        printf("Error: Cannot open input file\n");
        return;
    }

    // First pass: collect labels
    char line[MAX_LINE];
    int current_address = 0;

    while (fgets(line, sizeof(line), in)) {
        char *token = strtok(line, " \t\n");
        if (token == NULL || token[0] == ';') continue;

        if (strcmp(token, "LBL") == 0) {
            token = strtok(NULL, " \t\n");
            if (token) {
                add_label(token, current_address);
            }
        } else {
            current_address++;
        }
    }

    // Reset file for second pass
    rewind(in);
    bytecode_pos = 0;

    // Second pass: generate bytecode
    while (fgets(line, sizeof(line), in)) {
        char *token = strtok(line, " \t\n");
        if (token == NULL || token[0] == ';' || strcmp(token, "LBL") == 0) continue;

        struct Instruction instr = {0};
        instr.opcode = get_opcode(token);

        // Parse first operand
        token = strtok(NULL, " \t\n");
        if (token) {
            parse_operand(token, &instr.op1_type, &instr.op1);
        }

        // Parse second operand
        token = strtok(NULL, " \t\n");
        if (token) {
            parse_operand(token, &instr.op2_type, &instr.op2);
        }

        bytecode[bytecode_pos++] = instr;
    }

    fclose(in);

    // Write bytecode to output file
    FILE *out = fopen(output_file, "wb");
    if (!out) {
        printf("Error: Cannot open output file\n");
        return;
    }

    // Write header first
    write_bytecode_header(out, bytecode_pos);

    // Write each instruction
    for (int i = 0; i < bytecode_pos; i++) {
        // Add additional validation before writing
        if (bytecode[i].opcode > OP_HLT) {
            printf("Warning: Invalid opcode %d at position %d\n", bytecode[i].opcode, i);
            continue;
        }

        printf("Writing instruction %d at address %d: opcode=%d (%s)\n", 
               i, 
               CODE_START + i * sizeof(struct Instruction),
               bytecode[i].opcode,
               opcode_names[bytecode[i].opcode]);

        // Add padding for alignment if needed
      
        
        write_instruction(out, &bytecode[i]);
    }

    fclose(out);
    printf("Compilation successful. Generated %d instructions.\n", bytecode_pos);
}
