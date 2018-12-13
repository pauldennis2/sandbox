/**
 * @author Paul Dennis (pd236m)
 * May 4, 2018
 */
package got20q;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterDataReport {
	
	int numCharacters;
	Map<Enum, Integer> hairMap;
	Map<Enum, Integer> houseMap;
	Map<Enum, Integer> originMap;
	int numAlive;
	int numMen;
	int numNobles;
	
	List<Character> characters;
	
	Map<Class, Map<Enum, Integer>> propertyMap;
	
	public CharacterDataReport (List<Character> characters) {
		this.characters = characters;
		numCharacters = characters.size();
		hairMap = new HashMap<>();
		houseMap = new HashMap<>();
		originMap = new HashMap<>();
		propertyMap = new HashMap<>();
		
		propertyMap.put(HairColor.class, hairMap);
		propertyMap.put(House.class, houseMap);
		propertyMap.put(Location.class, originMap);
		
		doAnalysis();
	}
	
	private void doAnalysis () {
		Arrays.stream(HairColor.values()).forEach(value -> hairMap.put(value, 0));
		Arrays.stream(House.values()).forEach(value -> houseMap.put(value, 0));
		Arrays.stream(Location.values()).forEach(value -> originMap.put(value, 0));
		//Count up the hair
		characters.stream()
			.forEach(c -> {
				hairMap.put(c.hairColor, hairMap.get(c.hairColor) + 1);
				houseMap.put(c.house, houseMap.get(c.house) + 1);
				originMap.put(c.origin, originMap.get(c.origin) + 1);
				if (c.isAlive) {
					numAlive++;
				}
				if (c.isMale) {
					numMen++;
				}
				if (c.isNoble) {
					numNobles++;
				}
			});
	}
	
	@Override
	public String toString () {
		StringBuilder builder = new StringBuilder();
		builder.append("\nAnalyzed " + numCharacters + " characters.");
		builder.append("\nNumber alive: " + numAlive);
		builder.append("\nNumber male: " + numMen);
		builder.append("\nNumber noble:" + numNobles);
		builder.append("\n=Hair color=");
		hairMap.keySet().forEach(hairColor -> builder.append("\n" + hairColor.toString() + " : " + hairMap.get(hairColor)));
		builder.append("\n=House=");
		houseMap.keySet().forEach(house -> builder.append("\n" + house.toString() + " : " + houseMap.get(house)));
		builder.append("\n=Original Location=");
		originMap.keySet().forEach(origin -> builder.append("\n" + origin.toString() + " : " + originMap.get(origin)));
		return builder.toString();
	}

}
