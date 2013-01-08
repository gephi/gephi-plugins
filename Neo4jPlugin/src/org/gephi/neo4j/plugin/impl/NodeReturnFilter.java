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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.gephi.neo4j.plugin.api.FilterDescription;
import org.gephi.neo4j.plugin.api.FilterOperator;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.helpers.Predicate;

/**
 *
 * @author Martin Škurla
 */
class NodeReturnFilter implements Predicate<Path> {

    private final Map<PropertyParsingKey, Object> mapper;
    private final Set<PropertyParsingKey> notParsableProperties;
    private final Collection<FilterDescription> filterDescriptions;
    private final boolean restrictMode;
    private final boolean matchCase;

    NodeReturnFilter(Collection<FilterDescription> filterDescriptions, boolean restrictMode, boolean matchCase) {
        this.filterDescriptions = filterDescriptions;
        this.restrictMode = restrictMode;
        this.matchCase = matchCase;

        this.mapper = new HashMap<PropertyParsingKey, Object>();
        this.notParsableProperties = new HashSet<PropertyParsingKey>();
    }

    @Override
    public boolean accept(Path path) {
        return accept(path.endNode());
    }

    public boolean accept(Node node) {
        for (FilterDescription filterDescription : filterDescriptions) {
            if (node.hasProperty(filterDescription.getPropertyKey())) {
                Object nodePropertyValue = node.getProperty(filterDescription.getPropertyKey());

                boolean isValid =
                        doValidation(nodePropertyValue, filterDescription.getOperator(), filterDescription.getPropertyValue());

                if (isValid == false) {
                    return false;
                }
            } else {
                return !restrictMode;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private <T> T parseValue(String expectedValue, Class<T> finalType) throws NotParsableException {
        PropertyParsingKey key = new PropertyParsingKey(expectedValue, finalType);
        if (notParsableProperties.contains(key)) {
            throw new NotParsableException();
        }

        Object parsedValue = mapper.get(key);

        if (parsedValue == null) {
            try {
                parsedValue = TypeHelper.parseFromString(expectedValue, finalType);
                mapper.put(key, parsedValue);
                return (T) parsedValue;
            } catch (NotParsableException npe) {
                notParsableProperties.add(key);
                throw npe;
            }
        }

        throw new AssertionError();
    }

    private boolean doValidation(Object nodePropertyValue, FilterOperator operator, String expectedValue) {
        try {
            if (TypeHelper.isWholeNumber(nodePropertyValue)) {
                return operator.executeOnWholeNumbers((Number) nodePropertyValue,
                        parseValue(expectedValue, Long.class));
            } else if (TypeHelper.isRealNumber(nodePropertyValue)) {
                return operator.executeOnRealNumbers((Number) nodePropertyValue,
                        parseValue(expectedValue, Double.class));
            } else if (TypeHelper.isBoolean(nodePropertyValue)) {
                return operator.executeOnBooleans((Boolean) nodePropertyValue,
                        parseValue(expectedValue, Boolean.class));
            } else if (TypeHelper.isCharacter(nodePropertyValue)) {
                return operator.executeOnCharacters((Character) nodePropertyValue,
                        parseValue(expectedValue, Character.class),
                        matchCase);
            } else if (TypeHelper.isArray(nodePropertyValue)) {
                if (TypeHelper.isWholeNumberArray(nodePropertyValue)) {
                    return operator.executeOnWholeNumberArrays(nodePropertyValue,
                            parseValue(expectedValue, Long[].class));
                } else if (TypeHelper.isRealNumberArray(nodePropertyValue)) {
                    return operator.executeOnRealNumberArrays(nodePropertyValue,
                            parseValue(expectedValue, Double[].class));
                } else if (TypeHelper.isBooleanArray(nodePropertyValue)) {
                    return operator.executeOnBooleanArrays(nodePropertyValue,
                            parseValue(expectedValue, Boolean[].class));
                } else if (TypeHelper.isCharacterArray(nodePropertyValue)) {
                    return operator.executeOnCharacterArrays(nodePropertyValue,
                            parseValue(expectedValue, Character[].class),
                            matchCase);
                } else if (TypeHelper.isStringArray(nodePropertyValue)) {
                    return operator.executeOnStringArrays(nodePropertyValue,
                            parseValue(expectedValue, String[].class),
                            matchCase);
                } else {
                    throw new AssertionError();
                }
            } else if (TypeHelper.isString(nodePropertyValue)) {
                return operator.executeOnStrings((String) nodePropertyValue, expectedValue, matchCase);
            } else {
                throw new AssertionError();
            }
        } catch (NotParsableException npe) {
            return false;
        }
    }

    private static class PropertyParsingKey {

        private final String textValue;
        private final Class<?> finalType;

        PropertyParsingKey(String textValue, Class<?> finalType) {
            this.textValue = textValue;
            this.finalType = finalType;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PropertyParsingKey)) {
                return false;
            }

            PropertyParsingKey key = (PropertyParsingKey) o;
            return this.finalType == key.finalType
                    || this.textValue.equals(key.textValue);
        }

        @Override
        public int hashCode() {
            return textValue.hashCode() + finalType.hashCode();
        }
    }
}
