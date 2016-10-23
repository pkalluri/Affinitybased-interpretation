import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocialNetworkModel implements WorldModelInterface {
	private List<String> characters;
	private Map<  Pair<String>, RelationshipInterface  > relationships;	
	private int number_updates;
	
	public SocialNetworkModel() {
		this.characters = new ArrayList<String> ();
		this.relationships = new HashMap<  Pair<String>, RelationshipInterface  > ();
		this.number_updates = 0;
	}
	
	@Override
	public void update(DescriptionUnit descriptionUnit, ActionKnowledge actionKnowledge, boolean saveRecords, Map<Pair<String>, Map<Integer,Map<RelationshipType,Double>>> recordHolder) {

		/***
		 * Add newly emerged pairs
		 */
		Pair<String> actingPair = new Pair<String>(descriptionUnit.actor, descriptionUnit.actedUpon);
		/***
		 * Only add this pair
		 */
//		if (!this.relationships.containsKey(actingPair)) {
//			relationships.put(actingPair, new Relationship_TypeProbModel(RelationshipType.values()));
//		}
		/***
		 * Add all implied pairs
		 */
		if (  !characters.contains(descriptionUnit.actor)  ) {
			for (String character : characters) {
				relationships.put(new Pair<String>(character,descriptionUnit.actor)  , new Relationship_TypeProbModel(RelationshipType.values()));
			}//done adding pairs
//			relationships.put(new Pair<String>(descriptionUnit.actor,descriptionUnit.actor)  , new Relationship_TypeProbModel());
			characters.add(descriptionUnit.actor);
		}
		if (  (!characters.contains(descriptionUnit.actedUpon))  ) {
			for (String character : characters) {
				relationships.put(new Pair<String>(character,descriptionUnit.actedUpon)  , new Relationship_TypeProbModel(RelationshipType.values()));
			}//done adding pairs
//			relationships.put(new Pair<String>(descriptionUnit.actedUpon,descriptionUnit.actedUpon)  , new Relationship_TypeProbModel());
			characters.add(descriptionUnit.actedUpon);
		}
		
		/***
		 * Update the acting pair's relationship model
		 */
		RelationshipInterface relationship = relationships.get(actingPair);
		relationship.update(actionKnowledge, this.number_updates + 1);
		
		/***
		 * Save if save parameter is ON.
		 */
		if (saveRecords) { 
//			relationship.saveCurrentState(recordHolder);
			Map<Integer,Map<RelationshipType,Double>> timeToBeliefs;
			if (!recordHolder.containsKey(actingPair)) {
				timeToBeliefs = new HashMap<Integer,Map<RelationshipType,Double>>();
				recordHolder.put(actingPair, timeToBeliefs);
			} else {
				timeToBeliefs = recordHolder.get(actingPair);
			}
			timeToBeliefs.put(number_updates, relationship.getCopyOfBeliefs());
		}
		this.number_updates ++;
	}

	private boolean hasRelationship(String actor, String actedUpon) {
		boolean found = false;
		Pair<String> newPair = new Pair<String>(actor, actedUpon);
		for (Pair<String> rel :  this.relationships.keySet()) {
			if (rel.equals(newPair)) {
				found = true;
			}
		}
		return !found;
	}

	@Override
	public double distanceFrom(WorldModelInterface otherRelationshipInfo) {
		SocialNetworkModel otherSocialNetworkModel = (SocialNetworkModel) otherRelationshipInfo;
		double distance = 0;
//		Pairs that exist in both should match well
		for (Pair<String> pair : this.relationships.keySet()) {
//			System.out.println(pair);
			if (otherSocialNetworkModel.relationships.containsKey(pair)) {
//				System.out.println("both");
				distance += this.relationships.get(pair).distanceFrom(otherSocialNetworkModel.relationships.get(pair));
			}
			distance -= .01; //Reward social models that get many correct
		}
		return distance;
	}

//	@Override
//	public int chooseBetweenPlausibleAlternatives(List<Description> possibleChoices) {
//		//get max number of events in any story
//		//keep track of highest probability description
//		for (Description choice : possibleChoices) {
//			//how probable is this description
//			//normalize
//		}
//		//return highest prob choice
//		return 0;
//	}

	@Override
	public String toString() {
		return this.relationships.toString();
	}

	@Override
	public double probabilityOf(DescriptionUnit descriptionUnit, ActionKnowledge actionKnowledge) throws Exception {
		double DEFAULT_PROBABILITY = 1; //if pair doesn't match
		
		RelationshipInterface relationship = this.relationships.get(new Pair<String>(descriptionUnit.actor, descriptionUnit.actedUpon));
		if (relationship != null) {
			return relationship.probabilityOf(descriptionUnit, actionKnowledge) ;
		} else {
			return DEFAULT_PROBABILITY;
		}
	}

	@Override
	public void reviewBeliefs(boolean saveRecords, Map<Pair<String>, Map<Integer, Map<RelationshipType, Double>>> recordHolder) {
		this.assumeUninformedRelationshipAreNeutralRelationships(saveRecords, recordHolder);
		
	}
	
	private void assumeUninformedRelationshipAreNeutralRelationships(boolean saveRecords, Map<Pair<String>, Map<Integer,Map<RelationshipType,Double>>> recordHolder) {
		double BIG_PROBABILITY_TO_SMALL_PROBABILITY_RATIO = 5;
		for (Map.Entry<Pair<String>, RelationshipInterface> entry : this.relationships.entrySet()) {//if all beliefs are neutral
			if (!entry.getValue().hasOpinion()) { //rewrite as neutral relationship
				RelationshipInterface neutralRelationshipModel = new Relationship_TypeProbModel(RelationshipType.values(), RelationshipType.Neutral, BIG_PROBABILITY_TO_SMALL_PROBABILITY_RATIO);
				entry.setValue(neutralRelationshipModel);
//				System.out.println("After review: " + this.relationships);
				/***
				 * Save if save parameter is ON.
				 */
				if (saveRecords) { 
//					relationship.saveCurrentState(recordHolder);
					Map<Integer,Map<RelationshipType,Double>> timeToBeliefs;
					Pair<String> actingPair = entry.getKey();
					if (!recordHolder.containsKey(actingPair)) {
						timeToBeliefs = new HashMap<Integer,Map<RelationshipType,Double>>();
						recordHolder.put(actingPair, timeToBeliefs);
					} else {
						timeToBeliefs = recordHolder.get(actingPair);
					}
					timeToBeliefs.put(number_updates, neutralRelationshipModel.getCopyOfBeliefs());
				}
				
			}
		}
	}
}
