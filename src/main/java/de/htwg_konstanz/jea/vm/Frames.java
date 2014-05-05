package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

public class Frames {
	public static Set<Frame> processFrames(Set<Frame> originals, FrameProcessor processor) {
		Set<Frame> resultSet = new HashSet<Frame>();
		for (Frame original : originals)
			resultSet.add(processor.process(original));

		return resultSet;
	}
}
