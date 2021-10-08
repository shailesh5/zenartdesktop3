public class PrintVowels {
	 public void getVowels(String str){
		  String newStr = str.toLowerCase();
			int vowelsCount = 0;
		    for(int i=0;i<newStr.length();i++){
			  if(newStr.charAt(i)=='a'||newStr.charAt(i)=='e'||newStr.charAt(i)=='i'||newStr.charAt(i)=='o'||newStr.charAt(i)=='u'){
			     vowelsCount++;
			  }
			}
			System.out.println("Number of vowels inString: " + vowelsCount);
		  }

	       public static void main(String []args){
		      String str = new String("	This is very simple");
		      PrintVowels vo =new PrintVowels();
		      vo.getVowels(str);
		   }
}