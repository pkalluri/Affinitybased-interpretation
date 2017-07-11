import java.text.DecimalFormat;
import java.text.NumberFormat;
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
	private RecentlyObservedAgentsMemory memory;
	
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
		
		this.memory = new RecentlyObservedAgentsMemory();
		
		this.verbose = verbose;
	}

	////////////////////////////////////////////////////////////
	//////// INTERPRETTING A SOCIAL SCENARIO ///////////////////
	////////////////////////////////////////////////////////////

	/***
	 * Build a world model of the given scenario, given the scenario may or may not be a followupScenario.
	 * If the scenario is a followupScenario, then memory flows continuously from the last interpreted scenario 
	 * into the interpretation of this scenario.
	 * @param scenario the scenario to build a world model of
	 * @param followupScenario true iff the agent should consider its memory from the previous scenario while building
	 * @return the world model of the given scenario 
	 * @throws InsufficientKnowledgeException there was insufficient knowledge to get a world model of the scenario
	 */
	public AffinitybasedWorldModel getWorldModelOf(Scenario scenario, boolean followupScenario) throws InsufficientKnowledgeException {
		AffinitybasedWorldModel worldModel = new AffinitybasedWorldModel();
		if (!followupScenario) {
			memory.reset();
		}
		for (ActionEvent actionEvent : scenario.actionEvents ) {
			try {
				ActionEvent fullActionEvent = this.getFullActionEvent(actionEvent); //get action event with 2 agents, by assuming implicit response
				if (verbose) {System.out.print("\"" + fullActionEvent + "\" --> ");}

				//check for knowledge
				if (	!actionKnowledgebase.containsKey(fullActionEvent.action)	) { 
					throw new InsufficientKnowledgeException (fullActionEvent.action);
				}
			
				//update based on the valid unit
				ActionKnowledge actionKnolwedge = actionKnowledgebase.get(actionEvent.action);
//				if (verbose) {System.out.print(actionKnolwedge + " ");}
				worldModel.update(fullActionEvent, actionKnolwedge);
				if (verbose) {System.out.println(worldModel);}
				
				this.updateMemory(fullActionEvent); //update memory based on this unit
			} catch (UnableToFillActionEventException e) {
				if (verbose) {System.out.println("Could not process this event.");}
				this.updateMemory(actionEvent); //still update memory
			}
		}//done with events
		worldModel.reflectOnAndRefineBeliefs();
		if (verbose) {System.out.println(" Reflecting --> " + worldModel);}
		return worldModel;
	}
	

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
		RecentlyObservedAgentsMemory preChoosingMemory = memory.clone();
		
		//Consider choices
		int longestDescriptionLength = getMaxDescriptionLength(possibleChoices);
		
		double probabilityOfBestChoice = 0;
		int bestChoiceNumber = -1;
		
		int choiceNumber = 0;
		//keep track of highest probability description
		for (Scenario choice : possibleChoices) {

			choiceNumber ++;
			
			if (verbose) {System.out.println("___Reading choice___");}
			double probabilityOfThisChoice = 1;
			double sumOfEventProbs = 0; // the sum of the probabilities of all events 
			int numTimesUpdated = 0;
			
			memory.setRecentlyObservedAgents(preChoosingMemory.getSecondToLastObservedAgent(), preChoosingMemory.getLastObservedAgent());
			for (ActionEvent actionEvent : choice.actionEvents ) {

				try {
					ActionEvent fullActionEvent = this.getFullActionEvent(actionEvent); //get valid unit

					if (verbose) {System.out.print("\"" + fullActionEvent + "\" --> ");}
	
	
					//check for knowledge
					if (	!actionKnowledgebase.containsKey(fullActionEvent.action)	) { 
						throw new InsufficientKnowledgeException (fullActionEvent.action);
					}
				
					//how likely is the event
					ActionKnowledge actionKnowledge = actionKnowledgebase.get(actionEvent.action);
//					if(verbose) {System.out.print(actionKnowledge + " ");}
					double probabilityOfThisEvent = worldModel.probabilityOf(fullActionEvent,actionKnowledge );
					sumOfEventProbs += probabilityOfThisEvent;
					probabilityOfThisChoice *= probabilityOfThisEvent;
					if(verbose) {
						NumberFormat format = NumberFormat.getPercentInstance();
						format.setMinimumFractionDigits(2);
						System.out.println("" + format.format(probabilityOfThisEvent));
					}
					numTimesUpdated ++;
					this.updateMemory(fullActionEvent); //update memory based on this unit
				} catch (UnableToFillActionEventException e) {
					this.updateMemory(actionEvent); //update memory based on original unit
				}
			}//done with units
			
			double eventProbForNormalizing = sumOfEventProbs/(double)numTimesUpdated;
			for (int i = numTimesUpdated; i < longestDescriptionLength; i++) {
				if(verbose) {
					NumberFormat format = NumberFormat.getPercentInstance();
					format.setMinimumFractionDigits(2);
					System.out.println("(Normalizing) " + format.format(eventProbForNormalizing));
				}
				probabilityOfThisChoice *= eventProbForNormalizing;
			}//done normalizing
			
			if (numTimesUpdated == 0) {
				probabilityOfThisChoice = -1; //do not consider complete disjoint
				if(verbose) {System.out.println("Choice not considered; Assigned probability of " + probabilityOfThisChoice);}
			}
			NumberFormat format = NumberFormat.getPercentInstance();
			format.setMinimumFractionDigits(2);
			if (verbose) {System.out.println(" P=" + format.format(probabilityOfThisChoice));}
			
			//update best choice
			if (probabilityOfThisChoice == probabilityOfBestChoice) {
				throw new UndecidedAgentException();
			} else if (probabilityOfThisChoice > probabilityOfBestChoice) {
				probabilityOfBestChoice = probabilityOfThisChoice;
				bestChoiceNumber = choiceNumber;
			}
		}//done with choice

		return bestChoiceNumber;

	}

	@Override
	public int doTricopaTask(TricopaTask tricopaTask) throws InsufficientKnowledgeException, UndecidedAgentException {
		if (this.verbose) {System.out.println("___Reading premise___");}
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
			memory.update(actionEvent.actedUpon, this.verbose);
		}
		/***
		 * Then, update memory based on actor agent, in order to give actor agent recency/salience in immediate memory.
		 */
		if (this.isAgent(actionEvent.actor)	) {
			memory.update(actionEvent.actor, this.verbose);
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
	
		if (isAgent(memory.getLastObservedAgent()) && !actionEvent.actedUpon.equals(memory.getLastObservedAgent())) { //if different
			modifiedActionEvent = new ActionEvent(memory.getLastObservedAgent(),actionEvent.action,actionEvent.actedUpon);					
		} else if (isAgent(memory.getSecondToLastObservedAgent()) && !actionEvent.actedUpon.equals(memory.getSecondToLastObservedAgent())) { //if different
			modifiedActionEvent = new ActionEvent(memory.getSecondToLastObservedAgent(),actionEvent.action,actionEvent.actedUpon);					
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

		if (isAgent(memory.getLastObservedAgent()) && !descriptionUnit.actor.equals(memory.getLastObservedAgent())) { //if different
			replacement_character = memory.getLastObservedAgent(); //use last
			modifiedActionEvent = new ActionEvent(descriptionUnit.actor,descriptionUnit.action,replacement_character);					
		} else if (isAgent(memory.getSecondToLastObservedAgent()) && !descriptionUnit.actor.equals(memory.getSecondToLastObservedAgent())) { //if different
			replacement_character = memory.getSecondToLastObservedAgent(); //use second to last
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
