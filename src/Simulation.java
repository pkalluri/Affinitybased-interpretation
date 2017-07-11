import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 * Given specified files, spawns an AffinitybasedAgent, administers a single Scenario or a TricopaTask set to the agent, and prints a summary of the results.
 * 
 * Files may include a scenario file, a knowledge file, a Tricopa tasks file, and/or a Tricopa answers file.
 * Files' format is assumed to adhere to the instructions in the ReadMe.
 * 
 * @author pkalluri
 */
public class Simulation {
	
	////////////////////////////////////////////////////////////
	//////// FOR READING IN THE RELEVANT FILES /////////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Construct Scenario based on the indicated scenario file.
	 * 
	 * @param fileName the name of the scenario file
	 * @return the scenario based on the indicated scenario file
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static Scenario getScenarioFromFile(String fileName) throws URISyntaxException, IOException {
		//String together lines
		List<String> lines = Simulation.getLines(fileName);	
		String linesTogether = "";
		for (String line : lines) {
			linesTogether += line.trim();
		}

		return Simulation.getScenario(linesTogether); //Construct scenario
	}
	
	/***
	 * Construct TricopaTask database based on the indicated Tricopa tasks file.
	 * 
	 * @param fileName the name of the Tricopa tasks file
	 * @param verbose
	 * @return the TricopaTask database based on the indicated Tricopa tasks file
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static Map<Integer, TricopaTask> getTricopaTasksFromFile(String fileName, boolean verbose) throws IOException, URISyntaxException {
        Map<Integer,TricopaTask> tricopaTasks  = new HashMap<Integer,TricopaTask>();
        
        List<String> lines = Simulation.getLines(fileName);	
		
		int taskNumber = -1;
		Scenario premise = null;
		List<Scenario> possibleChoices = new ArrayList<Scenario>();
		
		boolean readingPremise = false;
		for (String line : lines) {
			if (verbose) {System.out.println("line: "+ line);}
			if (isNumbered(line)) {
				//Add last task if it exists
				if (!(taskNumber==-1)) { //not first task
					tricopaTasks.put(	taskNumber, new TricopaTask(premise, possibleChoices)	);
				}
				
				//Setup new task
				taskNumber = getTaskNumber(line);
				premise = null;
				possibleChoices = new ArrayList<Scenario>();
				readingPremise = true;
			}
			else if (isLiterals(line)) {
				if (verbose) { System.out.println("LITERALS");}
				Scenario currDescription = getScenario(line);
				if (verbose) {System.out.println(currDescription.actionEvents);}
				if (readingPremise) {
					premise = currDescription;
					readingPremise = false;
				} else {
					possibleChoices.add(currDescription);
				}
			} else { //if alternative narrative or blank space
				//do nothing
			}
		}//done with all lines
		//TODO add last task?
		return tricopaTasks;
	}
	
	/***
	 * Construct answer database based on the indicated Tricopa answers file.
	 * 
	 * @param fileName the name of the Tricopa answers file
	 * @return the answer database based on the indicated Tricopa answers file
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static Map<Integer, Integer> getAnswersFromFile(String fileName) throws URISyntaxException, IOException {
    	Map<Integer,Integer> answers = new HashMap<Integer,Integer>();
    	
        List<String> lines = Simulation.getLines(fileName);	
    	
    	for (String line : lines) {
    		if (isNumbered(line)) {
				int taskNumber = getTaskNumber(line);
				
				String trimmedLine = line.trim();
				char answerLetter = trimmedLine.charAt(trimmedLine.length() -1);
				int answerNumber = -1;
				if (answerLetter == 'a') {answerNumber = 1;}
				else if (answerLetter == 'b') {answerNumber = 2;}
				
				answers.put(taskNumber, answerNumber);
    		}
    	}
    	return answers;
	}
	
	/***
	 * Construct ActionKnowledge database  based on the indicated knowledge file.
	 * 
	 * @param fileName the name of the knowledge file
	 * @param verbose
	 * @return the ActionKnowledge database based on the indicated knowledge file
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static Map<String, ActionKnowledge> getActionKnowledgebase(String fileName, boolean verbose) throws URISyntaxException, IOException {
		//The knowledge file is interpreted with the constraint that the ratio between two probabilities in a single action's relative observation distribution must always be 1 or this ratio:
		double RATIO_PERMITTED_WITHIN_ROD = 2; 		
		
		List<String> lines = Simulation.getLines(fileName);	
    	
		Map<String, ActionKnowledge> actionKnowledgebase = new HashMap<String, ActionKnowledge>();
    	for (String line : lines) {
			String trimmedLine = line.trim();
			if (!trimmedLine.isEmpty()) {
				String[] args = trimmedLine.split("\\s+");

				//Set up map. For a single action, each entry in this map indicates whether the action is expected in the context of the given RelationshipType
				Map<RelationshipType, Boolean> expectationsGivenRelationshipType = new HashMap<RelationshipType, Boolean>();
				for ( RelationshipType relationshipType: Arrays.asList(RelationshipType.values()) ) {
					expectationsGivenRelationshipType.put(relationshipType, false);
				}
				if (args.length >1) { //there is more information to read
					for (char c : args[1].toCharArray()) {
						switch(c) {
							case '=':
								expectationsGivenRelationshipType.put(RelationshipType.FRIEND, true);
								break;
							case '-':
								expectationsGivenRelationshipType.put(RelationshipType.ENEMY, true);
								break;
							case '0':
								expectationsGivenRelationshipType.put(RelationshipType.NEUTRAL, true);
								break;
						}//done with char
					}//done with all chars on this line
				}
				if(verbose) {System.out.println(expectationsGivenRelationshipType);}
				actionKnowledgebase.put(args[0], new ActionKnowledge(expectationsGivenRelationshipType, RATIO_PERMITTED_WITHIN_ROD));
			}
    	}//done with all lines
    	return actionKnowledgebase;
	}
	
	
	////////////////////////////////////////////////////////////
	////////HELPERS FOR READING IN FILES////////////////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Get all lines from the indicated file.
	 * @param fileName
	 * @return the lines from the indicated file
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private static List<String> getLines(String fileName) throws URISyntaxException, IOException {
		URI uri = Simulation.class.getResource(fileName).toURI();
		return Files.readAllLines(Paths.get(uri), Charset.defaultCharset());
	}
	
	/***
	 * Construct Scenario based on the given line of logical literals.
	 * @param line line of logical literals
	 * @return scenario based on the given line of logical literals
	 */
	private static Scenario getScenario(String line) {
		List<ActionEvent> scenarioUnits = new ArrayList<ActionEvent>();
		
		String REFERENCE_REGEX = "e[0-9]+|E[0-9]+"; //Regex to use for matching the e[#] notation used when one event refers to another event
		String LITERAL_DELIMITER = "\\(|\\)"; //Regex to use for matching the ([literal]) notation of literals
		String[] literals = line.split(LITERAL_DELIMITER, 0);
		
		for (String literal : literals) {
			if (!literal.isEmpty()) {
				String[] args = literal.split("\\s");
				if (args.length > 4) {
				}
				else if (args.length>=1 && isPredicate(args[0])) { //predicate
					//Turn it into a description unit
					String action = args[0].substring(0, args[0].length()-1);
					String actor = args[2];
					String actedUpon = null; //for now
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
						throw new IllegalArgumentException("A literal must have 3 or 4 arguments after its predicate.");
					}
						
					scenarioUnits.add(new ActionEvent(actor, action, actedUpon));
				}
				
			}//done creating description unit
		}//done with all literals in this line

		return new Scenario(scenarioUnits);
	}

	/***
	 * Return true iff text is a predicate.
	 * @param string
	 */
	private static boolean isPredicate(String string) {
		String PREDICATE_END_TAG = "'";
		
		Collection<String> PREDICATES_TO_IGNORE = new ArrayList<String>();
		PREDICATES_TO_IGNORE.add("par'");
//		PREDICATES_TO_IGNORE.add("goal'");

		return string.endsWith(PREDICATE_END_TAG) && !PREDICATES_TO_IGNORE.contains(string);
	}

	/***
	 * Return true iff text is a line of literals.
	 * @param line
	 */
	private static boolean isLiterals(String line) {
		String START_OF_LITERALS = "(";
		return line.startsWith(START_OF_LITERALS);
	}

	/***
	 * Return true iff line begins with a digit
	 * @param line
	 * @return
	 */
	private static boolean isNumbered(String line) {
		if (!line.isEmpty()) {
			char firstChar = line.charAt(0);
			return Character.isDigit(firstChar);
		} else { //if line is empty
			return false;
		}
	}
	

	/***
	 * Get task number from given line of text.
	 * @param line
	 * @return task number from given line of text
	 */
	private static int getTaskNumber(String line) {
		String AFTER_NUMBER_TAG = "\\.|\\s";
		String[] args = line.split(AFTER_NUMBER_TAG);

		int taskNumber = Integer.parseInt(args[0]);
		return taskNumber;
	}
	
	
	/////////////////////////////////////////////////////////////////
	///HELPERS FOR SETTING UP, BEFORE ADMINISTERING TRICOPA TASKS////
	/////////////////////////////////////////////////////////////////	
	/***
	 * Get range between a and b, including a and not including b.
	 * 
	 * (Like python's range function).
	 * 
	 * @param a lower bound of range, included
	 * @param b upper bound of range, not included
	 * @return range between a and b, including a and not including b
	 */
	public static Set<Integer> GetRange(int a, int b) {
		if (a>b) {throw new IllegalArgumentException("The lower bound must be less than the upper bound.");}
		Set<Integer> range = new HashSet<Integer>();
		for (int i=a; i<b; i++) {
			range.add(i);
		}
		return range;
	}

	/***
	 * Get the subset of tricopaTasks indicated by the taskNumbers argument.
	 * @param tricopaTasks Tricopa tasks
	 * @param taskNumbers the numbers of the desired tasks
	 * @return the subset of tricopaTasks indicated by the taskNumbers argument
	 */
	private static Map<Integer, TricopaTask> getTasks(Map<Integer,TricopaTask> tricopaTasks, Set<Integer> taskNumbers) {
		Map<Integer,TricopaTask> subset = new HashMap<Integer,TricopaTask>();
		for (Integer taskNum : taskNumbers) {
			subset.put(taskNum, tricopaTasks.get(taskNum));
		}
		return subset;
	}

	/////////////////////////////////////////////////////////////////
	/// FOR ADMINISTERING TRICOPA TASKS /////////////////////////////
	/////////////////////////////////////////////////////////////////		
	/***
	 * Administer tricopaTasks to socialAgent, assess performance against true answers, and return socialAgent's performance.
	 * @param socialAgent
	 * @param tricopaTasks
	 * @param answers
	 * @param verbose
	 * @return
	 * @throws Exception
	 */
	public static Map<Integer, TricopaTaskPerformance> administerTricopaTasks(TricopaParticipant socialAgent, Map<Integer, TricopaTask> tricopaTasks, Map<Integer,Integer> answers, boolean verbose) throws Exception {
		Map<Integer,TricopaTaskPerformance> performanceOnTasks = new HashMap<Integer,TricopaTaskPerformance>();
		
		for (Map.Entry<Integer, TricopaTask> numberedTricopaTask : tricopaTasks.entrySet()) {
			int taskNumber = numberedTricopaTask.getKey();			
			TricopaTask tricopaTask = numberedTricopaTask.getValue();
			if (verbose) {System.out.println("\nTASK " + taskNumber);}
			
			
			try {
				int chosen = socialAgent.doTricopaTask(tricopaTask);
				
				if ( chosen == answers.get(taskNumber) ) {
					performanceOnTasks.put(taskNumber, TricopaTaskPerformance.CORRECT);
					if (verbose) {System.out.println("CORRECT " + "\n(The correct choice was " + answers.get(taskNumber) + ")");}
				} else {
					performanceOnTasks.put(taskNumber, TricopaTaskPerformance.INCORRECT);
					if (verbose) {System.out.println("INCORRECT " + "\n(The correct choice was " + answers.get(taskNumber) + ")");}
				}
			} catch (UndecidedAgentException e) {
				performanceOnTasks.put(taskNumber, TricopaTaskPerformance.INCOMPLETE);
				if (verbose) {System.out.println("INCOMPLETE " + "\n(The correct choice was " + answers.get(taskNumber) + ")");}
			}
		}//done with all tasks
		return performanceOnTasks;
	}
	
	
	/////////////////////////////////////////////////////////////////
	/// FOR REPORTING AGENT PERFORMANCE ON TRICOPA TASKS ////////////
	/////////////////////////////////////////////////////////////////	
	/***
	 * Construct short text description of number of tasks completed correctly, total number of tasks administered, and calculated percentage accuracy, given performanceOnTasks.
	 * @param performanceOnTasks each entry of performanceOnTasks indicates the number of a tricopa task and the TricopaTaskPerformance on the task
	 * @return short text description of number of tasks completed correctly, total number of tasks administered, and calculated percentage accuracy, given performanceOnTasks
	 */
	public static String getScoreStatement(Map<Integer, TricopaTaskPerformance> performanceOnTasks) {
		int score = 0;
		for ( TricopaTaskPerformance currPerformance : performanceOnTasks.values()) {
			if (currPerformance.equals(TricopaTaskPerformance.CORRECT)) {score++;}
		}
		int numTasks = performanceOnTasks.size();
		
		//Construct text
		NumberFormat format = NumberFormat.getPercentInstance();
		format.setMinimumFractionDigits(2);
		return "\n\nTHIS AGENT'S SCORE ON THE TRICOPA CORPUS IS " + score + "/" + numTasks + "=" + format.format((double)score/(double)(numTasks));
	}
	
	/***
	 * Construct a table representation of performanceOnTasks, where each task number is mapped to 'X' for not administered, '1' for performed correctly,
	 *  '-1' for performed incorrectly, or '0' for administered but agent was undecided.
	 * @param performanceOnTasks
	 * @return a table representation of performanceOnTasks
	 */
	public static String getTableRepresentationOfPerformance(Map<Integer, TricopaTaskPerformance> performanceOnTasks) {
		String sheet = "";
		
		List<Integer> orderedTaskNumbers = Simulation.asSortedList(performanceOnTasks.keySet());
		
		int lastTaskNumber = 1;
		for (int currTaskNumber : orderedTaskNumbers) {
			//add information about skipped tasks
			for (int i =lastTaskNumber; i<currTaskNumber; i++) {
				sheet += i+": \n";
			}
			
			//add information about current task 
			if (performanceOnTasks.get(currTaskNumber) == TricopaTaskPerformance.INCORRECT) {
				sheet+= currTaskNumber+": -1" + "\n";
			}
			else if (performanceOnTasks.get(currTaskNumber) == TricopaTaskPerformance.INCOMPLETE) {
				sheet+= currTaskNumber+": 0" + "\n";
			}
			else if (performanceOnTasks.get(currTaskNumber) == TricopaTaskPerformance.CORRECT) {
				sheet+= currTaskNumber+": 1" + "\n";
			}
			
			lastTaskNumber = currTaskNumber;
		}
		return sheet;
	}
	
	/***
	 * Construct a sorted list containing the elements of the given collection.
	 * @param collection
	 * @return
	 */
	private static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> collection) {
		List<T> list = new ArrayList<T>(collection);
		java.util.Collections.sort(list);
		return list;
	}

	
	/////////////////////////////////////////////////////////////////
	/// FOR REPORTING AGENT INTERPRETATION OF A SINGLE SCENARIO /////
	/////////////////////////////////////////////////////////////////	
	/***
	 * 
	 * @param relationshipBeliefsOverTime
	 * @return
	 */
	public static String getTableRepresentationOfRelationshipBeliefsOverTime(Pair<String> relationship, Map<Integer, Map<RelationshipType, Double>> relationshipBeliefsOverTime) {
		try {
			String sheet = "\nRelationship between " + relationship + "\n";
			int minTime = Collections.min(relationshipBeliefsOverTime.keySet());
			int maxTime = Collections.max(relationshipBeliefsOverTime.keySet());
			sheet += "time\tBelief that the relationship is...\n";
			sheet += "\tFriend\t\tNeutral\t\tEnemy\n";
			for (int currTime = minTime; currTime<=maxTime; currTime++) {
				if (relationshipBeliefsOverTime.containsKey(currTime)) {
					sheet += currTime + "\t";
					Map<RelationshipType, Double> currBelief = relationshipBeliefsOverTime.get(currTime);
					for (RelationshipType relationshipType : RelationshipType.values()) {
						NumberFormat format = NumberFormat.getPercentInstance();
						format.setMinimumFractionDigits(2);
						sheet +=  format.format(currBelief.get(relationshipType)) + "\t\t";
					}//done with all relationship types
					sheet += "\n";
				}
			}
			return sheet;
		} catch (NullPointerException e) {
			System.err.println(e.getClass() + ": " + e.getMessage());
		}
		return null;//failed
	}
	
	

	/***
	 * Given specified files, spawns a AffinitybasedAgent, administers a single Scenario or a TricopaTask set to the agent, and prints a summary of the results.
	 * 
	 * Files may include a scenario file, a knowledge file, a Tricopa tasks file, and/or a Tricopa answers file.
	 * Files' format is assumed to adhere to the instructions in FilesFormat.txt
	 * 
	 * @author pkalluri
	 */
	public static void main(String[] args) throws Exception {
		/***
		 * Parameters
		 */		
		boolean VERBOSE_FILE_READING = false;
		boolean VERBOSE_AGENT = true;

		boolean ADMINISTER_SINGLE_SCENARIO = true;
		//Used iff ADMINISTER_SINGLE_SCENARIO parameter is set to true:
		String SCENARIO_FILENAME = "/Scenario.txt"; 
		String SCENARIO_KNOWLEDGE_FILENAME = "/Knowledge.txt";
		Set<String> OBJECTS_IN_SCENARIO = new HashSet<String>();
		Pair<String> RELATIONSHIP_TO_REPORT_ON = new Pair<String>("C","BT");
		
		boolean ADMINISTER_TRICOPA_TASKS = true;
		//Used iff ADMINISTER_TRICOPA_TASKS parameter is set to true:
		String TRICOPA_TASKS_FILENAME = "/Tricopa_Tasks.txt";
		String TRICOPA_KNOWLEDGE_FILENAME = "/Knowledge.txt";
		Set<String> OBJECTS_IN_TRICOPA_TASKS = new HashSet<String>(Arrays.asList("CORNER","D", "OUTSIDE", "INSIDE", "x", "BEHINDBOX"));
		String TRICOPA_ANSWERS_FILENAME = "/Tricopa_Answers.txt";
		Set<Integer> TASK_NUMS_TO_DO = Simulation.GetRange(1, 101); //Which task numbers to do
		
		//Which task numbers to consider exceptions:
		Set<Integer> TASK_NUMS_TO_REMOVE = new HashSet<Integer>();
		TASK_NUMS_TO_REMOVE.addAll(Arrays.asList(11,20,22,43,44,91, 94,95,99)); //1-CHARACTER TASKS
		//TASKS_TO_REMOVE.addAll(Arrays.asList(4)); //ASYMMETRIC TASKS
		TASK_NUMS_TO_REMOVE.addAll(Arrays.asList(47,50,56,74,76,77,80,84,86,92,94,99)); //TASKS WITH 'NOT' LITERAL		
			
		
		/***
		 * Administer single story.
		 */
		if (ADMINISTER_SINGLE_SCENARIO) {
			System.out.println("...ADMINISTERING A SCENARIO...");

			/***
			 * Spawn a social agent with knowledge from knowledge file
			 */
			Map<String, ActionKnowledge> actionKnowledgebase = Simulation.getActionKnowledgebase(SCENARIO_KNOWLEDGE_FILENAME, VERBOSE_FILE_READING);
			AffinitybasedAgent affinitybasedAgent = new AffinitybasedAgent(actionKnowledgebase, OBJECTS_IN_SCENARIO, VERBOSE_AGENT);

			/***
			 * Administer story to social agent
			 * Social agent reads the story and in real-time updates interpreted relationship information
			 */
			Scenario story = Simulation.getScenarioFromFile(SCENARIO_FILENAME);
			Map<Pair<String>, Map<Integer,Map<RelationshipType,Double>>> allRelationshipInfo = 
					affinitybasedAgent.getWorldModelOf(story, false).getHistory();
			
			/***
			 * Print table of relationship beliefs over time regarding the indicated relationship
			 */
			System.out.println(Simulation.getTableRepresentationOfRelationshipBeliefsOverTime(RELATIONSHIP_TO_REPORT_ON, allRelationshipInfo.get(RELATIONSHIP_TO_REPORT_ON)));
		} // done administering story
		if (ADMINISTER_TRICOPA_TASKS) {		
			System.out.println("...ADMINISTERING TRICOPA TASKS...");
			/***
			 * Spawn a social agent with knowledge from knowledge file
			 */
			Map<String, ActionKnowledge> actionKnowledgebase = Simulation.getActionKnowledgebase(TRICOPA_KNOWLEDGE_FILENAME, VERBOSE_FILE_READING);
			TricopaParticipant socialAgent = new AffinitybasedAgent(actionKnowledgebase, OBJECTS_IN_TRICOPA_TASKS, VERBOSE_AGENT);
			
			/***
			 * Set up, before administering tasks to social agent
			 */
			Map<Integer,TricopaTask> allTricopaTasks = Simulation.getTricopaTasksFromFile(TRICOPA_TASKS_FILENAME, VERBOSE_FILE_READING);
			Map<Integer,TricopaTask> tasksToDo = Simulation.getTasks(allTricopaTasks, TASK_NUMS_TO_DO);
			for (Integer task_number : TASK_NUMS_TO_REMOVE) {
				tasksToDo.remove(task_number);
			}
			Map<Integer,Integer> answers = Simulation.getAnswersFromFile(TRICOPA_ANSWERS_FILENAME);

			
			/***
			 * Administer Tricopa tasks to social agent
			 * For each task, social agent reads the tasks, updates interpreted relationship information, and answers the task
			 * runner reports the social agent's performance
			 */
			Map<Integer,TricopaTaskPerformance> performanceOnTasks = Simulation.administerTricopaTasks(socialAgent, tasksToDo, answers, VERBOSE_AGENT);
			
			
			/***
			 * Print results
			 */
//			System.out.println("\n" + Simulation.getTableRepresentationOfPerformance(performanceOnTasks));
			System.out.println("" + Simulation.getScoreStatement(performanceOnTasks));
		}//done administering Tricopa tasks
		
	}



}
