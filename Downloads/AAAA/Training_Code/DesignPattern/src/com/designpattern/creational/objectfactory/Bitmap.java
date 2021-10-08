package com.designpattern.creational.objectfactory;

import com.sun.javafx.geom.Point2D;

public class Bitmap implements Image{
	
	private Point2D location;
	private String name;
	
	public Bitmap(String name) {
		this.name = name;
	}

	@Override
	public void reset() {
		this.location = null;
		System.out.println("Reset method is called !!!");
		
	}

	@Override
	public void draw() {
		System.out.println("Drawing :"+name+"@"+location);
		
	}

	@Override
	public Point2D getLocation() {
		return this.location;
	}

	@Override
	public void setLocation(Point2D point2d) {
		this.location = point2d;
		
	}

}
