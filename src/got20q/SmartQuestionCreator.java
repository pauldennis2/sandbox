/**
 * @author Paul Dennis (pd236m)
 * May 4, 2018
 */
package got20q;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmartQuestionCreator {
	
	CharacterDataReport report;
	Question question;
	int numCharacters;

	public SmartQuestionCreator (CharacterDataReport report) {
		this.report = report;
		numCharacters = report.characters.size();
	}
	
	public void createQuestion () {
		
	}
	
	private void hairColorCombos () {
		HairColor[] combo = new HairColor[2];
		double bestCombo = 0.0;
		Arrays.stream(HairColor.values()).forEach(hairColor -> {
			List<HairColor> otherColors = Arrays.asList(HairColor.values());
			otherColors.remove(hairColor);
			
			otherColors.forEach(otherColor -> {
				int total = report.hairMap.get(hairColor) + report.hairMap.get(otherColor);
				double percent = (double) total / (double) numCharacters;
				//If this combo is closer to 50% than any previous
				if (Math.abs(percent - 0.5) < Math.abs(bestCombo - 0.5)) {
					combo[0] = hairColor;
					combo[1] = otherColor;
				}
			});
		});
	}
}
