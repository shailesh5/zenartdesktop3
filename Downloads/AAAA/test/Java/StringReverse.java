public class StringReverse{
        public void reverseGivenString(String str){
		  char [] newArray = str.toCharArray();
		  String reverseString = new String();		
		  for(int i = newArray.length-1;i>=0;i--){		    
			reverseString = reverseString+newArray[i];
		  }
		  System.out.println(reverseString);
		}

      public static void main(String []args){
	     String str = new String("java programming");
	     StringReverse obj = new StringReverse();
		 obj.reverseGivenString(str);
	  }
}
