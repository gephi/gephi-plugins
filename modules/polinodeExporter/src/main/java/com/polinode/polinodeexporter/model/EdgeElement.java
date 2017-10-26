/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
