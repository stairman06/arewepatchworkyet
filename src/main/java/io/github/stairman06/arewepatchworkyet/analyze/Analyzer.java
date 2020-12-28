package io.github.stairman06.arewepatchworkyet.analyze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class Analyzer {
    private static final String[] DEFAULT_SKIPPED_NAMES = { "java/", "com/mojang/", "org/apache/", "it/unimi/", "net/fabricmc/", "com/google/" };
    /**
     * Contains methods that are known to be implemented.
     * The key is the class which implements it, the value is a HashSet containing {@link Method}
     */
    public static HashMap<String, HashSet<Method>> implementedMethods = new HashMap<>();

    public static HashMap<String, TreeSet<Method>> neededMethods = new HashMap<>();

    /**
     * Stores a list of defined classes and their respective superclasses.
     * This also stores implemented interfaces.
     */
    public static HashMap<String, HashSet<String>> superCache = new HashMap<>();

    public static void analyzeMethodName(String owner, String name, String descriptor) {
        if(owner.startsWith("[L")) {
            owner = owner.substring(2);
        }

        for(String toSkip : DEFAULT_SKIPPED_NAMES) {
            if(owner.startsWith(toSkip)) {
                return;
            }
        }

        if(name.equals("clone") || name.equals("hashCode") || name.equals("toString") || name.equals("equals")) {
            return; // Object methods are useless
        }

        if(name.equals("name") || name.equals("ordinal")) {
            return; // enum methods, technically conflict could exist but eh
        }

        boolean exists = checkIfMethodExists(owner, name, descriptor);

        if(!exists) {
            // The method does not exist in the target owner,
            // so we need to add it to neededMethods
            addNeededMethod(owner, new Method(name, descriptor));
        }
    }

    private static void addNeededMethod(String owner, Method method) {
        if (!neededMethods.containsKey(owner)) {
            neededMethods.put(owner, new TreeSet<>());
        }

        neededMethods.get(owner).add(method);
    }

    /**
     * This checks if a method is known to exist in a given owner.
     * It will also check superclasses and interfaces.
     * @param owner The owner of the method
     * @param name Name of the method
     * @param descriptor The method's descriptor
     * @return true if the method exists
     */
    private static boolean checkIfMethodExists(String owner, String name, String descriptor) {
        if(owner.startsWith("java/")) { // If we've gotten here and are checking a JDK class, it doesn't exist
            return false;
        }

        // Loop through implemented methods to see if it exists
        for(Method method : implementedMethods.getOrDefault(owner, new HashSet<>())) {
            if (method.name.equals(name) && method.descriptor.equals(descriptor)) {
                return true;
            }
        }

        // Loop through superclasses to check if it exists
        for(String superclass : superCache.getOrDefault(owner, new HashSet<>()))  {
            if(checkIfMethodExists(superclass, name, descriptor)) {
                return true;
            }
        }

        return false;
    }
}
