/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.neo4j.plugin.impl;

import org.gephi.neo4j.plugin.api.ClassNotFulfillRequirementsException;
import org.gephi.neo4j.plugin.api.FileSystemClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

import static org.gephi.neo4j.plugin.impl.FileSystemClassLoaderImpl.ReflectionUtils.*;


/**
 *
 * @author Martin Škurla
 */
@ServiceProvider(service = FileSystemClassLoader.class)
public class FileSystemClassLoaderImpl extends ClassLoader implements FileSystemClassLoader {

    private final Map<String, Class<?>> nameToClassMapper = new HashMap<String, Class<?>>();
    private Class<?>[] requiredImplementedInterfaces;
    private boolean requirePublicNonparamConstructor;

    public FileSystemClassLoaderImpl() {
        super(FileSystemClassLoaderImpl.class.getClassLoader());
    }

    @Override
    public Class<?> loadClass(File file) throws ClassNotFoundException {
        return loadClass(file, false, new Class<?>[0]);
    }

    @Override
    public Class<?> loadClass(File file, boolean requirePublicNonparamConstructor, Class<?>... requiredImplementedInterfaces)
            throws ClassNotFoundException {
        this.requiredImplementedInterfaces = requiredImplementedInterfaces;
        this.requirePublicNonparamConstructor = requirePublicNonparamConstructor;

        if (file == null || requiredImplementedInterfaces == null) {
            throw new NullPointerException();
        }

        return super.loadClass(file.getAbsolutePath(), true);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException, NoClassDefFoundError, IllegalArgumentException, ClassNotFulfillRequirementsException {
        validatePrerequisites(name);

        Class<?> loadedClass = loadClassFromFile(new File(name));

        validateClassRequirements(loadedClass);

        return loadedClass;
    }

    private void validatePrerequisites(String name) throws ClassNotFoundException, IllegalArgumentException {
        File classFile = new File(name);
        if (!(classFile.exists() && classFile.isFile())) {
            throw new ClassNotFoundException("Class " + name + " was not found.");
        }

        if (!name.endsWith(".class")) {
            throw new IllegalArgumentException("Name of the file must have suffix '.class'.");
        }

        if (!areAllInterfaces(requiredImplementedInterfaces)) {
            throw new IllegalArgumentException("Not all of " + Arrays.toString(requiredImplementedInterfaces)
                    + " are interfaces.");
        }
    }

    private void validateClassRequirements(Class<?> loadedClass) throws ClassNotFulfillRequirementsException {
        if (!isClass(loadedClass)) {
            throw new IllegalArgumentException("Type " + loadedClass + " is not class.");
        }

        if (requirePublicNonparamConstructor && !hasPublicNonparamConstructor(loadedClass)) {
            throw new ClassNotFulfillRequirementsException(
                    "Class " + loadedClass.getName() + " does not have nonparam constructor.");
        }

        if (requiredImplementedInterfaces.length > 0 && !isImplementingAllInterfaces(loadedClass, requiredImplementedInterfaces)) {
            String message = String.format("Class %s does not implement all of interfaces: %s",
                    loadedClass.getName(),
                    Arrays.toString(requiredImplementedInterfaces));

            throw new ClassNotFulfillRequirementsException(message);
        }
    }

    private Class<?> loadClassFromFile(File classFile) throws NoClassDefFoundError {
        int classFileLength = (int) classFile.length();
        byte[] classFileContent = new byte[classFileLength];

        readFileContent(classFile, classFileContent);

        String binaryClassName = classFile.getName().substring(0, classFile.getName().lastIndexOf("."));

        try {
            // very special case when we try to load class with default package, but it was already
            // loaded as part of other class
            // this situation only exists when other loaded class extends currently class located without
            // package and currently class in in classpath
            return super.loadClass(binaryClassName);
        } catch (ClassNotFoundException cnfe) {
            // do nothing, explanation is above
        }

        File parentDirectory = classFile;
        while (true) {
            if (nameToClassMapper.containsKey(binaryClassName)) {
                return nameToClassMapper.get(binaryClassName);
            }

            try {
                Class<?> definedClass = defineClass(binaryClassName, classFileContent, 0, classFileLength);

                nameToClassMapper.put(binaryClassName, definedClass);

                return definedClass;
            } catch (NoClassDefFoundError error) {
                parentDirectory = parentDirectory.getParentFile();
                if (parentDirectory == null) {
                    throw new NoClassDefFoundError("Class file " + classFile.getName()
                            + " is not located in proper directory structure.");
                }

                binaryClassName = parentDirectory.getName() + "." + binaryClassName;
            }
        }
    }

    private void readFileContent(File sourceFile, byte[] targetByteArray) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(sourceFile);
            fis.read(targetByteArray);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    static class ReflectionUtils {

        private ReflectionUtils() {
        }

        static boolean isImplementingAllInterfaces(Class<?> type, Class<?>... interfaceTypes) {
            List<Class<?>> requiredInterfaces = Arrays.asList(interfaceTypes);

            Set<Class<?>> typeInterfaces = new HashSet<Class<?>>();

            while (type != null) {
                typeInterfaces.addAll(Arrays.asList(type.getInterfaces()));
                type = type.getSuperclass();
            }

            return typeInterfaces.containsAll(requiredInterfaces);
        }

        static boolean hasPublicNonparamConstructor(Class<?> type) {
            Constructor<?>[] constructors = type.getConstructors();

            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterTypes().length == 0
                        && Modifier.isPublic(constructor.getModifiers())) {
                    return true;
                }
            }

            return false;
        }

        static boolean isClass(Class<?> type) {
            return !type.isAnnotation()
                    && !type.isArray()
                    && !type.isInterface()
                    && !type.isPrimitive();
        }

        static boolean areAllInterfaces(Class<?>... interfaceTypes) {
            int interfaceTypesCount = 0;
            for (Class<?> interfaceType : interfaceTypes) {
                if (interfaceType.isInterface()) {
                    interfaceTypesCount++;
                }
            }

            return interfaceTypesCount == interfaceTypes.length;
        }
    }
}
