package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

public class Frames {
	// Creates new set of processed frames. If the processed frames are copies
	// of the original frames depends on the frame processor.
	public static Set<Frame> processFrames(Set<Frame> originals, FrameProcessor processor) {
		Set<Frame> resultSet = new HashSet<Frame>();
		for (Frame original : originals)
			resultSet.add(processor.process(original));

		return resultSet;
	}
}
