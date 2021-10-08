package mock_assignment.car_parking.users;

import mock_assignment.car_parking.Customer;
import mock_assignment.car_parking.Slot;

public interface User {
	
	static Slot slot = new Slot();
			
	public default void allocateSlot(Customer cust) {
		try {
			int slotId = slot.getAvailableSlot();
			slot.allocate(slotId, cust);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public default void releaseSlot(int slotId) {
		slot.release(slotId);
	}
	
	public default void viewSlots() {
		slot.viewSlots();
	}
	
}
