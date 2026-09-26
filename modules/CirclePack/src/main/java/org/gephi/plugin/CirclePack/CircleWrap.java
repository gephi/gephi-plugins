package org.gephi.plugin.CirclePack;

import java.util.HashMap;

public class CircleWrap {
    public CircleWrap wrappedCircle = null;//hacky d3 reference thing

    HashMap<String, CircleWrap> children = new HashMap<>();
    public Object id;
    public CircleWrap next;
    public CircleWrap previous;

    public double x;
    public double y;
    public double r = 999;

    public CircleWrap(String id) {
        this.id = id;
    }

    public CircleWrap(CircleWrap circle) {
        this.wrappedCircle = circle;
        this.r = 1010101;//TEMP to check if this is used (shouldn't be!)
    }

    public CircleWrap() {
    }

    public CircleWrap(double radius) {
        this.r = radius;
    }

    public CircleWrap(double x, double y, double r) {
        if (r < 0) throw new java.lang.IllegalArgumentException("CircleWrap cannot have negative radius: " + r);
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public void addChild(String id, CircleWrap child) {
        this.children.put(id, child);
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getId() {
        return this.id;
    }

    public CircleWrap getChild(String id){
        if (!children.containsKey(id)){
            CircleWrap circleWrap = new CircleWrap();
            children.put(id, circleWrap);
        }
        return children.get(id);
    }

    public void applyPositionToChildren() {
        if (hasChildren()) {
            for (CircleWrap child : this.children.values()) {
                child.x += this.x;
                child.y += this.y;
                child.applyPositionToChildren();
            }
        }
    }

    public String toConStr() {
        return "new CircleWrap(" + this.x + "," + this.y + "," + this.r + ")";
    }

    @Override
    public String toString() {
        return "[CircleWrap:" + this.x + "," + this.y + "," + this.r + "]";
    }

    public boolean hasChildren() {
        return this.children != null && this.children.size() > 0;
    }

}
