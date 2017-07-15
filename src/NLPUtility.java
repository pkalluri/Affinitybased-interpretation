
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class NLPUtility {
	
	

	/***
	 * Get list of ActionEVents from the given lines.
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
//					System.out.println("sentence: " + line);
//					System.out.println(tdl);
			try {
				ActionEvent actionEvent = NLPUtility.getActionEvent(tdl);
//						System.out.println("***" + actionEvent);
				actionEvents.add(actionEvent);
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
	 */
	public static ActionEvent getActionEvent(List<TypedDependency> tdl) throws FailedActionEventCreationException {
		String action = null;
		String actor = null;
		String actedUpon = null;
		
//		IndexedWord possibleAction;
//		IndexedWord possibleActor;
//		IndexedWord possibleActedUpon;
		
		for (TypedDependency td : tdl) {
			GrammaticalRelation reln = td.reln();
			String relnName = td.reln().getShortName();
//			System.out.println(relnName);
			
			if (relnName.equals("nsubj")) { //found actor and action
				if (action==null) { //only first time encountered
					actor = td.dep().originalText();
					action = td.gov().originalText();
				} else { //ignore
					break; //be done
				}
			}
			if (relnName.matches("compound")) { //ignore later parts of compound sentences
				break;
			}
			if (relnName.matches("(nmod.*)")) { //found possible actedUpon
				if (actedUpon==null) { //only then consider filling
					String specific = reln.getSpecific();
					if (specific!=null && reln.getSpecific().matches("at")){
						//ignore
					} else {
						actedUpon = td.dep().originalText();
					}
				}
			}
			if (relnName.matches("(dobj)")) { //found possible actedUpon
				actedUpon = td.dep().originalText();
			}
			if (relnName.matches("(xcomp)")) { //multi-part action
				action += td.dep().originalText();
			}
		}
		if (action==null || actor==null) {
			throw new FailedActionEventCreationException();
		}
		
		//build it
		return new ActionEvent(actor, action, actedUpon);
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
		String SCENARIO_NL_FILE = "Macbeth-Sentences.txt";
		Scenario scenario = FileUtility.getScenarioFromNLFile(SCENARIO_NL_FILE);
	}
}
