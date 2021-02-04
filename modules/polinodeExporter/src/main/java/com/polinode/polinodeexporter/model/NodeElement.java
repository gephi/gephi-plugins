/*
 Copyright Polinode, 2017
 */
package com.polinode.polinodeexporter.model;

import java.util.HashMap;

public class NodeElement {
    public String id;
    public HashMap<String, Object> attributes;
    public double x;
    public double y;
    public String color;
    public double size;
    public String type;
    public ElementImage image;
    public ElementIcon icon;

    public NodeElement() {
	attributes = new HashMap<String, Object>();
    }
}

