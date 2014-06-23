package playground;

import java.net.URL;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;

public class GoogleReflectionTest {

	public static void main(String[] args) {
		Set<URL> urls = ClasspathHelper.forClassLoader();

		System.out.println("urls: ");
		for (URL url : urls)
			System.out.println("- " + url);

		Object[] urlArray = urls.toArray();

		Reflections reflections = new Reflections(urlArray);
		Set<Class<? extends java.util.Set>> classes = reflections
				.getSubTypesOf(java.util.Set.class);

		System.out.println("classes: ");
		for (Class<? extends java.util.Set> clazz : classes)
			System.out.println("- " + clazz);

	}

}
