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

/**
 *
 * @author Martin Škurla
 */
class TypeHelper {

    private static final String TRUE_LITERAL_VALUE = "true";
    private static final String FALSE_LITERAL_VALUE = "false";
    private static final String WHOLE_NUMBER_REGEX = "\\d+";
    private static final String REAL_NUMBER_REGEX = "\\d+(\\.\\d+)?";
    private static final String BOOLEAN_REGEX = "(" + TRUE_LITERAL_VALUE + "|" + FALSE_LITERAL_VALUE + ")";
    private static final String CHARACTER_REGEX = "\\w";
    private static final String STRING_REGEX = "\\w+";

    private TypeHelper() {
    }

    public static boolean isWholeNumber(Object object) {
        Class<?> clazz = object.getClass();

        return clazz == Byte.class
                || clazz == Short.class
                || clazz == Integer.class
                || clazz == Long.class;
    }

    public static boolean isWholeNumberArray(Object object) {
        Class<?> componentType = object.getClass().getComponentType();

        return componentType == byte.class
                || componentType == short.class
                || componentType == int.class
                || componentType == long.class;
    }

    public static boolean isRealNumber(Object object) {
        Class<?> clazz = object.getClass();

        return clazz == Float.class
                || clazz == Double.class;
    }

    public static boolean isRealNumberArray(Object object) {
        Class<?> componentType = object.getClass().getComponentType();

        return componentType == float.class
                || componentType == double.class;
    }

    public static boolean isCharacter(Object object) {
        return object.getClass() == Character.class;
    }

    public static boolean isCharacterArray(Object object) {
        return object.getClass().getComponentType() == char.class;
    }

    public static boolean isBoolean(Object object) {
        return object.getClass() == Boolean.class;
    }

    public static boolean isBooleanArray(Object object) {
        return object.getClass().getComponentType() == boolean.class;
    }

    public static boolean isString(Object object) {
        return object.getClass() == String.class;
    }

    public static boolean isStringArray(Object object) {
        return object.getClass().getComponentType() == String.class;
    }

    public static boolean isArray(Object object) {
        return object.getClass().isArray();
    }

    public static Long parseWholeNumber(String input) throws NotParsableException {
        try {
            return Long.valueOf(input);
        } catch (NumberFormatException nfe) {
            throw new NotParsableException();
        }
    }

    public static Long[] parseWholeNumberArray(String input) throws NotParsableException {
        String[] tokenizedInput = ArrayTokenizer.tokenizeInput(input, WHOLE_NUMBER_REGEX);

        Long[] array = new Long[tokenizedInput.length];

        for (int index = 0; index < tokenizedInput.length; index++) {
            array[index] = parseWholeNumber(tokenizedInput[index]);
        }

        return array;
    }

    public static Double parseRealNumber(String input) throws NotParsableException {
        try {
            return Double.valueOf(input);
        } catch (NumberFormatException nfe) {
            throw new NotParsableException();
        }
    }

    public static Double[] parseRealNumberArray(String input) throws NotParsableException {
        String[] tokenizedInput = ArrayTokenizer.tokenizeInput(input, REAL_NUMBER_REGEX);

        Double[] array = new Double[tokenizedInput.length];

        for (int index = 0; index < tokenizedInput.length; index++) {
            array[index] = parseRealNumber(tokenizedInput[index]);
        }

        return array;
    }

    public static Boolean parseBoolean(String input) throws NotParsableException {
        if (input.equals(TRUE_LITERAL_VALUE)) {
            return Boolean.TRUE;
        } else if (input.equals(FALSE_LITERAL_VALUE)) {
            return Boolean.FALSE;
        } else {
            throw new NotParsableException();
        }
    }

    public static Boolean[] parseBooleanArray(String input) throws NotParsableException {
        String[] tokenizedInput = ArrayTokenizer.tokenizeInput(input, BOOLEAN_REGEX);

        Boolean[] array = new Boolean[tokenizedInput.length];

        for (int index = 0; index < tokenizedInput.length; index++) {
            array[index] = parseBoolean(tokenizedInput[index]);
        }

        return array;
    }

    public static Character parseCharacter(String input) throws NotParsableException {
        if (input.length() == 1) {
            return Character.valueOf(input.charAt(0));
        } else {
            throw new NotParsableException();
        }
    }

    public static Character[] parseCharacterArray(String input) throws NotParsableException {
        String[] tokenizedInput = ArrayTokenizer.tokenizeInput(input, CHARACTER_REGEX);

        Character[] array = new Character[tokenizedInput.length];

        for (int index = 0; index < tokenizedInput.length; index++) {
            array[index] = parseCharacter(tokenizedInput[index]);
        }

        return array;
    }

    public static String[] parseStringArray(String input) throws NotParsableException {
        return ArrayTokenizer.tokenizeInput(input, STRING_REGEX);
    }

    public static Object parseFromString(String input, Class<?> finalType) throws NotParsableException {
        if (finalType == String.class) {
            return input;
        }

        if (finalType == Long.class) {
            return parseWholeNumber(input);
        } else if (finalType == Double.class) {
            return parseRealNumber(input);
        } else if (finalType == Boolean.class) {
            return parseBoolean(input);
        } else if (finalType == Character.class) {
            return parseCharacter(input);
        } else if (finalType == String[].class) {
            return parseStringArray(input);
        } else if (finalType == Long[].class) {
            return parseWholeNumberArray(input);
        } else if (finalType == Double[].class) {
            return parseRealNumberArray(input);
        } else if (finalType == Boolean[].class) {
            return parseBooleanArray(input);
        } else if (finalType == Character[].class) {
            return parseCharacterArray(input);
        } else {
            throw new AssertionError();
        }
    }

    private static class ArrayTokenizer {

        private static final String ARRAY_LITERAL_START_REGEX = "\\[";
        private static final String ARRAY_LITERAL_END_REGEX = "\\]";
        private static final String ARRAY_LITERAL_SEPARATOR_REGEX = ",";

        private ArrayTokenizer() {
        }

        private static String removeWhitespacesFromArrayLiteral(String arrayLiteral) {
            return arrayLiteral.replaceAll("\\s*" + ARRAY_LITERAL_START_REGEX + "\\s*", ARRAY_LITERAL_START_REGEX).replaceAll("\\s*" + ARRAY_LITERAL_SEPARATOR_REGEX + "\\s*", ARRAY_LITERAL_SEPARATOR_REGEX).replaceAll("\\s*" + ARRAY_LITERAL_END_REGEX + "\\s*", ARRAY_LITERAL_END_REGEX);
        }

        private static String removeStartAndEndCharacterFromArrayLiteral(String arrayLiteral) {
            return arrayLiteral.replaceAll(ARRAY_LITERAL_START_REGEX, "").replaceAll(ARRAY_LITERAL_END_REGEX, "");
        }

        private static String generateRegularExpression(String arrayItemRegex) {
            // for example => [ digit+ (,digit+)* ]
            return String.format("%s%s(%s%s)*%s",
                    ARRAY_LITERAL_START_REGEX,
                    arrayItemRegex,
                    ARRAY_LITERAL_SEPARATOR_REGEX,
                    arrayItemRegex,
                    ARRAY_LITERAL_END_REGEX);
        }

        private static String[] tokenizeInput(String input, String arrayItemRegex) throws NotParsableException {
            String parsedInput = removeWhitespacesFromArrayLiteral(input);
            String regex = generateRegularExpression(arrayItemRegex);

            if (!parsedInput.matches(regex)) {
                throw new NotParsableException();
            }
            parsedInput = removeStartAndEndCharacterFromArrayLiteral(parsedInput);

            return parsedInput.split(ARRAY_LITERAL_SEPARATOR_REGEX);
        }
    }
}
