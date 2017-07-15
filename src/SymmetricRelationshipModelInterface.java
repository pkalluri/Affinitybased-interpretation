
/***
 * A SymmetricRelationshipModel models the relationship between two agents, imposing symmetry of the relationship.
 * Symmetry of the relationship means an action committed by either agent affects the SymmetricRelationshipModel in the same way.
 * @author pkalluri
 *
 */
public interface SymmetricRelationshipModelInterface {

	/***
	 * Update relationship model with the action indicated by the given actionKnowledge.
	 * @param actionKnowledge the ActionKnowledge of a specific action 
	 */
	public void update(ActionROD actionKnowledge);
	
	/***
	 * Update relationship model with the action indicated by the given actionKnowledge and 
	 * place factor X emphasis (e.g. 2X emphasis) on this update relative to a standard update.
	 * @param actionKnowledge the ActionKnowledge of a specific action 
	 */
	public void update(ActionROD actionKnowledge, double factor);

	
	
	/***
	 * Get a score quantifying the difference between this relationship model and the given otherRelationshipModel.
	 * @param otherRelationshipModel the RelationshipModel to compare to
	 * @return a score quantifying the difference between this relationship model and the given otherRelationshipModel
	 */
	public double distanceScore(SymmetricRelationshipModelInterface otherRelationshipModel);

	/***
	 * Get the probability of the action indicated by the given actionKnowledge.
	 * @param actionKnowledge the ActionKnowledge of a specific action 
	 * @return the probability of the action indicated by the given actionKnowledge
	 */
	public double probabilityOf(ActionROD actionKnowledge);

	
	
	/***
	 * Returns true iff this SymmetricRelationshipModel is more informative about the relationship than 
	 * having no information about the relationship.
	 * @return
	 */
	public boolean isInformative();	

}
