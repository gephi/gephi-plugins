/*
Copyright (C) 2015  Scott A. Hale
Website: http://www.scotthale.net/

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/license
 */
package uk.ac.ox.oii.jsonexporter.model;

/**
 *
 * @author shale
 */
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

