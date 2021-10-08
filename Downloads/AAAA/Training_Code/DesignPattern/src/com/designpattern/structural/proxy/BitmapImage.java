package com.designpattern.structural.proxy;

import javafx.geometry.Point2D;

public class BitmapImage implements Image{
	
	private String name;
	private Point2D location;
	
	public BitmapImage(String name) {
		this.name = name;
	}
	
	@Override
	public void setLocation(Point2D point2d) {
		location = point2d;
		
	}
	@Override
	public Point2D getLocation() {
		return location;
	}
	@Override
	public void render() {
		System.out.println("Rendered ---> "+this.name);
		
	}
	

	

}
