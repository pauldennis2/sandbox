/**
 * @author Paul Dennis (pd236m)
 * May 3, 2018
 */
package got20q;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class GameOfThrones20Questions {
	
	Scanner inputScanner;
	int numQuestionsAsked;
	List<Character> remainingCharacters;
	
	Character blank;
	
	boolean debug = true;
	
	public static void main(String[] args) {
		new GameOfThrones20Questions().playGame();
	}
	
	public void playGame () {
		
		//Standard questions
		List<Question> standardQuestions = Question.getStandardQuestions();
		while (standardQuestions.size() > 0) {
			Question q = standardQuestions.get(0);
			boolean success = askQuestion(q);
			if (success) {
				standardQuestions.remove(0);
			}
		}
		System.out.println("Reached the end of my standard questions. Hmmm....");
		
		/* Once down to 3 characters it's best to ask directly 
		 * Down to 3 characters

			Ask directly:
			33% 1 guess
			33% 2 guesses
			33% 3 guesses
			
			Do a 2-1:
			66% in the 2 group
				50% - 1 guess
				50% - 2 guesses
			
			33% in the 1 group
				100% - 1 guess
			
			2-1, simplified:
			33% 2 guesses
			33% 3 guesses
			33% 2 guesses
			*
			*(Depends on what you consider the rules of the game to be -
			* if you are down to one possibility does that count as a guess?
			* decided yes, you have to guess the character).
		 */
		while (remainingCharacters.size() > 3) {
			findBestDoubleComboQuestionAndAsk();
		}
		
		System.out.println("OK, no more intelligent questions I guess... time to get direct.");
		while (remainingCharacters.size() >= 1) {
			askDirectNameQuestion(remainingCharacters.get(0));
		}
		System.out.println("Well, I give up. Either you lied or this character isn't in my data files.");
	}
	
	static Map<String, String> propertyInfoMap;
	public static void provideInfoOnProperty (String property) {
		if (propertyInfoMap == null) {
			propertyInfoMap = new HashMap<>();
			propertyInfoMap.put("origin", "This represents the initial 'home' area - where we see the character initially." +
					"\nIt doesn't represent where they were born. Syrio Forel's origin is King's Landing, for example." +
					"\nBut even though we first see the Lannisters/Baratheons in the North, they are from King's Landing.");
			
			propertyInfoMap.put("hairColor", "This represents a character's hair color. In some cases, it's a bit ambiguous." +
					"\nBlond - characters with golden blond or white blond hair (Viserys, Cersei and kids)" +
					"\nGrey - characters whose hair is mostly grey or white from old age (Roose Bolton, Alliser Thorne)" +
					"\nDark - characters with dark brown or black hair (Tyrion, The Hound)" +
					"\nLight - characters with dark blond or light brown hair (Theon, Jorah)" +
					"\nBald - characters with shaved/bald head (Varys, Pyat Pree, Xaro - NOT Grey Worm)" + 
					"\nRed - characters with red hair (Ygritte, Tormund)");
			
			propertyInfoMap.put("noble", "Represents whether a character was BORN a Westerosi noble." +
					"\nDoes not include characters granted noble titles (like Bronn)." +
					"\nDoes include characters stripped of titles (Jorah).");
			
			propertyInfoMap.put("alive", "Represents whether a character is alive as of the latest episode." +
					"\nDoes include characters who are 'half-dead' like The Mountain and Benjen Stark.");
			
			propertyInfoMap.put("house", "Represents the noble House to which the character belongs (if any)." +
					"\nHouses with only 1 notable member are lumped into 'Other'.");
		}
		String response = propertyInfoMap.get(property);
		if (response != null) {
			System.out.println(response);
		} else {
			System.out.println("No info on property: " + property);
		}
	}
	
	public void findBestDoubleComboQuestionAndAsk () {
		List<GenericDoubleAnalyzer> analyzers = createAndPackAnalyzers(new CharacterDataReport(remainingCharacters));
		GenericDoubleAnalyzer best = findBestAnalyzerCombo(analyzers);
		Question q = buildQuestionFromGenericDoubleAnalyzer(best);
		askQuestion(q);
	}
	
	public Question buildQuestionFromGenericDoubleAnalyzer (GenericDoubleAnalyzer analyzer) {
		Enum firstProp = analyzer.firstProp;
		Enum secondProp = analyzer.secondProp;
		PrettyPrint pp1 = (PrettyPrint) firstProp;
		PrettyPrint pp2 = (PrettyPrint) secondProp;
		
		Map<Class, String> propertyNamesMap = new HashMap<>();
		propertyNamesMap.put(HairColor.class, "hairColor");
		propertyNamesMap.put(Location.class, "origin");
		propertyNamesMap.put(House.class, "house");
		
		String questionString = "Is the character " + pp1.getString();
		if (!firstProp.equals(secondProp)) {
			questionString += " or " + pp2.getString();
		}
		questionString += "?";
		
		Question question = new Question(questionString,
				(character) -> {
					try {
						Field field1 = Character.class.getField(propertyNamesMap.get(firstProp.getClass()));
						Field field2 = Character.class.getField(propertyNamesMap.get(secondProp.getClass()));
						Enum charsFirstProp = (Enum) field1.get(character);
						Enum charsSecondProp = (Enum) field2.get(character);
						return (firstProp == charsFirstProp || secondProp == charsSecondProp);
					} catch (Exception e) {
						throw new AssertionError(e);
					}
				},
				"OK - one of those things is true. Hmm.",
				"Alright, neither of those statements is true.");
		
		return question;
	}
	
	public List<GenericDoubleAnalyzer> createAndPackAnalyzers (CharacterDataReport report) {
		GenericDoubleAnalyzer<HairColor, HairColor> hairAnalyzer = new GenericDoubleAnalyzer<>(report);
		GenericDoubleAnalyzer<Location, Location> originAnalyzer = new GenericDoubleAnalyzer<>(report);
		GenericDoubleAnalyzer<House, House> houseAnalyzer = new GenericDoubleAnalyzer<>(report);
		
		GenericDoubleAnalyzer<HairColor, Location> hairOriginAnalyzer = new GenericDoubleAnalyzer<>(report);
		GenericDoubleAnalyzer<HairColor, House> hairHouseAnalyzer = new GenericDoubleAnalyzer<>(report);
		GenericDoubleAnalyzer<Location, House> locationHouseAnalyzer = new GenericDoubleAnalyzer<>(report);
		
		hairOriginAnalyzer.doAnalysis(HairColor.class, Location.class);
		hairHouseAnalyzer.doAnalysis(HairColor.class, House.class);
		locationHouseAnalyzer.doAnalysis(Location.class, House.class);
		
		hairAnalyzer.doAnalysis(HairColor.class, HairColor.class);
		originAnalyzer.doAnalysis(Location.class, Location.class);
		houseAnalyzer.doAnalysis(House.class, House.class);
		
		List<GenericDoubleAnalyzer> analyzers = new ArrayList<>();
		analyzers.add(hairAnalyzer);
		analyzers.add(originAnalyzer);
		analyzers.add(houseAnalyzer);
		analyzers.add(hairOriginAnalyzer);
		analyzers.add(hairHouseAnalyzer);
		analyzers.add(locationHouseAnalyzer);
		
		return analyzers;
	}
	
	public static GenericDoubleAnalyzer findBestAnalyzerCombo (List<GenericDoubleAnalyzer> analyzers) {
		int response = -1;
		double bestPercent = 0.0;
		int i = 0;
		for (GenericDoubleAnalyzer analyzer : analyzers) {
			double thisPct = analyzer.percent;
			if (Math.abs(thisPct - 0.5) < Math.abs(bestPercent - 0.5)) {
				response = i;
				bestPercent = thisPct;
			}
			i++;
		}
		return analyzers.get(response);
	}
	
	public void askDirectNameQuestion (Character character) {
		numQuestionsAsked++;
		System.out.println("Is it " + character.name + "?");
		String response = inputScanner.nextLine();
		if (response.toLowerCase().contains("y")) {
			System.out.println("Great! I guessed it in " + numQuestionsAsked + " guesses.");
			System.exit(0);
		} else {
			remainingCharacters.remove(character);
		}
	}
	
	//Response indicates whether question was successfully asked and answered
	public boolean askQuestion (Question question) {
		System.out.println(question.text);
		String response = inputScanner.nextLine();
		
		if (response.contains("?")) {
			response = response.replaceAll("\\?", "");
			System.out.println("Attempting to look up character " + response + ".");
			printCharacter(response);
			return false;
		} else if (response.contains(":")) {
			response = response.replaceAll(":", "");
			System.out.println("Attempting to look up property " + response + ".");
			provideInfoOnProperty(response);
			return false;
		} else {
			if (response.toLowerCase().contains("y")) {
				remainingCharacters = remainingCharacters.stream()
						.filter(question.filter)
						.collect(Collectors.toList());
				System.out.println(question.yesResponse);
			} else {
				remainingCharacters.removeIf(question.filter);
				System.out.println(question.noResponse);
			}
			System.out.println("Number of remaining characters = " + remainingCharacters.size());
			if (debug) {
				printRemainingCharacters();
			}
			numQuestionsAsked++;
			return true;
		}
	}
	
	public GameOfThrones20Questions () {
		inputScanner = new Scanner(System.in);
		blank = new Character();//Unused
		remainingCharacters = Character.getCharacterList();
		System.out.println("Welcome to Game of Thrones 20 Questions.");
		System.out.println("Think of a Game of Thrones character from the following list of " + remainingCharacters.size() + " characters:");
		int i = 0;
		for (Character c : remainingCharacters) {
			System.out.print(c.name + " | ");
			i++;
			if (i % 5 == 0) {
				System.out.println();
			}
		}
		System.out.println("\nI will try to guess who it is.");
		System.out.println("Any response containing a 'y' is read as yes (yeah, yes, y)");
		System.out.println("Any other response means no.");
		System.out.println("If you want to see a character's info, simply type their name with a question mark '?'");
		System.out.println("For example if you enter \"Theon?\" you would see:\n");
		printCharacter("Theon");
		System.out.println("You can also ask about properties by using a colon ':'.");
		System.out.println("For example, if you enter \":noble\" you would see:\n");
		provideInfoOnProperty("noble");
		System.out.println("\nThe system may ask a \"combined\" question using an or statement.");
		System.out.println("For example: Does the character have blond hair OR are they from Westeros?");
		System.out.println("You should answer yes if either or both are true.");
	}
	
	public void printCharacter(String firstName) {
		List<Character> characters = Character.getCharacterList();
		boolean match = false;
		for (Character c : characters) {
			if (c.name.startsWith(firstName)) {
				System.out.println(c);
				match = true;
			}
		}
		if (!match) {
			System.out.println("Could not find character named " + firstName);
		}
	}
	
	public void printRemainingCharacters() {
		remainingCharacters.stream()
			.map(character -> character.name.split(" ")[0])
			.forEach(firstName -> System.out.print(firstName + " "));
		System.out.println();
	}
}
