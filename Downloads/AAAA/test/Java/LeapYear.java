public class LeapYear {
	 public static void main(String []args){
		 int year= 2012;
		 boolean isLeap= false;
		 if(year % 4 ==0){
			 if(year%100==0){
				 if(year % 400 == 0)
					 isLeap= true;
				 else
					 isLeap = false;
			 }
			 else
				 isLeap = true; 
		 }
		 else{
			 isLeap = false; 
		 }
		 if(isLeap == true )
			 System.out.println(year + " is leap year");
		 else
			 System.out.println(year + " is not leap year");
	 }
}
