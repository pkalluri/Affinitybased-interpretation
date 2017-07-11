/***
 * When an observer encounters non-agents (empty or objects) in ActionEvent slots, it may try to 
 * fill the ActionEvents using an assumption that the non-agents are in fact emotional stand-ins
 * for recently observed agents. When the observer tries and fails to fill the ActionEvent, it will internally
 * throw an UnableToFillActionEventException.
 * @author pkalluri
 *
 */
public class UnableToFillActionEventException extends Exception {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = -4732681681128566650L;

}
