package com.designpattern.creational.objectfactory;

import com.sun.javafx.geom.Point2D;

public class Client {
	
	private static final ObjectPool<Bitmap> bitmapPool = new ObjectPool<>(()->new Bitmap("test.bmp"), 5);
	public static void main(String[] args) {
		
		Bitmap bmp1 = bitmapPool.get();
		bmp1.setLocation(new Point2D(10, 2));
		
		Bitmap bmp2 = bitmapPool.get();
		bmp2.setLocation(new Point2D(5, 3));

		bitmapPool.release(bmp1);
		bitmapPool.release(bmp2);
		
		
	}

}
