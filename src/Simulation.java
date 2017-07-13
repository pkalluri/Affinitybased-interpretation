import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * Given specified files, the Simulation class spawns an AffinitybasedAgent, administers a Scenario or TricopaTasks to the agent, and prints a summary of the results.
 * Files' format must adhere to the instructions in the ReadMe.
 * 
 * @author pkalluri
 */
public class Simulation {
	
	////////////////////////////////////////////////////////////
	//////// FOR READING IN FILES //////////////////////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Get the Scenario based on the indicated Scenario File.
	 * 
	 * @param fileName the name of the Scenario File
	 * @return the scenario based on the indicated Scenario File
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static Scenario getScenarioFromFile(String fileName) throws URISyntaxException, IOException {
		String linesTogether = "";
		List<String> lines = Simulation.getLines(fileName);	
		for (String line : lines) {
			linesTogether += line.trim(); //String together lines
		}
		return Simulation.getScenario(linesTogether);
	}
	
	/***
	 * Get the TricopaTask database based on the indicated Tricopa Tasks File.
	 * 
	 * @param fileName the name of the Tricopa Tasks File
	 * @param verbose
	 * @return the TricopaTask database based on the indicated Tricopa Tasks File
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
				Scenario currScenario = getScenario(line);
				if (verbose) {System.out.println("literals: " + currScenario.actionEvents);}
				if (readingPremise) {
					premise = currScenario;
					readingPremise = false;
				} else {
					possibleChoices.add(currScenario);
				}
			} else { //if alternative narrative or blank space
				//do nothing
			}
		}//done with all lines
		if (!(taskNumber==-1)) { //some tasks have occurred
			tricopaTasks.put(	taskNumber, new TricopaTask(premise, possibleChoices)	);
		}
		return tricopaTasks;
	}
	
	/***
	 * Get the Set of known non-agents based on the indicated Nonagents File.
	 * 
	 * @param fileName the name of the known Nonagents File
	 * @return the Set of known non-agents based on the indicated Nonagents File
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static Set<String> getNonagentsFromFile(String fileName) throws URISyntaxException, IOException {
    	Set<String> objects = new HashSet<String>();
    	
        List<String> lines = Simulation.getLines(fileName);	
    	for (String line : lines) {
    		objects.add(line.trim());
    	}
    	
    	return objects;
	}
	
	/***
	 * Get the database of answers based on the indicated Tricopa Answers File.
	 * 
	 * @param fileName the name of the Tricopa Answers file
	 * @return the database of answers based on the indicated Tricopa Answers File.
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
	 * Get the Set of task numbers to exclude from the indicated Tricopa Exclusions File.
	 * @param fileName the name of the Tricopa Exclusions File
	 * @return the Set of task numbers to exclude from the indicated Tricopa Exclusions File
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public static Set<Integer> getExclusionsFromFile(String fileName) throws URISyntaxException, IOException {
		Set<Integer> toExclude = new HashSet<Integer> ();
        List<String> lines = Simulation.getLines(fileName);
        for (String line : lines) {
    		if (!isComment(line)) {
    			String trimmedLine = line.trim();
    			if (!trimmedLine.isEmpty()) {
    				String[] nums = trimmedLine.split("\\s+");
    				for (String num : nums) {
    					toExclude.add(Integer.parseInt(num));
    				}
    			}
    		}
    	}
    	return toExclude;
	}
	
	/***
	 * Get the ActionKnowledge database based on the indicated Knowledge File.
	 * 
	 * @param fileName the name of the Knowledge File
	 * @param verbose
	 * @return the ActionKnowledge database based on the indicated Knowledge File.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static Map<String, ActionROD> getActionKnowledgebase(String fileName, boolean verbose) throws URISyntaxException, IOException {
		//The knowledge file is interpreted with the constraint that the ratio between two probabilities in a single action's relative observation distribution must always be 1 or this ratio:
		double RATIO_PERMITTED_WITHIN_ROD = 2; 		
		
		List<String> lines = Simulation.getLines(fileName);	
    	
		Map<String, ActionROD> actionKnowledgebase = new HashMap<String, ActionROD>();
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
							case 'F':
								expectationsGivenRelationshipType.put(RelationshipType.FRIEND, true);
								break;
							case 'E':
								expectationsGivenRelationshipType.put(RelationshipType.ENEMY, true);
								break;
							case 'N':
								expectationsGivenRelationshipType.put(RelationshipType.NEUTRAL, true);
								break;
						}//done with char
					}//done with all chars on this line
				}
				if(verbose) {System.out.println(expectationsGivenRelationshipType);}
				actionKnowledgebase.put(args[0], new ActionROD(expectationsGivenRelationshipType, RATIO_PERMITTED_WITHIN_ROD));
			}
    	}//done with all lines
    	return actionKnowledgebase;
	}
	
	/***
	 * Get all lines from the indicated file.
	 * @param fileName
	 * @return the lines from the indicated file
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private static List<String> getLines(String fileName) throws URISyntaxException, IOException {				
		String s = fileName;		
		URI uri = Simulation.class.getResource(s).toURI();
//		if (verbose) { System.out.println(uri);} //for debug
		return Files.readAllLines(Paths.get(uri), Charset.defaultCharset());
	}
	
	////////////////////////////////////////////////////////////
	////////FOR PARSING LOWER-LEVEL TEXT////////////////////////
	////////////////////////////////////////////////////////////
	/***
	 * Get the Scenario based on the given line of logical literals.
	 * @param line line of logical literals
	 * @return scenario based on the given line of logical literals
	 */
	private static Scenario getScenario(String line) {
		List<ActionEvent> scenarioUnits = new ArrayList<ActionEvent>();
		
		String REFERENCE_REGEX = "e[0-9]+|E[0-9]+"; //Regex to use for matching the e# notation used when one event refers to another event
		String LITERAL_DELIMITER = "\\(|\\)"; //Regex to use for matching the (literal) notation of literals
		String[] literals = line.split(LITERAL_DELIMITER, 0);
		
		for (String literal : literals) {
			if (!literal.isEmpty()) {
				String[] args = literal.split("\\s");
				if (args.length != 3 && args.length!=4) {
					//ignore this literal
				}
				else if (args.length>=1 && isPredicate(args[0])) { //predicate
					boolean validActionEvent = true;

					//Turn it into an ActionEvent
					String action = args[0].substring(0, args[0].length()-1);
					//ignore args[1] which is the e# style tag
					String actor = args[2];
					if (actor.matches(REFERENCE_REGEX)) {
						validActionEvent = false;
					} 
					
					String actedUpon = null; //for now
					if (args.length == 4) { //there is an actedUpon
						actedUpon = args[3];
						if (actedUpon.matches(REFERENCE_REGEX)) {
							actedUpon = null;
//							validActionEvent = false;
						}
					}
					
					if (validActionEvent) {
						scenarioUnits.add(new ActionEvent(actor, action, actedUpon));
					} else {
//						System.out.println("Problem args: " + Arrays.asList(args)); //debug what kind of literals are being ignored
					}
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
	 * Return true iff line begins with // (and is thus a comment).
	 * @param line
	 * @return true iff line begins with // (and is thus a comment)
	 */
	public static boolean isComment(String line) {
		return line.startsWith("//");
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
			if (verbose) {
				System.out.println();
				System.out.println("TASK " + taskNumber);
			}
			
			try {
				int chosen = socialAgent.doTricopaTask(tricopaTask);
				
				if ( chosen == answers.get(taskNumber) ) {
					performanceOnTasks.put(taskNumber, TricopaTaskPerformance.CORRECT);
					if (verbose) {System.out.println("CORRECT");}
				} else {
					performanceOnTasks.put(taskNumber, TricopaTaskPerformance.INCORRECT);
					if (verbose) {System.out.println("INCORRECT");}
				}
			} catch (UndecidedAgentException e) {
				performanceOnTasks.put(taskNumber, TricopaTaskPerformance.INCOMPLETE);
				if (verbose) {System.out.println("INCOMPLETE (THE CORRECT ANSWER WAS " + answers.get(taskNumber) + ")");}
			}
			if (verbose) {System.out.println();}
			if (verbose) {System.out.println("****************************************************************");}
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
		int numCorrect = 0;
		int numAnswered = 0;
		for ( TricopaTaskPerformance currPerformance : performanceOnTasks.values()) {
			if (currPerformance.equals(TricopaTaskPerformance.CORRECT)) {numCorrect++; numAnswered++;}
			else if (currPerformance.equals(TricopaTaskPerformance.INCORRECT)) {numAnswered++;}
		}
		int numTasks = performanceOnTasks.size();
		
		//Construct text
		NumberFormat format = NumberFormat.getPercentInstance();
		format.setMinimumIntegerDigits(2);
		
		String statement = "";
		statement += "ON THE " + numTasks + " TASKS, THE AGENT ANSWERED " + numAnswered + "/" + numTasks + "=" + format.format((double)numAnswered/(double)(numTasks)) + "\n";
		statement += "ON THE " + numAnswered + " TASKS ANSWERED, THE AGENT CORRECTLY ANSWERED " + numCorrect + "/" + numAnswered + "=" + format.format((double)numCorrect/(double)(numAnswered)) + "\n";
		return statement;
	}
	
	/***
	 * TODO test more
	 * Construct a table-like text representation of performanceOnTasks, where each task number is mapped to 'X' for not administered, '1' for performed correctly,
	 *  '-1' for performed incorrectly, or '0' for administered but agent was undecided. This is useful for pasting directly into Excel and creating a heatmap.
	 * @param performanceOnTasks
	 * @return a table-like text representation of performanceOnTasks
	 */
	public static String getTableRepresentationOfPerformance(Map<Integer, TricopaTaskPerformance> performanceOnTasks) {
		String sheet = "";
		
		List<Integer> orderedTaskNumbers = Simulation.asSortedList(performanceOnTasks.keySet());
		
		int lastTaskNumber = 1;
		for (int currTaskNumber : orderedTaskNumbers) {
			//add information about skipped tasks
			for (int i =lastTaskNumber; i<currTaskNumber; i++) {
				sheet += i+":\t \n";
			}
			
			//add information about current task 
			if (performanceOnTasks.get(currTaskNumber) == TricopaTaskPerformance.INCORRECT) {
				sheet+= currTaskNumber+":\t -1" + "\n";
			}
			else if (performanceOnTasks.get(currTaskNumber) == TricopaTaskPerformance.INCOMPLETE) {
				sheet+= currTaskNumber+":\t 0" + "\n";
			}
			else if (performanceOnTasks.get(currTaskNumber) == TricopaTaskPerformance.CORRECT) {
				sheet+= currTaskNumber+":\t 1" + "\n";
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
	/// FOR REPORTING AGENT INTERPRETATION OF A SINGLE SCENARIO ///// TODO COME BACK TO THIS IF NECESSARY
	/////////////////////////////////////////////////////////////////	
//	/***
//	 * 
//	 * @param relationshipBeliefsOverTime
//	 * @return
//	 */
//	public static String getTableRepresentationOfRelationshipBeliefsOverTime(Pair<String> relationship, Map<Integer, Map<RelationshipType, Double>> relationshipBeliefsOverTime) {
//		try {
//			String sheet = "";
//			int minTime = Collections.min(relationshipBeliefsOverTime.keySet());
//			int maxTime = Collections.max(relationshipBeliefsOverTime.keySet());
//			sheet += "Time\tBelief that the relationship " + relationship + " is\n";
//			sheet += "\tFriend\tNeutral\tEnemy\n";
//			sheet += "-----------------------------\n";
//			for (int currTime = minTime; currTime<=maxTime; currTime++) {
//				if (relationshipBeliefsOverTime.containsKey(currTime)) {
//					sheet += currTime + "\t";
//					Map<RelationshipType, Double> currBelief = relationshipBeliefsOverTime.get(currTime);
//					for (Map.Entry<RelationshipType,Double> entry : currBelief.entrySet()) {
//						NumberFormat format = NumberFormat.getPercentInstance();
//						format.setMinimumIntegerDigits(2);
//						sheet +=  format.format(entry.getValue()) + "\t";
//					}//done with all relationship types
//					sheet += "\n";
//				}
//			}
//			return sheet;
//		} catch (NullPointerException e) {
//			System.err.println(e.getClass() + ": " + e.getMessage());
//		}
//		return null;//failed
//	}	
	

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
		 * Default arameters
		 */		
		boolean VERBOSE_FILE_READING = false; //useful to turn on when debugging file reading
		
		boolean VERBOSE_AGENT = false;

		boolean ADMINISTER_SINGLE_SCENARIO = false;
		//Used iff ADMINISTER_SINGLE_SCENARIO parameter is set to true:
		String SCENARIO_FILENAME = null; 
		String SCENARIO_KNOWLEDGE_FILENAME = null;
		String SCENARIO_NONAGENTS_FILENAME = null;
		boolean QUERY_SPECIFIC_RELATIONSHIP = false;
		Pair<String> QUERIED_RELATIONSHIP = null;
		
		boolean ADMINISTER_TRICOPA_TASKS = false;
		//Used iff ADMINISTER_TRICOPA_TASKS parameter is set to true:
		String TRICOPA_TASKS_FILENAME = null;
		String TRICOPA_KNOWLEDGE_FILENAME = null;
		String TRICOPA_NONAGENTS_FILENAME = null;
		String TRICOPA_ANSWERS_FILENAME = null;
		String TRICOPA_EXCLUDE_FILENAME = null;
	
		/***
		 * Setting params from main args
		 */
		switch (args[0]) {
		case "s":
			ADMINISTER_SINGLE_SCENARIO = true;
			ADMINISTER_TRICOPA_TASKS = false;
			
			switch (args[1]) {
			case "y":
				VERBOSE_AGENT =  true;
				break;
			case "n":
				VERBOSE_AGENT =  false;
				break;
			default:
				throw new IllegalArgumentException();
			}
			SCENARIO_FILENAME = args[2]; 
			SCENARIO_KNOWLEDGE_FILENAME = args[3];
			SCENARIO_NONAGENTS_FILENAME = args[4];
			if (args.length==7) {
				QUERY_SPECIFIC_RELATIONSHIP = true;
				QUERIED_RELATIONSHIP = new Pair<String>(args[5],args[6]);
			}
			break;
		case "t":
			ADMINISTER_SINGLE_SCENARIO = false;
			ADMINISTER_TRICOPA_TASKS = true;
			
			switch (args[1]) {
			case "y":
				VERBOSE_AGENT =  true;
				break;
			case "n":
				VERBOSE_AGENT =  false;
				break;
			default:
				throw new IllegalArgumentException();
			}
			TRICOPA_TASKS_FILENAME = args[2];
			TRICOPA_KNOWLEDGE_FILENAME = args[3];
			TRICOPA_NONAGENTS_FILENAME = args[4];
			TRICOPA_ANSWERS_FILENAME = args[5];
			if (args.length == 7) {
				TRICOPA_EXCLUDE_FILENAME = args[6]; //optional
			}
			break;
		default:
			throw new IllegalArgumentException();
		}
		
		
		/***
		 * Administer single story.
		 */
		if (ADMINISTER_SINGLE_SCENARIO) {
			/***
			 * Spawn a social agent with knowledge
			 */
			Map<String, ActionROD> actionKnowledgebase = Simulation.getActionKnowledgebase(SCENARIO_KNOWLEDGE_FILENAME, VERBOSE_FILE_READING);
			Set<String> nonagentsSet = Simulation.getNonagentsFromFile(SCENARIO_NONAGENTS_FILENAME);
			AffinitybasedAgent affinitybasedAgent = new AffinitybasedAgent(actionKnowledgebase, nonagentsSet, VERBOSE_AGENT);

			/***
			 * Administer story to social agent
			 */
			Scenario story = Simulation.getScenarioFromFile(SCENARIO_FILENAME);
			
			/***
			 * Social agent reads the story, possibly with a focus on the queried relationship.
			 */
			if (QUERY_SPECIFIC_RELATIONSHIP) {
				affinitybasedAgent.read(story, false, QUERIED_RELATIONSHIP);
				affinitybasedAgent.stateBelief(QUERIED_RELATIONSHIP);				
			} else {
				affinitybasedAgent.read(story, false);
			}
		} // done administering story
		if (ADMINISTER_TRICOPA_TASKS) {		
			/***
			 * Spawn a social agent with knowledge from knowledge file
			 */
			Map<String, ActionROD> actionKnowledgebase = Simulation.getActionKnowledgebase(TRICOPA_KNOWLEDGE_FILENAME, VERBOSE_FILE_READING);
			Set<String> knownNonagents = Simulation.getNonagentsFromFile(TRICOPA_NONAGENTS_FILENAME);
			TricopaParticipant socialAgent = new AffinitybasedAgent(actionKnowledgebase, knownNonagents, VERBOSE_AGENT);
			
			/***
			 * Set up, before administering tasks to social agent
			 */
			Map<Integer,TricopaTask> allTricopaTasks = Simulation.getTricopaTasksFromFile(TRICOPA_TASKS_FILENAME, VERBOSE_FILE_READING);
			Map<Integer,Integer> answers = Simulation.getAnswersFromFile(TRICOPA_ANSWERS_FILENAME);

			Set<Integer> taskNumsToDo = GetRange(1, allTricopaTasks.size()); //Which task numbers to do
			if (TRICOPA_EXCLUDE_FILENAME != null) { //Possibly exclude some tasks
				Set<Integer> taskNumsToExclude = Simulation.getExclusionsFromFile(TRICOPA_EXCLUDE_FILENAME); 	//Which task numbers to consider exceptions
				taskNumsToExclude.add(22);
				for (Integer task_number : taskNumsToExclude) {
					taskNumsToDo.remove(task_number);
				}
			}
			Map<Integer,TricopaTask> tasksToDo = Simulation.getTasks(allTricopaTasks, taskNumsToDo);

			
			/***
			 * Administer Tricopa tasks to social agent
			 * For each task, social agent reads the tasks, updates interpreted relationship information, and answers the task
			 * runner reports the social agent's performance
			 */
			Map<Integer,TricopaTaskPerformance> performanceOnTasks = Simulation.administerTricopaTasks(socialAgent, tasksToDo, answers, VERBOSE_AGENT);
			
			
			/***
			 * Print results
			 */
//			System.out.println("\n" + Simulation.getTableRepresentationOfPerformance(performanceOnTasks)); TODO
			System.out.println("" + Simulation.getScoreStatement(performanceOnTasks));
		}//done administering Tricopa tasks
		
	}



}
