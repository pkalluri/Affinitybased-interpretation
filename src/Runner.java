import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Runner {
//	private static Map<Integer,String[][][]> Test;
	Map<Integer,TricopaTask> tricopaTaskMap;
	Map<Integer,Integer> answerMap;
	Map<String, ActionKnowledge> actionKnowledgebase;
//	private static Map<Integer,Integer> Answers;;
	

	private void loadKnowledgeDB(String fileName) throws URISyntaxException, IOException {
        List<String> lines = getLines(fileName);	
    	this.actionKnowledgebase = new HashMap<String, ActionKnowledge>();
    	
    	for (String line : lines) {
			String trimmedLine = line.trim();
			if (!trimmedLine.isEmpty()) {
				String[] args = trimmedLine.split("\\s+");
//				for (String arg : args) {
////					System.out.println(arg);
//				}
				switch (args[1]) {
					case "+":
//						System.out.println(args[0] + " " + "+");
						actionKnowledgebase.put(args[0], new ActionKnowledge(RelationshipType.Friend));
						break;
					case "-":
//						System.out.println(args[0] + " " + "-");
						actionKnowledgebase.put(args[0], new ActionKnowledge(RelationshipType.Enemy));
						break;
					case "--":
//						System.out.println(args[0] + " " + "-");
						actionKnowledgebase.put(args[0], new ActionKnowledge(RelationshipType.Friend, .1, RelationshipType.Enemy, .9));
						break;
					case "++":
//						System.out.println(args[0] + " " + "-");
						actionKnowledgebase.put(args[0], new ActionKnowledge(RelationshipType.Friend, .9, RelationshipType.Enemy, .1));
						break;
					case "0":
//						System.out.println(args[0] + " " + "0");
						actionKnowledgebase.put(args[0], new ActionKnowledge());
						break;
				}
			}
    	}//done with all lines
	}
	
	private void loadAnswerDB(String fileName) throws URISyntaxException, IOException {
        List<String> lines = getLines(fileName);	
    	this.answerMap = new HashMap<Integer,Integer>();
    	
    	for (String line : lines) {
    		if (isNumbered(line)) {
				int taskNumber = getTaskNumber(line);
				
				String trimmedLine = line.trim();
				char answerLetter = trimmedLine.charAt(trimmedLine.length() -1);
				int answerNumber = -1;
				if (answerLetter == 'a') {answerNumber = 1;}
				else if (answerLetter == 'b') {answerNumber = 2;}
				
				answerMap.put(taskNumber, answerNumber);
    		}
    	}
	}
	
	
	private List<String> getLines(String fileName) throws URISyntaxException, IOException {
		URI uri = this.getClass().getResource(fileName).toURI();
		return Files.readAllLines(Paths.get(uri), Charset.defaultCharset());
	}
	/***
	 * Get all tasks from file.
	 * @param verbose 
	 * @param path 
	 * @return
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	private void loadTaskDB(String fileName, boolean verbose) throws IOException, URISyntaxException {
        List<String> lines = getLines(fileName);	
		
		this.tricopaTaskMap = new HashMap<Integer,TricopaTask>();
		
		int taskNumber = -1;
		Description premise = null;
		List<Description> possibleChoices = new ArrayList<Description>();
		
		boolean readingPremise = false;
		for (String line : lines) {
			if (verbose) {System.out.println("line: "+ line);}
			if (isNumbered(line)) {
				//Add last task if it exists
				if (!(taskNumber==-1)) { //not first task
					tricopaTaskMap.put(	taskNumber, new TricopaTask(premise, possibleChoices)	);
				}
				
				//Setup new task
				taskNumber = getTaskNumber(line);
				premise = null;
				possibleChoices = new ArrayList<Description>();
				readingPremise = true;
			}
			else if (isLiterals(line)) {
				if (verbose) { System.out.println("LITERALS");}
				Description currDescription = getDescription(line);
				if (verbose) {System.out.println(currDescription.getDecriptionUnits());}
				if (readingPremise) {
					premise = currDescription;
					readingPremise = false;
				} else {
					possibleChoices.add(currDescription);
				}
			} else { //if alternative narrative or blank space
				//do nothing
			}
		}
				
	}
	
	private static Description getDescription(String descriptionLine) {
//		String LITERAL_FORMAT = "\\w+(?:,\\s*\\w+)*";
		List<DescriptionUnit> descriptionUnits = new ArrayList<DescriptionUnit>();
		
		String REFERENCE_REGEX = "e[0-9]+|E[0-9]+";
		String LITERAL_DELIMITER = "\\(|\\)";
		String[] literalsInLine = descriptionLine.split(LITERAL_DELIMITER, 0);
		
		for (String literal : literalsInLine) {
//			System.out.println("literal: " + literal);
			if (!literal.isEmpty()) {
				String[] args = literal.split("\\s");
				if (args.length > 4) {
				}
				else if (args.length>=1 && isPredicate(args[0])) { //predicate
					//Turn it into a description unit
					boolean successfulLiteralSoFar = true;
					String action = args[0].substring(0, args[0].length()-1);
					String actor = args[2];
					String actedUpon = null;
					if (actor.matches(REFERENCE_REGEX)) {
						actor = null; //replace with null
					} 
					
					if (args.length == 4) {
						actedUpon = args[3];
						
						if (actedUpon.matches(REFERENCE_REGEX)) {
							actedUpon = null; //replace with null
						}
					} else if (args.length == 3) {
						actedUpon = null;
					} else {
						throw new IllegalArgumentException("A literal less than 3 arguments after its predicate.");
					}
					if (successfulLiteralSoFar) {
						descriptionUnits.add(new DescriptionUnit(actor, action, actedUpon));
					}
				}
				
			}//done creating description unit
		}//done with all literals in this line

		return new Description(descriptionUnits);
	}

	private static boolean isPredicate(String string) {
		String PREDICATE_END_TAG = "'";
		
		Collection<String> PREDICATES_TO_IGNORE = new ArrayList<String>();
		PREDICATES_TO_IGNORE.add("par'");
//		PREDICATES_TO_IGNORE.add("goal'");

//		System.out.println("Poss predicate" + string);
		if (PREDICATES_TO_IGNORE.contains(string)) {
//			System.out.println("PREDICATE TO IGNORE!!!");
		}
		return string.endsWith(PREDICATE_END_TAG) && !PREDICATES_TO_IGNORE.contains(string);
	}

	private static boolean isLiterals(String line) {
		String START_OF_LITERALS = "(";
		return line.startsWith(START_OF_LITERALS);
	}

	private static int getTaskNumber(String line) {
		String AFTER_NUMBER_TAG = "\\.|\\s";
		String[] args = line.split(AFTER_NUMBER_TAG);

		int taskNumber = Integer.parseInt(args[0]);
		return taskNumber;
	}

	private static boolean isNumbered(String line) {
		if (!line.isEmpty()) {
			char firstChar = line.charAt(0);
			return Character.isDigit(firstChar);
		} else { //if line is empty
			return false;
		}
	}

//	/***
//	 * Load all answers.
//	 * @return
//	 */
//	private static void LoadAllAnswers() {
//		Answers = new HashMap<Integer,Integer>();
//		
//		Answers.put(1, 1);
//		Answers.put(2, 2);
//		Answers.put(3, 1);
//		Answers.put(4, 1);
//		Answers.put(5, 2);
//		Answers.put(6, 2);
//		Answers.put(7, 2);
//		Answers.put(8, 1);
//		Answers.put(9, 1);
//		Answers.put(10, 2);
//		
//		Answers.put(11, 2);
//		Answers.put(12, 1);
//		Answers.put(13, 2);
//		Answers.put(14, 1);
//		Answers.put(15, 2);
//		Answers.put(16, 2);
//		Answers.put(17, 1);
//		Answers.put(18, 2);
//		Answers.put(19, 1);
//		Answers.put(20, 2);
//	};

//	/***
//	 * Add tricopa tasks at the given indices to an existing map
//	 * @param tricopaTaskMap
//	 * @param taskNumbersToAdd
//	 * @return
//	 * @throws Exception
//	 */
//	public static Map<Integer,TricopaTask> AddTasks(Map<Integer,TricopaTask> tricopaTaskMap, Set<Integer> taskNumbersToAdd) throws Exception {
//		for (int taskNumber:taskNumbersToAdd) {
//			String[][][] task = Test.get(taskNumber);
//			//Collect Descriptions into list
//			List<Description> descriptions = new ArrayList<Description>();
//			for (int description_number=0; description_number<task.length; description_number++) {
//				String[][] raw_description = task[description_number];
//				//Collect DescriptionUnits into list
//				List<DescriptionUnit> descriptionUnits = new ArrayList<DescriptionUnit>();
//				for (String[] raw_description_unit : raw_description) {
//					descriptionUnits.add(new DescriptionUnit(raw_description_unit));
//				}//DescriptionUnits collected into list
//				descriptions.add(new Description(descriptionUnits));
//			}//Descriptions collected into list
//			tricopaTaskMap.put(taskNumber, new TricopaTask(descriptions.get(0), descriptions.subList(1, descriptions.size()), descriptions.get(Answers.get(taskNumber))));
//		}
//		return tricopaTaskMap;
//	}
	
//	/***
//	 * Add tricopa tasks at the given indices to an existing list
//	 * @param tricopaTasks
//	 * @param taskNumbersToAdd
//	 * @return
//	 * @throws Exception
//	 */
//	public static List<TricopaTask> AddTasks(List<TricopaTask> tricopaTasks, Set<Integer> taskNumbersToAdd) throws Exception {
//		for (int taskNumber:taskNumbersToAdd) {
//			String[][][] task = Test.get(taskNumber);
//			//Collect Descriptions into list
//			List<Description> descriptions = new ArrayList<Description>();
//			for (int description_number=0; description_number<task.length; description_number++) {
//				String[][] raw_description = task[description_number];
//				//Collect DescriptionUnits into list
//				List<DescriptionUnit> descriptionUnits = new ArrayList<DescriptionUnit>();
//				for (String[] raw_description_unit : raw_description) {
//					descriptionUnits.add(new DescriptionUnit(raw_description_unit));
//				}//DescriptionUnits collected into list
//				descriptions.add(new Description(descriptionUnits));
//			}//Descriptions collected into list
//			tricopaTasks.add(new TricopaTask(descriptions.get(0), descriptions.subList(1, descriptions.size()), descriptions.get(Answers.get(taskNumber))));
//		}
//		return tricopaTasks;
//	}
	
	
	/***
	 * Used if I switch back to the array model of the task database.
	 * @param tricopaTasks
	 * @return
	 */
//	private static String[][][][] Test = 
//		{ 
//				{	{{"c","hits","lt"},{"lt","chases","c"}},					{{"c","playsWith","lt"}},				{{"c","angryWith","lt"}}	},
//		};
//	private static int[] Answers = {2};
//	public static List<TricopaTask> AddAllTasks(List<TricopaTask> tricopaTasks) throws Exception {
//		for (int line_number=0; line_number<Test.length; line_number++) {
//			String[][][] task = Test[line_number];
//			//Collect Descriptions into list
//			List<Description> descriptions = new ArrayList<Description>();
//			for (int description_number=0; description_number<task.length; description_number++) {
//				String[][] raw_description = task[description_number];
//				//Collect DescriptionUnits into list
//				List<DescriptionUnit> descriptionUnits = new ArrayList<DescriptionUnit>();
//				for (String[] raw_description_unit : raw_description) {
//					descriptionUnits.add(new DescriptionUnit(raw_description_unit));
//				}//DescriptionUnits collected into list
//				descriptions.add(new Description(descriptionUnits));
//			}//Descriptions collected into list
//			tricopaTasks.add(new TricopaTask(descriptions.get(0), descriptions.subList(1, descriptions.size()), descriptions.get(Answers[line_number])));
//		}
//		return tricopaTasks;
//	}
	
	
	
	/***
	 * Add the original, demonstrative tricopa task to existing list
	 * @param tricopaTasks
	 * @return
	 */
	@SuppressWarnings("unused")
	private static List<TricopaTask> AddOneTask(List<TricopaTask> tricopaTasks) {		
		Description premise = new Description(  Arrays.asList(new DescriptionUnit[] {new DescriptionUnit("c","hits","lt"), new DescriptionUnit("lt","chases","c")})  );		
		Description choice1 = new Description(  Arrays.asList(new DescriptionUnit[] {new DescriptionUnit("c","playsWith","lt")})  );
		Description choice2 = new Description(  Arrays.asList(new DescriptionUnit[] {new DescriptionUnit("c","angryWith","lt")})  );
		
		List<Description> possibleChoices = Arrays.asList(new Description[] {choice1,choice2});
		Description correctChoice = choice2;
		tricopaTasks.add(new TricopaTask(premise, possibleChoices, correctChoice) );
		
		return tricopaTasks;
	}
	
	/***
	 * Get a knowledge.
	 * @param actionKnowledgebase
	 */
	private void loadKnowledgebase() {
		Map<String, ActionKnowledge> actionKnowledgebase = new HashMap<String, ActionKnowledge>();
		RelationshipType friend = RelationshipType.Friend;
		RelationshipType enemy = RelationshipType.Enemy;
		
		/***
		 * First test task
		 */
		actionKnowledgebase.put("hits", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("chases", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("playsWith", new ActionKnowledge(friend, .75, enemy, .25) );
		actionKnowledgebase.put("angryWith", new ActionKnowledge(friend, .25, enemy, .75) );
		
		/***
		 * First 10 tasks
		 */
		actionKnowledgebase.put("creepUpOn", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("flinch", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("startle", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("happyThat", new ActionKnowledge(friend, .75, enemy, .25) );
		actionKnowledgebase.put("see", new ActionKnowledge(friend, .5, enemy, .5) );
		
		actionKnowledgebase.put("approach", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("shake", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("unhappy", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("afraid", new ActionKnowledge(friend, .25, enemy, .75) );
		
		actionKnowledgebase.put("ignore", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("annoy", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("happyThat", new ActionKnowledge(friend, .75, enemy, .25) );
		actionKnowledgebase.put("see", new ActionKnowledge(friend, .5, enemy, .5) );
		
		actionKnowledgebase.put("inside", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("outside", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("knocks", new ActionKnowledge(friend, .75, enemy, .25) );
		actionKnowledgebase.put("open", new ActionKnowledge(friend, .75, enemy, .25) );
		actionKnowledgebase.put("close", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("dislike", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("like", new ActionKnowledge(friend, .75, enemy, .25) );
		
		actionKnowledgebase.put("argueWith", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("exit", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("moveTo", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("happy", new ActionKnowledge(friend, .75, enemy, .25) );
		actionKnowledgebase.put("upset", new ActionKnowledge(friend, .25, enemy, .75) );
		
		actionKnowledgebase.put("greet", new ActionKnowledge(friend, .75, enemy, .25) );
		actionKnowledgebase.put("goal", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("defend", new ActionKnowledge(friend, .75, enemy, .25) );
		
		actionKnowledgebase.put("examine", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("angry", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("curious", new ActionKnowledge(friend, .5, enemy, .5) );
		
		actionKnowledgebase.put("hit", new ActionKnowledge(friend, .25, enemy, .75) );
		
		actionKnowledgebase.put("poke", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("annoy", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("prevent", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("playWith", new ActionKnowledge(friend, .75, enemy, .25) );
		
		actionKnowledgebase.put("turn", new ActionKnowledge(friend, .5, enemy, .5) );
		
		actionKnowledgebase.put("meander", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("inside", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("tired", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("fearThat", new ActionKnowledge(friend, .25, enemy, .75) );
		
		actionKnowledgebase.put("dance", new ActionKnowledge(friend, .75, enemy, .25) );
		actionKnowledgebase.put("surprise", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("excitedThat", new ActionKnowledge(friend, .75, enemy, .25) );
		
		actionKnowledgebase.put("console", new ActionKnowledge(friend, .75, enemy, .25) );
		
		actionKnowledgebase.put("jump", new ActionKnowledge(friend, .25, enemy, .75) );
		
		actionKnowledgebase.put("asleep", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("forgotToDo", new ActionKnowledge(friend, .5, enemy, .5) );

		actionKnowledgebase.put("enter", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("hug", new ActionKnowledge(friend, .75, enemy, .25) );
		actionKnowledgebase.put("angryAt", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("chase", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("attack", new ActionKnowledge(friend, .25, enemy, .75) );
		actionKnowledgebase.put("knock", new ActionKnowledge(friend, .5, enemy, .5) );
		actionKnowledgebase.put("excited", new ActionKnowledge(friend, .75, enemy, .25) );
		
		actionKnowledgebase.put("excited", new ActionKnowledge(friend, .75, enemy, .25) );


		this.actionKnowledgebase = actionKnowledgebase;
	}
	
	/***
	 * Helper method: get range (like python's range function)
	 * @param a
	 * @param b
	 * @return
	 */
	public static Set<Integer> GetRange(int a, int b) {
		if (a>b) {throw new IllegalArgumentException("Range requires the lower bound to be less than the higher bound.");}
		Set<Integer> range = new HashSet<Integer>();
		for (int i=a; i<b; i++) {
			range.add(i);
		}
		return range;
	}
	

	



	private Map<Integer, TricopaTask> getTasks(Set<Integer> taskNumsToDo) {
		Map<Integer,TricopaTask> tasksToDo = new HashMap<Integer,TricopaTask>();
		for (Integer taskNum : taskNumsToDo) {
			tasksToDo.put(taskNum, this.tricopaTaskMap.get(taskNum));
		}
		return tasksToDo;
	}


	private static String getScoreStatement(Map<Integer, Integer> performance) {
		int score = 0;
		for ( Integer currPerformance : performance.values()) {
			if (currPerformance == 1) {score++;}
		}
		int numTasks = performance.size();
		return "\n\nSCORE ON TRICOPA-CORPUS IS: " + score + "/" + numTasks + "=" + (double)score/(double)(numTasks);
	}

//	private static Map<Integer, String> getEasyView(Map<Integer, Boolean> performance) {
//		Map<Integer,String> easyViewPerformance = new HashMap<Integer,String> ();
//		for (Map.Entry<Integer, Boolean> entry : performance.entrySet()) {
//			if (entry.getValue()==t) { easyViewPerformance.put(entry.getKey(), "S"); }
//			else {
//				easyViewPerformance.put(entry.getKey(), "X");
//			}
//		}
//		return easyViewPerformance;
//	}
	private static String getSpreadsheetView(Map<Integer, Integer> performance) {
		String sheet = "";
		for (int i = 1; i<100; i++) {
			if (performance.containsKey(i)) {
				sheet+= performance.get(i) + "\n";
			} else {
				sheet += "X\n";;
			}
			
		}
		return sheet;
	}

	private Map<Integer, Integer> getHumanPerformance(HumanAgent humanAgent, Map<Integer, TricopaTask> tasksToDo, boolean verbose) throws Exception {
		//Create metrics
		Map<Integer,Integer> success = new HashMap<Integer,Integer>();
		
		//Run
		for (Map.Entry<Integer, TricopaTask> numberedTricopaTask : tasksToDo.entrySet()) {
			int taskNumber = numberedTricopaTask.getKey();
			if (verbose) {System.out.println("\n*TASK " + taskNumber);}
			
			TricopaTask tricopaTask = numberedTricopaTask.getValue();
			int chosen = humanAgent.doTricopaTask(tricopaTask.getPremise(), tricopaTask.getPossibleChoices());
			if (chosen == -1) {
				success.put(taskNumber, 0);
				if (verbose) {System.out.println("*Answer was " + this.answerMap.get(taskNumber) + ". HUMAN WAS UNDECIDED");}
			}
			else if ( isCorrectChoice(chosen, taskNumber) ) {
				success.put(taskNumber, 1);
				if (verbose) {System.out.println("*CORRECT");}
			} else {
				success.put(taskNumber, -1);
				if (verbose) {System.out.println("*WRONG");}
			}
		}//done with all tasks
		return success;
	}



	private boolean isCorrectChoice(int chosen, int taskNumber) {
		return (chosen == this.answerMap.get(taskNumber));
	}

	/***
	 * Run test.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		boolean VERBOSE = false;

		/***
		 * Setup task map from db
		 */
//		Set<Integer> TASKS_TO_ADD = GetRange(1,21);
//		Set<Integer> TASKS_TO_REMOVE = new HashSet<Integer>(Arrays.asList(11,20));
//		Runner.LoadAllTasks();
//		Runner.LoadAllAnswers();
//		Map<Integer,TricopaTask> tricopaTaskMap = new HashMap<Integer,TricopaTask>();
//		AddTasks(tricopaTaskMap, TASKS_TO_ADD);
//		System.out.println(tricopaTaskMap.size());
//		for (Integer task_number : TASKS_TO_REMOVE) {
//			tricopaTaskMap.remove(task_number);
//		}
		
		Runner runner = new Runner();

		/***
		 * Setup task map, answer map, and knowledgebase from file
		 */
		runner.loadTaskDB("/TRICOPA.txt", VERBOSE);
		runner.loadAnswerDB("/Answers.txt");
		runner.loadKnowledgeDB("/Knowledge.txt"); 
		System.out.println(runner.actionKnowledgebase);
		
		VERBOSE = true;

		/***
		 * Run simulation
		 */
		//Select task set
//		Set<Integer> taskNumsToDo = new HashSet<Integer>(Arrays.asList(14));
		Set<Integer> taskNumsToDo = Runner.GetRange(1, 100);
		Map<Integer,TricopaTask> tasksToDo = runner.getTasks(taskNumsToDo);
		Set<Integer> TASKS_TO_REMOVE = new HashSet<Integer>();
		TASKS_TO_REMOVE.addAll(Arrays.asList(11,20,22,43,44,49,91, 94,95,99)); //1-CHARACTER TASKS
//		TASKS_TO_REMOVE.addAll(Arrays.asList(4)); //ASYMMETRIC TASKS
		TASKS_TO_REMOVE.addAll(Arrays.asList(47,50,56,74,76,77,80,84,86,92,94,99)); //TASKS WITH NOT LITERAL
		for (Integer task_number : TASKS_TO_REMOVE) {
			tasksToDo.remove(task_number);
		}
//		System.out.println(tasksToDo);

		//Create knowledgeable agent
		HumanAgent humanAgent = new HumanAgent(runner.actionKnowledgebase);
		humanAgent.setVerbose(VERBOSE);
		Map<Integer,Integer> performance = runner.getHumanPerformance(humanAgent, tasksToDo, VERBOSE);
		
		
		/***
		 * Print metrics
		 */
		System.out.print("\n" +	performance );
//		System.out.println("\n" + Runner.getEasyView(performance));
		System.out.println("\n" + Runner.getSpreadsheetView(performance));
		System.out.println("\n" + Runner.getScoreStatement(performance));

	}
	



}
