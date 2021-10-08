package mock_assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnmodifiableList {
	
	public static void main(String[] args) {
		ArrayList<Integer> dataList = new ArrayList<Integer>();
		dataList.add(5);
		dataList.add(3);
		
		System.out.println(dataList);
		List list = Collections.unmodifiableList(dataList);
		list.add(9);
		
	}

}
