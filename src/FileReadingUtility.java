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

/***
 * The FileReadingUtility contains methods to aid in this project's interpretation of expected text files. 
 * @author pkalluri
 *
 */
public class FileReadingUtility {
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
		List<String> lines = FileReadingUtility.getLines(fileName);	
		for (String line : lines) {
			linesTogether += line.trim(); //String together lines
		}
		return FileReadingUtility.getScenario(linesTogether);
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
        List<String> lines = FileReadingUtility.getLines(fileName);	
		
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
    	
        List<String> lines = FileReadingUtility.getLines(fileName);	
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
    	
        List<String> lines = FileReadingUtility.getLines(fileName);	
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
	 * Get the Set of task numbers to exclude from the indicated Tricopa Exclude File.
	 * @param fileName the name of the Tricopa Exclusions File
	 * @return the Set of task numbers to exclude from the indicated Tricopa Exclude File
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public static Set<Integer> getExclusionsFromFile(String fileName) throws URISyntaxException, IOException {
		Set<Integer> toExclude = new HashSet<Integer> ();
        List<String> lines = FileReadingUtility.getLines(fileName);
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
		
		List<String> lines = FileReadingUtility.getLines(fileName);	
    	
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
		URI uri = FileReadingUtility.class.getResource(s).toURI();
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
	
}
