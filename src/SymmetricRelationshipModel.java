
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	public SymmetricRelationshipModel() {
		beliefs = new HashMap<RelationshipType,Double>();
		
		double uniformProbability = 1./(double)(RelationshipType.values().length);
		for (RelationshipType type : RelationshipType.values()) {
	 		beliefs.put(type, uniformProbability);
		}
		assert isValid();
	}
	
	/***
	 * Creates new relationship model informed by a belief that the relationship is factor X (e.g. 2X) times as likely
	 * to be the given likelyRelationshipType than any other RelationshipType.
	 * @param likelyRelationshipType the RelationshipType thought to be likely
	 * @param factor the factor by which the likelyRelationshipType is thought to be more likely than any other RelationshipType
	 */
	public SymmetricRelationshipModel(RelationshipType likelyRelationshipType, double factor) {
		Map<RelationshipType, Boolean> beliefs = new HashMap<RelationshipType,Boolean>();
		for (RelationshipType relationshipType : RelationshipType.values()) {
			beliefs.put(relationshipType, false);
		}
		beliefs.put(likelyRelationshipType, true);

		this.beliefs = ProbabilityMapUtility.createProbabilityMap(beliefs, factor);	
		assert isValid();
	}
	
	/***
	 * Creates new relationship model informed by a belief that the given orderedRelationshipTypes are in the increasing order of likelihood.
	 * @param orderedRelationshipTypes the RelationshipType thought to be likely
	 * @param factor the factor by which the likelyRelationshipType is thought to be more likely than any other RelationshipType
	 */
	public SymmetricRelationshipModel(List<RelationshipType> orderedRelationshipTypes) {
		Map<RelationshipType, Double> beliefs = new HashMap<RelationshipType,Double>();
		double emphasis = 1;
		for (RelationshipType relationshipType: orderedRelationshipTypes) {
			beliefs.put(relationshipType, emphasis);
			emphasis ++;
		}
		
		ProbabilityMapUtility.normalize(beliefs);

		this.beliefs = beliefs;
	}
	
	//////////////////////////////////////////
	/////// OVERRIDE INTERFACE METHODS ///////
	//////////////////////////////////////////
	
	@Override
	public void update(ActionROD actionKnowledge) {
		double totalProbability = 0;
		for (Map.Entry<RelationshipType, Double> belief : this.beliefs.entrySet()) {
			double actionProbabilityGivenRelationshipType = actionKnowledge.getRelativeProbabilityGiven(belief.getKey());
			double currRelativeProbability = (belief.getValue())*actionProbabilityGivenRelationshipType;
			belief.setValue(currRelativeProbability);
			totalProbability = totalProbability + currRelativeProbability;
		} //done considering all relationship types
		
		for (Map.Entry<RelationshipType, Double> belief : this.beliefs.entrySet()) {
			//Normalize, so that probabilities sum to 1
			belief.setValue( belief.getValue() / totalProbability);
		}//done normalizing	
		assert isValid();
	}
	
	@Override
	public void update(ActionROD actionKnowledge, double emphasis) {
		for (int i=0; i<emphasis; i++) {
			update(actionKnowledge);
		}
		assert isValid();
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
	public double probabilityOf(ActionROD actionKnowledge) {
		double probabilityOfObservation = 0;
		
		for (Entry<RelationshipType, Double> belief : this.beliefs.entrySet()) {
			probabilityOfObservation += actionKnowledge.getRelativeProbabilityGiven(belief.getKey()) * belief.getValue();
		}
		return probabilityOfObservation;
	}

	@Override
	public boolean isInformative() {
		boolean isInformative = false;
		int numEntries = this.beliefs.size();
		for (Double val : this.beliefs.values()) {
			if ((val - 1./(double)numEntries) >.001) { //has opinion
				isInformative = true;
			}
		}
		return isInformative;
	}

	@Override
	public String toString() {
		String toPrint = "{ ";
		for (Map.Entry<RelationshipType, Double> entry : this.beliefs.entrySet()) {
			toPrint += entry.getKey().toString().charAt(0) + "=" + new DecimalFormat("##.#").format(entry.getValue()) + " ";
		}
		return toPrint + "}";
		
	}
	
	/***
	 * Get concise String representation.
	 * @return
	 */
	public String toConciseString() {
		String toPrint = "";
		List<RelationshipType> orderedRelationshipTypes = new ArrayList<RelationshipType>();
		orderedRelationshipTypes.add(RelationshipType.FRIEND);
		orderedRelationshipTypes.add(RelationshipType.NEUTRAL);
		orderedRelationshipTypes.add(RelationshipType.ENEMY);
		for (RelationshipType relationshipType: orderedRelationshipTypes) {
			NumberFormat format = NumberFormat.getPercentInstance();
			format.setMinimumIntegerDigits(2);
			toPrint += format.format(this.beliefs.get(relationshipType)) + "|";		}		
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
	 * Get map mapping the RelationshipTypes to the believed probability of each RelationshipType, for this relationship.
	 * Beliefs must sum to 1.	 
	 * @return map from the RelationshipTypes to the believed probability of each RelationshipType for this relationship
	 */
	public Map<RelationshipType,Double> getBeliefs() {
		return new HashMap<RelationshipType,Double>(this.beliefs);
	}

}
