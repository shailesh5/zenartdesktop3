package mock_assignment.car_parking_new.users;

import mock_assignment.car_parking_new.CarParking;

public class Admin implements User {
	
	public boolean addOperator(CarParking parking, Operator operator) {
		 return parking.getOperators().add(operator);
	}
	
	public boolean removeOperator(CarParking parking, Operator operator) {
		return parking.getOperators().remove(operator);
	}

}
