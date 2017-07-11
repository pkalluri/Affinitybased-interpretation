/***
 * An ActionEvent consists of an actor agent completing an action optionally upon an acted upon agent.
 * An ActionEvent is immutable.
 * 
 * @author pkalluri
 */
public final class ActionEvent {
	/**
	 * The identifying name of the actor agent completing the action.
	 */
	public final String actor;
	/***
	 * The action being completed, expressed as a present tense 3rd person singular verb.
	 */
	public final String action;
	/***
	 * The identifying name of the optional acted upon agent.
	 */
	public final String actedUpon;
	
	/***
	 * Create an ActionEvent in which the actor agent completes the action optionally upon an actedUpon agent.
	 * @param actor the identifying name of the actor agent completing the action
	 * @param action the action being completed, expressed as a present tense 3rd person singular verb
	 * @param actedUpon the identifying name of the optional, acted upon agent
	 */
	public ActionEvent(String actor, String action, String actedUpon) {
		this.actor = actor;
		this.action = action;
		this.actedUpon = actedUpon;
	}
	
	@Override
	public String toString() {
		return actor + " " + action + " " + actedUpon;
	}
}