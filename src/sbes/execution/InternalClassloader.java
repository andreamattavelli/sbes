package sbes.execution;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sbes.logging.Logger;
import sbes.stub.GenerationException;
import sbes.stub.generator.StubGenerator;

public class InternalClassloader {

	private static final Logger logger = new Logger(InternalClassloader.class);
	private final ClassLoader classLoader;

	public InternalClassloader(String classpath) {
		try {
			ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

			if (classpath == null || classpath.equals("")) {
				logger.debug("The inner classpath is empty, relying on SystemClassLoader");
				classLoader = systemClassLoader;
			}
			else {
				List<File> paths = new ArrayList<File>();
				for (String path : classpath.split(":")) {
					File newPath = new File(path);
					if (!newPath.exists()) {
						throw new MalformedURLException("The new path " + newPath + " does not exist");
					}
					else {
						paths.add(newPath);
					}
				}

				List<URL> urls = new ArrayList<URL>();
				if (systemClassLoader instanceof URLClassLoader) {
					urls.addAll(Arrays.asList(((URLClassLoader) systemClassLoader).getURLs()));
				}

				for (File newPath : paths) {
					urls.add(newPath.toURI().toURL());
				}
				classLoader = new URLClassLoader(urls.toArray(new URL[0]), StubGenerator.class.getClassLoader());
			}
		} catch (MalformedURLException | SecurityException e) {
			logger.error("Unable to load ClassLoader", e);
			throw new GenerationException(e);
		}
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}

}
