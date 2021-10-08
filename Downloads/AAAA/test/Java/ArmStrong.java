public class ArmStrong {
	 public static void main(String []args){
		 int n=0,p,temp;
		 int x= 132;
		 temp= x;
		 while(x>0){
			 p=x%10;
			 x=x/10;
			 n=n+(p*p*p);
		 }
		 if(temp==n)
			 System.out.println("Armastrong number");
		 else
			 System.out.println("Not armastrong number");
	 }
}
