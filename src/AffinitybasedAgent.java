
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
 * The AffinitybasedAgent currently assumes a principle of implicit response: it assumes that in ActionEvents about 
 * non-agents, the non-agents are in fact emotional stand-ins for recently observed agents.
 * 
 * @author pkalluri
 *
 */
public class AffinitybasedAgent implements TricopaParticipant {
	
	/***
	 * The agent's knowledge about actions.
	 */
	private final Map<String, ActionROD> actionKnowledgebase;
	
	/***
	 * The Strings that the agent knows refer to non-agents.
	 */
	private final Set<String> characters;
	
	/***
	 * The agent's memory of recently observed agents.
	 */
	private RecentlyObservedAgentsMemory rememberedAgents;
	
	/***
	 * The agent's memory of the most recently built AffinitybasedWorldModel.
	 */
	private AffinitybasedWorldModel  rememberedWorldModel;
	
	private final boolean verbose;
	
	private NumberFormat percentageFormat; //convenient format to use for percentages
	
	////////////////////////////////////////////////////////////
	//////// CONSTRUCTOR ///////////////////////////////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Create AffinitybasedAgent with the given actionKnowledgebase and knowledge of the given knownNonagents.
	 * @param actionKnowledgebase the agent's knowledge about actions
	 * @param characters the Strings that the agent knows refer to characters
	 * @param verbose
	 */
	public AffinitybasedAgent(Map<String, ActionROD> actionKnowledgebase, Set<String> characters, boolean verbose) {
		this.actionKnowledgebase = actionKnowledgebase;
		this.characters = characters;
		
		this.rememberedAgents = new RecentlyObservedAgentsMemory();
		this.rememberedWorldModel = null;
		
		this.verbose = verbose;
		
		this.percentageFormat = NumberFormat.getPercentInstance();
		this.percentageFormat.setMinimumIntegerDigits(2);
	}

	////////////////////////////////////////////////////////////
	//////// READING SOCIAL SCENARIO ///////////////////////////
	////////////////////////////////////////////////////////////

	/***
	 * Read the given scenario, given the scenario may or may not be a followupScenario.
	 * If the scenario is a followupScenario, then memory flows continuously from the last interpreted scenario 
	 * into the interpretation of this scenario.
	 * @param scenario the scenario to read
	 * @param followupScenario true iff the agent should consider its memory from the previous scenario while building
	 * @throws InsufficientActionKnowledgeException there was insufficient knowledge to read the scenario
	 */
	public void read(Scenario scenario, boolean followupScenario) throws InsufficientActionKnowledgeException {
		this.getWorldModelOf(scenario, followupScenario, false, null);
	}
	
	/***
	 * Read the given scenario focusing on the given relationship, given the scenario may or may not be a followupScenario.
	 * Focusing on the given relationship means that, if this agent is verbose, it will only log events affecting at least one agent of the relationship
	 * and will only log beliefs regarding this relationship.
	 * If the scenario is a followupScenario, then memory flows continuously from the last interpreted scenario 
	 * into the interpretation of this scenario.
	 * @param scenario the scenario to read
	 * @param followupScenario true iff the agent should consider its memory from the previous scenario while building
	 * @throws InsufficientActionKnowledgeException there was insufficient knowledge to read the scenario
	 */
	public void read(Scenario scenario, boolean followupScenario, Pair<String> relationship) throws InsufficientActionKnowledgeException {
		this.getWorldModelOf(scenario, followupScenario, true, relationship);
	}
	
	/***
	 * Build a world model of the given scenario with a possible focus on the given relationship,
	 * given the scenario may or may not be a followupScenario.
	 * Focusing on the given relationship means that, if this agent is verbose, it will only log events affecting at least one agent of the relationship
	 * and will only log beliefs regarding this relationship.
	 * If the scenario is a followupScenario, then memory flows continuously from the last interpreted scenario 
	 * into the interpretation of this scenario.
	 * @param scenario the scenario to build a world model of
	 * @param followupScenario true iff the agent should consider its memory from the previous scenario while building
	 * @return the world model of the given scenario 
	 * @throws InsufficientActionKnowledgeException there was insufficient knowledge to get a world model of the scenario
	 */
	private AffinitybasedWorldModel getWorldModelOf(Scenario scenario, boolean followupScenario, boolean focus, Pair<String> relationship) throws InsufficientActionKnowledgeException {
		if (verbose) {
			this.printThreeColumnTextLine("", "(Friend|Neutral|Enemy)", "(Friend|Neutral|Enemy)");
			this.printThreeColumnTextLine("Event", "Action R.O.D.", "Beliefs about relationships");
			System.out.println("----------------------------------------------------------------");
		}
		
		//Reset memory
		AffinitybasedWorldModel worldModel = new AffinitybasedWorldModel();
		if (!followupScenario) { //new scneario, reset memory of agents
			rememberedAgents.reset();
		}
		this.rememberedWorldModel = worldModel;
		
		for (ActionEvent actionEvent : scenario.actionEvents ) {
			//check for knowledge
			if (	!actionKnowledgebase.containsKey(actionEvent.action)	) { 
				throw new InsufficientActionKnowledgeException (actionEvent.action);
			}
			ActionROD actionKnowledge = actionKnowledgebase.get(actionEvent.action);
			try {
				ActionEvent fullActionEvent = this.getFullActionEvent(actionEvent); //get action event with 2 agents, by assuming implicit response
				
				worldModel.update(fullActionEvent, actionKnowledge);
				this.updateMemory(fullActionEvent);
				
				if (verbose) {
					if (!focus) {
						String worldModelConciseString = worldModel.toConciseString();
						String newline = System.getProperty("line.separator");
						if (worldModelConciseString.contains(newline) ) { // long world model string
							worldModelConciseString = this.tabOverMultiLineString(worldModelConciseString);
						}						
						this.printThreeColumnTextLine(actionEvent.toString(), actionKnowledge.toConciseString(), worldModelConciseString);				
					} else { 
						if (fullActionEvent.containsEither(relationship)) { //focus on only logging information relevant to the given relationship
							String worldModelString = worldModel.toConciseString(relationship); //(will never be long string)
							this.printThreeColumnTextLine(actionEvent.toString(), actionKnowledge.toConciseString(), worldModelString);				
						}
					}
				}
			} catch (UnableToFillActionEventException e) {
				this.updateMemory(actionEvent); //still update memory
				
				if (verbose) {
					if (!focus) {
						String worldModelConciseString = worldModel.toConciseString();
						String newline = System.getProperty("line.separator");
						if (worldModelConciseString.contains(newline) ) { // long world model string
							worldModelConciseString = this.tabOverMultiLineString(worldModelConciseString);
						}						
						this.printThreeColumnTextLine(actionEvent.toString(), actionKnowledge.toConciseString(), worldModelConciseString);				
					} else { 
						//don't print, not relevant to focus
					}
				}
			}
		}//done with events
		worldModel.reflectOnAndRefineBeliefs();
		
		if (verbose) {
			if (!focus) {
				String worldModelConciseString = worldModel.toConciseString();
				String newline = System.getProperty("line.separator");
				if (worldModelConciseString.contains(newline) ) { // long world model string
					worldModelConciseString = this.tabOverMultiLineString(worldModelConciseString);
				}						
				this.printThreeColumnTextLine("Reflecting", "", worldModelConciseString);				
			} else { 
				String worldModelString = worldModel.toConciseString(relationship); //(will never be long string)
				this.printThreeColumnTextLine("Reflecting", "", worldModelString);				
			}
			
			System.out.println(); //end of reading this scenario
		}
		return worldModel;
	}
	
	/***
	 * Return true iff the given actionEvent is about the given relationship.
	 * @param actionEvent
	 * @param relationship
	 * @return true iff the given actionEvent is about the given relationship
	 */
	public boolean actionEventIsAbout(ActionEvent actionEvent, Pair<String> relationship) {
		Set<String> eventAgents = new HashSet<String>();
		eventAgents.add(actionEvent.actor);
		eventAgents.add(actionEvent.actedUpon);		
		return (eventAgents.equals(relationship.getElements()));
	}
	
	////////////////////////////////////////////////////////////
	//////// QUERYING RECENT WORLD MODEL ///////////////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Get map mapping RelationshipTypes to believed probability of the RelationshipTypes, for the given relationship
	 * and the recently read scenario.
	 * @param relationship
	 * @return map mapping RelationshipTypes to believed probability of the RelationshipTypes, for the given relationship
	 * and recently read scenario
	 */
	public Map<RelationshipType,Double> getBeliefs(Pair<String> relationship) {
		return this.rememberedWorldModel.getBeliefs(relationship);
	}
	
	/***
	 * State the belief regarding the RelationshipType and confidence for the given relationship
	 * given the most recently read scenario.
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
		str += this.percentageFormat.format(highestProbability) + " confidence.";
		
		System.out.println(str);
	}	

	////////////////////////////////////////////////////////////
	//////// CHOOSING BETWEEN INTERPRETATIONS  /////////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Given a premise Scenario, consider the possibleChoices and return which is thought more likely to apply now. 
	 * @param premise
	 * @param possibleChoices
	 * @return the number of the choice (1 or 2) thought more likely to apply now
	 * @throws InsufficientActionKnowledgeException there was insufficient knowledge to get a world model of the scenario
	 * @throws UndecidedAgentException the agent was unable to decide between the choices
	 */
	private int choiceOfPlausibleAlternatives(Scenario premise, List<Scenario> possibleChoices) 
					throws InsufficientActionKnowledgeException, UndecidedAgentException {
		AffinitybasedWorldModel worldModel = getWorldModelOf(premise, false, false, null);
		RecentlyObservedAgentsMemory preChoosingMemory = new RecentlyObservedAgentsMemory(this.rememberedAgents); //clone
		
		//Consider choices
		int longestDescriptionLength = getMaxDescriptionLength(possibleChoices);
		
		double probabilityOfBestChoice = 0;
		int bestChoiceNumber = -1;
		
		int choiceNumber = 0;
		//keep track of highest probability description
		for (Scenario choice : possibleChoices) {
			choiceNumber ++;
			
			double probabilityOfThisChoice = 1;
			double sumOfEventProbs = 0; // the sum of the probabilities of all events (used for normalizing)
			int numProbabilityUpdates = 0;
			
			this.rememberedAgents = preChoosingMemory;
			if (verbose) {
				this.printThreeColumnTextLine("Possible event", "Action R.O.D.","p");
				System.out.println("----------------------------------------------------------------");
			}
			for (ActionEvent actionEvent : choice.actionEvents ) {
				//check for knowledge
				if (	!actionKnowledgebase.containsKey(actionEvent.action)	) { 
					throw new InsufficientActionKnowledgeException (actionEvent.action);
				}
				ActionROD actionKnowledge = actionKnowledgebase.get(actionEvent.action);
				
				try {
					ActionEvent fullActionEvent = this.getFullActionEvent(actionEvent); //get valid unit	

					double probabilityOfThisEvent = worldModel.probabilityOf(fullActionEvent,actionKnowledge );
					sumOfEventProbs += probabilityOfThisEvent;
					probabilityOfThisChoice *= probabilityOfThisEvent;	
					numProbabilityUpdates ++;
					this.updateMemory(fullActionEvent); //update memory based on this unit
					
					if (verbose) {
						this.printThreeColumnTextLine(actionEvent.toString(), actionKnowledge.toConciseString(),this.percentageFormat.format(probabilityOfThisEvent)) ;
					}			
				} catch (UnableToFillActionEventException e) {
					this.updateMemory(actionEvent); //update memory based on original unit
					if (verbose) {
						this.printThreeColumnTextLine(actionEvent.toString(), actionKnowledge.toConciseString(), "N/A") ;
					}		
				}
			}//done with units
			
			if (numProbabilityUpdates != 0) {
				double eventProbForNormalizing = sumOfEventProbs/(double)numProbabilityUpdates; //average
				for (int i = numProbabilityUpdates; i < longestDescriptionLength; i++) {
					if (verbose) {
						this.printThreeColumnTextLine("Normalizing", "", this.percentageFormat.format(eventProbForNormalizing)) ;
					}		
					probabilityOfThisChoice *= eventProbForNormalizing;
				}//done normalizing
			}
			
			if (numProbabilityUpdates == 0) {
				probabilityOfThisChoice = 0; //do not reward complete disjoint
			}
			
			if (verbose) {
				this.printThreeColumnTextLine("", "", "P=" + this.percentageFormat.format(probabilityOfThisChoice)) ;
				System.out.println(); //end of this choice
			}
			
			//update best choice
			if (choiceNumber!=1 && probabilityOfThisChoice == probabilityOfBestChoice) { //tie
				if(verbose) {System.out.println("I am undecided.");}
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
	
	/***
	 * Get the length of the longest Scenario in the given list of Scenarios.
	 * @param scenarios the list of Scenarios to compare the lengths of
	 * @return the length of the longest Scenario in the given list of Scenarios
	 */
	private int getMaxDescriptionLength(List<Scenario> scenarios) {
		return Math.max(scenarios.get(0).length, scenarios.get(1).length);
	}

	@Override
	public int doTricopaTask(TricopaTask tricopaTask) throws InsufficientActionKnowledgeException, UndecidedAgentException {
		int choice = this.choiceOfPlausibleAlternatives(tricopaTask.premise, tricopaTask.possibleChoices);
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
	 * fill the ActionEvents using a principle of implicit response.
	 * This assumes that the non-agents are in fact emotional stand-ins for recently observed agents.
	 * @param actionEvent
	 * @return a full ActionEvent 
	 * @throws UnableToFillActionEventException if unable
	 */
	private ActionEvent getFullActionEvent(ActionEvent actionEvent) throws UnableToFillActionEventException {		
		boolean full = true;
	
		ActionEvent modifiedActionEvent =  actionEvent;
		if (!this.isAgent(	actionEvent.actor) ) {
			try {
				modifiedActionEvent = this.replaceActor(actionEvent);
				full = true;
			} catch (UnableToFillActionEventException e) {
				full = false;
			}
		}//replaced
		if (!this.isAgent(	actionEvent.actedUpon) ) {
			try {
				modifiedActionEvent = this.replaceActedUpon(actionEvent);
				full = true;
			} catch (UnableToFillActionEventException e) {
				full = false;
			}
		}//replaced
		
		if (!full) { throw new UnableToFillActionEventException(); }
		return modifiedActionEvent;			
	}
	
	/***
 	 * When encountering non-agents (empty or objects) in ActionEvent actor slots, try to 
	 * fill the ActionEvents using a principle of implicit response.
	 * This assumes that the non-agents are in fact emotional stand-ins for recently observed agents.
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
		
		if (modifiedActionEvent == null) { 
			throw new UnableToFillActionEventException();
		} else { 
			return modifiedActionEvent;
		}
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

		if (isAgent(rememberedAgents.getLastObservedAgent()) && !descriptionUnit.actor.equals(rememberedAgents.getLastObservedAgent())) { //if different
			modifiedActionEvent = new ActionEvent(descriptionUnit.actor,descriptionUnit.action,this.rememberedAgents.getLastObservedAgent());					
		} else if (isAgent(rememberedAgents.getSecondToLastObservedAgent()) && !descriptionUnit.actor.equals(rememberedAgents.getSecondToLastObservedAgent())) { //if different
			modifiedActionEvent = new ActionEvent(descriptionUnit.actor,descriptionUnit.action,this.rememberedAgents.getSecondToLastObservedAgent());					
		}

		if (modifiedActionEvent == null) {
			throw new UnableToFillActionEventException();
		} else {
			return modifiedActionEvent;
		}
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
		return (s!=null && this.characters.contains(s));
	}
	
	////////////////////////////////////////////////////////////
	//////// HELPERS - RE: VERBOSITY ///////////////////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Helper: format text into clean lines of text with 3 columns.
	 * @param s1 the text to be put in column 1
	 * @param s2 the text to be put in column 2
	 * @param s3 the text to be put in column 3
	 */
	private void printThreeColumnTextLine(String s1, String s2, String s3) {
		System.out.format("%-24s %-24s %s %n", s1, s2, s3);
	}
	
	/***
	 * Helper: get a version of the given multi-line string with all lines tabbed over.
	 * @param str a multi-line string
	 * @return
	 */
	private String tabOverMultiLineString(String str) {
		String s = str.replaceAll("(?m)^", "\t\t\t\t\t\t");
		s = s.substring(6); //remove beginning of 
		return s;
	}
	
}
