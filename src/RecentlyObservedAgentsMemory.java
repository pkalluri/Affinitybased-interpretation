
/***
 * RecentlyObservedAgentsMemory contains the two most recently observed agents, and 
 * is updated as agents continue to be encountered.
 * @author pkalluri
 *
 */
public class RecentlyObservedAgentsMemory {
	
	private String lastObservedAgent;
	private String secondToLastObservedAgent;

	////////////////////////////////////////////////////////////
	//////// CONSTRUCTOR ///////////////////////////////////////
	////////////////////////////////////////////////////////////

	/***
	 * Construct new, uninformed RecentlyObservedAgentsMemory.
	 */
	public RecentlyObservedAgentsMemory() {
		this.lastObservedAgent = null;
		this.secondToLastObservedAgent = null;
	}
	
	/***
	 * Construct new RecentlyObservedAgentsMemory from existing RecentlyObservedAgentsMemory. (like clone).
	 */
	public RecentlyObservedAgentsMemory(RecentlyObservedAgentsMemory other) {
		this.lastObservedAgent = other.lastObservedAgent;
		this.secondToLastObservedAgent = other.secondToLastObservedAgent;
	}
	
	////////////////////////////////////////////////////////////
	//////// GETTERS AND SETTERS ///////////////////////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Get last observed agent.
	 * @return last observed agent
	 */
	public String getLastObservedAgent() {
		return this.lastObservedAgent;
	}
	
	/***
	 * Get second to last observed agent.
	 * @return second to last observed agent
	 */
	public String getSecondToLastObservedAgent() {
		return this.secondToLastObservedAgent;
	}
	
	/***
	 * Set recently observed agents to given lastObservedAgent and secondToLastObservedAgent.
	 * @param secondToLastObservedAgent
	 * @param lastObservedAgent
	 */
	public void setRecentlyObservedAgents(String secondToLastObservedAgent, String lastObservedAgent) {
		this.lastObservedAgent = lastObservedAgent;
		this.secondToLastObservedAgent = secondToLastObservedAgent;
	}
	
	/***
	 * Resets memory as though it is a new, uninformed RecentlyObservedAgentsMemory.
	 */
	public void reset() {
		this.lastObservedAgent = null;
		this.secondToLastObservedAgent = null;
	}
	
	////////////////////////////////////////////////////////////
	//////// UPDATE ////////////////////////////////////////////
	////////////////////////////////////////////////////////////
	
	/***
	 * Update memory, given the just now observed agent.
	 * @param agent the just now observed agent
	 * @return true iff the update did 
	 */
	public void update(String agent, boolean verbose) {
		if (this.lastObservedAgent == null || !this.lastObservedAgent.equals(agent)) {
			//Slide the recently observed agents over
			this.secondToLastObservedAgent = this.lastObservedAgent;
			this.lastObservedAgent = agent;
//			if (verbose) {System.out.println(this);}
		}
	}
	
	
	
	@Override
	public String toString() {
		String NULL_AGENT_STRING = "_";
		String toReturn = "Memory: ";
		
		if (this.secondToLastObservedAgent == null) {
			toReturn += NULL_AGENT_STRING;
		} else {
			toReturn += this.secondToLastObservedAgent;
		}
		
		toReturn += " ";
		
		if (this.lastObservedAgent == null) {
			toReturn += NULL_AGENT_STRING;
		} else {
			toReturn += this.lastObservedAgent;
		}
		return toReturn;
	}

}
