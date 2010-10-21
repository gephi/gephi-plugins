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

import java.util.HashMap;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeType;

/**
 *
 * @author Martin Škurla
 */
class Neo4jArrayToGephiAttributeTypeMapper {

    private static final Map<Class<?>, AttributeType> mapper;

    private Neo4jArrayToGephiAttributeTypeMapper() {
    }

    static {
        mapper = new HashMap<Class<?>, AttributeType>();

        mapper.put(byte.class, AttributeType.LIST_BYTE);
        mapper.put(short.class, AttributeType.LIST_SHORT);
        mapper.put(int.class, AttributeType.LIST_INTEGER);
        mapper.put(long.class, AttributeType.LIST_LONG);
        mapper.put(float.class, AttributeType.LIST_FLOAT);
        mapper.put(double.class, AttributeType.LIST_DOUBLE);
        mapper.put(boolean.class, AttributeType.LIST_BOOLEAN);
        mapper.put(char.class, AttributeType.LIST_CHARACTER);
        mapper.put(String.class, AttributeType.LIST_STRING);
    }

    public static AttributeType map(Object neo4jArray) {
        Class<?> componentType = neo4jArray.getClass().getComponentType();

        return mapper.get(componentType);
    }
}
