package de.htwg_konstanz.jea;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.ba.ClassContext;

// This class must either be used thread-confined, or reporter must be thread-safe, otherwise concurrent calls to
// reporter.reportBug() can result in race conditions.
public final class JeaDetector implements Detector {
	private static final Logger logger = Logger.getLogger("JeaDetector");

	private final BugReporter reporter;

	public JeaDetector(BugReporter reporter) {

		this.reporter = reporter;
		logger.setLevel(Level.INFO);

		FileHandler fh = null;
		try {
			fh = new FileHandler("/Users/haase/Desktop/log.txt");
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fh.setFormatter(new SimpleFormatter());
		fh.setLevel(Level.INFO);
		logger.addHandler(fh);

	}

	@Override
	public void report() {
		logger.info("JeaDetector.report()");
		System.err.println("JeaDetector.report()");
	}

	@Override
	public void visitClassContext(ClassContext classContext) {
		logger.info("JeadDetector.visitClassContext(): " + classContext.getJavaClass());
	}
}
