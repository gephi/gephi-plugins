/*
 Copyright Scott A. Hale, 2016
 */
package uk.ac.ox.oii.sigmaexporter.model;

public class GraphNode  extends GraphElement{
	
	private String label;
	private double x;
	private double y;
        private String id;
			
	public GraphNode(String id) {
		super();
                this.id=id;
		label="";
		size=1;
		x = 100 - 200*Math.random();
		y = 100 - 200*Math.random();
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
	
	

}

