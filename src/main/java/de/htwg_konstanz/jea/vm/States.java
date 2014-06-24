package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

public class States {
	// Creates new set of processed frames. If the processed frames are copies
	// of the original frames depends on the frame processor.
	public static Set<State> processStates(Set<State> originals, StateProcessor processor) {
		Set<State> resultSet = new HashSet<State>();
		for (State original : originals)
			resultSet.add(processor.process(original));

		return resultSet;
	}
}
