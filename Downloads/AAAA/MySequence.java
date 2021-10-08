
public class MySequence {
	
	private static int MAX_VAL = 100;
	private int counter = 1;
	private static Object obj = new Object();
	
	public void printEven() {
		
		while(counter < MAX_VAL) {
			synchronized (obj) {
				while(counter % 2 != 0) {
					try {
						obj.wait();	
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println(counter);
				counter++;
				obj.notify();
			}	
		}
	}
	
	
	public void printOdd() {
		
		while(counter < MAX_VAL) {
			synchronized (obj) {
				while(counter % 2 == 0) {
					try {
						obj.wait();	
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println(counter);
				counter++;
				obj.notify();
			}	
		}
	}	
	
	
	public static void main(String[] args) throws InterruptedException {
		
		MySequence m = new MySequence();
		
		Thread t1 = new Thread(() -> m.printEven());
		Thread t2 = new Thread(() -> m.printOdd());
		
		t1.start();
		t2.start();
		
		t1.join();
		t2.join();
		
	}

}
