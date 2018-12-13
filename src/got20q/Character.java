/**
 * @author Paul Dennis (pd236m)
 * May 3, 2018
 */
package got20q;

import java.util.ArrayList;
import java.util.List;
import static got20q.HairColor.*;
import static got20q.House.*;
import static got20q.Location.*;

public class Character {
	
	private static List<Character> characterList;
	
	//Social information (house, noble status) is based on what we know about a character when we're first introduced
	//For example Jon is NOT considered noble, nor is Ramsay, but Jorah is. i.e. Were they BORN noble?
	String name;
	boolean isAlive; //Counts people who are questionably alive (Benjen Stark, The Mountain)
	boolean isMale; //Birth gender (Varys, Grey Worm are male)
	boolean isNoble; //Westerosi noble (not Khal Drogo, for example)
	List<String> allies; //NYI
	List<String> enemies; //NYI
	List<Integer> seasonsPresent; //NYI
	public HairColor hairColor;
	public House house; //House you married into (if any)
	public Location origin; //Where they start in the series
	Location current; //NYI
	
	//Blank constructor to build up our guess
	public Character () {
		name = "Unknown";
	}

	public Character(String name, boolean isAlive, boolean isMale, boolean isNoble, HairColor hairColor, House house,
			Location origin) {
		super();
		this.name = name;
		this.isAlive = isAlive;
		this.isMale = isMale;
		this.isNoble = isNoble;
		this.hairColor = hairColor;
		this.house = house;
		this.origin = origin;
		
		if ((isNoble && house == NOT_NOBLE) || (!isNoble && house != NOT_NOBLE)){
			throw new AssertionError("Data validation error.");
		}
	}
	
	public static List<Character> getCharacterList () {
		if (characterList == null) {
			buildCharacterList();
		}
		return characterList;
	}
	
	@Override
	public String toString () {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		if (isNoble) {
			builder.append(" of House " + house);
		}
		if (isMale) {
			builder.append(" (M)");
		} else {
			builder.append(" (F)");
		}
		if (isAlive) {
			builder.append("\nStatus: Still alive");
		} else {
			builder.append("\nStatus: Gone onto a better place.");
		}
		builder.append("\nHair: " + hairColor);
		builder.append("\nOriginal Location: " + origin);
		return builder.toString();
	}
	
	private static void buildCharacterList () {
		characterList = new ArrayList<>();
		//Boolean order of arguments:             Alive,   Male,  Noble
		//Starks
		characterList.add(new Character("Arya Stark", true, false, true, DARK, STARK, THE_NORTH));
		characterList.add(new Character("Sansa Stark", true, false, true, LIGHT, STARK, THE_NORTH));
		characterList.add(new Character("Eddard Stark", false, true, true, LIGHT, STARK, THE_NORTH));
		characterList.add(new Character("Rob Stark", false, true, true, DARK, STARK, THE_NORTH));
		characterList.add(new Character("Jon Snow", true, true, false, DARK, NOT_NOBLE, THE_NORTH));
		characterList.add(new Character("Catelyn Stark", false, false, true, DARK, STARK, THE_NORTH));
		characterList.add(new Character("Brandon Stark", true, true, true, DARK, STARK, THE_NORTH));
		characterList.add(new Character("Rickon Stark", false, true, true, DARK, STARK, THE_NORTH));
		characterList.add(new Character("Benjen Stark", true, true, true, DARK, STARK, WALL_AND_BEYOND));
		characterList.add(new Character("Hodor", false, true, false, DARK, NOT_NOBLE, THE_NORTH));
		
		//Lannisters
		characterList.add(new Character("Tywin Lannister", false, true, true, BLOND, LANNISTER, WESTEROS));
		characterList.add(new Character("Tyrion Lannister", true, true, true, DARK, LANNISTER, KINGS_LANDING));
		characterList.add(new Character("Jamie Lannister", true, true, true, BLOND, LANNISTER, KINGS_LANDING));
		characterList.add(new Character("Cersei Lannister", true, false, true, BLOND, LANNISTER, KINGS_LANDING));
		characterList.add(new Character("Lancel Lannister", false, true, true, DARK, LANNISTER, KINGS_LANDING));
		
		//Baratheons
		characterList.add(new Character("Stannis Baratheon", false, true, true, DARK, BARATHEON, WESTEROS));
		characterList.add(new Character("Renly Baratheon", false, true, true, DARK, BARATHEON, WESTEROS));
		characterList.add(new Character("Joffrey Baratheon", false, true, true, BLOND, BARATHEON, KINGS_LANDING));
		characterList.add(new Character("Robert Baratheon", false, true, true, DARK, BARATHEON, KINGS_LANDING));
		characterList.add(new Character("Tommen Baratheon", false, true, true, BLOND, BARATHEON, KINGS_LANDING));
		characterList.add(new Character("Shireen Baratheon", false, false, true, DARK, BARATHEON, KINGS_LANDING));
		characterList.add(new Character("Myrcella Baratheon", false, false, true, BLOND, BARATHEON, KINGS_LANDING));
		characterList.add(new Character("Davos Seaworth", true, true, false, DARK, NOT_NOBLE, WESTEROS));
		
		//Greyjoys
		characterList.add(new Character("Yara Greyjoy", true, false, true, LIGHT, GREYJOY, IRON_ISLANDS));
		characterList.add(new Character("Theon Greyjoy", true, true, true, LIGHT, GREYJOY, IRON_ISLANDS));
		characterList.add(new Character("Euron Greyjoy", true, true, true, DARK, GREYJOY, IRON_ISLANDS));
		characterList.add(new Character("Balon Greyjoy", false, true, true, DARK, GREYJOY, IRON_ISLANDS));
		
		//Misc
		characterList.add(new Character("Varys", true, true, false, BALD, NOT_NOBLE, KINGS_LANDING));
		characterList.add(new Character("Gendry", true, true, false, DARK, NOT_NOBLE, KINGS_LANDING));
		characterList.add(new Character("Petyr Baelish", false, true, true, DARK, OTHER, KINGS_LANDING));
		characterList.add(new Character("Syrio Forel", false, true, false, DARK, NOT_NOBLE, KINGS_LANDING));
		characterList.add(new Character("Melisandre", true, false, false, RED, NOT_NOBLE, WESTEROS));
		characterList.add(new Character("High Sparrow", false, true, false, GREY, NOT_NOBLE, KINGS_LANDING));
		characterList.add(new Character("Gregor Clegane", true, true, true, DARK, CLEGANE, KINGS_LANDING));
		characterList.add(new Character("Sandor Clegane", true, true, true, DARK, CLEGANE, KINGS_LANDING));
		
		//Khaleesi!
		characterList.add(new Character("Grey Worm", true, true, false, DARK, NOT_NOBLE, ESSOS));
		characterList.add(new Character("Missandei", true, false, false, DARK, NOT_NOBLE, ESSOS));
		characterList.add(new Character("Danaerys Targaryen", true, false, true, BLOND, TARGARYEN, ESSOS));
		characterList.add(new Character("Viserys Targaryen", false, true, true, BLOND, TARGARYEN, ESSOS));
		characterList.add(new Character("Khal Drogo", false, true, false, DARK, NOT_NOBLE, ESSOS));
		characterList.add(new Character("Jorah Mormont", true, true, true, LIGHT, MORMONT, ESSOS));
		characterList.add(new Character("Rhaegar Targaryen", false, true, true, LIGHT, TARGARYEN, WESTEROS));
		characterList.add(new Character("Aerys Targaryen", false, true, true, LIGHT, TARGARYEN, KINGS_LANDING));
		
		//The North
		characterList.add(new Character("Ygritte", false, false, false, RED, NOT_NOBLE, THE_NORTH));
		characterList.add(new Character("Tormund Giantsbane", true, true, false, RED, NOT_NOBLE, WALL_AND_BEYOND));
		characterList.add(new Character("Mance Rayder", false, true, false, DARK, NOT_NOBLE, WALL_AND_BEYOND));
		characterList.add(new Character("Jeor Mormont", false, true, true, GREY, MORMONT, WALL_AND_BEYOND));
		characterList.add(new Character("Craster", false, true, false, GREY, NOT_NOBLE, WALL_AND_BEYOND));
		characterList.add(new Character("Aemon Targaryen", false, true, true, GREY, TARGARYEN, WALL_AND_BEYOND));
		
		//Dorne
		characterList.add(new Character("Ellaria Sand", true, false, false, DARK, NOT_NOBLE, DORNE));
		characterList.add(new Character("Doran Martell", false, true, true, DARK, MARTELL, DORNE));
		characterList.add(new Character("Oberyn Martell", false, true, true, DARK, MARTELL, DORNE));
		characterList.add(new Character("Sand Snakes", true, false, false, DARK, NOT_NOBLE, DORNE));
		
		//Tyrell
		characterList.add(new Character("Loras Tyrell", false, true, true, DARK, TYRELL, WESTEROS));
		characterList.add(new Character("Olenna Tyrell", false, false, true, GREY, TYRELL, WESTEROS));
		characterList.add(new Character("Margaery Tyrell", false, false, true, DARK, TYRELL, WESTEROS));
		
		//More misc
		characterList.add(new Character("Thoros of Myr", true, true, false, DARK, NOT_NOBLE, WESTEROS));
		characterList.add(new Character("Hot Pie", true, true, false, DARK, NOT_NOBLE, WESTEROS));
		characterList.add(new Character("Walder Frey", false, true, true, GREY, FREY, WESTEROS));
		characterList.add(new Character("Roose Bolton", false, true, true, GREY, OTHER, THE_NORTH));
		characterList.add(new Character("Ramsay Snow", false, true, false, DARK, NOT_NOBLE, THE_NORTH));
		characterList.add(new Character("Samwell Tarly", true, true, true, DARK, OTHER, WALL_AND_BEYOND));
		characterList.add(new Character("Bronn", true, true, false, DARK, NOT_NOBLE, WESTEROS));//SP
		characterList.add(new Character("Edmure Tully", true, true, true, DARK, TULLY, WESTEROS));
		characterList.add(new Character("Meryn Trant", false, true, true, DARK, OTHER, KINGS_LANDING));
		characterList.add(new Character("Brienne of Tarth", true, false, true, BLOND, OTHER, WESTEROS));
		characterList.add(new Character("Jaqen H'ghar", true, true, false, DARK, NOT_NOBLE, ESSOS));
		characterList.add(new Character("Maester Pycelle", true, true, false, GREY, NOT_NOBLE, KINGS_LANDING));
		characterList.add(new Character("Qyburn", true, true, false, GREY, NOT_NOBLE, KINGS_LANDING));
		characterList.add(new Character("Three-Eyed Raven", false, true, false, GREY, NOT_NOBLE, WALL_AND_BEYOND));
		characterList.add(new Character("Jojen Reed", false, true, true, LIGHT, REED, WALL_AND_BEYOND));
		characterList.add(new Character("Meera Reed", true, false, true, DARK, REED, WALL_AND_BEYOND));
		characterList.add(new Character("Lyanna Mormont", true, false, true, DARK, MORMONT, THE_NORTH));

		characterList.add(new Character("Lysa Arryn", false, false, true, DARK, ARRYN, WESTEROS));
		characterList.add(new Character("Robin Arryn", true, true, true, DARK, ARRYN, WESTEROS));
		characterList.add(new Character("Jon Arryn", false, true, true, DARK, ARRYN, WESTEROS));
		
		characterList.add(new Character("Barristan Selmy", false, true, true, GREY, OTHER, WESTEROS));
		characterList.add(new Character("Grenn", true, true, false, DARK, NOT_NOBLE, WALL_AND_BEYOND));
		characterList.add(new Character("Alliser Thorne", false, true, true, GREY, OTHER, WALL_AND_BEYOND));
		characterList.add(new Character("Ollie", false, true, false, LIGHT, NOT_NOBLE, WALL_AND_BEYOND));
		characterList.add(new Character("Beric Dondarrion", true, true, true, DARK, OTHER, WESTEROS));
		characterList.add(new Character("Podrick Payne", true, true, true, DARK, OTHER, KINGS_LANDING));
		characterList.add(new Character("Gilly", true, false, false, DARK, NOT_NOBLE, WALL_AND_BEYOND));
		
		characterList.add(new Character("Xaro Xhoan Daxos", false, true, false, BALD, NOT_NOBLE, ESSOS));
		characterList.add(new Character("Daario Naharis", true, true, false, DARK, NOT_NOBLE, ESSOS));
		characterList.add(new Character("Pyat Pree", false, true, false, BALD, NOT_NOBLE, ESSOS));
		
		//Cant fully remember
		/*
		 * Wildling woman who helped Bran escape
		 * People from Qarth/Slaver's Bay
		 */

		//Too obscure maybe, or not human:
		/*
		 * The Night King
		 * Kevan Lannister
		 * Nun who tortures Cersei
		 * Witch who killed Khal Drogo
		 * Dragons
		 */
	}
}
