
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class NLPUtility {
	
	

	/***
	 * Get list of ActionEvents from the given lines.
	 * @param lines
	 * @return
	 */
	public static List<ActionEvent> getActionEvents(List<String> lines) {
		List<ActionEvent> actionEvents = new ArrayList<ActionEvent>();
		
		//Get general NLP tools
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
	    String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	    LexicalizedParser parser = LexicalizedParser.loadModel(parserModel);
	    TreebankLanguagePack tlp = parser.treebankLanguagePack(); // PennTreebankLanguagePack for English
	    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		
		for (String line : lines) {
			//Replace 2 word names with 1 word names
			line = line.replaceAll("(([A-Z][a-z]+)(?=\\s[A-Z])(?:\\s([A-Z][a-z]+))+)", "$2$3");

			List<TypedDependency> tdl = NLPUtility.getTypedDependencyList(line, tokenizerFactory, parser, gsf);
					System.out.println("Sentence: " + line);
//					System.out.println(tdl);
			try {
				List<ActionEvent> someActionEvents = NLPUtility.getActionEventsFromTDL(tdl);
				System.out.println("Extracted events: " + someActionEvents);
				actionEvents.addAll(someActionEvents);
			} catch (FailedActionEventCreationException e) {
				//Skip this line
			}
		}
		return actionEvents;
	}
	
	/***
	 * Construct ActionEvent from given tdl.
	 * @param tdl
	 * @return
	 * @throws FailedActionEventCreationException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static List<ActionEvent> getActionEventsFromTDL(List<TypedDependency> tdl) throws FailedActionEventCreationException, IllegalArgumentException {
		List<ActionEvent> actionEvents = new ArrayList<ActionEvent>();
		
		String action = null;
		String actor = null;
		String actedUpon = null;
		
//		IndexedWord possibleAction;
//		IndexedWord possibleActor;
//		IndexedWord possibleActedUpon;
//		boolean lastLoop = false;
//		while (!lastLoop) { //loop through tdl until tdl does not provide updates
//			lastLoop = true; //possibly
//		}
		boolean firstSubject = true;

		for (TypedDependency td : tdl) { //loop through all
			GrammaticalRelation reln = td.reln();
//			IndexedWord gov = td.gov();
//			
//			System.out.print(reln.getShortName());
//			System.out.println();
//			System.out.print(reln.getLongName());
//			System.out.println();
//			System.out.print(reln.getParent());
//			System.out.println();
//			for (Field field : gov.getClass().getDeclaredFields()) {
//			    field.setAccessible(true);
//			    String name = field.getName(); // 
//			    Object value = null;
//				try {
//					value = field.get(gov);
//				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//				}
//			    System.out.printf("Field name: %s, Field value: %s%n", name, value);
//			}
			
//			String relnShortName = td.reln().getShortName();
			String relnLongName = td.reln().getLongName();

			
//			if (relnName.equals("nsubj")) { //found actor and action
//				if (action==null) { //only first time encountered
//					actor = td.dep().originalText();
//					action = td.gov().originalText();
//				} else { //ignore
//					break; //be done
//				}
//			}
//			if (relnName.matches("compound")) { //ignore later parts of compound sentences
//				break; //be done
//			}
//			if (relnName.matches("(nmod.*)")) { //found possible actedUpon
//				if (actedUpon==null) { //only then consider filling
//					String specific = reln.getSpecific();
//					if (specific!=null && reln.getSpecific().matches("at")){
//						//ignore
//					} else {
//						actedUpon = td.dep().originalText();
//					}
//				}
//			}
//			if (relnName.matches("(dobj)")) { //found possible actedUpon
//				actedUpon = td.dep().originalText();
//			}
//			if (relnName.matches("(xcomp)")) { //multi-part action
//				action += td.dep().originalText();
//			}
			
			if (relnLongName.equals("nominal subject")) { //found actor and action
				if (firstSubject == true) { //only first time encountered
//				if (action!=null && actor!=null) {
//					actionEvents.add(new ActionEvent(actor, action, actedUpon)); 
//				}
					actor = td.dep().originalText();
					action = td.gov().originalText();
					firstSubject = false; //update
				} else { //ignore
					break; //be done
				}
			} else if (relnLongName.equals("direct object")) { //found possible actedUpon
				actedUpon = td.dep().originalText();
			} else if (relnLongName.equals("copula")) { //multi-part action
				action = td.dep().originalText() + getInitCaps(action);
			}else if (relnLongName.equals("xclausal complement")) {
				action += NLPUtility.getInitCaps( td.dep().originalText() );
			} else if (relnLongName.equals("controlling nominal subject")) {
				if (td.dep().originalText().equals(actor)){
//					action += NLPUtility.getInitCaps(td.dep().originalText());
				} else { //ignore
					actedUpon = null;
					break; //be done
				}
				
			} else if (relnLongName.equals("coordination") || relnLongName.equals("compound modifier")) {
//				if (action!=null && actor!=null) {
////					actionEvents.add(new ActionEvent(actor, action, actedUpon)); 
//				}
//				actor = null;
//				action = null;
//				actedUpon = null;
				break;
			}else if (relnLongName.equals("marker")) {
//				System.out.println(reln.getSpecific());
			}
			
			if (actedUpon==null) { //check more things if still looking for actedUpon
				if (relnLongName.equals("possession modifier") ) { //e.g. A is B's wife
					action += "Of";
					actedUpon = td.dep().originalText();
				} else if (relnLongName.equals("nmod_preposition")) { //e.g. A is an enemy of B
					String specific = reln.getSpecific();
					if (specific!=null && reln.getSpecific().matches("at")){
						//ignore
					} else {
						action += getInitCaps(reln.getSpecific());
						actedUpon = td.dep().originalText();
					}
				}
			}//done checking more things
		} //done parsing tdl
		if (action!=null && actor!=null) {
			actionEvents.add(new ActionEvent(actor, action, actedUpon)); 
		}
		
		//build it
		return actionEvents;
	}
	
	private static String getInitCaps(String str) {
		// TODO Auto-generated method stub
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	/***
	 * Get typed dependency list for the given sentence, using the given general NLP tools.
	 * @param sentence
	 * @param tokenizerFactory
	 * @param parser
	 * @param gsf
	 * @return typed dependency list for the given sentence
	 */
	private static List<TypedDependency> getTypedDependencyList(String sentence, TokenizerFactory<CoreLabel> tokenizerFactory, LexicalizedParser parser, GrammaticalStructureFactory gsf) {
	    Tokenizer<CoreLabel> tokenizer = tokenizerFactory.getTokenizer(new StringReader(sentence));
	    List<CoreLabel> tokens = tokenizer.tokenize();	
	    
	    Tree parse = parser.apply(tokens);
	    
	    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
	    
	    return tdl;
	}
	
	/***
	 * Demo use of this ability for getting a scenario from a natural language file.
	 * @param args
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static void main(String[] args) throws URISyntaxException, IOException {
		String SCENARIO_NL_FILE = "files/Macbeth-NL.txt";
		@SuppressWarnings("unused")
		Scenario scenario = FileUtility.getScenarioFromNLFile(SCENARIO_NL_FILE);
//		for (ActionEvent a : scenario.actionEvents) {
//			System.out.println(a);
//		}
	}
}
