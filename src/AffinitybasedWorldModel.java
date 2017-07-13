import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * The AffinitybasedWorldModel implements the WorldModel interface.
 * 
 * The AffinitybasedWorldModel currently assumes a principle of default bias: it assumes that upon reflection, 
 * relationships that continue to be uninformative should be replaced with the default assumption that uninformative relationships
 * are in fact more likely to be Neutral than Friend and more likely to be Friend than Enemy.
 * @author pkalluri
 *
 */
public class AffinitybasedWorldModel implements WorldModel{
	/***
	 * The agents known to exist
	 */
	private List<String> agents;
	
	/***
	 * The relationships between the agents known to exist -- mapped to their current relationship models.
	 */
	private Map<Pair<String>, SymmetricRelationshipModel> affinityBeliefs;
	
	/***
	 * The relationships between the agents known to exist  -- mapped to their entire relationship model history.
	 */
	private Map<Pair<String>, Map<Integer,Map<RelationshipType,Double>>> affinityBeliefHistory;
	
	/***
	 * The number of updates that have been made to this world model.
	 */
	private int age;
	
	//////////////////////////////////////////
	/////// PARAMETERS ///////////////////////
	//////////////////////////////////////////
	/***
	 * Always store the affinity belief history of the world.
	 */
	private boolean STORE_HISTORY = true;
	
	//////////////////////////////////////////
	/////// CONSTRUCTOR //////////////////////
	//////////////////////////////////////////
	
	/***
	 * Construct a new AffinitybasedWorldModel containing no information.
	 */
	public AffinitybasedWorldModel() {
		this.agents = new ArrayList<String> ();
		this.affinityBeliefs = new HashMap<Pair<String>, SymmetricRelationshipModel> ();
		this.affinityBeliefHistory = new HashMap<Pair<String>, Map<Integer,Map<RelationshipType,Double>>>();
		this.age = 1;
	}
	
	//////////////////////////////////////////
	/////// OVERRIDE INTERFACE METHODS ///////
	//////////////////////////////////////////
	@Override
	public int getAge() {
		return this.age;
	}
	
	@Override
	public void update(ActionEvent actionEvent, ActionROD actionKnowledge) {		
		/***
		 * Add all implied pairs to the world model
		 */
		if (  !agents.contains(actionEvent.actor)  ) {
			for (String agent : agents) {
				affinityBeliefs.put(new Pair<String>(agent,actionEvent.actor)  , new SymmetricRelationshipModel());
			}//done adding pairs
			agents.add(actionEvent.actor);
		}
		if (  (!agents.contains(actionEvent.actedUpon))  ) {
			for (String character : agents) {
				affinityBeliefs.put(new Pair<String>(character,actionEvent.actedUpon)  , new SymmetricRelationshipModel());
			}//done adding pairs
			agents.add(actionEvent.actedUpon);
		}
		

		/***
		 * Update the acting pair's relationship model
		 */
		Pair<String> actingPair = new Pair<String>(actionEvent.actor, actionEvent.actedUpon);
		SymmetricRelationshipModel relationship = affinityBeliefs.get(actingPair);
		relationship.update(actionKnowledge, this.age + 1);
		
		/***
		 * Save if save parameter is ON.
		 */
		if (STORE_HISTORY) { 
			Map<Integer,Map<RelationshipType,Double>> timeToBeliefs;
			if (!affinityBeliefHistory.containsKey(actingPair)) {
				timeToBeliefs = new HashMap<Integer,Map<RelationshipType,Double>>();
				affinityBeliefHistory.put(actingPair, timeToBeliefs);
			} else {
				timeToBeliefs = affinityBeliefHistory.get(actingPair);
			}
			timeToBeliefs.put(age, relationship.getBeliefs());
		}
		this.age ++;
	}
	
	@Override
	public void reflectOnAndRefineBeliefs() {
		/***
		 * When the affinity based world model is reflected on, there emerges a partial belief that all 
		 * uninformative relationships are in fact neutral relationships.
		 * This serves as a heuristic for reasoning, because uninformative relationships obstruct reasoning
		 * more than generally, heuristically true / sometimes untrue assumptions of neutrality.
		 */
//		this.assumeUninformedRelationshipAreNeutralRelationships();
		this.assumeUninformedRelationshipAre( Arrays.asList(RelationshipType.ENEMY, RelationshipType.FRIEND, RelationshipType.NEUTRAL) ); 
	}
	
	/***
	 * Impose a partial belief that all uninformative relationships should in fact default to be
	 * informed by a belief that the given orderedRelationshipTypes are in the increasing order of likelihood.
	 * This serves as a heuristic for reasoning, because uninformative relationships obstruct reasoning
	 * more than generally, heuristically true / sometimes untrue assumptions.
	 */
	private void assumeUninformedRelationshipAre(List<RelationshipType> orderedRelationshipTypes) {
		for (Map.Entry<Pair<String>, SymmetricRelationshipModel> entry : this.affinityBeliefs.entrySet()) {
			if (!entry.getValue().isInformative()) { //if all beliefs are uninformative, rewrite
				SymmetricRelationshipModel defaultRelationshipModel = new SymmetricRelationshipModel(orderedRelationshipTypes);
				entry.setValue(defaultRelationshipModel);
				/***
				 * Save if save parameter is ON.
				 */
				if (STORE_HISTORY) { 
					Map<Integer,Map<RelationshipType,Double>> timeToBeliefs;
					Pair<String> actingPair = entry.getKey();
					if (!affinityBeliefHistory.containsKey(actingPair)) {
						timeToBeliefs = new HashMap<Integer,Map<RelationshipType,Double>>();
						affinityBeliefHistory.put(actingPair, timeToBeliefs);
					} else {
						timeToBeliefs = affinityBeliefHistory.get(actingPair);
					}
					timeToBeliefs.put(age, defaultRelationshipModel.getBeliefs());
				}
				
			}
		}
	}
	
	/***
	 * Impose a partial belief that all uninformative relationships are in fact neutral relationships.
	 * This serves as a heuristic for reasoning, because uninformative relationships obstruct reasoning
	 * more than generally, heuristically true / sometimes untrue assumptions of neutrality.
	 */
	private void assumeUninformedRelationshipAreNeutralRelationships() {
		double BIG_PROBABILITY_TO_SMALL_PROBABILITY_RATIO = 2;
		for (Map.Entry<Pair<String>, SymmetricRelationshipModel> entry : this.affinityBeliefs.entrySet()) {
			if (!entry.getValue().isInformative()) { //if all beliefs are neutral, rewrite as neutral relationship
				SymmetricRelationshipModel neutralRelationshipModel = new SymmetricRelationshipModel(RelationshipType.NEUTRAL, BIG_PROBABILITY_TO_SMALL_PROBABILITY_RATIO);
				entry.setValue(neutralRelationshipModel);
				/***
				 * Save if save parameter is ON.
				 */
				if (STORE_HISTORY) { 
					Map<Integer,Map<RelationshipType,Double>> timeToBeliefs;
					Pair<String> actingPair = entry.getKey();
					if (!affinityBeliefHistory.containsKey(actingPair)) {
						timeToBeliefs = new HashMap<Integer,Map<RelationshipType,Double>>();
						affinityBeliefHistory.put(actingPair, timeToBeliefs);
					} else {
						timeToBeliefs = affinityBeliefHistory.get(actingPair);
					}
					timeToBeliefs.put(age, neutralRelationshipModel.getBeliefs());
				}
				
			}
		}
	}
	
	@Override
	public double distanceScore(WorldModel otherRelationshipInfo) {
		AffinitybasedWorldModel otherSocialNetworkModel = (AffinitybasedWorldModel) otherRelationshipInfo;
		double distance = 0;
		//Pairs that exist in both should match well
		for (Pair<String> pair : this.affinityBeliefs.keySet()) {
			if (otherSocialNetworkModel.affinityBeliefs.containsKey(pair)) {
				distance += this.affinityBeliefs.get(pair).distanceScore(otherSocialNetworkModel.affinityBeliefs.get(pair));
			}
			distance -= .01; //Reward social models for modeling the same pairs
		}
		return distance;
	}

	@Override
	public double probabilityOf(ActionEvent descriptionUnit, ActionROD actionKnowledge) {
		double DEFAULT_PROBABILITY = 1; //if pair doesn't match
		
		SymmetricRelationshipModelInterface relationship = this.affinityBeliefs.get(new Pair<String>(descriptionUnit.actor, descriptionUnit.actedUpon));
		if (relationship != null) {
			return relationship.probabilityOf(actionKnowledge) ;
		} else {
			return DEFAULT_PROBABILITY;
		}
	}

	@Override
	public String toString() {
		return this.affinityBeliefs.toString();
	}
	
	/***
	 * Get a concise String representation of current beliefs about relationships in the world.
	 * @return a concise String representation
	 */
	public String toConciseString() {
		String toPrint = "";
		int MAX_NUMBER_OF_ENTRIES_PER_LINE = 5;
		
		int numberOfEntries = 0;
		for (Map.Entry<Pair<String>, SymmetricRelationshipModel> entry : this.affinityBeliefs.entrySet()) {
			toPrint += entry.getKey() + ":" + entry.getValue().toConciseString() + ", ";
			numberOfEntries ++;
			if (numberOfEntries % MAX_NUMBER_OF_ENTRIES_PER_LINE == 0) {
				toPrint += "\n";
			}
		}		
		if (toPrint == "") {return toPrint;}
		else { return toPrint.substring(0,toPrint.length()-2);}
	}
	
	/***
	 * Get a concise String representation of current beliefs about the given relationships only.
	 * @param relationship
	 * @return a concise String representation of current beliefs about the given relationships only
	 */
	public String toConciseString(Pair<String> relationship) {
		String str = "";
		SymmetricRelationshipModel relationshipModel = this.affinityBeliefs.get(relationship);
		if (relationshipModel != null) {
			str = relationship + ":" + relationshipModel.toConciseString();
		} else {
			str = relationship + ":" + new SymmetricRelationshipModel().toConciseString();
		}
		return str;
	}
	
	//////////////////////////////////////////
	/////// ADDED METHODS ////////////////////
	//////////////////////////////////////////
	
	//Perhaps useful in the future
//	/***
//	 * Get the history of the world model as a map from each relationship to the history of that relationship.
//	 * @return the history of the world model as a map from each relationship to the history of that relationship
//	 */
//	public Map<Pair<String>, Map<Integer, Map<RelationshipType, Double>>> getHistory() {
//		return affinityBeliefHistory;
//	}
	
	/***
	 * Get the beliefs about the given relationship as a map mapping possible RelationshipTypes to believed probability of the RelationshipTypes.
	 * @param relationship 
	 * @return the beliefs about the given relationship as a map mapping possible RelationshipTypes to believed probability of the RelationshipTypes
	 */
	public Map<RelationshipType, Double> getBeliefs(Pair<String> relationship) {
		if (!this.affinityBeliefs.containsKey(relationship)) {
			return new SymmetricRelationshipModel().getBeliefs();
		}
		return this.affinityBeliefs.get(relationship).getBeliefs();
	}
}
