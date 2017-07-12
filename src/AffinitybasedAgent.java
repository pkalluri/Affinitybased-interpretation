import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 * An AffinitybasedAgent interprets social scenarios by deducing the affinities of the constituent relationships.
 * Subsequently, the AffinitybasedAgent is able to use the inferred affinity relations to choose the most probable
 * statement from multiple possible statements about the scenario.
 * 
 * This code implements the affinity based reasoning described in Kalluri & Gervas (2017).
 * @author pkalluri
 *
 */
public class AffinitybasedAgent implements TricopaParticipant {
	
	/***
	 * The agent's knowledge about actions.
	 */
	private final Map<String, ActionKnowledge> actionKnowledgebase;
	
	/***
	 * The Strings that the agent knows refer to objects.
	 */
	private final Set<String> knownObjects;
	
	/***
	 * The agent's memory of recently observed agents.
	 */
	private RecentlyObservedAgentsMemory rememberedAgents;
	
	/***
	 * The agent's memory of the most recently built AffinitybasedWorldModel.
	 */
	private AffinitybasedWorldModel  rememberedWorldModel;
	
	private final boolean verbose;
	
	////////////////////////////////////////////////////////////
	//////// CONSTRUCTOR ///////////////////////////////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Create AffinitybasedAgent with the given actionKnowledgebase and knowledge of the given knownObjects.
	 * @param actionKnowledgebase the agent's knowledge about actions
	 * @param knownObjects the Strings that the agent knows refer to objects.
	 * @param verbose
	 */
	public AffinitybasedAgent(Map<String, ActionKnowledge> actionKnowledgebase, Set<String> knownObjects, boolean verbose) {
		this.actionKnowledgebase = actionKnowledgebase;
		this.knownObjects = knownObjects;
		
		this.rememberedAgents = new RecentlyObservedAgentsMemory();
		this.rememberedWorldModel = null;
		
		this.verbose = verbose;
	}

	////////////////////////////////////////////////////////////
	//////// INTERPRETTING A SOCIAL SCENARIO ///////////////////
	////////////////////////////////////////////////////////////

	/***
	 * Reads the given scenario, given the scenario may or may not be a followupScenario.
	 * @param scenario
	 * @param followupScenario
	 * @throws InsufficientKnowledgeException
	 */
	public void read(Scenario scenario, boolean followupScenario) throws InsufficientKnowledgeException {
		this.getWorldModelOf(scenario, followupScenario);
	}
	
	/***
	 * Build a world model of the given scenario, given the scenario may or may not be a followupScenario.
	 * If the scenario is a followupScenario, then memory flows continuously from the last interpreted scenario 
	 * into the interpretation of this scenario.
	 * @param scenario the scenario to build a world model of
	 * @param followupScenario true iff the agent should consider its memory from the previous scenario while building
	 * @return the world model of the given scenario 
	 * @throws InsufficientKnowledgeException there was insufficient knowledge to get a world model of the scenario
	 */
	private AffinitybasedWorldModel getWorldModelOf(Scenario scenario, boolean followupScenario) throws InsufficientKnowledgeException {
		AffinitybasedWorldModel worldModel = new AffinitybasedWorldModel();
		if (!followupScenario) {
			rememberedAgents.reset();
		}
		if (verbose) {
			System.out.format("%-24s %-24s %s %n", "", "(Friend|Neutral|Enemy)", "(Friend|Neutral|Enemy)");

			System.out.format("%-24s %-24s %s %n", "Event", "Action R.O.D.", "Beliefs about relationships");

			System.out.println("----------------------------------------------------------------");
		}
		for (ActionEvent actionEvent : scenario.actionEvents ) {
			//check for knowledge
			if (	!actionKnowledgebase.containsKey(actionEvent.action)	) { 
				throw new InsufficientKnowledgeException (actionEvent.action);
			}
			ActionKnowledge actionKnowledge = actionKnowledgebase.get(actionEvent.action);
			try {
				ActionEvent fullActionEvent = this.getFullActionEvent(actionEvent); //get action event with 2 agents, by assuming implicit response
				
				worldModel.update(fullActionEvent, actionKnowledge);
				this.updateMemory(fullActionEvent); //update memory based on this unit
			} catch (UnableToFillActionEventException e) {
				this.updateMemory(actionEvent); //still update memory
			}
			
			if (verbose) {
				String worldModelString = worldModel.toShortString().replaceAll("(?m)^", "\t\t\t\t\t");
				if (worldModelString != "") {worldModelString = worldModelString.substring(5);}
				System.out.format("%-24s %-24s %s %n", actionEvent, actionKnowledge.toShortString(), worldModelString);				
			}
			
		}//done with events
		worldModel.reflectOnAndRefineBeliefs();
		if (verbose) {
			String worldModelString = worldModel.toShortString().replaceAll("(?m)^", "\t\t\t\t\t");
			if (worldModelString != "") {worldModelString = worldModelString.substring(5);}
			System.out.format("%-24s %-24s %s %n", "Reflecting", "", worldModelString);				
		}
		if(verbose) {System.out.println();}
		this.rememberedWorldModel = worldModel;
		return worldModel;
	}
	////////////////////////////////////////////////////////////
	//////// QUERYING RECENT WORLD MODEL ///////////////////////
	////////////////////////////////////////////////////////////
	/***
	 * State the belief regarding the given relationship given the most recently observed WorldModel.
	 * @param relationship
	 */
	public void stateBelief(Pair<String> relationship) {
		Map<RelationshipType,Double> beliefs = this.rememberedWorldModel.getBeliefs(relationship);
		
		double highestProbability = 0;
		Set<RelationshipType> likelyRelationshipTypes = null;
		for (Map.Entry<RelationshipType, Double> belief : beliefs.entrySet()) {
			double currProbability = belief.getValue();
			if (currProbability > highestProbability) {
				highestProbability = currProbability;
				likelyRelationshipTypes = new HashSet<RelationshipType>();
				likelyRelationshipTypes.add(belief.getKey());
			} else if (currProbability == highestProbability) {
				likelyRelationshipTypes.add(belief.getKey());
			}
		}
		
		
		String str = "I believe that the relationship between " + relationship + " is a ";
		for (RelationshipType likelyRelationshipType : likelyRelationshipTypes) {
			str += likelyRelationshipType.toString().toLowerCase() + " or ";
		}
		str = str.substring(0,str.length()-4) + " relationship with ";
		NumberFormat format = NumberFormat.getPercentInstance();
		format.setMinimumIntegerDigits(2);
		str += format.format(highestProbability) + " confidence.";
		
		System.out.println(str);
	}
//	
//	/***
//	 * Helper: gets a string representation of the relationship 
//	 * @param p
//	 * @return
//	 */
//	private String relationshipString(Pair relationship) {
//		relationship.getElements().
//		return 
//	}
	

	////////////////////////////////////////////////////////////
	//////// MORE REASONING ABOUT SOCIAL SCENARIOS /////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Given a premise Scenario, consider the possibleChoices and return which is thought more likely to apply now. 
	 * @param premise
	 * @param possibleChoices
	 * @return the number of the choice (1 or 2) thought more likely to apply now
	 * @throws InsufficientKnowledgeException
	 * @throws UndecidedAgentException
	 */
	private int choiceOfPlausibleAlternatives(Scenario premise, List<Scenario> possibleChoices) 
					throws InsufficientKnowledgeException, UndecidedAgentException {
		AffinitybasedWorldModel worldModel = getWorldModelOf(premise, false);
		RecentlyObservedAgentsMemory preChoosingMemory = rememberedAgents.clone();
		
		//Consider choices
		int longestDescriptionLength = getMaxDescriptionLength(possibleChoices);
		
		double probabilityOfBestChoice = 0;
		int bestChoiceNumber = -1;
		
		int choiceNumber = 0;
		//keep track of highest probability description
		for (Scenario choice : possibleChoices) {

			choiceNumber ++;
			
//			if (this.verbose) {System.out.println("...agent is reading a possible interpretation...");}
			double probabilityOfThisChoice = 1;
			double sumOfEventProbs = 0; // the sum of the probabilities of all events 
			int numTimesUpdated = 0;
			
			rememberedAgents.setRecentlyObservedAgents(preChoosingMemory.getSecondToLastObservedAgent(), preChoosingMemory.getLastObservedAgent());
			if (verbose) {
				System.out.format("%-24s %-24s %s %n", "Possible event", "Action R.O.D.","p");
				System.out.println("----------------------------------------------------------------");
			}
			for (ActionEvent actionEvent : choice.actionEvents ) {
				//check for knowledge
				if (	!actionKnowledgebase.containsKey(actionEvent.action)	) { 
					throw new InsufficientKnowledgeException (actionEvent.action);
				}
				ActionKnowledge actionKnowledge = actionKnowledgebase.get(actionEvent.action);
				
				try {
					ActionEvent fullActionEvent = this.getFullActionEvent(actionEvent); //get valid unit	

					double probabilityOfThisEvent = worldModel.probabilityOf(fullActionEvent,actionKnowledge );
					sumOfEventProbs += probabilityOfThisEvent;
					probabilityOfThisChoice *= probabilityOfThisEvent;
					
					if (verbose) {
						NumberFormat format = NumberFormat.getPercentInstance();
						format.setMinimumIntegerDigits(2);
						System.out.format("%-24s %-24s %s %n", actionEvent, actionKnowledge.toShortString(),format.format(probabilityOfThisEvent), "") ;
					}				

					numTimesUpdated ++;
					this.updateMemory(fullActionEvent); //update memory based on this unit
				} catch (UnableToFillActionEventException e) {
					if (verbose) {
						NumberFormat format = NumberFormat.getPercentInstance();
						format.setMinimumIntegerDigits(2);
						System.out.format("%-24s %-24s %s %n", actionEvent, actionKnowledge.toShortString(), "N/A") ;
					}		
					this.updateMemory(actionEvent); //update memory based on original unit
				}
			}//done with units
			
			double eventProbForNormalizing = sumOfEventProbs/(double)numTimesUpdated;
			for (int i = numTimesUpdated; i < longestDescriptionLength; i++) {
				if (verbose) {
					NumberFormat format = NumberFormat.getPercentInstance();
					format.setMinimumIntegerDigits(2);
					System.out.format("%-24s %-24s %s %n", "Normalizing", "", format.format(eventProbForNormalizing), "") ;
				}		
				probabilityOfThisChoice *= eventProbForNormalizing;
			}//done normalizing
			
			if (numTimesUpdated == 0) {
				probabilityOfThisChoice = -1; //do not consider complete disjoint
//				if(verbose) {System.out.println("Choice not considered; Assigned probability of " + probabilityOfThisChoice);}
			}
			
			if (verbose) {
				NumberFormat format = NumberFormat.getPercentInstance();
				format.setMinimumIntegerDigits(2);
				System.out.format("%-24s %-24s %s %n", "", "", "P=" + format.format(probabilityOfThisChoice), "") ;
				System.out.println();
//				System.out.println("Probability of this choice: " + format.format(probabilityOfThisChoice));
			}
			
			//update best choice
			if (probabilityOfThisChoice == probabilityOfBestChoice) {
				System.out.println("The agent says: \"I can't make a decision.\"");
				throw new UndecidedAgentException();
			} else if (probabilityOfThisChoice > probabilityOfBestChoice) {
				probabilityOfBestChoice = probabilityOfThisChoice;
				bestChoiceNumber = choiceNumber;
			}
		}//done with choice

		if (verbose) {
			System.out.println("I choose interpretation " + bestChoiceNumber + ".");
		}
		return bestChoiceNumber;

	}

	@Override
	public int doTricopaTask(TricopaTask tricopaTask) throws InsufficientKnowledgeException, UndecidedAgentException {
//		if (this.verbose) {System.out.println("...agent is reading premise...");}
		int choice = this.choiceOfPlausibleAlternatives(tricopaTask.premise, tricopaTask.possibleChoices);
//		if (verbose) {System.out.println("Agent chose: " + choice);}
		return choice;
	}
	
	////////////////////////////////////////////////////////////
	//////// HELPERS - RE: MEMORY //////////////////////////////
	////////////////////////////////////////////////////////////

	/***
	 * Update memory with the given actionEvent.
	 * @param actionEvent
	 */
	public void updateMemory(ActionEvent actionEvent) {
		/***
		 * Update memory based on actedUpon agent.
		 */
		if (this.isAgent(actionEvent.actedUpon)	) {
			rememberedAgents.update(actionEvent.actedUpon, this.verbose);
		}
		/***
		 * Then, update memory based on actor agent, in order to give actor agent recency/salience in immediate memory.
		 */
		if (this.isAgent(actionEvent.actor)	) {
			rememberedAgents.update(actionEvent.actor, this.verbose);
		}
	}


	////////////////////////////////////////////////////////////
	//////// HELPERS - RE: ASSUMING IMPLICIT REACTION //////////
	////////////////////////////////////////////////////////////
	
	/***
 	 * When encountering non-agents (empty or objects) in ActionEvent slots, try to 
	 * fill the ActionEvents using an assumption that the non-agents are in fact emotional stand-ins
	 * for recently observed agents.
	 * @param actionEvent
	 * @return a full ActionEvent 
	 * @throws UnableToFillActionEventException if unable
	 */
	private ActionEvent getFullActionEvent(ActionEvent actionEvent) throws UnableToFillActionEventException {		
		boolean success = true;
	
		ActionEvent modifiedActionEvent =  actionEvent;
		
		if (!this.isAgent(	actionEvent.actor) ) {
			success = false;
			try {
				modifiedActionEvent = this.replaceActor(actionEvent);
				success = true;
			} catch (UnableToFillActionEventException e) {
				//not successful
			}
			
		}//replaced
		if (!this.isAgent(	actionEvent.actedUpon) ) {
			success = false;
			try {
				modifiedActionEvent = this.replaceActedUpon(actionEvent);
				success = true;
			} catch (UnableToFillActionEventException e) {
				//not successful
			}
		}//replaced
		
		if (!success) { throw new UnableToFillActionEventException(); }

		return modifiedActionEvent;			
	}
	
	/***
	 * When an observer encounters non-agents (empty or objects) in the actor slot of an ActionEvent,
	 * try to replace the actor using an assumption that the non-agents are in fact emotional stand-ins
	 * for recently observed agents.
	 * @param actionEvent
	 * @return an ActionEvent with replaced actor
	 * @throws UnableToFillActionEventException if unable
	 */
	private ActionEvent replaceActor(ActionEvent actionEvent) throws UnableToFillActionEventException {
		ActionEvent modifiedActionEvent = null;		
	
		if (isAgent(rememberedAgents.getLastObservedAgent()) && !actionEvent.actedUpon.equals(rememberedAgents.getLastObservedAgent())) { //if different
			modifiedActionEvent = new ActionEvent(rememberedAgents.getLastObservedAgent(),actionEvent.action,actionEvent.actedUpon);					
		} else if (isAgent(rememberedAgents.getSecondToLastObservedAgent()) && !actionEvent.actedUpon.equals(rememberedAgents.getSecondToLastObservedAgent())) { //if different
			modifiedActionEvent = new ActionEvent(rememberedAgents.getSecondToLastObservedAgent(),actionEvent.action,actionEvent.actedUpon);					
		}
		
		if (modifiedActionEvent == null) { throw new UnableToFillActionEventException();}
		
		return modifiedActionEvent;
	}
	
	/***
	 * When an observer encounters non-agents (empty or objects) in the actedUpon slot of an ActionEvent,
	 * try to replace the actor using an assumption that the non-agents are in fact emotional stand-ins
	 * for recently observed agents.
	 * @param actionEvent
	 * @return an ActionEvent with replaced actedUpon agent
	 * @throws UnableToFillActionEventException if unable
	 */
	private ActionEvent replaceActedUpon(ActionEvent descriptionUnit) throws UnableToFillActionEventException {
		ActionEvent modifiedActionEvent = null;
		String replacement_character;

		if (isAgent(rememberedAgents.getLastObservedAgent()) && !descriptionUnit.actor.equals(rememberedAgents.getLastObservedAgent())) { //if different
			replacement_character = rememberedAgents.getLastObservedAgent(); //use last
			modifiedActionEvent = new ActionEvent(descriptionUnit.actor,descriptionUnit.action,replacement_character);					
		} else if (isAgent(rememberedAgents.getSecondToLastObservedAgent()) && !descriptionUnit.actor.equals(rememberedAgents.getSecondToLastObservedAgent())) { //if different
			replacement_character = rememberedAgents.getSecondToLastObservedAgent(); //use second to last
			modifiedActionEvent = new ActionEvent(descriptionUnit.actor,descriptionUnit.action,replacement_character);					
		}

		if (modifiedActionEvent == null) { throw new UnableToFillActionEventException();}

		return modifiedActionEvent;
	}
	
	////////////////////////////////////////////////////////////
	//////// MISCELANEOUS HELPERS //////////////////////////////
	////////////////////////////////////////////////////////////

	/***
	 * Returns true iff the String s refers to an agent.
	 * @param s
	 * @return true iff the String s refers to an agent
	 */
	private boolean isAgent(String s) {
		return (s!=null && !knownObjects.contains(s));
	}
	
	/***
	 * Get the length of the longest Scenario in the given list of Scenarios.
	 * @param scenarios the list of Scenarios to compare the lengths of
	 * @return the length of the longest Scenario in the given list of Scenarios
	 */
	private int getMaxDescriptionLength(List<Scenario> scenarios) {
		return Math.max(scenarios.get(0).length, scenarios.get(1).length);
	}
	
}
