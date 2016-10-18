import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HumanAgent {
	private Map<String, ActionKnowledge> actionKnowledgebase;
	
	/***
	 * For augmentations
	 */
	String secondToLast_character = "_";
	String last_character = "_";

	private boolean verbose;
	
	static Set<String> OBJECTS = new HashSet<String>(Arrays.asList("CORNER","D", "OUTSIDE", "x", "BEHINDBOX"));
//	static boolean REPLACE_OBJECTS = false;
	static boolean REPLACE_OBJECTS = true;
	
	public HumanAgent(Map<String, ActionKnowledge> actionKnowledgebase) {
		this.actionKnowledgebase = actionKnowledgebase;
	}
	
	public boolean isCharacter(String s) {
		if (s==null || s.equals("_")) {
			return false;
		} else { //valid string
			if (REPLACE_OBJECTS) { 
				return (!OBJECTS.contains(s)); 
			} else {
				return true;
			}
		}
	}
	
	private WorldModelInterface extractRelationshipInfo(Description premise, boolean forceCharacters, boolean continuing) throws Exception {
		WorldModelInterface relationshipInfo = new SocialNetworkModel();
		if (!continuing) {
			this.setMemory("_", "_");
		}
		if (verbose) {System.out.println(this.memoryToString());}
		for (DescriptionUnit descriptionUnit : premise.getDecriptionUnits() ) {
			if (verbose) {System.out.println(descriptionUnit);}
			DescriptionUnit validDescriptionUnit = this.getValidDescriptionUnit(descriptionUnit, forceCharacters); //get valid unit
			if (validDescriptionUnit != null) {
				if (verbose) {System.out.print(validDescriptionUnit + " ");}

				//check for knowledge
				if (	!actionKnowledgebase.containsKey(validDescriptionUnit.action)	) { 
					throw new Exception ("No knowledge about this action: " + validDescriptionUnit.action);
				}
			
				//update based on the valid unit
				relationshipInfo.update(validDescriptionUnit, actionKnowledgebase.get(descriptionUnit.action));
				if (verbose) {System.out.println(relationshipInfo);}
				
				this.updateMemory(validDescriptionUnit); //update memory based on this unit
			} else {
				if (verbose) {System.out.println("Couldn't make valid.");}
				this.updateMemory(descriptionUnit); //update memory based on original unit
			}
		}//done with units
		return relationshipInfo;
	}
	
	private String memoryToString() {
		return "Memory: " + this.secondToLast_character + " " + this.last_character;
	}

	private void setMemory(String secondToLast_character, String last_character) {
		this.last_character = last_character;
		this.secondToLast_character = secondToLast_character;
	}

	private DescriptionUnit getValidDescriptionUnit(DescriptionUnit descriptionUnit, boolean forceCharacters) {
		if (descriptionUnit.actedUpon == null) { //Replace with empty string to enable comparisons
			descriptionUnit = new DescriptionUnit(descriptionUnit.actor,descriptionUnit.action,"_");
		}
		if (descriptionUnit.actor == null) { //Replace with empty string to enable comparisons
			descriptionUnit = new DescriptionUnit("_",descriptionUnit.action,descriptionUnit.actedUpon);
		}
		
		boolean gotValidDescriptionUnit = true;
		
		/***
		 * @AUGMENTATION Modify description units to force characters
		 */
		if (forceCharacters){
			if (!this.isCharacter(	descriptionUnit.actor) ) {
				gotValidDescriptionUnit = false;
				descriptionUnit = this.replaceActor(descriptionUnit);
				gotValidDescriptionUnit = descriptionUnit != null;
			}//replaced
			if (!this.isCharacter(	descriptionUnit.actedUpon) ) {
				gotValidDescriptionUnit = false;
				descriptionUnit = this.replaceActedUpon(descriptionUnit);
				gotValidDescriptionUnit = descriptionUnit != null;
			}//replaced
		}
		
		if (gotValidDescriptionUnit) {
			return descriptionUnit;
		} else {
			return null;
		}
	}
	
	private DescriptionUnit replaceActor(DescriptionUnit descriptionUnit) {
		DescriptionUnit validDescriptionUnit = null;		

		if (isCharacter(last_character) && !descriptionUnit.actedUpon.equals(last_character)) { //if different
			validDescriptionUnit = new DescriptionUnit(last_character,descriptionUnit.action,descriptionUnit.actedUpon);					
		} else if (isCharacter(this.secondToLast_character) && !descriptionUnit.actedUpon.equals(secondToLast_character)) { //if different
			validDescriptionUnit = new DescriptionUnit(secondToLast_character,descriptionUnit.action,descriptionUnit.actedUpon);					
		}
		
		return validDescriptionUnit;
	}
	
	private DescriptionUnit replaceActedUpon(DescriptionUnit descriptionUnit) {
		DescriptionUnit validDescriptionUnit = null;
		String replacement_character;
		

		if (isCharacter(last_character) && !descriptionUnit.actor.equals(last_character)) { //if different
			replacement_character = last_character; //use last
			validDescriptionUnit = new DescriptionUnit(descriptionUnit.actor,descriptionUnit.action,replacement_character);					
		} else if (isCharacter(this.secondToLast_character) && !descriptionUnit.actor.equals(secondToLast_character)) { //if different
			replacement_character = secondToLast_character; //use second to last
			validDescriptionUnit = new DescriptionUnit(descriptionUnit.actor,descriptionUnit.action,replacement_character);					
		}
		
		return validDescriptionUnit;
	}

	private boolean updateMemory(DescriptionUnit descriptionUnit) {
		boolean updated = false;
		if (this.isCharacter(descriptionUnit.actedUpon)	) {
			this.updateMemory(descriptionUnit.actedUpon);
			updated = true;
		}
		updated = this.updateMemory(descriptionUnit.actor) || updated;
		return updated;
	}
	

	private boolean updateMemory(String latestChar) {
		if (!this.last_character.equals(latestChar)) {
			this.secondToLast_character = last_character;
			this.last_character = latestChar;
			if (verbose) {System.out.println("Updated " + this.memoryToString());}
			return true;
		}
//		if (verbose) {System.out.println("Not updated " + this.memoryToString());}
		return false;
	}

	@SuppressWarnings("unused")
	private int chooseBetweenPlausibleAlternatives_FindDistance(WorldModelInterface relationshipInfo,
			List<Description> possibleChoices, String remembered_secondToLast_character, String remembered_last_character) throws Exception {
		int bestChoiceNumber = 0;
		Description bestChoice = null;
		double bestChoice_distance = Double.MAX_VALUE;
		int currChoiceNumber = 0;
		for (Description currChoice : possibleChoices) {
			currChoiceNumber ++;
			System.out.println("REASONING ABOUT PLAUSIBLE CHOICE...");
			
			//Refresh memory to end of premise
			this.secondToLast_character = remembered_secondToLast_character;
			this.last_character = remembered_last_character;
			
			double currChoice_distance = this.distance(currChoice, relationshipInfo);
			System.out.println("Distance to premise: " + currChoice_distance);
			if (currChoice_distance < bestChoice_distance) {
				bestChoiceNumber = currChoiceNumber;
				bestChoice = currChoice;
				bestChoice_distance = currChoice_distance;
			} else if (currChoice_distance == bestChoice_distance) {
				/***
				 * Do not pick randomly (That leads to false impression of success). Pick neither, guaranteeing failure is documented.
				 * (We don't want to let these fail soft, because these are the especially interseting cases!)
				 */
//				return new Description(null,null,null);
				bestChoiceNumber = -1;
			}
		}
		System.out.println(bestChoiceNumber);
		return bestChoiceNumber;
	}
	
	private int chooseBetweenPlausibleAlternatives(WorldModelInterface relationshipInfo,
			List<Description> possibleChoices, String remembered_secondToLast_character, String remembered_last_character, boolean forceCharacters) throws Exception {
				
		//longest description length
		int longestDescriptionLength = getMaxDescriptionLength(possibleChoices);
		
		double probabilityOfBestChoice = 0;
		int bestChoiceNumber = -1;
		
		int choiceNumber = 0;
		//keep track of highest probability description
		for (Description choice : possibleChoices) {

			choiceNumber ++;
			
			if (verbose) {System.out.println("CONSIDERING CHOICE");}
			double probabilityOfThisChoice = 1;
			int numTimesUpdated = 0;
			
			this.setMemory(remembered_secondToLast_character, remembered_last_character);
			
			for (DescriptionUnit descriptionUnit : choice.getDecriptionUnits() ) {
				if (verbose) {System.out.println(descriptionUnit);}
//				System.out.println(this.memoryToString());
//				System.out.println("Original unit: " + descriptionUnit);
				DescriptionUnit validDescriptionUnit = this.getValidDescriptionUnit(descriptionUnit, forceCharacters); //get valid unit
				if (validDescriptionUnit != null) {
					if (verbose) {System.out.print(validDescriptionUnit + " ");}
	
	
					//check for knowledge
					if (	!actionKnowledgebase.containsKey(validDescriptionUnit.action)	) { 
						throw new Exception ("No knowledge about this action: " + validDescriptionUnit.action);
					}
				
					//how likely is the valid unit
					double probabilityOfThisUnit = relationshipInfo.probabilityOf(validDescriptionUnit, actionKnowledgebase.get(descriptionUnit.action));
					probabilityOfThisChoice *= probabilityOfThisUnit;
					System.out.println(probabilityOfThisChoice);
					numTimesUpdated ++;
					this.updateMemory(validDescriptionUnit); //update memory based on this unit
				} else {
					this.updateMemory(descriptionUnit); //update memory based on original unit
				}
			}//done with units
			
			for (int i = numTimesUpdated; i < longestDescriptionLength; i++) {
				probabilityOfThisChoice *= probabilityOfThisChoice;
				System.out.println("Normalizing " + probabilityOfThisChoice);
			}//done normalizing
			
			if (numTimesUpdated == 0) {
				probabilityOfThisChoice = -1; //do not consider complete disjoint
			}
			System.out.println("Probability of choice: " + probabilityOfThisChoice);
			
			//update best choice
			if (probabilityOfThisChoice == probabilityOfBestChoice) {
				return -1; //indicating tie
			} else if (probabilityOfThisChoice > probabilityOfBestChoice) {
				probabilityOfBestChoice = probabilityOfThisChoice;
				bestChoiceNumber = choiceNumber;
			}
		}//done with choice

		return bestChoiceNumber;
	}
	
	private int getMaxDescriptionLength(List<Description> descriptions) {
		int maxDescriptionLength = 0;
		for (Description description : descriptions) {
			int currDescriptionLength = description.descriptionLength();
			if (currDescriptionLength > maxDescriptionLength) {
				maxDescriptionLength = currDescriptionLength;
			}
		}//done with all descriptions
		return maxDescriptionLength;
	}

	private double distance(Description currChoice, WorldModelInterface premiseRelationshipInfo) throws Exception {
//		RelationshipInfoInterface currChoiceRelationshipInfo = extractRelationshipInfo(currChoice, false, false); //At first
		WorldModelInterface currChoiceRelationshipInfo = extractRelationshipInfo(currChoice, true, true); //Later
		return currChoiceRelationshipInfo.distanceFrom(premiseRelationshipInfo);
	}


	public int doTricopaTask(Description premise, List<Description> possibleChoices) throws Exception {
		System.out.println("REASONING ABOUT PREMISE");
//		RelationshipInfoInterface relationshipInfo = extractRelationshipInfo(premise, false, false); //At first
		boolean forceCharacters = true;
		WorldModelInterface relationshipInfo = extractRelationshipInfo(premise, forceCharacters, false); //Later
		return chooseBetweenPlausibleAlternatives(relationshipInfo, possibleChoices, this.secondToLast_character, this.last_character, forceCharacters);
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
