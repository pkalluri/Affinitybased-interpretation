import java.util.HashMap;
import java.util.Map;

public class ProbabilityMapHelper {
	
	public static Map<RelationshipType, Double> createProbabilityMap(Map<RelationshipType,Boolean> likelyGivenRelationshipType, double bigProbabilitySmallProbabilityRatio) {
		Map<RelationshipType,Double> relativeProbsGivenRelationshipType = new HashMap<RelationshipType,Double>();
		for (Map.Entry<RelationshipType,Boolean> belief : likelyGivenRelationshipType.entrySet()) {
			if (belief.getValue()) { //likely
				relativeProbsGivenRelationshipType.put(belief.getKey(), bigProbabilitySmallProbabilityRatio);
			} else { //unlikely
				relativeProbsGivenRelationshipType.put(belief.getKey(), 1.0);
			}
		}//done putting all beliefs in local map
		ProbabilityMapHelper.normalize(relativeProbsGivenRelationshipType);
		return relativeProbsGivenRelationshipType;
	}

	
	private static void normalize(Map<RelationshipType, Double> relativeProbsGivenRelationshipType) {
		double sum = 0;
		for (Double relativeProbability : relativeProbsGivenRelationshipType.values()) {
			sum += relativeProbability;
		}
		
		for (Map.Entry<RelationshipType, Double> entry: relativeProbsGivenRelationshipType.entrySet()) {
			entry.setValue(entry.getValue()/sum);
		}
		
	}
	

}
