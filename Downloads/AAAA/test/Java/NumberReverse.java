public class NumberReverse{
    public void reverseGivenNumber(int number){
	     int i=0;
	     int reverse=0;
		 while(number!=0){			 
		 int reminder = number%10;
		  reverse = reverse*10 + reminder;
		 number = number/10;
		 }
	  System.out.println(reverse);
}
  public static void main(String []args){
     int num = 123456;
     NumberReverse obj = new NumberReverse();
	 obj.reverseGivenNumber(num);
  }
}