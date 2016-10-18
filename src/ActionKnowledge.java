import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ActionKnowledge {
	/***
	 * Constraint: Sum of probabilities across all relationship types always equals 1.
	 */
	private Map<RelationshipType,Double> relativeProbsGivenRelationshipType; 

	public ActionKnowledge(Map<RelationshipType,Double> relativeProbsGivenRelationshipType) {
		this.relativeProbsGivenRelationshipType = relativeProbsGivenRelationshipType;
	}
	
	public <T> ActionKnowledge(Map<RelationshipType,T> map, boolean mapOfDoubles, double bigProbabilitySmallProbabilityRatio) {
		if (mapOfDoubles) {
			this.relativeProbsGivenRelationshipType = (Map<RelationshipType,Double>) map;
		} else { //map of booleans
			Map<RelationshipType,Boolean> likelyGivenRelationshipType = (Map<RelationshipType,Boolean>) map;

			this.relativeProbsGivenRelationshipType = ProbabilityMapHelper.createProbabilityMap(likelyGivenRelationshipType, bigProbabilitySmallProbabilityRatio);
		}
	}
		
	

	//	
//	public ActionKnowledge(Map<RelationshipType,Boolean> relativeProbsGivenRelationshipType) {
//	this.relativeProbsGivenRelationshipType = new HashMap<RelationshipType,Double>();
//	this.relativeProbsGivenRelationshipType.put(RelationshipType.Friend, .5);
//	this.relativeProbsGivenRelationshipType.put(RelationshipType.Enemy, .5);
//}
//	
	/***
	 * These assume only 2 rel types.
	 * @param relationshipType
	 */
//	public ActionKnowledge(RelationshipType a, double aProbability, RelationshipType b, double bProbability) {
//		this.relativeProbsGivenRelationshipType = new HashMap<RelationshipType,Double>();
//		this.relativeProbsGivenRelationshipType.put(a, aProbability);
//		this.relativeProbsGivenRelationshipType.put(b, bProbability);
//	}

//	public ActionKnowledge(RelationshipType relationshipType) {
//		this.relativeProbsGivenRelationshipType = new HashMap<RelationshipType,Double>();
//		double POSITIVE_BIAS_VALUE = .75;
//		switch (relationshipType) {
//			case Friend:
//				this.relativeProbsGivenRelationshipType.put(RelationshipType.Friend, POSITIVE_BIAS_VALUE);
//				this.relativeProbsGivenRelationshipType.put(RelationshipType.Enemy, 1-POSITIVE_BIAS_VALUE);
//				break;
//			case Enemy:
//				this.relativeProbsGivenRelationshipType.put(RelationshipType.Enemy, POSITIVE_BIAS_VALUE);
//				this.relativeProbsGivenRelationshipType.put(RelationshipType.Friend, 1-POSITIVE_BIAS_VALUE);
//				break;
//		}
//	}



	public Double getProbabilityGiven(RelationshipType relationshipType) {
		return relativeProbsGivenRelationshipType.get(relationshipType);
	}
	
	@Override
	public String toString() {
		assert valid();
		String toPrint = "{ ";
		for (Map.Entry<RelationshipType, Double> entry : this.relativeProbsGivenRelationshipType.entrySet()) {
			toPrint += entry.getKey().toString().charAt(0) + "=" + new DecimalFormat("##.#").format(entry.getValue()) + " ";
		}
		return toPrint + "}";
	}

	private boolean valid() {
		double sum = 0;
		for (Double val : this.relativeProbsGivenRelationshipType.values()) {
			sum += val;
		}
		return sum == 1;
	}

	public Map<RelationshipType, Double> getProbabilities() {
		return this.relativeProbsGivenRelationshipType;
	}

}
