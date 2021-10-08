package mock_assignment;

import java.util.ArrayList;

class ImmutableList<T> extends ArrayList<T>{
	
	public ImmutableList(T...i) {
		for(T t :i) {
			super.add(t);
		}
	}
	
	@Override
	public boolean add(T t) {
		throw new RuntimeException("Unmodifiable List");
	}
	
}
public class ImmutableListTest {
	
	public static void main(String[] args) {
		ImmutableList<Integer> dataList = new ImmutableList<Integer>(2, 3);	
		System.out.println(dataList.toString());
		dataList.add(5);

	}
}