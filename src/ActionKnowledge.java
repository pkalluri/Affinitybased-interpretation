import java.text.DecimalFormat;
import java.util.Map;

/***
 * The ActionKnowledge corresponding to a particular action holds the relative observation distribution of that action over the RelationshipTypes.
 * @author pkalluri
 *
 */
public class ActionKnowledge {
	/***
	 * Relative observation distribution (R.O.D.) over the RelationshipTypes.
	 * Constraint: The sum of the R.O.D. probabilities over the RelationshipTypes always equals 1.
	 */
	private Map<RelationshipType,Double> relativeObservationDistribution; 

	//////////////////////////////////////////
	/////// CONSTRUCTORS /////////////////////
	//////////////////////////////////////////

	/***
	 * Construct the ActionKnowledge containing the given relativeObservationDistribution.
	 * @param relativeObservationDistribution the relative observation distribution over RelationshipTypes for this ActionKnowledge
	 */
	public ActionKnowledge(Map<RelationshipType,Double> relativeObservationDistribution) {
		this.relativeObservationDistribution = relativeObservationDistribution;
	}
	
	/***
	 * Construct the ActionKnowledge based on the given map from RelationshipTypes to whether this action is 
	 * likely given the RelationshipType. 
	 * 
	 * Interpreted with the constraint that the ratio between two probabilities in a single action's 
	 * relative observation distribution must always be 1 or the ratioPermittedWithinROD.
	 * @param likelyGivenRelationshipTypes
	 * @param ratioPermittedWithinROD the ratio between two probabilities in this action's 
	 * relative observation distribution must always be 1 or this ratio
	 */
	public ActionKnowledge(Map<RelationshipType,Boolean> likelyGivenRelationshipType, double ratioPermittedWithinROD) {
		this.relativeObservationDistribution = ProbabilityMapUtility.createProbabilityMap(likelyGivenRelationshipType, ratioPermittedWithinROD);
	}

	//////////////////////////////////////////
	/////// GETTERS //////////////////////////
	//////////////////////////////////////////

	/***
	 * Get the relative observation distribution of this action over the RelationshipTypes.
	 * @return the relative observation distribution of this action over the RelationshipTypes
	 */
	public Map<RelationshipType, Double> getProbabilities() {
		return this.relativeObservationDistribution;
	}

	/***
	 * Get the relative probability of observation of this action for the given RelationshipType.
	 * @param relationshipType
	 * @return the relative probability of observation of this action for the given RelationshipType
	 */
	public Double getProbabilityGiven(RelationshipType relationshipType) {
		return relativeObservationDistribution.get(relationshipType);
	}
	
	@Override
	public String toString() {
		assert isValid();
		String toPrint = "----[action knowledge:";
		for (Map.Entry<RelationshipType, Double> entry : this.relativeObservationDistribution.entrySet()) {
			toPrint += entry.getKey().toString().charAt(0) + "=" + new DecimalFormat("##.##").format(entry.getValue()) + "/";
		}
		return toPrint.substring(0, toPrint.length()-1) + "]---->";
	}
		
	//////////////////////////////////////////
	/////// INTERNAL CHECK ///////////////////
	//////////////////////////////////////////

	/***
	 * Check all constraints on a valid ActionKnowledge are satisfied.
	 * @return True iff all constraints on a valid ActionKnowledge are satisfied
	 */
	private boolean isValid() {
		double sum = 0;
		for (Double val : this.relativeObservationDistribution.values()) {
			sum += val;
		}
		return sum == 1;
	}

}
