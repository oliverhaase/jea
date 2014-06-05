package de.htwg_konstanz.jea;

public final class ClassHelper {
	public static boolean isRunnable(String type) {
		if (type.equals(""))
			return false;

		try {
			for (Class<?> interfaze : Class.forName(type).getInterfaces())
				if (interfaze.equals(java.lang.Runnable.class))
					return true;
			return false;

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
