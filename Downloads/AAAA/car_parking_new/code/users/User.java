package mock_assignment.car_parking_new.users;

import mock_assignment.car_parking_new.CarParking;
import mock_assignment.car_parking_new.ParkingSlot;
import mock_assignment.car_parking_new.Vehicle;
import mock_assignment.car_parking_new.billing.BillingSystem;
import mock_assignment.car_parking_new.billing.Receipt;

public interface User {
	
	public default ParkingSlot checkIn(CarParking parking, Vehicle vehicle) throws Exception {
		return parking.allocateSlot(vehicle);
	}

	public default void checkOut(CarParking carParking, Vehicle vehicle) {
		Receipt receipt = BillingSystem.generateBill(vehicle);
		BillingSystem.dayCollection.add(receipt);
		BillingSystem.printReceipt(receipt);
		carParking.releaseSlot(vehicle);
	}

	public default boolean checkAvailability(CarParking carParking) {
		return carParking.isParkingSlotAvailable();
	}
	
}
