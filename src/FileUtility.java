
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
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
import java.util.stream.Collectors;

/***
 * The FileReadingUtility contains methods to aid in this project's working with text files. 
 * @author pkalluri
 *
 */
public class FileUtility {
	////////////////////////////////////////////////////////////
	//////// FOR SETTING UP FILES //////////////////////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Given the scenario in the indicated Scenario File which may or may not be in natural language,
	 * makes a possible knowledge file with the given KNOWLEDGE_FILENAME and a possible characters file with the given CHARACTERS_FILENAME.
	 * TODO
	 * @param SCENARIO_FILENAME
	 * @param NL
	 * @param KNOWLEDGE_FILENAME
	 * @param CHARACTERS_FILENAME
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static void setupFiles(String SCENARIO_FILENAME, boolean NL, String KNOWLEDGE_FILENAME, String CHARACTERS_FILENAME) throws URISyntaxException, IOException {
		if ( FileUtility.fileExists(KNOWLEDGE_FILENAME)) {
			throw new FileAlreadyExistsException(KNOWLEDGE_FILENAME);
		}
		if ( FileUtility.fileExists(CHARACTERS_FILENAME)) {
			throw new FileAlreadyExistsException(CHARACTERS_FILENAME);
		}
		
		Scenario story;
		if (NL) {
			story = FileUtility.getScenarioFromNLFile(SCENARIO_FILENAME);
		} else {
			story = FileUtility.getScenarioFromFile(SCENARIO_FILENAME);
		}
		
		Set<String> actions = new HashSet<String>();
		Set<String> possibleCharacters = new HashSet<String>();
		for (ActionEvent actionEvent : story.actionEvents) {
			actions.add(actionEvent.action);
			possibleCharacters.add(actionEvent.actor);
			if (actionEvent.actedUpon != null) {
				possibleCharacters.add(actionEvent.actedUpon);
			}
		}
		
		//print to files
		FileUtility.makeFile(actions, KNOWLEDGE_FILENAME);
		FileUtility.makeFile(possibleCharacters, CHARACTERS_FILENAME);
	}
	
//	public static void setupTricopaFiles(String SCENARIO_FILENAME, boolean NL, String KNOWLEDGE_FILENAME, String CHARACTERS_FILENAME) throws URISyntaxException, IOException {
//		if ( FileUtility.fileExists(KNOWLEDGE_FILENAME)) {
//			throw new FileAlreadyExistsException(KNOWLEDGE_FILENAME);
//		}
//		if ( FileUtility.fileExists(CHARACTERS_FILENAME)) {
//			throw new FileAlreadyExistsException(CHARACTERS_FILENAME);
//		}
//		
//		Scenario story;		
//	}
	
	/***
	 * Return true iff file already exists.
	 * @param filename
	 * @return
	 */
	private static boolean fileExists(String filename) {
		File possibleFile = new File(filename);
		return possibleFile.exists();
	}

	/***
	 * Make file with the given filename containing the given lines.
	 * @param lines
	 * @param filename
	 * @throws IOException
	 */
	private static void makeFile(Collection<String> lines, String filename) throws IOException {
	/***
	 * Fill knowledge file.
	 */
	BufferedWriter output;
	output = new BufferedWriter(new FileWriter(filename, true));  //clears file every time
	for (String line : lines) {
		output.append(line);
		output.newLine();
	}
	output.close();
	}
	
	
	////////////////////////////////////////////////////////////
	//////// FOR READING IN FILES //////////////////////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Get the Scenario based on the indicated NL Scenario File.
	 * 
	 * @param fileName the name of the Scenario File
	 * @return the scenario based on the indicated NL Scenario File
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static Scenario getScenarioFromNLFile(String filename) throws URISyntaxException, IOException {
		List<String> lines = FileUtility.getLines(filename);	
		List<ActionEvent> actionEvents = NLPUtility.getActionEvents(lines);
		return new Scenario(actionEvents);
	}
	
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
		List<String> lines = FileUtility.getLines(fileName);	
		for (String line : lines) {
			linesTogether += line.trim(); //String together lines
		}
		return FileUtility.getScenario(linesTogether);
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
        List<String> lines = FileUtility.getLines(fileName);	
		
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
	public static Set<String> getCharactersFromFile(String fileName) throws URISyntaxException, IOException {
    	Set<String> objects = new HashSet<String>();
    	
        List<String> lines = FileUtility.getLines(fileName);	
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
    	
        List<String> lines = FileUtility.getLines(fileName);	
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
        List<String> lines = FileUtility.getLines(fileName);
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
		
		List<String> lines = FileUtility.getLines(fileName);	
    	
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
//		System.out.println("getlines"); //debug
//		if (!fileName.startsWith("/")) {
//			fileName = "/" + fileName;//add slash
//		}
		
		
		//Alternative reading strategies that may be more useful if this project is exported as a jar
		//and must access nearby files
//		InputStream f = new FileInputStream(fileName);
//		System.out.println(f);
//		
//		InputStream i = FileReadingUtility.class.getClass().getResourceAsStream(fileName);
//		System.out.println(i); //for debug
//		List<String> doc =
//			      new BufferedReader(new InputStreamReader(i,
//			          StandardCharsets.UTF_8)).lines().collect(Collectors.toList());		
//		System.out.println(doc); //for debug
//		
//		InputStream i2 = FileReadingUtility.class.getClassLoader().getResourceAsStream(fileName);
//		System.out.println(i2); //for debug
//		List<String> doc2 =
//			      new BufferedReader(new InputStreamReader(i2,
//			          StandardCharsets.UTF_8)).lines().collect(Collectors.toList());		
//		System.out.println(doc2); //for debug
		List<String> lines;
		
//		URL url = FileUtility.class.getResource(fileName);
//		if (url == null) {
//			throw new FileNotFoundException(fileName);
//		}
//		URI uri = url.toURI();
//		lines = Files.readAllLines(Paths.get(uri), Charset.defaultCharset());
//		
		
		File f = new File(fileName);
		lines = Files.readAllLines(f.toPath());
		
//		
//		BufferedReader input;
//		BufferedWriter output;
//		output = new BufferedWriter(new FileWriter(filename, true));  //clears file every time
//		for (String line : lines) {
//			output.append(line);
//			output.newLine();
//		}
//		output.close();
//		}
		
		return lines;
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
		Set<String> innerEvents = new HashSet<String>();
		
		for (String literal : literals) {
			if (!literal.isEmpty()) {
				
				//this is the part to comment out:
				boolean validActionEvent = true;
				String[] args = literal.split("\\s");
				if ( args.length>=3 && FileUtility.isPredicate(args[0]) ) {
					if (innerEvents.contains(args[1]) ) {
//						System.out.println(line);
						validActionEvent = false; //it's an inner event, so we do not count it as having occurred
					}
										
					//Turn it into an ActionEvent
					String action = args[0].substring(0, args[0].length()-1);
					//ignore args[1] which is the e# style tag
					String actor = args[2];
					if (actor.matches(REFERENCE_REGEX)) {
						innerEvents.add(actor); //update list of inner events
						validActionEvent = false;
					}
					
					String actedUpon = null; //for now
					if (args.length == 4) { //there is an actedUpon
						actedUpon = args[3];
						if (actedUpon.matches(REFERENCE_REGEX)) {
//							actedUpon = "(EVENT)";
							innerEvents.add(actedUpon); //update list of inner events
							actedUpon = null;
//							System.out.println("Problem args: " + Arrays.asList(args)); //debug what kind of literals are being ignored
						}
					}
					
					if (args.length > 4) {
						throw new RuntimeException(literal);
					}
					//update list of inner events
//					for (int i=2; i<args.length; i++) {
//						if (args[i].matches(REFERENCE_REGEX)) {
//							innerEvents.add(args[i]);
//						}
//					}
					
					if (validActionEvent) {
						scenarioUnits.add(new ActionEvent(actor, action, actedUpon));
					} else {
//						System.out.println("Problem args: " + Arrays.asList(args)); //debug what kind of literals are being ignored
					}
				}
				
				//this is the part to comment in:
//				String[] args = literal.split("\\s");
//				if (args.length>=3 && isPredicate(args[0])) { //parseable
//					boolean validActionEvent = true;
//
//					//Turn it into an ActionEvent
//					String action = args[0].substring(0, args[0].length()-1);
//					//ignore args[1] which is the e# style tag
//					String actor = args[2];
//					if (actor.matches(REFERENCE_REGEX)) {
//						validActionEvent = false;
//					} 
//					
//					String actedUpon = null; //for now
//					if (args.length == 4) { //there is an actedUpon
//						actedUpon = args[3];
//						if (actedUpon.matches(REFERENCE_REGEX)) {
////							actedUpon = "(EVENT)";
//							actedUpon = null;
////							System.out.println("Problem args: " + Arrays.asList(args)); //debug what kind of literals are being ignored
//						}
//					}
//					
//					if (validActionEvent) {
//						scenarioUnits.add(new ActionEvent(actor, action, actedUpon));
//					} else {
////						System.out.println("Problem args: " + Arrays.asList(args)); //debug what kind of literals are being ignored
//					}
//				}
				
				
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
		PREDICATES_TO_IGNORE.add("seq'");

		PREDICATES_TO_IGNORE.add("goal'");
		
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
	
	/***
	 * Test getting line from files.
	 * @param args
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException, IOException {
		String fileName = "Knowledge.txt";
		System.out.println(FileUtility.getLines(fileName));
		
	}
	
}
