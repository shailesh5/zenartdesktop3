package mock_assignment;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SortByValue {

	public static Map<Integer,String> sort(Map<Integer,String> inputMap){

		Set<Entry<Integer, String>> entrySet = inputMap.entrySet();
		
		LinkedList<Entry<Integer, String>> list = new LinkedList<>();
		
		for(Entry<Integer, String> entry : entrySet) {
			list.add(entry);
		}
		
		Collections.sort(list, (e1, e2)-> e1.getValue().compareTo(e2.getValue()));
		
		LinkedHashMap<Integer, String> dataMap = new LinkedHashMap<>();
		
		for(Entry<Integer, String> data : list) {
			dataMap.put(data.getKey(), data.getValue());
		}
		
		return dataMap;
	}
	
	public static void main(String[] args) {
		
		Map<Integer,String> inputMap = new HashMap<>();
		inputMap.put(1, "B");
		inputMap.put(2, "C");
		inputMap.put(3, "A");
		inputMap.put(4, "D");
		
		Map<Integer, String> outputMap = sort(inputMap);
		
		for(Entry<Integer, String> entry : outputMap.entrySet()) {
			System.out.println(entry.toString());
		}
	}
	

}
