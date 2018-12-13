/**
 * @author Paul Dennis (pd236m)
 * May 3, 2018
 */
package got20q;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Question {
	
	private static List<Question> standardQuestions;
	
	String text;
	Predicate<? super Character> filter;
	String yesResponse;
	String noResponse;
	
	Location location;
	HairColor hairColor;
	House house;

	public Question(String text, Predicate<? super Character> filter, String yesResponse, String noResponse) {
		super();
		this.text = text;
		this.filter = filter;
		this.yesResponse = yesResponse;
		this.noResponse = noResponse;
	}
	
	public static List<Question> getStandardQuestions() {
		if (standardQuestions == null) {
			buildStandardQuestions();
		}
		return standardQuestions;
	}
	
	private static void buildStandardQuestions () {
		standardQuestions = new ArrayList<>();
		standardQuestions.add(new Question("Is the character alive?", (c) -> c.isAlive, "I see - the character is alive.", "OK - they are dead."));
		standardQuestions.add(new Question("Is the character male?", (c) -> c.isMale, "OK, he's male.", "I see - a woman or girl."));
		standardQuestions.add(new Question("Is the character a Westerosi noble?", (c) -> c.isNoble, "A noble - lucky them.", "A commoner - sucks to be them."));
	}
	
	@Override
	public String toString () {
		StringBuilder builder = new StringBuilder();
		builder.append("\nQuestion Text: " + text);
		builder.append("\nYes Response: " + yesResponse);
		builder.append("\nNo Response: " + noResponse);
		return builder.toString();
	}
	
	public static void main(String[] args) {
		buildStandardQuestions();
		System.out.println(standardQuestions.get(0).toString());
	}

	public void setLocation(Location location) {
		this.location = location;
	}


	public void setHairColor(HairColor hairColor) {
		this.hairColor = hairColor;
	}

	public void setHouse(House house) {
		this.house = house;
	}
	
	
	
	
}