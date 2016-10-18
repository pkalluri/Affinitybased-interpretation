import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ActionKnowledge {
	private Map<RelationshipType,Double> relativeProbsGivenRelationshipType; 

	public ActionKnowledge(Map<RelationshipType,Double> relativeProbsGivenRelationshipType) {
		this.relativeProbsGivenRelationshipType = relativeProbsGivenRelationshipType;
	}
	
	public ActionKnowledge(RelationshipType a, double aProbability, RelationshipType b, double bProbability) {
		this.relativeProbsGivenRelationshipType = new HashMap<RelationshipType,Double>();
		this.relativeProbsGivenRelationshipType.put(a, aProbability);
		this.relativeProbsGivenRelationshipType.put(b, bProbability);
	}

	public ActionKnowledge(RelationshipType relationshipType) {
		this.relativeProbsGivenRelationshipType = new HashMap<RelationshipType,Double>();
		double POSITIVE_BIAS_VALUE = .75;
		switch (relationshipType) {
			case Friend:
				this.relativeProbsGivenRelationshipType.put(RelationshipType.Friend, POSITIVE_BIAS_VALUE);
				this.relativeProbsGivenRelationshipType.put(RelationshipType.Enemy, 1-POSITIVE_BIAS_VALUE);
				break;
			case Enemy:
				this.relativeProbsGivenRelationshipType.put(RelationshipType.Enemy, POSITIVE_BIAS_VALUE);
				this.relativeProbsGivenRelationshipType.put(RelationshipType.Friend, 1-POSITIVE_BIAS_VALUE);
				break;
		}
	}

	public ActionKnowledge() {
		this.relativeProbsGivenRelationshipType = new HashMap<RelationshipType,Double>();
		this.relativeProbsGivenRelationshipType.put(RelationshipType.Friend, .5);
		this.relativeProbsGivenRelationshipType.put(RelationshipType.Enemy, .5);
	}

	public Double getProbabilityGiven(RelationshipType relationshipType) {
		return relativeProbsGivenRelationshipType.get(relationshipType);
	}
	
	@Override
	public String toString() {
		return this.relativeProbsGivenRelationshipType.toString();
	}

	public Map<RelationshipType, Double> getProbabilities() {
		return this.relativeProbsGivenRelationshipType;
	}

}
