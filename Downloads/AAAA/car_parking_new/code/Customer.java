package mock_assignment.car_parking_new;

import java.time.LocalDateTime;

public class Customer {
	
	private String name;
	private String contactNo;
	private String address;
	private LocalDateTime checkInTime;
	
	public Customer(String name, String contactNo, String address, LocalDateTime checkInTime) {
		this.name = name;
		this.contactNo = contactNo;
		this.address = address;
		this.checkInTime = checkInTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContactNo() {
		return contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public LocalDateTime getCheckInTime() {
		return checkInTime;
	}

	public void setCheckInTime(LocalDateTime checkInTime) {
		this.checkInTime = checkInTime;
	}

}
