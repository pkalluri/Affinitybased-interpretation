import java.util.ArrayList;
import java.util.List;

public class Description {
	private List<DescriptionUnit> descriptionUnits;
	
	public Description(List<DescriptionUnit> descriptionUnits) {
		this.descriptionUnits = descriptionUnits;
	}

	public Description(DescriptionUnit descriptionUnit) {
		descriptionUnits = new ArrayList<DescriptionUnit>();
		descriptionUnits.add(descriptionUnit);
	}

	public Description(String actor, String action, String actedUpon) {
		descriptionUnits = new ArrayList<DescriptionUnit>();
		descriptionUnits.add(new DescriptionUnit(actor, action, actedUpon));
	}

	public List<DescriptionUnit> getDecriptionUnits() {
		return descriptionUnits;
	}
	
	@Override
	public String toString () {
		return descriptionUnits.toString();
	}

	public int descriptionLength() {
		return this.descriptionUnits.size();
	}
	
}
