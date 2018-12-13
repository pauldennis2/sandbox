/**
 * @author Paul Dennis (pd236m)
 * May 3, 2018
 */
package got20q;

public enum House implements PrettyPrint{
	LANNISTER(true),
	STARK(true),
	BARATHEON(true),
	FREY(false),
	GREYJOY(true),
	TARGARYEN(true),
	MARTELL(true),
	ARRYN(true),
	TULLY(false),
	TYRELL(true),
	//Recently added
	MORMONT(false),
	REED(false),
	CLEGANE(false),
	//Others
	OTHER(false),
	NOT_NOBLE(false);
	
	final boolean majorHouse;
	
	private House (boolean majorHouse) {
		this.majorHouse = majorHouse;
	}
	
	@Override
	public String toString () {
		String start = super.toString().toLowerCase();
		return start.substring(0, 1).toUpperCase() + start.substring(1);
	}
	
	@Override
	public String getString () {
		String response;
		if (this == NOT_NOBLE) {
			response = "not a noble";
		} else {
			if (this == OTHER) {
				response = "a member of a minor House";
			} else {
				response = "a member of House " + this.toString();
			}
		}
		return response;
	}
}
