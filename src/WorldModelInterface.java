import java.util.List;

public interface WorldModelInterface {

	void update(DescriptionUnit descriptionUnit, ActionKnowledge actionKnowledge);

	double distanceFrom(WorldModelInterface premiseRelationshipInfo);

	double probabilityOf(DescriptionUnit validDescriptionUnit, ActionKnowledge actionKnowledge) throws Exception;

	void reviewBeliefs();

}
