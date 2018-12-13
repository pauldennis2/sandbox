/**
 * @author Paul Dennis (pd236m)
 * May 3, 2018
 */
package got20q;

public enum Location implements PrettyPrint {
	THE_NORTH("The North"),
	WALL_AND_BEYOND("The Wall or farther North"),//Includes Night's watch
	DORNE("Dorne"),
	WESTEROS("Westeros(elsewhere)"),
	KINGS_LANDING("King's Landing"),
	IRON_ISLANDS("The Iron Islands"),
	ESSOS("Essos");
	
	final String pretty;
	
	private Location (String pretty) {
		this.pretty = pretty;
	}
	
	@Override
	public String toString () {
		return this.pretty;
	}
	
	@Override
	public String getString () {
		return "from " + this.toString();
	}
}
