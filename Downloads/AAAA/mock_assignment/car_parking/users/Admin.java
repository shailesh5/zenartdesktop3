package mock_assignment.car_parking.users;

public class Admin implements User {
	
	public void markAbandoned(int slotId, String reason) {
		slot.markAbandoned(slotId, reason);
	}
	
	public boolean addOperator() {
		return false;
	}
	
	public boolean removeOperator() {
		return false;
	}

}
