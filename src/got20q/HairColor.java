/**
 * @author Paul Dennis (pd236m)
 * May 3, 2018
 */
package got20q;

public enum HairColor implements PrettyPrint {
	BLOND("blond-haired"), //Pale blond or whiteish hair (not from old age)
	DARK("dark-haired"), //Black or dark brown hair
	RED("red-haired"),
	LIGHT("light-haired"), //Dirty blonde/light brown hair
	GREY("grey-haired"), //Includes white from old age
	BALD("bald"); //Does not include very short or partially bald
	
	final String pretty;
	
	private HairColor (String pretty) {
		this.pretty = pretty;
	}
	
	@Override
	public String toString () {
		String base = super.toString();
		return base.toLowerCase();
	}
	
	@Override
	public String getString () {
		return pretty;
	}
}
