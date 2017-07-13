import java.util.HashMap;
import java.util.Map;

/***
 * ProbabilityMapUtility is a useful utility for managing probability maps.
 * @author pkalluri
 *
 */
public class ProbabilityMapUtility {
	
	/***
	 * Creates new model informed by a belief that the likely T is factor X (e.g. 2X) times as likely
	 * as the other T.
	 * @param <T>
	 * @param likelyT the T thought to be likely
	 * @param factor the factor by which the likelyT is thought to be more likely than any other T
	 */
	public static <T> Map<T, Double> createProbabilityMap(Map<T,Boolean> likelyGivenRelationshipType, double bigProbabilitySmallProbabilityRatio) {
		Map<T,Double> relativeProbsGivenRelationshipType = new HashMap<T,Double>();
		for (Map.Entry<T,Boolean> belief : likelyGivenRelationshipType.entrySet()) {
			if (belief.getValue()) { //likely
				relativeProbsGivenRelationshipType.put(belief.getKey(), bigProbabilitySmallProbabilityRatio);
			} else { //unlikely
				relativeProbsGivenRelationshipType.put(belief.getKey(), 1.0);
			}
		}//done putting all beliefs in local map
		ProbabilityMapUtility.normalize(relativeProbsGivenRelationshipType);
		return relativeProbsGivenRelationshipType;
	}

	
	/***
	 * Maintain the relative probability distribution, but impose a constraint that the probability distribution must sum to 1.
	 * @param <T>
	 * @param relativeProbsGivenRelationshipType
	 */
	public static <T> void normalize(Map<T, Double> relativeProbsGivenRelationshipType) {
		double sum = 0;
		for (Double relativeProbability : relativeProbsGivenRelationshipType.values()) {
			sum += relativeProbability;
		}
		
		for (Map.Entry<T, Double> entry: relativeProbsGivenRelationshipType.entrySet()) {
			entry.setValue(entry.getValue()/sum);
		}
		
	}
	

}
