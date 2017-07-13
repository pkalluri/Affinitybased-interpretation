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
			Map<String, ActionROD> actionKnowledgebase = FileReadingUtility.getActionKnowledgebase(SCENARIO_KNOWLEDGE_FILENAME, VERBOSE_FILE_READING);
			Set<String> nonagentsSet = FileReadingUtility.getNonagentsFromFile(SCENARIO_NONAGENTS_FILENAME);
			AffinitybasedAgent affinitybasedAgent = new AffinitybasedAgent(actionKnowledgebase, nonagentsSet, VERBOSE_AGENT);

			/***
			 * Administer story to social agent
			 */
			Scenario story = FileReadingUtility.getScenarioFromFile(SCENARIO_FILENAME);
			
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
			Map<String, ActionROD> actionKnowledgebase = FileReadingUtility.getActionKnowledgebase(TRICOPA_KNOWLEDGE_FILENAME, VERBOSE_FILE_READING);
			Set<String> knownNonagents = FileReadingUtility.getNonagentsFromFile(TRICOPA_NONAGENTS_FILENAME);
			TricopaParticipant socialAgent = new AffinitybasedAgent(actionKnowledgebase, knownNonagents, VERBOSE_AGENT);
			
			/***
			 * Set up, before administering tasks to social agent
			 */
			Map<Integer,TricopaTask> allTricopaTasks = FileReadingUtility.getTricopaTasksFromFile(TRICOPA_TASKS_FILENAME, VERBOSE_FILE_READING);
			Map<Integer,Integer> answers = FileReadingUtility.getAnswersFromFile(TRICOPA_ANSWERS_FILENAME);

			Set<Integer> taskNumsToDo = GetRange(1, allTricopaTasks.size()); //Which task numbers to do
			if (TRICOPA_EXCLUDE_FILENAME != null) { //Possibly exclude some tasks
				Set<Integer> taskNumsToExclude = FileReadingUtility.getExclusionsFromFile(TRICOPA_EXCLUDE_FILENAME); 	//Which task numbers to consider exceptions
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
