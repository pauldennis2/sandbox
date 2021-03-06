/**
 * @author Paul Dennis (pd236m)
 * May 21, 2018
 */
package sts_heuristics;

public interface Conditional extends Tweakable, Comparable<Conditional>{
	boolean evaluate (DeckReport report);
	int getPriorityLevel();
	
	@Override
	Conditional tweak ();
}
