import java.util.HashSet;
import java.util.Set;

/***
 * A Pair contains two unordered elements of the same type. A pair is immutable.
 * 
 * @author pkalluri
 * 
 * @param <E>
 */
public class Pair<E>{
	private final Set<E> pair;
	
	/***
	 * Creates a new pair.
	 * @param a one element
	 * @param b another element 
	 */
	public Pair(E a, E b) {
		this.pair = new HashSet<E>();
		pair.add(a);
		pair.add(b);
	}
	
	/***
	 * Return a set containing the elements in this pair.
	 * @return a set containing the elements in this pair
	 */
	public Set<E> getElements() {
		return this.pair;
	}
	
	/***
	 * Test this Pair for equality with another Object.
	 * 
	 * @param other the other Object
	 * @return true iff the other Object is a Pair and the two elements in the first Pair are equal to the two elements in the second Pair
	 */
	@Override
	public boolean equals(Object other){
		if (!(other instanceof Pair)) {
			return false;
		}
		else {
			@SuppressWarnings("unchecked")
			Pair<E> otherPair = (Pair<E>) other;
			boolean matchesSoFar = true;
			for (E elt : this.pair) {
				matchesSoFar = matchesSoFar && (otherPair.pair.contains(elt));
			}
			for (E elt : otherPair.pair) {
				matchesSoFar = matchesSoFar && (this.pair.contains(elt));
			}
			return matchesSoFar;
		}
	}
	
	/***
	 * Generate a hash code for this Pair.
	 * The hash code is calculated using both elements of the Pair.
	 * 
	 * @return hash code for this Pair
	 */
	@Override
	public int hashCode() {
		int hash = 0;
		for (E elt : pair) {
			hash += elt.hashCode();
		}
		return hash;
	}
	
	/***
	 * String representation of this Pair.
	 * The String representation is in the format "( a b )".
	 * 
	 * @return String representation of this Pair
	 */
	@Override
	public String toString() {
//		String str = "(";
//		for (E elt : pair) {
//			str += elt;
//			str += ",";
//		}
//		return str.substring(0, str.length()-1)+")";
		String str = "";
		for (E elt : pair) {
			str += elt;
			str += "&";
		}
		return str.substring(0, str.length()-1);
		
	}

}
