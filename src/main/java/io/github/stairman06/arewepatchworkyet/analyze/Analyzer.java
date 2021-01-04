package io.github.stairman06.arewepatchworkyet.analyze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class Analyzer {
    /**
     * Default names to skip, these are usually not a problem.
     * Skips JDK classes, Mojang (DFU, Brigadier) classes, FastUtil, Fabric (@Environment annotation) classes, Google (GSON, Guava), ASM, and LWJGL classes
     * TODO: Make this user configurable, so the user can choose to skip unneeded classes (e.g. Create references JEI for API, but it's not required)
     * TODO: HARDCODED JEI EXCEPTION IS BAD!!
     */
    private static final String[] DEFAULT_SKIPPED_NAMES = {"java/", "com/mojang/", "org/apache/", "it/unimi/", "net/fabricmc/", "com/google/", "org/objectweb/", "org/lwjgl/", "mezz/jei/"};


    public static HashSet<String> implementedClasses = new HashSet<>();

    /**
     * Contains class members that are known to be implemented.
     * The key is the class which implements it, the value is a HashSet containing {@link ClassMember}
     */
    public static HashMap<String, HashSet<ClassMember>> implementedClassMembers = new HashMap<>();

    public static HashMap<String, HashSet<ClassMember>> forgeClassMembers = new HashMap<>();

    /**
     * Contains methods that are known to be needed.
     * The key is the class which contains the method, the value is a TreeSet containing {@link ClassMember}
     * The reason for TreeSet is alphabetical ordering
     */
    public static HashMap<String, TreeSet<ClassMember>> neededClassMembers = new HashMap<>();

    /**
     * Stores a list of defined classes and their respective superclasses.
     * This also stores implemented interfaces.
     */
    public static HashMap<String, HashSet<String>> superCache = new HashMap<>();

    public static HashMap<String, HashSet<String>> forgeSuperCache = new HashMap<>();

    public static HashMap<Stat, Integer> statMap = new HashMap<>();

    /**
     * Analyze a class member, and add it to {@literal neededMethods} if needed.
     *
     * @param owner      The owner of this method
     * @param name       The name of this method
     * @param descriptor THe descriptor of this method
     */
    public static void analyzeClassMember(ClassMember.Type type, String owner, String name, String descriptor, String caller) {
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

        boolean exists = checkIfClassMemberExists(type, owner, name, descriptor);

        if (!exists) {
            // The method does not exist in the target owner,
            // so we need to add it to neededMethods
            addNeededClassMember(owner, new ClassMember(type, name, descriptor, owner, caller));
        }
    }

    /**
     * Adds a method to {@literal neededMethods}, and creates the TreeSet if needed
     *
     * @param owner       Owner of the method
     * @param classMember Instance of {@link ClassMember}
     */
    private static void addNeededClassMember(String owner, ClassMember classMember) {
        if (!neededClassMembers.containsKey(owner)) {
            neededClassMembers.put(owner, new TreeSet<>());
        }

        neededClassMembers.get(owner).add(classMember);
    }

    private static boolean checkIfClassMemberExistsForge(ClassMember.Type type, String owner, String name, String descriptor) {
        for (ClassMember classMember : forgeClassMembers.getOrDefault(owner, new HashSet<>())) {
            if (classMember.name.equals(name) && classMember.descriptor.equals(descriptor)) {
                addNeededClassMember(owner, new ClassMember(type, name, descriptor, owner, null));
                return true;
            }
        }

        for (String superClass : forgeSuperCache.getOrDefault(owner, new HashSet<>())) {
            if (checkIfClassMemberExistsForge(type, superClass, name, descriptor)) {
                return true;
            }
        }

        return false;
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
    private static boolean checkIfClassMemberExists(ClassMember.Type type, String owner, String name, String descriptor) {
        if (owner.equals("java/util/AbstractList") || owner.equals("java/util/List") || owner.equals("java/util/ArrayList") || owner.equals("java/util/AbstractSet")) {
            if (name.equals("add") || name.equals("forEach") || name.equals("addAll") || name.equals("iterator") || name.equals("stream") || name.equals("isEmpty")) {
                return true; // hacky handling for JDK classes
            }
        }

        if (owner.startsWith("com/google/gson")) {
            if (name.equals("getType")) {
                return true; // hardcoded exception that is bad
            }
        }

        if (owner.startsWith("java/lang/Throwable")) {
            if (name.equals("printStackTrace")) {
                return true;
            }
        }

        if (owner.startsWith("java/util/function/Function")) {
            if (name.equals("apply") && descriptor.equals("(Ljava/lang/Object;)Ljava/lang/Object;")) {
                return true;
            }
        }

        if (owner.startsWith("java/util/function/Predicate")) {
            if (name.equals("and") || name.equals("or") || name.equals("negate")) {
                return true;
            }
        }

        if (owner.startsWith("java/")) { // If we've gotten here and are checking a JDK class, it doesn't exist
            return false;
        }

        // Skips intermediary method_ and field_ names, which are Minecraft methods and fields
        // This isn't really a good idea and should be replaced with a proper check for Minecraft classes sometime
        if (name.startsWith("method_") || name.startsWith("field_")) {
            return true;
        }

        // Loop through implemented methods to see if it exists
        for (ClassMember classMember : implementedClassMembers.getOrDefault(owner, new HashSet<>())) {
            if (classMember.name.equals(name) && classMember.descriptor.equals(descriptor)) {
                return true;
            }
        }

        // Loop through superclasses to check if it exists
        for (String superclass : superCache.getOrDefault(owner, new HashSet<>())) {
            if (checkIfClassMemberExists(type, superclass, name, descriptor)) {
                return true;
            }

            if (checkIfClassMemberExistsForge(type, superclass, name, descriptor)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isClassImplemented(String clazz) {
        // Special casing for JDK classes and Mojang (DFU, Brigadier) classes - need to make this better
        if (clazz.startsWith("java/") || clazz.startsWith("com/mojang/")) {
            return true;
        }

        return implementedClasses.contains(clazz);
    }

    public enum Stat {
        MIXIN,
        JS_COREMOD
    }

    public static void addStat(Stat stat) {
        statMap.put(stat, statMap.getOrDefault(stat, 0) + 1);
    }
}
