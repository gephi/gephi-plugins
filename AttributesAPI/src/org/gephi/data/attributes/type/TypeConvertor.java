/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla <bujacik@gmail.com>, Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.data.attributes.type;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.gephi.data.attributes.api.AttributeType;

/**
 * Class responsible for type manipulation and creation needed in Attributes API.
 *
 * @author Martin Škurla
 * @author Mathieu Bastian
 */
public final class TypeConvertor {

    private static final String CONVERSION_METHOD_NAME = "valueOf";

    private TypeConvertor() {
    }

    /**
     * Creates array of given type from single String value. String value is always parsed by given
     * separator into smaller chunks. Every chunk will represent independent object in final array.
     * The exact conversion process from String value into final type is done by
     * {@link #createInstanceFromString createInstanceFromString} method.
     *
     * @param <T>       type parameter representing final array type
     * @param input     input
     * @param separator separator which will be used in the process of tokenizing input
     * @param finalType type of final array
     * 
     * @return final array
     *
     * @throws NullPointerException     if any of given parameters is null
     * @throws IllegalArgumentException if array of given type cannot be created
     *
     * @see #createInstanceFromString createInstanceFromString
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] createArrayFromString(String input, String separator, Class<T> finalType) {
        if (input == null || separator == null || finalType == null) {
            throw new NullPointerException();
        }

        String[] stringValues = input.split(separator);
        T[] resultList = (T[]) Array.newInstance(finalType, stringValues.length);

        for (int i = 0; i < stringValues.length; i++) {
            String stringValue = stringValues[i].trim();
            T resultValue = null;

            if (finalType == String.class) {
                resultValue = (T) stringValue;
            } else {
                resultValue = TypeConvertor.<T>createInstanceFromString(stringValue, finalType);
            }

            resultList[i] = resultValue;
        }
        return resultList;
    }

    /**
     * Transforms String value to any kind of object with given type. The concrete conversion
     * must be done by the type itself. This assumes, that given type defines at least one of the
     * following:
     * <ul>
     * <li>public constructor with single parameter of type String
     * <li>factory method "valueOf" with single parameter of type String<br />
     * If given type does not definy any of these requirements, IllegalArgumentException will be
     * thrown.
     * 
     * @param <T>       type parameter representing final type
     * @param input     input
     * @param finalType type of final object
     *
     * @return final object
     *
     * @throws NullPointerException     if any of given parameters is null
     * @throws IllegalArgumentException if given type cannot be created
     */
    @SuppressWarnings("unchecked")
    public static <T> T createInstanceFromString(String input, Class<T> finalType) {
        if (input == null || finalType == null) {
            throw new NullPointerException();
        }

        T resultValue = null;

        try {
            Method conversionMethod = finalType.getMethod(CONVERSION_METHOD_NAME, String.class);

            resultValue = (T) conversionMethod.invoke(null, input);
        } catch (NoSuchMethodException e) {
            try {
                Constructor<T> constructor = finalType.getConstructor(String.class);
                resultValue = constructor.newInstance(input);
            } catch (NoSuchMethodException e1) {
                String errorMessage = String.format(
                        "Type '%s' does not have neither method 'T %s(String)' nor  constructor '<init>(String)'...",
                        finalType,
                        CONVERSION_METHOD_NAME);

                throw new IllegalArgumentException(errorMessage);
            } catch (Exception e2) {
            }
        } catch (Exception e) {
        }
        return resultValue;
    }

    /**
     * Converts given array of primitive type into array of wrapper type.
     *
     * @param <T>            type parameter representing final wrapper type
     * @param primitiveArray primitive array
     * 
     * @return wrapper array
     *
     * @throws NullPointerException     if given parameter is null
     * @throws IllegalArgumentException if given parameter is not array or given parameter is not
     *                                  array of primitive type
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] convertPrimitiveToWrapperArray(Object primitiveArray) {
        if (primitiveArray == null) {
            throw new NullPointerException();
        }

        if (!primitiveArray.getClass().isArray()) {
            throw new IllegalArgumentException("Given object is not of primitive array: " + primitiveArray.getClass());
        }

        Class<?> primitiveClass = primitiveArray.getClass().getComponentType();
        Class<T> wrapperClass = (Class<T>) getWrapperFromPrimitive(primitiveClass);
        int arrayLength = Array.getLength(primitiveArray);
        T[] wrapperArray = (T[]) Array.newInstance(wrapperClass, arrayLength);

        for (int i = 0; i < arrayLength; i++) {
            T arrayItem = (T) Array.get(primitiveArray, i);
            wrapperArray[i] = arrayItem;
        }

        return wrapperArray;
    }

    /**
     * Returns wrapper type from given primitive type.
     *
     * @param primitiveType primitive type
     * 
     * @return wrapper type
     *
     * @throws NullPointerException     if given parameter is null
     * @throws IllegalArgumentException if given parameter is not a primitive type
     */
    public static Class<?> getWrapperFromPrimitive(Class<?> primitiveType) {
        if (primitiveType == null) {
            throw new NullPointerException();
        }

        if (primitiveType == byte.class) {
            return Byte.class;
        } else if (primitiveType == short.class) {
            return Short.class;
        } else if (primitiveType == int.class) {
            return Integer.class;
        } else if (primitiveType == long.class) {
            return Long.class;
        } else if (primitiveType == float.class) {
            return Float.class;
        } else if (primitiveType == double.class) {
            return Double.class;
        } else if (primitiveType == boolean.class) {
            return Boolean.class;
        } else if (primitiveType == char.class) {
            return Character.class;
        }

        throw new IllegalArgumentException("Given type '" + primitiveType + "' is not primitive...");
    }

    /**
     * Returns the underlying static type from <code>dynamicType</code> For example
     * returns <code>FLOAT</code> if given type is <code>DYNAMIC_FLOAT</code>.
     * @param dynamicType a dynamic type
     * @return the underlying static type
     * @throws IllegalArgumentException if <code>dynamicType</code> is not dynamic
     */
    public static AttributeType getStaticType(AttributeType dynamicType) {
        if (!dynamicType.isDynamicType()) {
            throw new IllegalArgumentException("Given type '" + dynamicType + "' is not dynamic.");
        }
        switch (dynamicType) {
            case DYNAMIC_BIGDECIMAL:
                return AttributeType.BIGDECIMAL;
            case DYNAMIC_BIGINTEGER:
                return AttributeType.BIGINTEGER;
            case DYNAMIC_BOOLEAN:
                return AttributeType.BOOLEAN;
            case DYNAMIC_BYTE:
                return AttributeType.BYTE;
            case DYNAMIC_CHAR:
                return AttributeType.CHAR;
            case DYNAMIC_DOUBLE:
                return AttributeType.DOUBLE;
            case DYNAMIC_FLOAT:
                return AttributeType.FLOAT;
            case DYNAMIC_INT:
                return AttributeType.INT;
            case DYNAMIC_LONG:
                return AttributeType.LONG;
            case DYNAMIC_SHORT:
                return AttributeType.SHORT;
            case DYNAMIC_STRING:
                return AttributeType.STRING;
            default:
                return null;
        }
    }

    /**
     * Returns the corresponding dynamic type from <code>staticType</code> For example
     * returns <code>DYNAMIC_FLOAT</code> if given type is <code>FLOAT</code>.
     * @param staticType a static type
     * @return the corresponding dynamic type
     * @throws IllegalArgumentException if <code>staticType</code> is not static
     */
    public static AttributeType getDynamicType(AttributeType staticType) {
        if (staticType.isDynamicType()) {
            throw new IllegalArgumentException("Given type '" + staticType + "' is not static.");
        }
        switch (staticType) {
            case BIGDECIMAL:
                return AttributeType.DYNAMIC_BIGDECIMAL;
            case BIGINTEGER:
                return AttributeType.DYNAMIC_BIGINTEGER;
            case BOOLEAN:
                return AttributeType.DYNAMIC_BOOLEAN;
            case BYTE:
                return AttributeType.DYNAMIC_BYTE;
            case CHAR:
                return AttributeType.DYNAMIC_CHAR;
            case DOUBLE:
                return AttributeType.DYNAMIC_DOUBLE;
            case FLOAT:
                return AttributeType.DYNAMIC_FLOAT;
            case INT:
                return AttributeType.DYNAMIC_INT;
            case LONG:
                return AttributeType.DYNAMIC_LONG;
            case SHORT:
                return AttributeType.DYNAMIC_SHORT;
            case STRING:
                return AttributeType.DYNAMIC_STRING;
            default:
                return null;
        }
    }
}
