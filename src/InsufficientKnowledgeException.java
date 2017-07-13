/***
 * An InsufficientKnowledgeException indicates there was insufficient knowledge to continue.
 * @author pkalluri
 *
 */
public class InsufficientKnowledgeException extends Exception{

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 7317258730942975486L;

	/***
	 * Create InsufficientKnowledgeException with convenient text describing which knowledge was lacking.
	 * @param action
	 */
	public InsufficientKnowledgeException(String action) {
		super("Insufficient knowledge about \"" + action + "\" to continue.");
	}
}
