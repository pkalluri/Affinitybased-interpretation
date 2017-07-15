
/***
 * An InsufficientActionKnowledgeException indicates there was insufficient knowledge about a givne action to continue.
 * @author pkalluri
 *
 */
public class InsufficientActionKnowledgeException extends Exception{

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 7317258730942975486L;
	private String action;

	/***
	 * Create InsufficientKnowledgeException with convenient text describing which knowledge was lacking.
	 * @param action
	 */
	public InsufficientActionKnowledgeException(String action) {
		super("Insufficient knowledge about \"" + action + "\" to continue.");
		this.action = action;
	}
	
	/***
	 * Get action.
	 * @return
	 */
	public String getAction() {
		return this.action;
	}
}
