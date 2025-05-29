package org.finite.ModuleManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleRegistry {
    private static final ModuleRegistry instance = new ModuleRegistry();
    private final Map<String, Map<String, Map<String, Method>>> moduleMap;
     public static String[] registers = {"RAX", "RBX", "RCX", "RDX", "RBP", "RSP", "RIP", "RDI", "RSI", "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12", "R13", "R14", "R15", "RFLAGS", "FPR0", "FPR1", "FPR2", "FPR3", "FPR4", "FPR5", "FPR6", "FPR7", "FPR8", "FPR9", "FPR10", "FPR11", "FPR12", "FPR13", "FPR14", "FPR15"};
    public static String[] instructions = {"MOV", "ADD", "SUB", "MUL", "DIV", "AND", "OR", "XOR", "NOT", "SHL", "SHR", "CMP", "JMP", "JE", "JNE", "JG", "JGE", "JL", "JLE", "CALL", "RET", "PUSH", "POP", "HLT", "NOP","OUT"};
    
    // Add a map for custom library classes (for string-returning functions)
    private final Map<String, Class<?>> customLibClasses = new ConcurrentHashMap<>();

    // Add maps for includes and macros
    private final Map<String, Method> includeProviders = new HashMap<>();
    private final Map<String, Method> macroProviders = new HashMap<>();

    private ModuleRegistry() {
        // Use ConcurrentHashMap for better concurrency
        moduleMap = new ConcurrentHashMap<>();
    }

    public static ModuleRegistry getInstance() {
        return instance;
    }

    public void registerModule(String jarName, String className, String methodName, Method method) {
        moduleMap.computeIfAbsent(jarName, k -> new HashMap<>())
                .computeIfAbsent(className, k -> new HashMap<>())
                .put(methodName, method);
    }

    public Method getMethod(String jarName, String className, String methodName) {
        Map<String, Map<String, Method>> classMap = moduleMap.get(jarName);
        if (classMap == null) return null;
        Map<String, Method> methodMap = classMap.get(className);
        if (methodMap == null) return null;
        return methodMap.get(methodName);
    }

    public void registerMNIModule(String moduleName, String functionName, Method method) {
        registerModule("mni", moduleName, functionName, method);
    }

    public Method getMNIMethod(String moduleName, String functionName) {
        return getMethod("mni", moduleName, functionName);
    }

    public Map<String, Map<String, Map<String, Method>>> getModuleMap() {
        return moduleMap;
    }

    public void registerCustomLib(String libName, Class<?> clazz) {
        customLibClasses.put(libName, clazz);
    }

    public Class<?> getCustomLib(String libName) {
        return customLibClasses.get(libName);
    }

    // Register a method as a custom include or macro provider
    public void registerAnnotatedProviders(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(org.finite.ModuleManager.annotations.MNIInclude.class)) {
                String name = method.getAnnotation(org.finite.ModuleManager.annotations.MNIInclude.class).name();
                includeProviders.put(name, method);
            }
            if (method.isAnnotationPresent(org.finite.ModuleManager.annotations.MNIMacro.class)) {
                String name = method.getAnnotation(org.finite.ModuleManager.annotations.MNIMacro.class).name();
                macroProviders.put(name, method);
            }
        }
    }
    /*
     * * Call an include provider method by name.
     * * @param name The name of the include provider.
     * * @param args The arguments to pass to the include provider.
     * * @return The result of the include provider method.
     * * @throws Exception if the include provider is not found or if an error occurs during invocation.
     * * This method looks up the include provider by name and invokes it with the provided arguments.
     * * It is assumed that the include provider is a static method in a class.
     */
    public Object callIncludeProvider(String name, Object... args) throws Exception {
        Method method = includeProviders.get(name);
        if (method == null) {
            throw new RuntimeException("Include provider not found: " + name);
        }
        return method.invoke(null, args);
    }
    /*
     * * Call a macro provider method by name.
     * * * @param name The name of the macro provider.
     * * * @param args The arguments to pass to the macro provider.
     * * * @return The result of the macro provider method.
     * * * @throws Exception if the macro provider is not found or if an error occurs during invocation.
     * * * This method looks up the macro provider by name and invokes it with the provided arguments.
     * * * It is assumed that the macro provider is a static method in a class.
     */
    public Object callMacroProvider(String name, Object... args) throws Exception {
        Method method = macroProviders.get(name);
        if (method == null) {
            throw new RuntimeException("Macro provider not found: " + name);
        }
        return method.invoke(null, args);
    }

    /*
     * * Call a string-returning function from a custom library.
     * * @param libName The name of the custom library.
     * * @param functionName The name of the function to call.
     * * @param args The arguments to pass to the function.
     * * @return The result of the function call.
     * * @throws Exception if the function is not found or if an error occurs during invocation.
     */
    public Object callCustomLibFunction(String libName, String functionName, Object... args) throws Exception {
        Class<?> clazz = customLibClasses.get(libName);
        if (clazz == null) {
            throw new RuntimeException("Custom lib not found: " + libName);
        }
        Method method = clazz.getMethod(functionName, toClassArray(args));
        return method.invoke(null, args);
    }
    
    private Class<?>[] toClassArray(Object[] args) {
        if (args == null) return new Class<?>[0];
        Class<?>[] arr = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            arr[i] = args[i] == null ? Object.class : args[i].getClass();
        }
        return arr;
    }

    /*
     * * Get an include provider method by name.
     * * @param name The name of the include provider.
     * * @return The method object representing the include provider.
     * * @throws RuntimeException if the include provider is not found.
     */
    public Method getIncludeProvider(String name) {
        return includeProviders.get(name);
    }

    /*
     * * Get a macro provider method by name.
     * * @param name The name of the macro provider.
     * * @return The method object representing the macro provider.
     * * @throws RuntimeException if the macro provider is not found.
     */
    public Method getMacroProvider(String name) {
        if (registers.equals(name) || instructions.equals(name) || name.startsWith("R") || name.startsWith("L")) {
            // if the name is a register or instruction, we do nothing
            return null; // nothing to do, just return null  
        } else {
            //throw new RuntimeException("Macro provider not found: " + name);
        }
        Method method = macroProviders.get(name);
        if (method == null) {
            //could be a instruction
        }
        return method;
    }
}
