import java.util.Set;

public class SpreadsheetHelper {

	public static String prevValueIfIn(Set<Integer> taskNumbers, int highestPossibleTaskNum, int prevColNumber, boolean reverse) {
		String str = "";
		for (int i = 1; i <= highestPossibleTaskNum; i++) {
			if (!reverse) {
				if (taskNumbers.contains(i)) {
					str += "=OFFSET(INDIRECT(ADDRESS(ROW()," + prevColNumber + ")),0,-1)\n";
				} else {
					str += "X\n";
				}
			} else {//reverse 
				if (!taskNumbers.contains(i)) {
					str += "=OFFSET(INDIRECT(ADDRESS(ROW()," + prevColNumber + ")),0,-1)\n";
				} else {
					str += "X\n";
				}
			}
		}
		return str;
	}

//	public static String 
}
