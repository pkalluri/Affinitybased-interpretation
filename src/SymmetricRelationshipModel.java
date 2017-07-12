import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SymmetricRelationshipModel implements SymmetricRelationshipModelInterface {
	/***
	 * Maps each RelationshipType to the believed probability of that RelationshipType for this relationship.
	 * Beliefs must sum to 1.
	 */
	private Map<RelationshipType,Double> beliefs;
	
	//////////////////////////////////////////
	/////// CONSTRUCTORS /////////////////////
	//////////////////////////////////////////
	
	/***
	 * Creates new, uninformed relationship model.
	 */
	public SymmetricRelationshipModel(RelationshipType[] relationshipTypes) {
		beliefs = new HashMap<RelationshipType,Double>();
		
		double uniformProbability = 1./(double)(relationshipTypes.length);
		for (RelationshipType type : relationshipTypes) {
	 		beliefs.put(type, uniformProbability);
		}
	}
	
	/***
	 * Creates new relationship model informed by a belief that the relationship is factor X (e.g. 2X) times as likely
	 * to be the given likelyRelationshipType than any other RelationshipType.
	 * @param likelyRelationshipType the RelationshipType thought to be likely
	 * @param factor the factor by which the likelyRelationshipType is thought to be more likely than any other RelationshipType
	 */
	public SymmetricRelationshipModel(RelationshipType likelyRelationshipType, double factor) {
		Map<RelationshipType, Boolean> likelyGivenRelationshipType = new HashMap<RelationshipType,Boolean>();
		for (RelationshipType relationshipType : RelationshipType.values()) {
			likelyGivenRelationshipType.put(relationshipType, false);
		}
		likelyGivenRelationshipType.put(likelyRelationshipType, true);

		this.beliefs = ProbabilityMapUtility.createProbabilityMap(likelyGivenRelationshipType, factor);	
	}
	
	//////////////////////////////////////////
	/////// OVERRIDE INTERFACE METHODS ///////
	//////////////////////////////////////////
	
	@Override
	public void update(ActionKnowledge actionKnowledge) {
		double totalProbability = 0;
		for (Map.Entry<RelationshipType, Double> belief : this.beliefs.entrySet()) {
			double actionProbabilityGivenRelationshipType = actionKnowledge.getProbabilityGiven(belief.getKey());
			double currRelativeProbability = (belief.getValue())*actionProbabilityGivenRelationshipType;
			belief.setValue(currRelativeProbability);
			totalProbability = totalProbability + currRelativeProbability;
		} //done considering all relationship types
		
		for (Map.Entry<RelationshipType, Double> belief : this.beliefs.entrySet()) {
			//Normalize, so that probabilities sum to 1
			belief.setValue( belief.getValue() / totalProbability);
		}//done normalizing	
	}
	
	@Override
	public void update(ActionKnowledge actionKnowledge, double emphasis) {
		for (int i=0; i<emphasis; i++) {
			update(actionKnowledge);
		}
	}
	
	@Override
	public double distanceScore(SymmetricRelationshipModelInterface relationshipInterface) {
		SymmetricRelationshipModel other = (SymmetricRelationshipModel) relationshipInterface;
		double distance = 0;
		for (Map.Entry<RelationshipType,Double> belief : this.beliefs.entrySet()) {
			distance += Math.abs(belief.getValue() - other.beliefs.get(belief.getKey()));
		}		
		return distance;
	}

	@Override
	public double probabilityOf(ActionKnowledge actionKnowledge) {
		double probabilityOfObservation = 0;
		
		for (Entry<RelationshipType, Double> belief : this.beliefs.entrySet()) {
			probabilityOfObservation += actionKnowledge.getProbabilityGiven(belief.getKey()) * belief.getValue();
		}
		return probabilityOfObservation;
	}

	@Override
	public boolean isInformative() {
		boolean hasOpinion = false;
		int numEntries = this.beliefs.size();
		for (Double val : this.beliefs.values()) {
			if ((val - 1./(double)numEntries) >.001) { //has opinion
				hasOpinion = true;
			}
		}
		return hasOpinion;
	}

	@Override
	public String toString() {
		assert isValid();
		String toPrint = "{ ";
		for (Map.Entry<RelationshipType, Double> entry : this.beliefs.entrySet()) {
			toPrint += entry.getKey().toString().charAt(0) + "=" + new DecimalFormat("##.#").format(entry.getValue()) + " ";
		}
		return toPrint + "}";
		
	}
	
	public String toShortString() {
		assert isValid();
		String toPrint = "";
		for (Map.Entry<RelationshipType, Double> entry : this.beliefs.entrySet()) {
			NumberFormat format = NumberFormat.getPercentInstance();
			format.setMinimumIntegerDigits(2);
			toPrint += format.format(entry.getValue()) + "|";		}		
		return toPrint.substring(0,toPrint.length()-1);
	}

	//////////////////////////////////////////
	/////// PRIVATE HELPERS //////////////////
	//////////////////////////////////////////
	
	private boolean isValid() {
		double sum = 0;
		for (Double val : this.beliefs.values()) {
			sum += val;
		}
		return sum == 1;
	}

	//////////////////////////////////////////
	/////// ADDED METHODS ////////////////////
	//////////////////////////////////////////
	
	/***
	 * Get map from the RelationshipTypes to the believed probability of each RelationshipType for this relationship.
	 * Beliefs must sum to 1.	 
	 * @return map from the RelationshipTypes to the believed probability of each RelationshipType for this relationship
	 */
	public Map<RelationshipType,Double> getCopyOfBeliefs() {
		Map<RelationshipType, Double> copy = new HashMap<RelationshipType,Double>();
		for (Map.Entry<RelationshipType, Double> entry : this.beliefs.entrySet()) {
			copy.put(entry.getKey(), new Double(entry.getValue().doubleValue()));
		}
		return copy;
	}

}
