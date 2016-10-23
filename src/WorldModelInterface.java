import java.util.Map;

public interface WorldModelInterface {

	void update(DescriptionUnit descriptionUnit, ActionKnowledge actionKnowledge, boolean saveRecords, Map<Pair<String>, Map<Integer,Map<RelationshipType,Double>>> recordHolder);

	double distanceFrom(WorldModelInterface premiseRelationshipInfo);

	double probabilityOf(DescriptionUnit validDescriptionUnit, ActionKnowledge actionKnowledge) throws Exception;

	public void reviewBeliefs(boolean saveRecords, Map<Pair<String>, Map<Integer, Map<RelationshipType, Double>>> recordHolder);

}
