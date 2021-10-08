package com.designpattern.structural.proxy;

import com.sun.javafx.geom.Point2D;

public class ImageProxy implements Image{

	private String name;
	private Point2D location;
	private Image img;
	
	public ImageProxy(String name) {
		this.name = name;
	}
	
	@Override
	public void setLocation(Point2D point2d) {
		if(img == null) {
			location = point2d;
		}else {
			img.setLocation(point2d);
		}
		
	}

	@Override
	public Point2D getLocation() {
		if(img == null) {
			return location;
		}else {
			return img.getLocation();
		}
	
	}

	@Override
	public void render() {
		if(img == null) {
			img = new BitmapImage(name);
			if(img.getLocation() == null) {
				img.setLocation(location);
			}
		}
		img.render();		
	}
	
}
