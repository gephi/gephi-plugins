package com.polinode.polinodeexporter.model;

import java.util.HashMap;

/**
 *
 * @author stephenrogers
 */
public class EdgeElement {
    public String id;
    public String source;
    public String target;
    public HashMap attributes;
    
    public EdgeElement() {
	attributes = new HashMap();
    }
}
