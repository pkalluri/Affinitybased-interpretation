import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Relationship_TypeProbModel implements RelationshipInterface {
	private Map<RelationshipType,Double> beliefs;
	
	public Relationship_TypeProbModel() {
		beliefs = new HashMap<RelationshipType,Double>();
		RelationshipType[] types = RelationshipType.values();
		double INITIAL_PROBABILITY = 1./(double)(types.length);
		for (RelationshipType type : types) {
	 		beliefs.put(type, INITIAL_PROBABILITY);
		}
	}
	
	@Override
	public void update(ActionKnowledge actionKnowledge) {
		update(actionKnowledge,1); //No emphasis
	}
	
	@Override
	public void update(ActionKnowledge actionKnowledge, double emphasis) {
		double totalProbability = 0;
		double minNewProbability = Collections.min(actionKnowledge.getProbabilities().values());
		double BIAS_FRACTION = .1;
		double BIAS_POWER = emphasis;
		for (Map.Entry<RelationshipType, Double> belief : beliefs.entrySet()) {
			double probabilityGivenRelationshipType = actionKnowledge.getProbabilityGiven(belief.getKey());
			double currRelativeProbability = (belief.getValue())*Math.pow(probabilityGivenRelationshipType,BIAS_POWER);
			belief.setValue(currRelativeProbability);
			totalProbability = totalProbability + currRelativeProbability;
		} //done considering all relationship types
		
		for (Map.Entry<RelationshipType, Double> belief : beliefs.entrySet()) {
			//Normalize, so that probabilities sum to 1
			belief.setValue( belief.getValue() / totalProbability);
		}//done normalizing	
	}
	
	@Override
	public String toString() {
		return beliefs.toString();
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
		RelationshipType believedRelationshipType = this.getMostBelievedRelationshipType();
		double probabilityOfBelievedRelationshipType = this.getProbabilityOf(believedRelationshipType);
//		if (probabilityOfBelievedRelationshipType == this.getNeutralProbability() ) {
//			throw new Exception("");
//		}
		probabilityOfObservation += actionKnowledge.getProbabilityGiven(believedRelationshipType) * probabilityOfBelievedRelationshipType;
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

}
