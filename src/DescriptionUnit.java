
public final class DescriptionUnit {
	public final String actor;
	public final String action;
	public final String actedUpon;
	
	public DescriptionUnit(String actor, String action, String actedUpon) {
		this.actor = actor;
		this.action = action;
		this.actedUpon = actedUpon;
	}
	
	public DescriptionUnit(String[] parts) throws Exception {
		if (parts.length != 3) {
			throw new IllegalArgumentException("DescriptionUnit requires an actor, an action, and an acted-upon agent, only.");
		} else {
			this.actor = parts[0];
			this.action = parts[1];
			this.actedUpon = parts[2];
		}
	}
	
	@Override
	public String toString() {
		return actor + " " + action + " " + actedUpon;
	}
	
//	public String getActor() {
//		return actor;
//	}
//	public void setActor(String actor) {
//		this.actor = actor;
//	}
//	public String getAction() {
//		return action;
//	}
//	public void setAction(String action) {
//		this.action = action;
//	}
//	public String getActedUpon() {
//		return actedUpon;
//	}
//	public void setActedUpon(String actedUpon) {
//		this.actedUpon = actedUpon;
//	}
}
