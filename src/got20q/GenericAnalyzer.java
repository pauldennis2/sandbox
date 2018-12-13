/**
 * @author Paul Dennis (pd236m)
 * May 4, 2018
 */
package got20q;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenericAnalyzer<E extends Enum<E>> extends AbstractAnalyzer {
	
	CharacterDataReport report;
	List<E> combo;
	
	public GenericAnalyzer (CharacterDataReport report) {
		this.report = report;
	}
	
	public void doAnalysis (Class<E> e) {
		double bestComboPercent = 0.0;
		Set<E> set = EnumSet.allOf(e);
		//Very inefficient since we are doing all of these calculations twice
		//Once for A - B, once for B - A
		for (E first : set) {
			Set<E> others = new HashSet<>(set);
			others.remove(first);
			
			for (E second : others) {
				//"thisPropertyMap" represents the relevant map from the report 
				//(so if we are talking about HairColor, the hairMap)
				Map<Enum, Integer> thisPropertyMap = report.propertyMap.get(first.getClass());
				int total = thisPropertyMap.get(first) + thisPropertyMap.get(second);
				double percent = (double) total / (double) report.numCharacters;
				//If this combo is closer to 50% than any previous
				if (Math.abs(percent - 0.5) < Math.abs(bestComboPercent - 0.5)) {
					bestComboPercent = percent;
					combo = new ArrayList<>();
					combo.add(first);
					combo.add(second);
				}
			}
		}
		percent = bestComboPercent;
		if (combo != null) {
			System.out.println("Best Combo found:");
			combo.forEach(System.out::println);
			System.out.println("Best Combo % = " + bestComboPercent);
		}
	}

}
