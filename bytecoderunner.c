#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "common.h"

#define MAX_MEMORY 32768
#define DATA_START MEMORY_REGION_DATA
#define CODE_START MEMORY_REGION_CODE
#define STACK_START MEMORY_REGION_STACK

struct VM {
    int registers[16];    // General purpose registers
    int memory[MAX_MEMORY];  // Main memory
    int sp;              // Stack pointer
    int bp;              // Base pointer
    int flags;           // CPU flags
    struct Instruction current;  // Current instruction
    int pc;             // Program counter
};

#define FLAG_ZERO  (1 << 0)
#define FLAG_NEG   (1 << 1)
#define FLAG_CARRY (1 << 2)

static void init_vm(struct VM* vm) {
    memset(vm->registers, 0, sizeof(vm->registers));
    memset(vm->memory, 0, sizeof(vm->memory));
    vm->sp = STACK_START;
    vm->bp = STACK_START;
    vm->flags = 0;
    vm->pc = 0;
}

static int get_operand_value(struct VM* vm, enum OperandType type, int value) {
    printf("Getting operand value: type=%d, value=%d\n", type, value);  // Debug line
    switch(type) {
        case OP_TYPE_REGISTER:
            printf("Register %d value: %d\n", value, vm->registers[value]);  // Debug line
            return vm->registers[value];
        case OP_TYPE_IMMEDIATE:
            printf("Immediate value: %d\n", value);  // Debug line
            return value;
        case OP_TYPE_MEMORY:
            printf("Memory at %d: %d\n", value, vm->memory[value]);  // Debug line
            return vm->memory[value];
        case OP_TYPE_LABEL:
            return value;
        default:
            return 0;
    }
}

static void set_operand_value(struct VM* vm, enum OperandType type, int operand, int value) {
    printf("Setting value: type=%d, operand=%d, value=%d\n", type, operand, value);  // Debug line
    switch(type) {
        case OP_TYPE_REGISTER:
            vm->registers[operand] = value;
            printf("Register %d now contains: %d\n", operand, vm->registers[operand]);  // Debug line
            break;
        case OP_TYPE_MEMORY:
            vm->memory[operand] = value;
            printf("Memory at %d now contains: %d\n", operand, vm->memory[operand]);  // Debug line
            break;
        default:
            printf("Warning: Attempting to set value for non-writable operand type\n");  // Debug line
            break;
    }
}

static void update_flags(struct VM* vm, int result) {
    vm->flags = 0;
    if (result == 0) vm->flags |= FLAG_ZERO;
    if (result < 0) vm->flags |= FLAG_NEG;
}

static int load_program(const char* filename, struct VM* vm) {
    FILE* fp = fopen(filename, "rb");
    if (!fp) {
        printf("Error: Cannot open file %s\n", filename);
        return 0;
    }

    struct BytecodeHeader header;
    if (!read_bytecode_header(fp, &header)) {
        printf("Error: Invalid bytecode format\n");
        fclose(fp);
        return 0;
    }

    printf("Loading program with %d instructions\n", header.num_instr);

    // Read instructions into a temporary buffer first
    struct Instruction* program = malloc(header.num_instr * sizeof(struct Instruction));
    if (!program) {
        printf("Error: Memory allocation failed\n");
        fclose(fp);
        return 0;
    }

    // Read all instructions
    for (int i = 0; i < header.num_instr; i++) {
        if (!read_instruction(fp, &program[i])) {
            printf("Error: Failed to read instruction %d\n", i);
            free(program);
            fclose(fp);
            return 0;
        }
        // Copy instructions to code region
        memcpy(&vm->memory[CODE_START + i * sizeof(struct Instruction)], 
               &program[i], 
               sizeof(struct Instruction));
    }

    free(program);
    fclose(fp);
    return header.num_instr;
}

static int execute_instruction(struct VM* vm) {
    // Get instruction from code region
    struct Instruction* instr = (struct Instruction*)&vm->memory[CODE_START + vm->pc];
    int op1, op2, result;
    
    printf("\nExecuting instruction at PC=%d: opcode=%d\n", vm->pc, instr->opcode);
    printf("Instruction: op1_type=%d, op2_type=%d, op1=%d, op2=%d\n",
           instr->op1_type, instr->op2_type, instr->op1, instr->op2);
    
    op1 = get_operand_value(vm, instr->op1_type, instr->op1);
    op2 = get_operand_value(vm, instr->op2_type, instr->op2);

    switch(instr->opcode) {
        case OP_NOP:
            break;

        case OP_MOV:
            printf("MOV: Setting value type=%d, operand=%d to %d\n",
                   instr->op1_type, instr->op1, op2);
            set_operand_value(vm, instr->op1_type, instr->op1, op2);
            break;

        case OP_ADD:
            printf("ADD: %d + %d\n", op1, op2);
            result = op1 + op2;
            set_operand_value(vm, instr->op1_type, instr->op1, result);
            update_flags(vm, result);
            break;

        case OP_SUB:
            result = op1 - op2;
            set_operand_value(vm, instr->op1_type, instr->op1, result);
            update_flags(vm, result);
            break;

        case OP_MUL:
            result = op1 * op2;
            set_operand_value(vm, instr->op1_type, instr->op1, result);
            update_flags(vm, result);
            break;

        case OP_DIV:
            if (op2 == 0) {
                printf("Error: Division by zero\n");
                return 0;
            }
            result = op1 / op2;
            set_operand_value(vm, instr->op1_type, instr->op1, result);
            update_flags(vm, result);
            break;

        case OP_CMP:
            result = op1 - op2;
            update_flags(vm, result);
            break;

        case OP_JMP:
            vm->pc = op1;
            return 1;

        case OP_JE:
            if (vm->flags & FLAG_ZERO) {
                vm->pc = op1;
                return 1;
            }
            break;

        case OP_JNE:
            if (!(vm->flags & FLAG_ZERO)) {
                vm->pc = op1;
                return 1;
            }
            break;

        case OP_JG:
            if (!(vm->flags & FLAG_ZERO) && !(vm->flags & FLAG_NEG)) {
                vm->pc = op1;
                return 1;
            }
            break;

        case OP_JL:
            if (vm->flags & FLAG_NEG) {
                vm->pc = op1;
                return 1;
            }
            break;

        case OP_OUT:
            printf("%d\n", op1);
            break;

        case OP_HLT:
            return 0;
    }

    vm->pc += sizeof(struct Instruction);  // Increment by instruction size
    return 1;
}

int bytecoderunner_main(int argc, char* argv[]) {
    if (argc != 2) {
        printf("Usage: %s <input.bin>\n", argv[0]);
        return 1;
    }

    struct VM vm;
    init_vm(&vm);

    if (!load_program(argv[1], &vm)) {
        return 1;
    }

    // Main execution loop
    while (1) {
        // Load next instruction
        vm.current = *(struct Instruction*)&vm.memory[vm.pc];
        
        // Execute it
        if (!execute_instruction(&vm)) {
            break;
        }
    }

    return 0;
}

#ifdef STANDALONE
int main(int argc, char* argv[]) {
    return bytecoderunner_main(argc, argv);
}
#endif
