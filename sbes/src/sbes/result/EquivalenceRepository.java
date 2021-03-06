package sbes.result;

import japa.parser.ast.expr.MethodCallExpr;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sbes.ast.CloneMethodCallsVisitor;
import sbes.exceptions.SBESException;
import sbes.execution.InternalClassloader;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.util.ClassUtils;
import sbes.util.ReflectionUtils;
import sbes.util.StringUtils;

public class EquivalenceRepository {

	private static final Logger logger = new Logger(EquivalenceRepository.class);
	private static EquivalenceRepository instance = null;
	
	private Map<String, List<EquivalentSequence>> equivalences;
	private Map<String, List<EquivalentSequence>> spurious;
	
	private List<Method> excluded;
	private List<Method> queue;
	private Method[] methods;
	
	private EquivalenceRepository() {
		equivalences = new HashMap<String, List<EquivalentSequence>>();
		spurious = new HashMap<String, List<EquivalentSequence>>();
		excluded = new ArrayList<Method>();
		queue = new ArrayList<Method>();
		initTargetMethods();
	}
	
	private void initTargetMethods() {
		InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath());
		ClassLoader classloader = ic.getClassLoader();
		Class<?> c;
		try {
			c = Class.forName(ClassUtils.getCanonicalClassname(Options.I().getTargetMethod()), false, classloader);
		} catch (ClassNotFoundException e) {
			// infeasible, we already checked the classpath
			throw new SBESException("Target class not found");
		}
		// get class methods
		methods = ReflectionUtils.getClassMethods(c);
	}

	public static EquivalenceRepository getInstance() {
		if (instance == null) {
			instance = new EquivalenceRepository();
		}
		return instance;
	}
	
	public static void reset() {
		getInstance().excluded.clear();
		getInstance().queue.clear();
		getInstance().initTargetMethods();
	}
	
	public void addEquivalence(EquivalentSequence eqSeq) {
		if (!equivalences.containsKey(Options.I().getTargetMethod())) {
			equivalences.put(Options.I().getTargetMethod(), new ArrayList<EquivalentSequence>());
		}
		equivalences.get(Options.I().getTargetMethod()).add(eqSeq);
		
		// find methods to exclude
		CloneMethodCallsVisitor cov = new CloneMethodCallsVisitor();
		cov.visit(eqSeq.getBody(), null);
		for (MethodCallExpr mce : cov.getMethods()) {
			String methodName = mce.getName();
			if (methodName.equals("size")) {
				continue;
			}
			List<Method> eligible = new ArrayList<Method>();
			for (Method method : methods) {
				if (method.getName().equals(methodName)) {
					eligible.add(method);
				}
			}
			if (eligible.size() == 1) {
				logger.debug("Queued method to exclude: " + eligible.get(0).toString());
				queue.add(eligible.get(0));
			} else {
				int args;
				if (mce.getArgs() == null) {
					args = 0;
				} else {
					args = mce.getArgs().size();
				}
				for (Method method : eligible) {
					if (method.getParameterTypes().length == args && !excluded.contains(method)) {
						logger.debug("Queued method to exclude: " + method.toString());
						queue.add(method);
						break;
					}
				}
			}
		}
	}
	
	public List<Method> getExcluded() {
		return excluded;
	}
	
	public void addExcluded() {
		if (!queue.isEmpty()) {
			logger.debug("Excluded method: " + queue.get(0).toString());
			excluded.add(queue.remove(0));
		}
	}
	
	public boolean isExcluded(Method method) {
		for (Method excl : excluded) {
			if (method.getName().equals(excl.getName())) {
				if (method.getParameterTypes().length == 0 && excl.getParameterTypes().length == 0) {
					return true;
				}
				if (method.getParameterTypes().length == excl.getParameterTypes().length) {
					Class<?> methodParams[] = method.getParameterTypes();
					Class<?> exclParams[] = excl.getParameterTypes();
					int count = 0;
					for (int i = 0; i < methodParams.length; i++) {
						if (methodParams[i].getCanonicalName().equals(exclParams[i].getCanonicalName())) {
							count++;
						}
					}
					if (count == methodParams.length) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void addSpuriousResult(EquivalentSequence spuriousResult) {
		if (!spurious.containsKey(Options.I().getTargetMethod())) {
			spurious.put(Options.I().getTargetMethod(), new ArrayList<EquivalentSequence>());
		}
		spurious.get(Options.I().getTargetMethod()).add(spuriousResult);
	}

	public void printEquivalences() {
		logger.info("Statistics: ");
		if (!spurious.isEmpty()) {
			logger.info("Spurious sequences synthesized:");
			for (Entry<String, List<EquivalentSequence>> e : spurious.entrySet()) {
				System.out.println("Target method: " + e.getKey());
				int i = 1;
				for (EquivalentSequence eqSeq : e.getValue()) {
					System.out.println("Spurious" + i++);
					System.out.println(StringUtils.chomp(eqSeq.toString()));
					System.out.println();
				}
				System.out.println("================================================================================");
			}
			System.out.println();
		}
		else {
			logger.info("No spurious sequences synthesized");
		}
		
		if (!equivalences.isEmpty()) {
			logger.info("Equivalent sequences synthesized:");
			for (Entry<String, List<EquivalentSequence>> e : equivalences.entrySet()) {
				System.out.println("Target method: " + e.getKey());
				int i = 1;
				for (EquivalentSequence eqSeq : e.getValue()) {
					System.out.println("EqSeq" + i++);
					System.out.println(StringUtils.chomp(eqSeq.toString()));
					System.out.println();
				}
				System.out.println("================================================================================");
			}
			System.out.println();
		}
		else {
			logger.fatal("Unable to synthesize any equivalent sequence");
		}
	}
	
}
