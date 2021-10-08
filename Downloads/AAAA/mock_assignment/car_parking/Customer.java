package mock_assignment.car_parking;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Customer implements Allocable{
	
	private final double CHARGE_PER_HOUR = 40;
	private String customerName;
	private Vehicle vehicle;
	private LocalDateTime checkInTime;
	
	public Customer(String name, Vehicle vn, LocalDateTime dt) {
		this.customerName = name;
		this.vehicle = vn;
		this.checkInTime = dt;
	}

	public double calculateFee() {
		long diffInHours = ChronoUnit.HOURS.between(LocalDateTime.now(), checkInTime);
		return diffInHours*CHARGE_PER_HOUR;
	}
	
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public LocalDateTime getCheckInTime() {
		return checkInTime;
	}
	public void setCheckInTime(LocalDateTime checkInTime) {
		this.checkInTime = checkInTime;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}
}
