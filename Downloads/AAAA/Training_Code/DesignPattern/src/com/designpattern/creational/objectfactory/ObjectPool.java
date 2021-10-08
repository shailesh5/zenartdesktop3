package com.designpattern.creational.objectfactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

public class ObjectPool<T extends Poolable> {
	
	private BlockingQueue<T> availablePool;
	
	public ObjectPool(Supplier<T> creator, int count){
		availablePool = new LinkedBlockingQueue();
		
		for(int i=0; i < count; i++) {
			availablePool.offer(creator.get());
		}
	}

	public T get() {
		try {
			System.out.println("Before get :"+availablePool.size());
			return availablePool.take();
		}catch(InterruptedException e) {
			System.err.print("take() was interrupted");
		}
		return null;
	}
	
	public void release(T obj) {
		obj.reset();
		try {
			availablePool.put(obj);	
			System.out.println("After release :"+availablePool.size());
		}catch(InterruptedException e) {
			System.err.print("take() was interrupted");
		}
		
	}
}
