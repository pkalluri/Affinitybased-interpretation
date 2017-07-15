
/***
 * A WorldModel holds beliefs about a world that can be updated as ActionEvents occur in that world.
 * @author pkalluri
 *
 */
public interface WorldModel {
	
	/***
	 * Get age of world
	 * @return age of world
	 */
	public int getAge();

	/***
	 * Update world model with the given actionEvent, assuming the given actionKnowledge.
	 * @param actionEvent the ActionEvent occurring in this world
	 * @param actionKnowledge the ActionKnowledge about the action being completed
	 */
	public void update(ActionEvent actionEvent, ActionROD actionKnowledge);
	
	/***
	 * Reflect on beliefs and update beliefs based on this reflection.
	 */
	public void reflectOnAndRefineBeliefs();
	
	/***
	 * Get a score quantifying the difference between this world model and the given otherWorldModel.
	 * @param otherWorldModel the WorldModel to compare to
	 * @return a score quantifying the difference between this world model and the given otherWorldModel
	 */
	public double distanceScore(WorldModel otherWorldModel);

	/***
	 * Get the probability of the given actionEvent, assuming the given actionKnowledge.
	 * @param actionEvent the ActionEvent to get the probability of
	 * @param actionKnowledge the ActionKnowledge about the action being completed
	 * @return the probability of the given actionEvent, assuming the given actionKnowledge
	 */
	public double probabilityOf(ActionEvent validDescriptionUnit, ActionROD actionKnowledge) throws Exception;

}
