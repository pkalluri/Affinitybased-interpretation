
import java.util.ArrayList;
import java.util.List;

/***
 * A Scenario consists of an ordered sequence of ActionEvents and is immutable.
 * 
 * @author pkalluri
 */
public class Scenario {
	/***
	 * The ordered sequence of ActionEvents that make up this Scenario.
	 */
	public final List<ActionEvent> actionEvents;
	
	/***
	 * The number of ActionEvents that make up this Scenario.
	 */
	public final int length;
	
	/***
	 * Create a Scenario consisting of the given ordered sequence of actionEvents.
	 * @param actionEvents
	 */
	public Scenario(List<ActionEvent> actionEvents) {
		this.actionEvents = actionEvents;
		this.length = actionEvents.size();
	}
	
	@Override
	public String toString () {
		return actionEvents.toString();
	}
}
