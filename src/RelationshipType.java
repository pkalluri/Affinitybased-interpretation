
/***
 * The RelationshipType enum describes the three types of relationships: Friend, Enemy, and Neutral.
 * @author pkalluri
 *
 */
public enum RelationshipType {

	/***
	 * A relationship in which the agents act as friends toward one another.
	 */
	FRIEND, 
	
	/***
	 * A relationship in which the agents act as enemies toward one another.
	 */
	ENEMY,
	
	/***
	 * A relationship in which the agents act neutrally toward one another.
	 * Note that a neutral relationship does not necessarily signify a relationship that the observer knows little about.
	 * For example, an observer might be more willing to bet that the next action in a neutral relationship will be neutral
	 * while that same observer might be relatively less willing to bet that the next action of an unobserved relationship will 
	 * necessarily be neutral.
	 */
	NEUTRAL

}
