public class StudentGrade {
	    public static void main(String args[])
	    {
	    	int noOfSubjects= 5;
	        int totalMarks = 500;
	        int TotalMarksObtained = 450;
	        int i;
	        float sum=0, avg;
            			
	        avg = TotalMarksObtained/noOfSubjects;
			
	        System.out.print("Your Grade is ");
	        if(avg>80)
	        {
	            System.out.print("A");
	        }
	        else if(avg>60 && avg<=80)
	        {
	            System.out.print("B");
	        }
	        else if(avg>40 && avg<=60)
	        {
	            System.out.print("C");
	        }
	        else
	        {
	            System.out.print("D");
	        }
	    }
	}