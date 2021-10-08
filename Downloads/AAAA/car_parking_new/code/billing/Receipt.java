package mock_assignment.car_parking_new.billing;

public class Receipt {
	
	private static int receiptNumber = 0;
	
	private String regNo;
	private String modelName;
	private String ownerName;
	private Long totalHours;
	private Double price;
	private Double totalAmount;
	
	public Receipt() {
		receiptNumber++;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public static int getReceiptNumber() {
		return receiptNumber;
	}

	public static void setReceiptNumber(int receiptNumber) {
		Receipt.receiptNumber = receiptNumber;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public Long getTotalHours() {
		return totalHours;
	}

	public void setTotalHours(Long totalHours) {
		this.totalHours = totalHours;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Override
	public String toString() {
		return "Receipt [regNo=" + regNo + ", modelName=" + modelName + ", ownerName=" + ownerName + ", totalHours="
				+ totalHours + ", price=" + price + ", totalAmount=" + totalAmount + "]";
	}

	
	
}
