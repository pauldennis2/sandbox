/**
 * @author Paul Dennis (pd236m)
 * May 9, 2018
 */
package sts_heuristics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class AdaptiveStrategy extends StrategyBase implements Tweakable {

	//A Map between Conditions (which make dynamic decisions based on game state)
	//and List<String> representing how we will change our preferences if the condition is true
	//private Map<Conditional, List<String>> conditionsAndResultsMap;	
	
	private transient Hero hero;
	private transient AdaptiveStrategy old;
	
	private double improvement;
	private boolean isDiff = false;
	private String name;
	
	private String dadsName;
	private String momsName;
	
	//List of 100 3-6 letter words that can be combined to make strategy names.
	//Like "peruse-dragon98". 1,000,000 possibilities
	public final static List<String> NAME_FRAGMENTS = Arrays.asList("fierce", "strong", "silly", "react", "strike", "deny",
			"speak", "adapt", "seek", "frothy", "shake", "stir", "heal", "stompy", "newly", "prime", "singe", "sear",
			"strife", "happy", "kooky", "hobbit", "finish", "demand", "oblong", "peruse", "rogue",
			"copy", "joyful", "white", "green", "guard", "dragon", "delete", "verify", "curse", "grace",
			"arch", "plated", "smash", "crush", "death", "honor", "scar", "ranger", "sharp", "evade",
			"river", "brook", "lava", "shield", "sword", "cube", "galaxy", "moon", "star", "burst", "snow",
			"sphere", "spoon", "fork", "dirk", "salad", "magic", "cloak", "face", "pasta", "tiger", "kitten",
			"ninja", "pirate", "demon", "card", "hands", "goop", "war", "peace", "mage", "knight", "druid",
			"egg", "kernel", "panic", "feast", "turtle", "tale", "unwind", "relax", "spear", "relic", "shadow", "genie",
			"titan", "naga", "imp", "angel", "toad", "sage", "short", "flash");
	
	public final static List<String> HLPS = Arrays.asList("addCard", "upgradeCard", "removeCard", "maxHp");
	public final static List<String> BONUS_CHOICES = Arrays.asList("extraCard", "strongerBlock",
			"bulkHpIncrease", "perTurnHpIncrease");
	public final static List<String> CARDS = Card.ALL_CARD_NAMES;
	
	public static final String SUCCESSFUL_CONDITIONS_FILE = "data/conditions/successful_conditions.txt";
	private static Set<ConditionAndValues> successfulConditionsAndValues;
	
	private Map<String, Double> cardValues;
	private Map<String, Double> hlpValues;
	private String bonusChoice;
	private Map<Conditional, Map<String, Double>> conditionsAndValuesMap;
	
	private final List<Integer> attainment;
	
	//For the difference function
	private AdaptiveStrategy () {
		isDiff = true;
		name = "diff";
		attainment = Collections.synchronizedList(new LinkedList<>());
	}
	
	//Create a new random strategy
	public AdaptiveStrategy (Hero hero) {
		this.hero = hero;
		name = getNewName();
		Random r = new Random();
		
		cardValues = new RoundedDoubleMap();
		hlpValues = new RoundedDoubleMap();
		bonusChoice = BONUS_CHOICES.get(r.nextInt(BONUS_CHOICES.size() - 1));
		CARDS.forEach(cardName -> cardValues.put(cardName, r.nextDouble()));
		HLPS.forEach(hlpref -> hlpValues.put(hlpref, r.nextDouble()));
		
		conditionsAndValuesMap = new HashMap<>();

		//Create 0-2 high level pref conditions and 0-2 card pref conditions
		int conditionsCount = 0;
		while (r.nextDouble() > 0.25) {
			if (r.nextBoolean()) { //Create a new condition/values pair
				Map<String, Double> values = getRandomValues(HLPS, CARDS);
				
				if (r.nextBoolean() || r.nextBoolean()) {
					conditionsAndValuesMap.put(SingleCondition.getRandomCondition(), values);
				} else {
					conditionsAndValuesMap.put(new CompositeCondition(), values);
				}
			} else { //Use a previously successful condition
				if (successfulConditionsAndValues == null) {
					successfulConditionsAndValues = readSuccessfulConditionsFromFile();
				}
				ConditionAndValues cv = successfulConditionsAndValues.stream().collect(Collectors.toList())
						.get(r.nextInt(successfulConditionsAndValues.size() - 1));
				Conditional condition = cv.getCondition();
				Map<String, Double> values;
				if (r.nextBoolean()) { //Keep the values from previously successful condition
					values = cv.getValues();
				} else { //Generate new random values
					values = getRandomValues(HLPS, CARDS);
				}
				conditionsAndValuesMap.put(condition, values);
			}
			
			conditionsCount++;
			if (conditionsCount >= 4) {
				break;
			}
		}
		validate();
		
		attainment = Collections.synchronizedList(new LinkedList<>());
	}
	
	//Sometimes when a mommy strategy and a daddy strategy love each other VERY much...
	public AdaptiveStrategy (AdaptiveStrategy dad, AdaptiveStrategy mom) {
		attainment = Collections.synchronizedList(new LinkedList<>());
		this.dadsName = dad.name;
		this.momsName = mom.name;
		this.name = get2ParentChildName(dadsName, momsName);
		Random r = new Random();
		conditionsAndValuesMap = new HashMap<>();

		hlpValues = new RoundedDoubleMap();
		cardValues = new RoundedDoubleMap();
		
		if (r.nextBoolean()) {
			bonusChoice = dad.getBonusChoice();
		} else {
			bonusChoice = mom.getBonusChoice();
		}
		
		HLPS.forEach(hlp -> {
			if (r.nextBoolean()) { //Use dad's
				hlpValues.put(hlp, dad.hlpValues.get(hlp));
			} else { //Use mom's
				hlpValues.put(hlp, mom.hlpValues.get(hlp));
			}
		});
		
		CARDS.forEach(card -> {
			if (r.nextBoolean()) { //Use dad's
				cardValues.put(card, dad.cardValues.get(card));
			} else { //Use mom's
				cardValues.put(card, mom.cardValues.get(card));
			}
		});
		
		/*Keep 75% (on average) of each parent's conditions, then discard 25% of total
		 * 0.75 + 0.75 = 1.5,    1.5 * 0.75 = 1.125
		 * This means a 12% average increase in conditions, which is about right
		 * (We want child strategies to generally move toward being more complex)
		 */

		Map<Conditional, Map<String, Double>>  bothParentMaps = new HashMap<>(dad.conditionsAndValuesMap);
		bothParentMaps.putAll(mom.conditionsAndValuesMap);
		bothParentMaps.keySet().stream()
			.filter(condition -> r.nextDouble() > 0.4375) //This is equivalent to two 25% chances to be thrown out
			.forEach(condition -> this.conditionsAndValuesMap.put(condition, bothParentMaps.get(condition)));
		
		validate();
	}
	
	public AdaptiveStrategy (String fileString) {
		attainment = Collections.synchronizedList(new LinkedList<>());
		String[] tokens = fileString.split(":");
		name = tokens[0];
		if (name.length() > 64) {
			name = getShorterName(name);
		}
		averageLevelAttained = Double.parseDouble(tokens[1]);
		//Remove any whitespace or brackets []
		String hlpString = tokens[2].replaceAll("[\\{\\} ]", "");
		String cardPrefString = tokens[3].replaceAll("[\\{\\} ]", "");
		bonusChoice = tokens[4];
		conditionsAndValuesMap = new HashMap<>();
		
		hlpValues = new RoundedDoubleMap();
		cardValues = new RoundedDoubleMap();
		String[] hlpValueArray = hlpString.split(",");
		String[] cardValueArray = cardPrefString.split(",");
		
		//TODO: remove inelegant duplicated code
		for (String hlp : hlpValueArray) {
			String[] split = hlp.split("=");
			hlpValues.put(split[0], Double.parseDouble(split[1]));
		}
		for (String card : cardValueArray) {
			String[] split = card.split("=");
			cardValues.put(split[0], Double.parseDouble(split[1]));
		}
		
		if (tokens.length >= 6) {
			String conditionsMap = tokens[5];
			String[] conditionsStrings = conditionsMap.split(";");
			
			for (String conditionString : conditionsStrings) {
				String[] mapTokens = conditionString.split("-");
				Conditional condition;
				if (conditionString.contains("&&") || conditionString.contains("||")) {
					condition = new CompositeCondition(mapTokens[0].trim());
				} else {
					condition = new SingleCondition(mapTokens[0].trim());
				}
				//List<String> results = new ArrayList<>();
				Map<String, Double> values = new RoundedDoubleMap();
				String[] resultsTokens = mapTokens[1].split(",");
				for (String resultsToken : resultsTokens) {
					resultsToken = resultsToken.replaceAll("[\\{\\} ]", "");
					String[] split = resultsToken.split("=");
					values.put(split[0], Double.parseDouble(split[1]));
				}
				conditionsAndValuesMap.put(condition, values);
			}
		}
		validate();
	}
	
	private static String getShorterName (String name) {
		Random r = new Random();
		name = name.substring(0, 10) + "_" + NAME_FRAGMENTS.get(r.nextInt(NAME_FRAGMENTS.size() - 1));
		return name;
	}
	
	public AdaptiveStrategy getCopy () {
		AdaptiveStrategy copy = new AdaptiveStrategy();
		copy.cardValues = new RoundedDoubleMap(this.cardValues);
		copy.hlpValues = new RoundedDoubleMap(this.hlpValues);
		copy.isDiff = false;
		copy.conditionsAndValuesMap = new HashMap<>(this.conditionsAndValuesMap);
		copy.name = this.name;
		copy.bonusChoice = this.bonusChoice;
		copy.averageLevelAttained = this.averageLevelAttained;
		copy.validate();
		return copy;
	}
	
	@Override
	public AdaptiveStrategy tweak () {
		AdaptiveStrategy newStrat = new AdaptiveStrategy();
		newStrat.isDiff = false;
		newStrat.conditionsAndValuesMap = new HashMap<>(this.conditionsAndValuesMap);
		newStrat.hlpValues = new RoundedDoubleMap(this.hlpValues);
		newStrat.cardValues = new RoundedDoubleMap(this.cardValues);
		newStrat.name = getChildName(this.name);
		newStrat.bonusChoice = this.bonusChoice;
		Random r = new Random();
		
		//Half of the time we'll change a condition
		//Other half we'll change basic preferences
		int numConditions = conditionsAndValuesMap.size();
		if (r.nextBoolean() && numConditions > 0) {//Change conditions
			//Half of the time we will tweak an existing condition
			//Other half we will add or remove a condition
			if (r.nextBoolean()) { //Tweak a condition
				List<Conditional> conditions = conditionsAndValuesMap.keySet().stream().collect(Collectors.toList());
				Conditional toTweak = conditions.get(r.nextInt(conditions.size()));
				Map<String, Double> valuesToTweak = conditionsAndValuesMap.get(toTweak);
				if (r.nextBoolean()) {//By keeping preferences the same but re-rolling the condition itself
					Conditional tweaked = toTweak.tweak();
					newStrat.conditionsAndValuesMap.remove(toTweak);
					newStrat.conditionsAndValuesMap.put(tweaked, valuesToTweak);
				} else {//By changing preferences but leaving the condition itself unchanged
					//Building it into an OptionValue list sidesteps the ConcurrencyModification issue
					//we'd see otherwise.
					List<OptionValue> optionValues = new ArrayList<>();
					valuesToTweak.keySet().stream()
						.filter(prefName -> r.nextBoolean()) //Randomly toss out ~half
						.forEach(prefName -> optionValues.add(new OptionValue(prefName, valuesToTweak.get(prefName))));
					for (OptionValue optionValue : optionValues) {
						valuesToTweak.put(optionValue.getOption(), r.nextDouble());
					}
					newStrat.conditionsAndValuesMap.put(toTweak, valuesToTweak);
				}
			} else { //Add/remove a condition
				if (successfulConditionsAndValues == null) {
					successfulConditionsAndValues = readSuccessfulConditionsFromFile();
				}
				if (r.nextBoolean() || numConditions < 2) { //Add a condition
					if (r.nextBoolean() || successfulConditionsAndValues.size() == 0) { //Add a totally new condition
						Conditional condition = SingleCondition.getRandomCondition();
						Map<String, Double> values = getRandomValues(HLPS, CARDS);
						newStrat.conditionsAndValuesMap.put(condition, values);
					} else { //Add a previously successful condition
						ConditionAndValues cv = successfulConditionsAndValues.stream().collect(Collectors.toList())
								.get(r.nextInt(successfulConditionsAndValues.size() - 1));
						Conditional condition = cv.getCondition();
						Map<String, Double> values;
						if (r.nextBoolean()) { //Keep the values from previously successful condition
							values = cv.getValues();
						} else { //Generate new random values
							values = getRandomValues(HLPS, CARDS);
						}
						newStrat.conditionsAndValuesMap.put(condition, values);
					}
				} else if (numConditions >= 2) { //Remove a condition
					Conditional cond = conditionsAndValuesMap.keySet().stream().collect(Collectors.toList()).get(0);
					newStrat.conditionsAndValuesMap.remove(cond);
				}
			}
		} else {//Change base preferences
			if (r.nextBoolean()) {//By changing high level prefs
				Map<String, Double> newValues = getRandomValues(HLPS);
				newValues.keySet().forEach(hlpName -> hlpValues.put(hlpName, newValues.get(hlpName)));
			} else { //By changing card prefs
				Map<String, Double> newValues = getRandomValues(CARDS);
				newValues.keySet().forEach(cardName -> cardValues.put(cardName, newValues.get(cardName)));
			}
		}
		if (r.nextDouble() > 0.75) {
			bonusChoice = BONUS_CHOICES.get(r.nextInt(BONUS_CHOICES.size()));
		}
		if (newStrat.equals(this)) {
			return this.tweak();
		}
		newStrat.validate();
		
		return newStrat;
	}
	
	public static void addSuccessfulConditionsValues (Set<ConditionAndValues> successful) {
		if (successfulConditionsAndValues == null) {
			successfulConditionsAndValues = readSuccessfulConditionsFromFile(SUCCESSFUL_CONDITIONS_FILE);
		}
		successfulConditionsAndValues.addAll(successful);
		
		writeSuccessfulConditionsToFile();
	}
	
	private static Set<ConditionAndValues> readSuccessfulConditionsFromFile () {
		return readSuccessfulConditionsFromFile(SUCCESSFUL_CONDITIONS_FILE);
	}
	
	private static Set<ConditionAndValues> readSuccessfulConditionsFromFile (String fileName) {
		Set<ConditionAndValues> successful = new HashSet<>();
		try (Scanner scanner = new Scanner(new File(fileName))) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] tokens = line.split("-");
				String conditionString = tokens[0];
				Conditional condition;
				if (conditionString.contains("&&") || conditionString.contains("||")) {
					condition = new CompositeCondition(conditionString);
				} else {
					condition = new SingleCondition(conditionString);
				}
				Map<String, Double> values = new RoundedDoubleMap();
				String[] resultsTokens = tokens[1].split(",");
				for (String resultsToken : resultsTokens) {
					resultsToken = resultsToken.replaceAll("[\\{\\} ]", "");
					String[] split = resultsToken.split("=");
					values.put(split[0], Double.parseDouble(split[1]));
				}
				successful.add(new ConditionAndValues(condition, values));
			}
		} catch (FileNotFoundException ex) {
			System.err.println("Problem reading from file: " + fileName);
			ex.printStackTrace();
		} catch (Exception ex) {
			System.err.println("Some kind of error with file format probably. See below.");
			ex.printStackTrace();
		}
		return successful;
	}
	
	private static void writeSuccessfulConditionsToFile () {
		try (FileWriter fileWriter = new FileWriter(new File(SUCCESSFUL_CONDITIONS_FILE))) {
			for (ConditionAndValues condition : successfulConditionsAndValues) {
				fileWriter.write(condition.toString());
				fileWriter.write("\n");
			}
		} catch (IOException ex) {
			System.err.println("Problem writing to file: " + SUCCESSFUL_CONDITIONS_FILE);
			ex.printStackTrace();
		}
	}

	
	public synchronized void addAttainment (int level) {
		if (GameRunner.OUTPUT_LEVEL > 1) {
			System.out.println(this.name + " attained " + level + ".");
		}
		attainment.add(level);
	}
	
	/* Helper method. Given a list, will return a map with at least one
	 * of those values mapped to a random Double
	 */
	private static Map<String, Double> getRandomValues (List<String> inputList) {
		Random r = new Random();
		Map<String, Double> values = new RoundedDoubleMap();
		do {
			String prefName = inputList.get(r.nextInt(inputList.size() - 1));
			values.put(prefName, r.nextDouble());
		} while (r.nextBoolean());
		return values;
	}
	
	private static Map<String, Double> getRandomValues (List<String> firstInputList, List<String> secondInputList) {
		Random r = new Random();
		Map<String, Double> values = new RoundedDoubleMap();
		do {
			String prefName;
			if (r.nextBoolean()) {
				prefName = firstInputList.get(r.nextInt(firstInputList.size() - 1));
			} else {
				prefName = secondInputList.get(r.nextInt(secondInputList.size() - 1));
			}
			values.put(prefName, r.nextDouble());
		} while (r.nextBoolean());
		return values;
	}
	
	//Defines the file format
	public String getFileString () {
		StringBuilder response = new StringBuilder();
		response.append(name + ":" + averageLevelAttained + ":");
		response.append(hlpValues + ":" + cardValues + ":");
		response.append(bonusChoice + ":");
		boolean first = true;
		for (Conditional condition : conditionsAndValuesMap.keySet()) {
			if (first) {
				first = false;
			} else {
				response.append("; ");
			}
			response.append(condition.toString() + "-" + conditionsAndValuesMap.get(condition).toString());
		}
		return response.toString();
	}
	
	private void validate () {
		if ((!hlpValues.keySet().containsAll(HLPS)) || !cardValues.keySet().containsAll(CARDS)) {
			throw new AssertionError("Missing card or prefs (bad list)");
		}
		if (hlpValues.size() != HLPS.size() || cardValues.size() != CARDS.size()) {
			throw new AssertionError("Length mismatch.");
		}
		if (name == null || name.length() == 0) {
			throw new AssertionError("Missing name.");
		}
		if (!BONUS_CHOICES.contains(bonusChoice)) {
			throw new AssertionError("Bad/missing bonus choice.");
		}
	}
	
	public static void testWriteToFile () {
		List<AdaptiveStrategy> strategies = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			strategies.add(new AdaptiveStrategy(new Hero()));
		}
		writeStrategiesToFile(strategies, "data/strategies.txt");
		
		List<AdaptiveStrategy> rebuilt = readStrategiesFromFile("data/strategies.txt");
		
		System.out.println("Equal = " + rebuilt.equals(strategies));
	}
	
	public static List<AdaptiveStrategy> readStrategiesFromFile (String fileName) {
		List<AdaptiveStrategy> strategies = new ArrayList<>();
		try (Scanner fileScanner = new Scanner(new File(fileName))) {
			while (fileScanner.hasNextLine()) {
				strategies.add(new AdaptiveStrategy(fileScanner.nextLine()));
			}
		} catch (FileNotFoundException ex) {
			System.err.println("!!Could not read from file with name: " + fileName);
		}
		return strategies;
	}
	
	public static void writeStrategiesToFile (List<AdaptiveStrategy> strategies, String fileName) {
		try (FileWriter writer = new FileWriter(new File(fileName))) {
			for (AdaptiveStrategy strategy : strategies) {
				writer.write(strategy.getFileString());
				writer.write("\n");
			}
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new AssertionError("Couldn't write to file: " + fileName);
		}
	}
	
	public static String getParentName (String childName) {
		String[] words = childName.split("_");
		if (words.length < 2) {
			return "NO PARENT";
		}
		String response = "";
		for (int i = 0; i < words.length - 1; i++) {
			response += words[i];
		}
		return response;
	}
	
	private static String getNewName () {
		String name = "";
		Random r = new Random();
		int index = r.nextInt(NAME_FRAGMENTS.size());
		int otherIndex = r.nextInt(NAME_FRAGMENTS.size());
		if (otherIndex == index) { //If it lands on the same index three times oh well
			otherIndex = r.nextInt(NAME_FRAGMENTS.size());
		}
		name += NAME_FRAGMENTS.get(index);
		name += "-";
		name += NAME_FRAGMENTS.get(otherIndex);
		name += r.nextInt(10);
		name += r.nextInt(10);
		
		if (name.startsWith("joyful-dragon")) { //<-Reserved for The Creator
			return getNewName();
		}
		
		return name;
	}
	
	private static String get2ParentChildName (String dadsName, String momsName) {
		String dadsDigits = dadsName.replaceAll("[^\\d]", "");
		String momsDigits = momsName.replaceAll("[^\\d]", "");
		
		if (dadsDigits.length() > 5) {
			dadsDigits = dadsDigits.substring(0, 4);
		}
		if (momsDigits.length() > 5) {
			momsDigits = momsDigits.substring(0, 4);
		}
		
		String dadsFirst = dadsName.split("-")[0];
		String momsFirst = momsName.split("-")[0];
		
		String response = dadsFirst + dadsDigits + "-" + momsFirst + momsDigits + "_";
		Random r = new Random();
		response += NAME_FRAGMENTS.get(r.nextInt(NAME_FRAGMENTS.size()));
		response += r.nextInt(10);
		return response;
	}
	
	private static String getChildName (String parentName) {
		Random r = new Random();
		String childName = parentName;
		childName += "_" + NAME_FRAGMENTS.get(r.nextInt(NAME_FRAGMENTS.size()));
		childName += r.nextInt(10);
		return childName;
	}
	
	public static void main(String[] args) {
		testReadWriteSuccessful();
	}
	
	public static void testReadWriteSuccessful () {
		successfulConditionsAndValues = readSuccessfulConditionsFromFile(SUCCESSFUL_CONDITIONS_FILE);
		successfulConditionsAndValues.forEach(System.out::println);
		
		for (int i = 0; i < 3; i++) {
			SingleCondition condition = SingleCondition.getRandomCondition();
			Map<String, Double> values = getRandomValues(HLPS);
			ConditionAndValues thing = new ConditionAndValues(condition, values);
			successfulConditionsAndValues.add(thing);
			System.out.println(thing);
		}
		
		writeSuccessfulConditionsToFile();
		
		successfulConditionsAndValues = readSuccessfulConditionsFromFile(SUCCESSFUL_CONDITIONS_FILE);
		successfulConditionsAndValues.forEach(System.out::println);
	}
	
	public static void someTests () {
		//test2ParentNameGeneration();
		for (int i = 0; i < 5; i++) {
			AdaptiveStrategy strat = new AdaptiveStrategy(new Hero());
			String fileString = strat.getFileString();
			System.out.println(fileString);
			
			AdaptiveStrategy rebuilt = new AdaptiveStrategy(fileString);
			if (!rebuilt.equals(strat)) {
				System.err.println("Not equal!");
				System.err.println("rebuilt = \n" + rebuilt.getFileString());
			}
			System.out.println("-------");
		}
		testWriteToFile();
	}
	
	public static void test2ParentNameGeneration () {
		String dadsName = "pasta-snow50_guard9_frothy4_shield3_pirate5_relic0";
		String momsName = "unwind-mage54_pirate8_frothy2_turtle0_grace9_evade6_panic5";
		String childsName = get2ParentChildName(dadsName, momsName);
		System.out.println(childsName);
		if (!childsName.startsWith("pasta5094350-unwind54820965_")) {
			throw new AssertionError("Name generation error.");
		}
	}
	
	public static void testNameGeneration() {
		for (int i = 0; i < 20; i++) {
			String name = getNewName();
			System.out.print(name);
			String childName = getChildName(name);
			System.out.println("\t\t" + childName);
		}
		System.out.println("Possible names = " + NAME_FRAGMENTS.size() * NAME_FRAGMENTS.size() * 100);
		String child = "spoon-death72_stir0";
		System.out.println(child + " is a child of " + getParentName(child));
		System.out.println(getChildName(child) + " is a child of " + child);
		
		System.out.println("get parent name of peruse-dragon98: " + getParentName("peruse-dragon98"));
		System.out.println("\n\n");
		
		List<AdaptiveStrategy> strategies = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			strategies.add(new AdaptiveStrategy(new Hero()));
		}
		StrategyGroupReport report = analyzeStrategies(strategies);
		System.out.println(report);
	}

	@Override 
	public List<String> getHighLevelPrefs () {
		return getPreferences(true);
	}
	
	@Override 
	public List<String> getCardPrefs () {
		return getPreferences(false);
	}
	
	protected List<String> getPreferences (boolean highLevel) {
		DeckReport report = new DeckReport(hero);
		List<Conditional> liveConditions = conditionsAndValuesMap.keySet().stream()
			.filter(condition -> condition.evaluate(report))
			.collect(Collectors.toList());
		Map<String, Double> tempValues;
		if (liveConditions.size() >= 1) {
			//Sort in ascending order of priority
			Collections.sort(liveConditions);
			if (ClimbingGame.OUTPUT_LEVEL >= 3) {
				System.out.println("\tAll live conditions:");
				liveConditions.forEach(condition -> System.out.println("\t\t" + condition));
			}
			tempValues = new RoundedDoubleMap();
			Map<String, Double> optionValues;
			if (highLevel) {
				optionValues = hlpValues;
			} else {
				optionValues = cardValues;
			}
			//Copy over the values
			optionValues.keySet().forEach(optionName -> tempValues.put(optionName, optionValues.get(optionName)));
			//Edit them based on conditions
			for (Conditional condition : liveConditions) {
				Map<String, Double> overwriteValues = conditionsAndValuesMap.get(condition);
				for (String optionName : overwriteValues.keySet()) {
					//Replace IFF it's a HLP and we care about HLPS or it's a Card and we care about cards
					if ((highLevel && HLPS.contains(optionName)) || (!highLevel && CARDS.contains(optionName))){
						tempValues.put(optionName, overwriteValues.get(optionName));
					}
				}
			}
			
		} else {
			//If there were no active rules, use simpler logic
			if (highLevel) {
				tempValues = hlpValues;
			} else {
				tempValues = cardValues;
			}
		}
		
		List<OptionValue> listToSort = new ArrayList<>();
		for (String optionName : tempValues.keySet()) {
			listToSort.add(new OptionValue(optionName, tempValues.get(optionName)));
		}
		Collections.sort(listToSort);
		return listToSort.stream().map(OptionValue::getOption).collect(Collectors.toList());
	}
	
	@Override
	public String toString () {
		if (!isDiff) {
			String response = "";
			response += "High Level Values: " + hlpValues;
			response += "\nCard Values: " + cardValues;
			response += "\nBonus Choice: " + bonusChoice;
			response += "\nConditions:";
			for (Conditional condition : conditionsAndValuesMap.keySet()) {
				response += "\n\t" + condition + " - Values: " + conditionsAndValuesMap.get(condition);
			}
			if (averageLevelAttained != 0.0) {
				response += "\nAverage Attainment: " + averageLevelAttained;
			}
			return response;
		} else {
			return diffToString();
		}
	}
	
	private String diffToString () {
		if (!isDiff) {
			throw new AssertionError("Bad method call");
		}
		String response = "Difference:";
		if (hlpValues != null) {
			response += "\nHigh Level Values: " + hlpValues;
		}
		if (cardValues != null) {
			response += "\nCard Values: " + cardValues;
		}
		if (conditionsAndValuesMap != null) {
			for (Conditional condition : conditionsAndValuesMap.keySet()) {
				response += "\n\t" + condition + " - Result: " + conditionsAndValuesMap.get(condition);
			}
		}
		return response;
	}
	
	public AdaptiveStrategy getDifferences () {
		return getDifferences(this, old);
	}
	
	//Does not compare the average level attained since this isn't part
	//of our definition of equality
	@Override
	public boolean equals (Object other) {
		if (other instanceof AdaptiveStrategy) {
			AdaptiveStrategy otherStrat = (AdaptiveStrategy) other;
			boolean mapSame = this.conditionsAndValuesMap.equals(otherStrat.conditionsAndValuesMap);
			boolean cpsSame = this.cardValues.equals(otherStrat.cardValues);
			boolean hlpSame = this.hlpValues.equals(otherStrat.hlpValues);
			boolean bcSame = this.bonusChoice.equals(otherStrat.bonusChoice);
			//System.out.println("mapSame = " + mapSame + ", cpsSame = " + cpsSame + ", hlpSame = " + hlpSame);
			return mapSame && cpsSame && hlpSame && bcSame;
		}
		return false;
	}
	
	/**
	 * Returns a partially empty AdaptiveStrategy representing any fields of strat1
	 * that are different from those of strat2. Only strategy differences are considered
	 * (not results like averageLevelAttained).
	 * @param strat1
	 * @param strat2
	 * @return
	 * 
	 * !Alert!:
	 * Order matters here a LOT. getDifferences(myStrat, otherStrat) and getDifferences(otherStrat, myStrat)
	 * will have different outputs.
	 */
	public static AdaptiveStrategy getDifferences (AdaptiveStrategy strat1, AdaptiveStrategy strat2) {
		AdaptiveStrategy diff = new AdaptiveStrategy();
		if (!strat1.hlpValues.equals(strat2.hlpValues)) {
			diff.hlpValues = new RoundedDoubleMap(strat1.hlpValues);
		}
		if (!strat1.cardValues.equals(strat2.cardValues)) {
			diff.cardValues = new RoundedDoubleMap(strat1.cardValues);
		}
		if (!strat1.conditionsAndValuesMap.equals(strat2.conditionsAndValuesMap)) {
			Map<Conditional, Map<String, Double>> diffMap = new HashMap<>();
			for (Conditional condition : strat1.conditionsAndValuesMap.keySet()) {
				//If strat2 doesn't have it or if it is different (same check works for both)
				if (!strat1.conditionsAndValuesMap.get(condition).equals(strat2.conditionsAndValuesMap.get(condition))) {
					diffMap.put(condition, strat1.conditionsAndValuesMap.get(condition));
				}
			}
			diff.conditionsAndValuesMap = diffMap;
		}
		return diff;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bonusChoice == null) ? 0 : bonusChoice.hashCode());
		result = prime * result + ((cardValues == null) ? 0 : cardValues.hashCode());
		result = prime * result + ((conditionsAndValuesMap == null) ? 0 : conditionsAndValuesMap.hashCode());
		result = prime * result + ((hlpValues == null) ? 0 : hlpValues.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	//All Strategies must be either diffs or NOT diffs (not a mix)
	public static StrategyGroupReport analyzeStrategies (List<AdaptiveStrategy> strategies) {
		/*
		Map<StringIndex, Integer> cardPrefOccurances = new HashMap<>();
		Map<StringIndex, Integer> hlpOccurances = new HashMap<>();
		boolean diffs = strategies.get(0).isDiff;
		for (AdaptiveStrategy strategy : strategies) {
			if (strategy.isDiff != diffs) {
				throw new AssertionError("Cannot accept mix of diffs and non-diffs.");
			}
			int index = 0;
			if (strategy.cardValues != null) {
				for (String card : strategy.cardValues.keySet()) {
					StringIndex si = new StringIndex(card, index);
					if (cardPrefOccurances.containsKey(si)) {
						cardPrefOccurances.put(si, cardPrefOccurances.get(si) + 1);
					} else {
						cardPrefOccurances.put(si, 1);
					}
					index++;
				}
			}
			index = 0;
			if (strategy.hlpValues != null) {
				for (String hlp : strategy.hlpValues.keySet()) {
					StringIndex si = new StringIndex(hlp, index);
					if (hlpOccurances.containsKey(si)) {
						hlpOccurances.put(si, hlpOccurances.get(si) + 1);
					} else {
						hlpOccurances.put(si, 1);
					}
					index++;
				}
			}
			if (strategy.conditionsAndValuesMap != null) {
				//TODO : some analysis of the conditions/results
			}
		}
		*/
		//TODO - fix
		throw new AssertionError("Fix the analysis after switching to values");
		//return new StrategyGroupReport(cardPrefOccurances, hlpOccurances);
	}
	
	public void setHero (Hero hero) {
		this.hero = hero;
	}
	
	public Map<Conditional, Map<String, Double>> getConditionsAndValuesMap() {
		return conditionsAndValuesMap;
	}

	public void setConditionsAndValuesMap(Map<Conditional, Map<String, Double>> conditionsAndValuesMap) {
		this.conditionsAndValuesMap = conditionsAndValuesMap;
	}

	public double getImprovement() {
		return improvement;
	}

	public void setImprovement(double improvement) {
		this.improvement = improvement;
	}

	public boolean isDiff() {
		return isDiff;
	}

	public void setDiff(boolean isDiff) {
		this.isDiff = isDiff;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName () {
		return name;
	}
	
	public Map<String, Double> getCardValues () {
		return cardValues;
	}
	
	public Map<String, Double> getHlpValues () {
		return hlpValues;
	}
	
	public String getBonusChoice () {
		return bonusChoice;
	}
	
	public List<Integer> getAttainment () {
		return attainment;
	}
	
	public Hero getHero () {
		return hero;
	}
}
