package io.github.stairman06.arewepatchworkyet.analyze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class Analyzer {
    /**
     * Default names to skip, these are usually not a problem.
     * Skips JDK classes, Mojang (DFU, Brigadier) classes, FastUtil, Fabric (@Environment annotation) classes, and Google (GSON, Guava) classes
     * TODO: Make this user configurable, so the user can choose to skip unneeded classes (e.g. Create references JEI for API, but it's not required)
     */
    private static final String[] DEFAULT_SKIPPED_NAMES = {"java/", "com/mojang/", "org/apache/", "it/unimi/", "net/fabricmc/", "com/google/"};

    /**
     * Contains methods that are known to be implemented.
     * The key is the class which implements it, the value is a HashSet containing {@link Method}
     */
    public static HashMap<String, HashSet<Method>> implementedMethods = new HashMap<>();

    /**
     * Contains methods that are known to be needed.
     * The key is the class which contains the method, the value is a TreeSet containing {@link Method}
     * The reason for TreeSet is alphabetical ordering
     */
    public static HashMap<String, TreeSet<Method>> neededMethods = new HashMap<>();

    /**
     * Stores a list of defined classes and their respective superclasses.
     * This also stores implemented interfaces.
     */
    public static HashMap<String, HashSet<String>> superCache = new HashMap<>();

    /**
     * Analyze a method name, and add it to {@literal neededMethods} if needed.
     *
     * @param owner      The owner of this method
     * @param name       The name of this method
     * @param descriptor THe descriptor of this method
     */
    public static void analyzeMethodName(String owner, String name, String descriptor) {
        if (owner.startsWith("[L")) { // If this is an array, trim off the array indicator
            owner = owner.substring(2);
        }

        for (String toSkip : DEFAULT_SKIPPED_NAMES) {
            if (owner.startsWith(toSkip)) {
                return;
            }
        }

        // Skip methods that are default in Object
        if (name.equals("clone") || name.equals("hashCode") || name.equals("toString") || name.equals("equals")) {
            return; // Object methods are useless
        }

        // Skip methods that are default in Enum
        if (name.equals("name") || name.equals("ordinal")) {
            return;
        }

        boolean exists = checkIfMethodExists(owner, name, descriptor);

        if (!exists) {
            // The method does not exist in the target owner,
            // so we need to add it to neededMethods
            addNeededMethod(owner, new Method(name, descriptor, owner));
        }
    }

    /**
     * Adds a method to {@literal neededMethods}, and creates the TreeSet if needed
     *
     * @param owner  Owner of the method
     * @param method Instance of {@link Method}
     */
    private static void addNeededMethod(String owner, Method method) {
        if (!neededMethods.containsKey(owner)) {
            neededMethods.put(owner, new TreeSet<>());
        }

        neededMethods.get(owner).add(method);
    }


    /**
     * This checks if a method is known to exist in a given owner.
     * It will also check superclasses and interfaces.
     *
     * @param owner      The owner of the method
     * @param name       Name of the method
     * @param descriptor The method's descriptor
     * @return true if the method exists
     */
    private static boolean checkIfMethodExists(String owner, String name, String descriptor) {
        if (owner.startsWith("java/")) { // If we've gotten here and are checking a JDK class, it doesn't exist
            return false;
        }

        // Skips intermediary method_ names, which are Minecraft methods
        // This isn't really a good idea and should be replaced with a proper check for Minecraft classes sometime
        if (name.startsWith("method_")) {
            return true;
        }

        // Loop through implemented methods to see if it exists
        for (Method method : implementedMethods.getOrDefault(owner, new HashSet<>())) {
            if (method.name.equals(name) && method.descriptor.equals(descriptor)) {
                return true;
            }
        }

        // Loop through superclasses to check if it exists
        for (String superclass : superCache.getOrDefault(owner, new HashSet<>())) {
            if (checkIfMethodExists(superclass, name, descriptor)) {
                return true;
            }
        }

        return false;
    }
}
