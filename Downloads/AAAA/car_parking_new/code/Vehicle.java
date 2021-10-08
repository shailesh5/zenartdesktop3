package mock_assignment.car_parking_new;

public class Vehicle {

	private String regNo;
	private String modelName;
	private String color;
	private Customer customer;
	
	public Vehicle(String regNo, String modelName, String color, Customer customer) {
		this.regNo = regNo;
		this.modelName = modelName;
		this.color = color;
		this.customer = customer;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
}
