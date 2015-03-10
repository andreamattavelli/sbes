package sbes.cloning;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import sbes.exceptions.CloningException;

/**
 * Cloner: deep clone objects.
 * 
 * This class is thread safe. One instance can be used by multiple threads on
 * the same time.
 * 
 * @author kostantinos.kougios
 * 
 *         18 Sep 2008
 */
public class Cloner {

	private final IInstantiationStrategy instantiationStrategy;
	private final Set<Class<?>> ignored = new HashSet<Class<?>>();
	private final Set<Class<?>> ignoredInstanceOf = new HashSet<Class<?>>();
	private final Set<Class<?>> nullInstead = new HashSet<Class<?>>();
	private final Map<Object, Boolean> ignoredInstances = new IdentityHashMap<Object, Boolean>();
	private final ConcurrentHashMap<Class<?>, List<Field>> fieldsCache = new ConcurrentHashMap<Class<?>, List<Field>>();
	private boolean dumpClonedClasses = false;
	private boolean cloningEnabled = true;
	private boolean nullTransient = false;
	private boolean cloneSynthetics = true;

	public Cloner() {
		this.instantiationStrategy = ObjenesisInstantiationStrategy.getInstance();
		init();
	}

	public Cloner(final IInstantiationStrategy instantiationStrategy) {
		this.instantiationStrategy = instantiationStrategy;
		init();
	}

	public boolean isNullTransient() {
		return nullTransient;
	}

	/**
	 * this makes the cloner to set a transient field to null upon cloning.
	 * 
	 * NOTE: primitive types can't be nulled. Their value will be set to
	 * default, i.e. 0 for int
	 * 
	 * @param nullTransient
	 *            true for transient fields to be nulled
	 */
	public void setNullTransient(final boolean nullTransient) {
		this.nullTransient = nullTransient;
	}

	public void setCloneSynthetics(final boolean cloneSynthetics) {
		this.cloneSynthetics = cloneSynthetics;
	}

	private void init() {
		registerKnownJdkImmutableClasses();
		registerKnownConstants();
	}


	public void registerConstant(final Object o) {
		ignoredInstances.put(o, true);
	}

	public void registerConstant(final Class<?> c, final String privateFieldName) {
		try {
			final Field field = c.getDeclaredField(privateFieldName);
			field.setAccessible(true);
			final Object v = field.get(null);
			ignoredInstances.put(v, true);
		} catch (final SecurityException e) {
		} catch (final NoSuchFieldException e) {
		} catch (final IllegalArgumentException e) {
		} catch (final IllegalAccessException e) {
		}
	}

	/**
	 * registers some known JDK immutable classes. Override this to register
	 * your own list of jdk's immutable classes
	 */
	protected void registerKnownJdkImmutableClasses() {
		registerImmutable(String.class);
		registerImmutable(Integer.class);
		registerImmutable(Long.class);
		registerImmutable(Boolean.class);
		registerImmutable(Class.class);
		registerImmutable(Float.class);
		registerImmutable(Double.class);
		registerImmutable(Character.class);
		registerImmutable(Byte.class);
		registerImmutable(Short.class);
		registerImmutable(Void.class);
		registerImmutable(BigDecimal.class);
		registerImmutable(BigInteger.class);
		registerImmutable(URI.class);
		registerImmutable(URL.class);
		registerImmutable(UUID.class);
		registerImmutable(Pattern.class);
	}

	protected void registerKnownConstants() {
		registerStaticFields(TreeSet.class, HashSet.class, HashMap.class, TreeMap.class);
	}

	/**
	 * registers all static fields of these classes. Those static fields won't
	 * be cloned when an instance of the class is cloned.
	 * 
	 * This is useful i.e. when a static field object is added into maps or
	 * sets. At that point, there is no way for the cloner to know that it was
	 * static except if it is registered.
	 * 
	 * @param classes
	 *            array of classes
	 */
	public void registerStaticFields(final Class<?>... classes) {
		for (final Class<?> c : classes) {
			final List<Field> fields = allFields(c);
			for (final Field field : fields) {
				final int mods = field.getModifiers();
				if (Modifier.isStatic(mods) && !field.getType().isPrimitive()) {
					registerConstant(c, field.getName());
				}
			}
		}
	}

	/**
	 * spring framework friendly version of registerStaticFields
	 * 
	 * @param set
	 *            a set of classes which will be scanned for static fields
	 */
	public void setExtraStaticFields(final Set<Class<?>> set) {
		registerStaticFields((Class<?>[]) set.toArray());
	}

	/**
	 * instances of classes that shouldn't be cloned can be registered using
	 * this method.
	 * 
	 * @param c
	 *            The class that shouldn't be cloned. That is, whenever a deep
	 *            clone for an object is created and c is encountered, the
	 *            object instance of c will be added to the clone.
	 */
	public void dontClone(final Class<?>... c) {
		for (final Class<?> cl : c) {
			ignored.add(cl);
		}
	}

	public void dontCloneInstanceOf(final Class<?>... c) {
		for (final Class<?> cl : c) {
			ignoredInstanceOf.add(cl);
		}
	}

	public void setDontCloneInstanceOf(final Class<?>... c) {
		dontCloneInstanceOf(c);
	}

	/**
	 * instead of cloning these classes will set the field to null
	 * 
	 * @param c
	 *            the classes to nullify during cloning
	 */
	public void nullInsteadOfClone(final Class<?>... c) {
		for (final Class<?> cl : c) {
			nullInstead.add(cl);
		}
	}

	/**
	 * spring framework friendly version of nullInsteadOfClone
	 */
	public void setExtraNullInsteadOfClone(final Set<Class<?>> set) {
		nullInstead.addAll(set);
	}

	/**
	 * registers an immutable class. Immutable classes are not cloned.
	 * 
	 * @param c
	 *            the immutable class
	 */
	public void registerImmutable(final Class<?>... c) {
		for (final Class<?> cl : c) {
			ignored.add(cl);
		}
	}

	/**
	 * spring framework friendly version of registerImmutable
	 */
	public void setExtraImmutables(final Set<Class<?>> set) {
		ignored.addAll(set);
	}

	/**
	 * creates a new instance of c. Override to provide your own implementation
	 * 
	 * @param <T>
	 *            the type of c
	 * @param c
	 *            the class
	 * @return a new instance of c
	 */
	protected <T> T newInstance(final Class<T> c) {
		return instantiationStrategy.newInstance(c);
	}

	/**
	 * deep clones "o".
	 * 
	 * @param <T>
	 *            the type of "o"
	 * @param o
	 *            the object to be deep-cloned
	 * @return a deep-clone of "o".
	 */
	public <T> T deepClone(final T o) {
		if (o == null) {
			return null;
		}
		if (!cloningEnabled) {
			return o;
		}
		final Map<Object, Object> clones = new IdentityHashMap<Object, Object>(16);
		try {
			registerStaticFields(o.getClass());
			return cloneInternal(o, clones);
		} catch (final IllegalAccessException e) {
			throw new CloningException("error during cloning of " + o, e);
		}
	}

	public <T> T deepCloneDontCloneInstances(final T o, final Object... dontCloneThese) {
		if (o == null) {
			return null;
		}
		if (!cloningEnabled) {
			return o;
		}
		if (dumpClonedClasses) {
			System.out.println("start>" + o.getClass());
		}
		final Map<Object, Object> clones = new IdentityHashMap<Object, Object>(16);
		for (final Object dc : dontCloneThese) {
			clones.put(dc, dc);
		}
		try {
			return cloneInternal(o, clones);
		} catch (final IllegalAccessException e) {
			throw new CloningException("error during cloning of " + o, e);
		}
	}

	/**
	 * shallow clones "o". This means that if c=shallowClone(o) then c!=o. Any
	 * change to c won't affect o.
	 * 
	 * @param <T>
	 *            the type of o
	 * @param o
	 *            the object to be shallow-cloned
	 * @return a shallow clone of "o"
	 */
	public <T> T shallowClone(final T o) {
		if (o == null) {
			return null;
		}
		if (!cloningEnabled) {
			return o;
		}
		try {
			return cloneInternal(o, null);
		} catch (final IllegalAccessException e) {
			throw new CloningException("error during cloning of " + o, e);
		}
	}

	private final ConcurrentHashMap<Class<?>, Boolean> immutables = new ConcurrentHashMap<Class<?>, Boolean>();

	private boolean cloneAnonymousParent = true;

	/**
	 * override this to decide if a class is immutable. Immutable classes are
	 * not cloned.
	 * 
	 * @param clz
	 *            the class under check
	 * @return true to mark clz as immutable and skip cloning it
	 */
	protected boolean considerImmutable(final Class<?> clz) {
		return false;
	}

	protected Class<?> getImmutableAnnotation() {
		return Immutable.class;
	}

	/**
	 * decides if a class is to be considered immutable or not
	 * 
	 * @param clz
	 *            the class under check
	 * @return true if the clz is considered immutable
	 */
	private boolean isImmutable(final Class<?> clz) {
		final Boolean isIm = immutables.get(clz);
		if (isIm != null) {
			return isIm;
		}
		if (considerImmutable(clz)) {
			return true;
		}
		final Class<?> immutableAnnotation = getImmutableAnnotation();
		for (final Annotation annotation : clz.getDeclaredAnnotations()) {
			if (annotation.annotationType() == immutableAnnotation) {
				immutables.put(clz, Boolean.TRUE);
				return true;
			}
		}
		Class<?> c = clz.getSuperclass();
		while (c != null && c != Object.class) {
			for (final Annotation annotation : c.getDeclaredAnnotations()) {
				if (annotation.annotationType() == Immutable.class) {
					final Immutable im = (Immutable) annotation;
					if (im.subClass()) {
						immutables.put(clz, Boolean.TRUE);
						return true;
					}
				}
			}
			c = c.getSuperclass();
		}
		immutables.put(clz, Boolean.FALSE);
		return false;
	}

	/**
	 * PLEASE DONT CALL THIS METHOD The only reason for been public is because
	 * custom IFastCloner's must invoke it
	 */
	@SuppressWarnings("unchecked")
	public <T> T cloneInternal(final T o, final Map<Object, Object> clones) throws IllegalAccessException {
		if (o == null) {
			return null;
		}
		if (o == this) {
			return null;
		}
		
		registerStaticFields(o.getClass());
		
		if (ignoredInstances.containsKey(o)) {
			return o;
		}
		final Class<T> clz = (Class<T>) o.getClass();
		if (clz.isEnum()) {
			return o;
		}
		if (nullInstead.contains(clz)) {
			return null;
		}
		if (ignored.contains(clz)) {
			return o;
		}
		for (final Class<?> iClz : ignoredInstanceOf) {
			if (iClz.isAssignableFrom(clz)) {
				return o;
			}
		}
		if (isImmutable(clz)) {
			return o;
		}

		final Object clonedPreviously = clones != null ? clones.get(o) : null;
		if (clonedPreviously != null) {
			return (T) clonedPreviously;
		}

		if (clz.isArray()) {
			final int length = Array.getLength(o);
			final T newInstance = (T) Array.newInstance(clz.getComponentType(), length);
			if (clones != null) {
				clones.put(o, newInstance);
			}
			for (int i = 0; i < length; i++) {
				final Object v = Array.get(o, i);
				final Object clone = clones != null ? cloneInternal(v, clones) : v;
				Array.set(newInstance, i, clone);
			}
			return newInstance;
		}
		
		// FIX: do not clone singleton instances
		if (isSingleton(o)) {
			return o;
		}

		final T newInstance = newInstance(clz);
		if (clones != null) {
			clones.put(o, newInstance);
		}

		final List<Field> fields = allFields(clz);
		for (final Field field : fields) {
			final int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers)) {
				if (nullTransient && Modifier.isTransient(modifiers)) {
					final Class<?> type = field.getType();
					if (!type.isPrimitive()) {
						field.set(newInstance, null);
					}
				} else {
					final Object fieldObject = field.get(o);
					final boolean shouldClone = (cloneSynthetics || (!cloneSynthetics && !field.isSynthetic())) && (cloneAnonymousParent || ((!cloneAnonymousParent && !isAnonymousParent(field))));
					final Object fieldObjectClone = clones != null ? (shouldClone ? cloneInternal(fieldObject, clones) : fieldObject) : fieldObject;
					field.set(newInstance, fieldObjectClone);
				}
			}
		}
		return newInstance;
	}
	
	private boolean isSingleton(Object o) {
		Class<?> cls = o.getClass();
		Constructor<?>[] constructors = cls.getConstructors();
		if (constructors.length != 0) { // Class has public constructors
			return false;
		}
		
		List<Method> methods = Arrays.asList(cls.getMethods());
		for (Method m : methods) {
			if (Modifier.isStatic(m.getModifiers()) && m.getReturnType().equals(cls) && m.getParameterTypes().length == 0) {
				try {
					Object instance = m.invoke(m);
					if (instance == o) {
						return true;
					}
				} catch (IllegalAccessException e) {
//					return false;
				} catch (IllegalArgumentException e) {
//					return false;
				} catch (InvocationTargetException e) {
//					return false;
				}
			}
		}
		return false;
	}

	private boolean isAnonymousParent(final Field field) {
		return "this$0".equals(field.getName());
	}

	/**
	 * copies all properties from src to dest. Src and dest can be of different
	 * class, provided they contain same field names/types
	 * 
	 * @param src
	 *            the source object
	 * @param dest
	 *            the destination object which must contain as minimum all the
	 *            fields of src
	 */
	public <T, E extends T> void copyPropertiesOfInheritedClass(final T src, final E dest) {
		if (src == null) {
			throw new IllegalArgumentException("src can't be null");
		}
		if (dest == null) {
			throw new IllegalArgumentException("dest can't be null");
		}
		final Class<? extends Object> srcClz = src.getClass();
		final Class<? extends Object> destClz = dest.getClass();
		if (srcClz.isArray()) {
			if (!destClz.isArray()) {
				throw new IllegalArgumentException("can't copy from array to non-array class " + destClz);
			}
			final int length = Array.getLength(src);
			for (int i = 0; i < length; i++) {
				final Object v = Array.get(src, i);
				Array.set(dest, i, v);
			}
			return;
		}
		final List<Field> fields = allFields(srcClz);
		final List<Field> destFields = allFields(dest.getClass());
		for (final Field field : fields) {
			if (!Modifier.isStatic(field.getModifiers())) {
				try {
					final Object fieldObject = field.get(src);
					field.setAccessible(true);
					if (destFields.contains(field)) {
						field.set(dest, fieldObject);
					}
				} catch (final IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (final IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * reflection utils
	 */
	private void addAll(final List<Field> l, final Field[] fields) {
		for (final Field field : fields) {
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			l.add(field);
		}
	}

	/**
	 * reflection utils, override this to choose which fields to clone
	 */
	protected List<Field> allFields(final Class<?> c) {
		List<Field> l = fieldsCache.get(c);
		if (l == null) {
			l = new LinkedList<Field>();
			final Field[] fields = c.getDeclaredFields();
			addAll(l, fields);
			Class<?> sc = c;
			while ((sc = sc.getSuperclass()) != Object.class && sc != null) {
				addAll(l, sc.getDeclaredFields());
			}
			fieldsCache.putIfAbsent(c, l);
		}
		return l;
	}

	public boolean isDumpClonedClasses() {
		return dumpClonedClasses;
	}

	/**
	 * will println() all cloned classes. Useful for debugging only.
	 * 
	 * @param dumpClonedClasses
	 *            true to enable printing all cloned classes
	 */
	public void setDumpClonedClasses(final boolean dumpClonedClasses) {
		this.dumpClonedClasses = dumpClonedClasses;
	}

	public boolean isCloningEnabled() {
		return cloningEnabled;
	}

	public void setCloningEnabled(final boolean cloningEnabled) {
		this.cloningEnabled = cloningEnabled;
	}

	/**
	 * if false, anonymous classes parent class won't be cloned. Default is true
	 */
	public void setCloneAnonymousParent(final boolean cloneAnonymousParent) {
		this.cloneAnonymousParent = cloneAnonymousParent;
	}

	public boolean isCloneAnonymousParent() {
		return cloneAnonymousParent;
	}
}
