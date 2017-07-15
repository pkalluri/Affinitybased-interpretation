
/***
 * A TricopaParticipant can be administered TricopaTasks.
 * @author pkalluri
 *
 */
public interface TricopaParticipant {
	
	/***
	 * Do the given tricopaTask and return the number (1 or 2) indicating the choice thought to be correct.
	 * @param tricopaTask the TricopaTask to do
	 * @return the number (1 or 2) indicating the choice thought to be correct
	 * @throws UndecidedAgentException the agent was unable to decide
	 * @throws InsufficientActionKnowledgeException the agent had insufficient knowledge to decide
	 */
	public int doTricopaTask(TricopaTask tricopaTask) throws InsufficientActionKnowledgeException, UndecidedAgentException;
}
