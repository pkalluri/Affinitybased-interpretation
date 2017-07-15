
import java.util.List;

/***
 * A TricopaTask consists of a premise Scenario and two additional Scenarios that each may or may not logically 
 * follow from the premise. A TricopaTask is immutable.
 * 
 * The TricopaTask format is the format of the Triangle-COPA challenge problems introduced in Maslan et al. (2015).
 *  
 * @author pkalluri
 *
 */
public class TricopaTask {
	/***
	 * The premise Scenario
	 */
	public final Scenario premise;
	
	/***
	 * A list containing the two Scenarios that each may or may not logically follow from the premise
	 */
	public final List<Scenario> possibleChoices;
	
	/***
	 * Create a TricopaTask consisting of the given premise Scenario and the two additional given Scenarios.
	 * @param premise the premise Scenario
	 * @param possibleChoices a list containing the two Scenarios that each may or may not logically follow from the premise 
	 */
	public TricopaTask(Scenario premise, List<Scenario> possibleChoices) {
		this.premise = premise;
		this.possibleChoices = possibleChoices;
	}
	
	@Override
	public String toString() {
		return premise.toString() + possibleChoices.toString();
	}

	

}
