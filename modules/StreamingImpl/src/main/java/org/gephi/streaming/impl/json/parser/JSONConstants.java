package org.gephi.streaming.impl.json.parser;

import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;

public class JSONConstants {
	
    public enum Types {
        AN("an", EventType.ADD, ElementType.NODE),
        CN("cn", EventType.CHANGE, ElementType.NODE),
        DN("dn", EventType.REMOVE, ElementType.NODE),
        AE("ae", EventType.ADD, ElementType.EDGE),
        CE("ce", EventType.CHANGE, ElementType.EDGE),
        DE("de", EventType.REMOVE, ElementType.EDGE),
        CG("cg", EventType.CHANGE, ElementType.GRAPH);

        private String value;
        private EventType eventType;
        private ElementType elementType;
        private Types(String value, EventType eventType, ElementType elementType) {
                this.value = value;
                this.eventType = eventType;
                this.elementType = elementType;
        }

        public String value() {
                return value;
        }

        public EventType getEventType() {
            return eventType;
        }

        public ElementType getElementType() {
            return elementType;
        }

        public static Types fromString(String strtype) {
            for (Types type: Types.values()) {
                if (type.value.equalsIgnoreCase(strtype)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid type");
        }
    }
	
    public enum Fields {

        ID("id"),
        T("t"), //timestamp
        SOURCE("source"),
        TARGET("target"),
        DIRECTED("directed");
        private String value;

        private Fields(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    };
}
