import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		this.age = 0;
	}
	
	//////////////////////////////////////////
	/////// OVERRIDE INTERFACE METHODS ///////
	//////////////////////////////////////////

	@Override
	public void update(ActionEvent actionEvent, ActionKnowledge actionKnowledge) {		
		/***
		 * Add all implied pairs to the world model
		 */
		if (  !agents.contains(actionEvent.actor)  ) {
			for (String character : agents) {
				affinityBeliefs.put(new Pair<String>(character,actionEvent.actor)  , new SymmetricRelationshipModel(RelationshipType.values()));
			}//done adding pairs
			agents.add(actionEvent.actor);
		}
		if (  (!agents.contains(actionEvent.actedUpon))  ) {
			for (String character : agents) {
				affinityBeliefs.put(new Pair<String>(character,actionEvent.actedUpon)  , new SymmetricRelationshipModel(RelationshipType.values()));
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
			timeToBeliefs.put(age, relationship.getCopyOfBeliefs());
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
		this.assumeUninformedRelationshipAreNeutralRelationships();
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
//				if (verbose) {System.out.println(" Reflecting --> " + this);}
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
					timeToBeliefs.put(age, neutralRelationshipModel.getCopyOfBeliefs());
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
	public double probabilityOf(ActionEvent descriptionUnit, ActionKnowledge actionKnowledge) {
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
	
	//////////////////////////////////////////
	/////// ADDED METHODS ////////////////////
	//////////////////////////////////////////
	
	/***
	 * Get the history of the world model as a map from each relationship to the history of that relationship.
	 * @return the history of the world model as a map from each relationship to the history of that relationship
	 */
	public Map<Pair<String>, Map<Integer, Map<RelationshipType, Double>>> getHistory() {
		return affinityBeliefHistory;
	}
}
