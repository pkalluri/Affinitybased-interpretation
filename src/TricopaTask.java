import java.util.List;

public class TricopaTask {
	private Description premise;
	private List<Description> possibleChoices;
	private Description correctChoice;
	
	public TricopaTask(Description premise, List<Description> possibleChoices, Description correctChoice) {
		this.premise = premise;
		this.possibleChoices = possibleChoices;
		this.correctChoice = correctChoice;
	}
	
	public TricopaTask(Description premise, List<Description> possibleChoices) {
		this.premise = premise;
		this.possibleChoices = possibleChoices;
		this.correctChoice = null;
	}
	

	public Description getPremise() {
		return premise;
	}

	public void setPremise(Description premise) {
		this.premise = premise;
	}

	public List<Description> getPossibleChoices() {
		return possibleChoices;
	}

	public void setPossibleChoices(List<Description> possibleChoices) {
		this.possibleChoices = possibleChoices;
	}

	public Description getCorrectChoice() {
		return correctChoice;
	}

	public void setCorrectChoice(Description correctChoice) {
		this.correctChoice = correctChoice;
	}
	
	@Override
	public String toString() {
		return premise.toString() + possibleChoices.toString();
	}

	

}
