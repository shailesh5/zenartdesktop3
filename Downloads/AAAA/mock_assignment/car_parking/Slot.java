package mock_assignment.car_parking;

public class Slot {
	
	private Allocable[] slots;
	
	public Slot() {
		slots = new Customer[10];
	}
	
	public void allocate(int slotId, Customer cust) throws Exception {
		
		Customer allocatedCust = (Customer) slots[slotId];
		if(allocatedCust != null) {
			throw new Exception("Slot Already Allocated.");
		}
		allocatedCust = cust;
	}
	
	public void release(int slotId) {
		slots[slotId] = null;
	}
	
	public int getAvailableSlot() throws Exception {
		
		int slotId = 0;
		for(int i = 0; i < 10; i++) {
			if(slots[i] == null) {
				slotId = i;
				break;
			}
		}
		
		if(slotId > 0) {
			return slotId;
		}else {
			throw new Exception("No Slot Available.");
		}
	}

	public void viewSlots() {
		
		Customer c = null; 
		Abandoned ab = null;
		for(int i=0; i<10; i++) {
			
			if(slots[i] instanceof Customer) {
				c = (Customer) slots[i] ;
				if(c == null)
					System.out.println("Slot Id : "+i+" Is Available");
				else {
					System.out.println("Slot Id : "+i+" Is Occupied By Vehicle Having Number : "+c.getVehicle().getVehicleRegNumber());
				}
			}else {
				ab = (Abandoned) slots[i] ;
				System.out.println("Slot Id : "+i+" Is Abandoned ");
			}
			 
		}
	}
	
	public void markAbandoned(int slotId, String reason) {
		slots[slotId] = new Abandoned(reason);
	}
}
