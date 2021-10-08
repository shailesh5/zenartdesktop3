package mock_assignment.car_parking_new.billing;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import mock_assignment.car_parking_new.Vehicle;

public class BillingSystem {
	
	public static ArrayList<Receipt> dayCollection = new ArrayList<>();
	
	public static Receipt generateBill(Vehicle vehicle) {
		
		Receipt receipt = new Receipt();
		receipt.setOwnerName(vehicle.getCustomer().getName());
		receipt.setModelName(vehicle.getModelName());
		receipt.setRegNo(vehicle.getRegNo());
		receipt.setPrice(40.0);
		receipt.setTotalHours(ChronoUnit.HOURS.between(LocalDateTime.now(), vehicle.getCustomer().getCheckInTime()));
		receipt.setTotalAmount(receipt.getPrice()*receipt.getTotalHours());
		
		return receipt;
	}
	
	public static void printReceipt(Receipt receipt) {
		System.out.println(receipt.toString());
	}

}
