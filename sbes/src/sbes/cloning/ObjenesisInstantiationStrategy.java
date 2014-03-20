package sbes.cloning;

import sbes.cloning.objenesis.Objenesis;
import sbes.cloning.objenesis.ObjenesisStd;

/**
 * @author kostantinos.kougios
 *
 * 17 Jul 2012
 */
public class ObjenesisInstantiationStrategy implements IInstantiationStrategy {

	private final Objenesis objenesis = new ObjenesisStd();

	public <T> T newInstance(Class<T> c) {
		return (T) objenesis.newInstance(c);
	}

	private static ObjenesisInstantiationStrategy instance = new ObjenesisInstantiationStrategy();

	public static ObjenesisInstantiationStrategy getInstance() {
		return instance;
	}
}
