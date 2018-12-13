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
import java.util.stream.Collectors;

import javax.sound.midi.Soundbank;

public class GenericDoubleAnalyzer<A extends Enum<A>, B extends Enum<B>> extends AbstractAnalyzer{
	
	CharacterDataReport report;
	A firstProp;
	B secondProp;
	
	static boolean debug = true;
	static boolean debug2 = true;
	
	public GenericDoubleAnalyzer (CharacterDataReport report) {
		this.report = report;
	}
	
	public void doAnalysis (Class<A> a, Class<B> b) {
		double bestComboPercent = 0.0;
		Set<A> aSet = EnumSet.allOf(a);
		Set<B> bSet = EnumSet.allOf(b);
		
		report.propertyMap.get(a);
		//"xPropertyMap" represents the relevant map from the report 
		//(so if we are talking about HairColor, the hairMap)
		Map<Enum, Integer> aPropertyMap = report.propertyMap.get(a);
		Map<Enum, Integer> bPropertyMap = report.propertyMap.get(b);
		//Very inefficient since we are doing all of these calculations twice
		//Once for A - B, once for B - A
		for (A first : aSet) {
			for (B second : bSet) {
				int total = aPropertyMap.get(first) + bPropertyMap.get(second);
				double percent = (double) total / (double) report.numCharacters;
				//If this combo is closer to 50% than any previous
				if (Math.abs(percent - 0.5) < Math.abs(bestComboPercent - 0.5)) {
					bestComboPercent = percent;
					firstProp = first;
					secondProp = second;
					if (debug2) {
						System.out.println("\t\tFirst prop = " + firstProp);
						System.out.println("\t\tSecond prop = " + secondProp);
						System.out.println("\t\tPercent = " + percent);
					}
				}
			}
		}
		percent = bestComboPercent;
		if (debug) {
			System.out.println("\t=====================");
			System.out.println("\tI analyzed " + firstProp.getClass().getSimpleName() + " and " + secondProp.getClass().getSimpleName() + ". Best combo:");
			System.out.println("\tFirst prop = " + firstProp);
			System.out.println("\tSecond prop = " + secondProp);
			System.out.println("\tPercent = " + percent);
		}
	}
	
	public static void main(String[] args) {
		List<Character> characters = Character.getCharacterList().stream()
				.filter(c -> c.isAlive && c.isMale && c.isNoble)
				.collect(Collectors.toList());
		CharacterDataReport report = new CharacterDataReport(characters);
		System.out.println(report);
		GenericDoubleAnalyzer<House, House> doubleAnalyzer = new GenericDoubleAnalyzer<>(report);
		doubleAnalyzer.doAnalysis(House.class, House.class);
	}

}
