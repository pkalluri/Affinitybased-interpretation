
public interface RelationshipInterface {

	public void update(ActionKnowledge actionKnowledge);

	public double distanceFrom(RelationshipInterface abstractRelationship);

	public void update(ActionKnowledge actionKnowledge, double emphasis);

	public double probabilityOf(DescriptionUnit descriptionUnit, ActionKnowledge actionKnowledge) throws Exception;

	public boolean hasOpinion();

}
