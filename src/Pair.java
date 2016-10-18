import java.util.HashSet;
import java.util.Set;

public class Pair<E>{
	private Set<E> pair;
	
	public Pair(E a, E b) {
		this.pair = new HashSet<E>();
		pair.add(a);
		pair.add(b);
	}
	
	@Override
	public boolean equals(Object other){
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
	
	@Override
	public int hashCode() {
		int hash = 0;
		for (E elt : pair) {
			hash += elt.hashCode();
		}
		return hash;
	}
	
	@Override
	public String toString() {
		String str = "( ";
		for (E elt : pair) {
			str += elt;
			str += " ";
		}
//		str = Integer.toString(hashCode());
		return str+")";
	}

}
