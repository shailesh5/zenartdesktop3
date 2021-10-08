package com.designpattern.structural.proxy;

import com.sun.javafx.geom.Point2D;

public class ImageFactory {

	public static Image getImage(String name) {
		return new ImageProxy(name);
	}
	
}
