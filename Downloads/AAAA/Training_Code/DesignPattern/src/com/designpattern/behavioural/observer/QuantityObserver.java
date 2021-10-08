package com.designpattern.behavioural.observer;

public class QuantityObserver implements OrderObserver {

	@Override
	public void updated(Order order) {
		
		int qty = order.getQuantity();
		
		if(qty >= 5) {
			order.setShippingCost(2);
		}else {
			order.setShippingCost(5);
		}
	}

}
