import java.util.Set;

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
	
	/***
	 * Return True iff the ActionEvent contains at least one agent from the given relationship.
	 * @param relationship
	 * @return True iff the ActionEvent contains at least one agents from the given relationship
	 */
	public boolean containsEither(Pair<String> relationship) {
		Set<String> relationshipAgents = relationship.getElements();
		return (relationshipAgents.contains(this.actor) || relationshipAgents.contains(this.actedUpon));
	}
	
	@Override
	public String toString() {
		String str = "";
		if (this.actor == null) {
			str += "";
		} else {
			str += this.actor;
		}
		
		str += " " + this.action + " ";
		
		if (this.actedUpon == null) {
			str += "";
		} else {
			str += this.actedUpon;
		}
		return str;
	}
}