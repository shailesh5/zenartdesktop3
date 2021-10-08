import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameOfDice {

	//Participants who are playing and have not score accumulative score
	private static ConcurrentHashMap<String, ArrayList<Integer>> scores = new ConcurrentHashMap<>();
	
	//Participants who have completed and order of completion i.e. ranking
	private static LinkedHashMap<String, ArrayList<Integer>> leaderboard = new LinkedHashMap<>();
	
	//Participants who are penalised after having 2 consecutive 1's
	private static CopyOnWriteArrayList<String> penalisedList = new CopyOnWriteArrayList<>();
	
	public static void main(String[] args) {

		Scanner scanner = new Scanner(System.in);
		int noOfPlayers = Integer.parseInt(args[0]);
		int accPoints = Integer.parseInt(args[1]);
		
		//assigning player names and making entry
		for(int i = 1; i <= noOfPlayers; i++) {
			scores.put("Player-"+i, new ArrayList<>());
		}

		while(!scores.isEmpty()) {
			
			Set<String> keySet = scores.keySet();
			boolean chance = true;
			int lastRollValue = 0;
			int secondLastRollValue = 0;
			int score = 0;
			
			for(String key : keySet) {
				
				// check whether it is in the 
				if(penalisedList.contains(key)) {
					System.out.println(key+" you have been penalised as you scored consecutive 1's two times.");
					penalisedList.remove(key);
					continue;
				}
				
				do {

					System.out.println(key+" its your turn (press ‘r’ to roll the dice) ");
			        String input = scanner.next();
			        
			        if("r".equals(input)) {
			        	
			        	int faceValue = rollDice();
			        	System.out.println("Dice turned up :"+faceValue);
			        	
			        	scores.get(key).add(faceValue);
			        	
			        	score = getScore(key);
			        	
			        	// if reached acc points then remove from current playing and add in the finished.
			        	if(score >= accPoints) {
			        		System.out.println("Congratulations !!! , "+key+" you have reached "+accPoints+" points !!!");
			        		leaderboard.put(key, scores.get(key));
			        		scores.remove(key);
			        		break;
			        	}

			        	ArrayList<Integer> scoreList = scores.get(key);
			        	
			        	// if current and last roll having 1 then add to penalised list
			        	if(!scoreList.isEmpty() && scoreList.size() > 2) {
							lastRollValue = scoreList.get(scoreList.size()-1);
							secondLastRollValue = scoreList.get(scoreList.size()-2);
						
							if(lastRollValue == 1 && secondLastRollValue == 1) {
								penalisedList.add(key);
							}
			        	}
			        	
			        	if(faceValue == 6) {
			        		chance = true;
			        		System.out.println(key+" Yeah, It's 6 !!! You Got Another Chance !!! ");
			        	}else  {
			        		chance = false;
			        	}
			        	
			        	showScoreBoard();
			        }else {
			        	chance = true;
			        }
				}while(chance);
			}
		}
		
		System.out.println("The Final Scores");
		showScoreBoard();

	}
	
	private static void showScoreBoard() {
		
		System.out.println("The Score Board");
		System.out.println("===============");
		
		if(leaderboard != null) {
			Set<String> leaderSet = leaderboard.keySet();
			for(String key : leaderSet) {
				System.out.println(key+"-->"+getScore(key));
			}
		}
		
		if(scores != null) {
			Set<String> keySet = scores.keySet();
			for(String key : keySet) {
				System.out.println(key+"-->"+getScore(key));
			}
		}
	}
	
	private static int getScore(String key) {
		
		if(leaderboard.containsKey(key)) {
			return leaderboard.get(key).stream().reduce(0, (a, b) -> a + b);
		}else {
			return scores.get(key).stream().reduce(0, (a, b) -> a + b);
		}
		
	}

	private static int rollDice() {
		Random random = new Random();
		int rand = 0;
		while (true){
		    rand = random.nextInt(7);
		    if(rand !=0) break;
		}
		return rand;
	}

}
