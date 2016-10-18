import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Relationship_TypeProbModel implements RelationshipInterface {
	private Map<RelationshipType,Double> beliefs;
	
	/***
	 * Creates relationship model with no opinion.
	 */
	public Relationship_TypeProbModel(RelationshipType[] relationshipTypes) {
		beliefs = new HashMap<RelationshipType,Double>();
		double INITIAL_PROBABILITY = 1./(double)(relationshipTypes.length);
		for (RelationshipType type : relationshipTypes) {
	 		beliefs.put(type, INITIAL_PROBABILITY);
		}
//		System.out.println(beliefs);
	}
	
	/***
	 * Creates relationship model with bias towards given.
	 * @param bigProbabilitySmallProbabilityRatio 
	 */
	public Relationship_TypeProbModel(RelationshipType[] relationshipTypes, RelationshipType likelyRelationshipType, double bigProbabilitySmallProbabilityRatio) {
		Map<RelationshipType, Boolean> likelyGivenRelationshipType = new HashMap<RelationshipType,Boolean>();
		for (RelationshipType relationshipType : relationshipTypes) {
			likelyGivenRelationshipType.put(relationshipType, false);
		}
		likelyGivenRelationshipType.put(likelyRelationshipType, true);

		this.beliefs = ProbabilityMapHelper.createProbabilityMap(likelyGivenRelationshipType, bigProbabilitySmallProbabilityRatio);
//		System.out.println(beliefs);
	}
	
	@Override
	public void update(ActionKnowledge actionKnowledge) {
		update(actionKnowledge,1); //No emphasis
	}
	
	@Override
	public void update(ActionKnowledge actionKnowledge, double emphasis) {
		double totalProbability = 0;
//		double minNewProbability = Collections.min(actionKnowledge.getProbabilities().values());
//		double BIAS_FRACTION = .1;
		double BIAS_POWER = emphasis;
		for (Map.Entry<RelationshipType, Double> belief : this.beliefs.entrySet()) {
			double actionProbabilityGivenRelationshipType = actionKnowledge.getProbabilityGiven(belief.getKey());
			double currRelativeProbability = (belief.getValue())*Math.pow(actionProbabilityGivenRelationshipType,BIAS_POWER);
			belief.setValue(currRelativeProbability);
			totalProbability = totalProbability + currRelativeProbability;
		} //done considering all relationship types
		
		for (Map.Entry<RelationshipType, Double> belief : this.beliefs.entrySet()) {
			//Normalize, so that probabilities sum to 1
			belief.setValue( belief.getValue() / totalProbability);
		}//done normalizing	
	}
	
	@Override
	public String toString() {
		assert valid();
		String toPrint = "{ ";
		for (Map.Entry<RelationshipType, Double> entry : this.beliefs.entrySet()) {
			toPrint += entry.getKey().toString().charAt(0) + "=" + new DecimalFormat("##.#").format(entry.getValue()) + " ";
		}
		return toPrint + "}";
		
	}

	private boolean valid() {
		double sum = 0;
		for (Double val : this.beliefs.values()) {
			sum += val;
		}
		return sum == 1;
	}

	@Override
	public double distanceFrom(RelationshipInterface relationshipInterface) {
		Relationship_TypeProbModel other = (Relationship_TypeProbModel) relationshipInterface;
		double distance = 0;
		for (Map.Entry<RelationshipType,Double> belief : this.beliefs.entrySet()) {
			distance += Math.abs(belief.getValue() - other.beliefs.get(belief.getKey()));
		}		
		return distance;
	}

	@Override
	public double probabilityOf(DescriptionUnit descriptionUnit, ActionKnowledge actionKnowledge) throws Exception {
		double probabilityOfObservation = 0;
//		RelationshipType believedRelationshipType = this.getMostBelievedRelationshipType();
//		double probabilityOfBelievedRelationshipType = this.getProbabilityOf(believedRelationshipType);
//		if (probabilityOfBelievedRelationshipType == this.getNeutralProbability() ) {
//			throw new Exception("");
//		}
		
		for (Entry<RelationshipType, Double> belief : this.beliefs.entrySet()) {
			probabilityOfObservation += actionKnowledge.getProbabilityGiven(belief.getKey()) * belief.getValue();
		}
		return probabilityOfObservation;
	}

	private RelationshipType getMostBelievedRelationshipType() throws Exception {
		double mostBelievedProbability = 0;
		RelationshipType mostBelievedRelationshipType = null;
		for (RelationshipType currRelationshipType : this.beliefs.keySet()) {
			double currProbability = this.beliefs.get(currRelationshipType);
			if (currProbability > mostBelievedProbability) {
				mostBelievedProbability = currProbability;
				mostBelievedRelationshipType = currRelationshipType;
			}
		}//done with all beliefs
		if (mostBelievedRelationshipType == null) {
			throw new Exception("All beliefs about this relationship were 0: " + this.beliefs);
		}
		return mostBelievedRelationshipType;
	}

	private double getProbabilityOf(RelationshipType relationshipType) {
		return this.beliefs.get(relationshipType);
	}

	@Override
	public boolean hasOpinion() {
		boolean hasOpinion = false;
		for (Double val : this.beliefs.values()) {
			if (val != 1./this.beliefs.size()) {
				hasOpinion = true;
			}
		}
		return hasOpinion;
	}

}
