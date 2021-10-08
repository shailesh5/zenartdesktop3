package mock_assignment.car_parking_new;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import mock_assignment.car_parking_new.users.Operator;

public class CarParking {
	
	HashMap<ParkingSlot, Vehicle> parking;
	List<Operator> operators;
	
	public CarParking(int capacity) {
		parking = new HashMap<>(capacity);
		ParkingSlot slot = null;
		for(int i = 0; i < 10; i++) {
			slot = new ParkingSlot("PS00"+i, 10.0*i);
			parking.put(slot, null);
		}
		
		operators = new ArrayList<Operator>(); 
	}

	private ParkingSlot getNearestAvailableSlot() {
		List<Entry<ParkingSlot, Vehicle>> availableSlots = parking.entrySet().stream().filter((e)->e.getValue() == null).sorted((e1, e2)-> e1.getKey().getDistance().compareTo(e2.getKey().getDistance())).collect(Collectors.toList());
		return availableSlots != null ? availableSlots.get(0).getKey() : null;
	}
	
	public ParkingSlot allocateSlot(Vehicle vehicle) throws Exception {
		
		ParkingSlot slot = getNearestAvailableSlot();
		
		if(slot == null)
			throw new Exception("No Slot Available.");
		else
		   parking.put(slot, vehicle);
		
		return slot;
	}
	
	public void releaseSlot(Vehicle vehicle) {
		ParkingSlot slot = parking.entrySet().stream().filter((e)->e.getValue().equals(vehicle)).collect(Collectors.toList()).get(0).getKey();
		parking.put(slot, null);
	}
	
	public boolean isParkingSlotAvailable() {
		ParkingSlot slot = getNearestAvailableSlot();
		boolean flag = false;
		
		if(slot != null)
		   flag = true;
		
		return flag;
	}

	public List<Operator> getOperators() {
		return operators;
	}

	
}
