package de.htwg_konstanz.jea;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

public class ClassPathFinder {

	private static Reflections reflections = new Reflections(
			new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath()));

	public static Set<String> getSubTypsOf(String type) {
		Set<?> types = null;
		try {
			types = reflections.getSubTypesOf(Class.forName(type));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		HashSet<String> classes = new HashSet<String>();

		for (Object object : types) {
			Class<?> c = (Class<?>) object;
			classes.add(c.getName());
		}

		return classes;
	}

	public static String[] getClassesByPath(String packageName) {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		String path = packageName.replace('.', '/');

		Enumeration<URL> resources = null;
		try {
			resources = classLoader.getResources(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<File> dirs = new ArrayList<File>();

		while (resources.hasMoreElements())
			dirs.add(new File(resources.nextElement().getFile()));

		ArrayList<String> classes = new ArrayList<String>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new String[0]);
	}

	private static List<String> findClasses(File directory, String packageName) {
		List<String> classes = new ArrayList<String>();

		if (!directory.exists())
			return classes;

		File[] files = directory.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if (file.isDirectory())
				classes.addAll(findClasses(file, packageName + "." + fileName));
			else if (fileName.endsWith(".class"))
				classes.add(packageName + '.' + fileName.substring(0, fileName.length() - 6));
		}
		return classes;
	}

	public static String[] getClassesByReflection(String packageName) {
		ClassLoader[] classLoaders = { ClasspathHelper.contextClassLoader(),
				ClasspathHelper.staticClassLoader() };

		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(false), new ResourcesScanner())
				.setUrls(ClasspathHelper.forClassLoader(classLoaders))
				.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packageName))));

		Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

		int i = 0;
		String[] classNames = new String[classes.size()];
		for (Class<?> cls : classes) {
			classNames[i++] = cls.getName();
		}

		return classNames;
	}

}
