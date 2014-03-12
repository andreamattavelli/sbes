package sbes.result;

import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.stmt.BlockStmt;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sbes.SBESException;
import sbes.ast.CloneObjVisitor;
import sbes.execution.InternalClassloader;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.stub.GenerationException;
import sbes.util.ClassUtils;

public class EquivalenceRepository {

	private static final Logger logger = new Logger(EquivalenceRepository.class);
	private static EquivalenceRepository instance = null;
	
	private List<EquivalentSequence> equivalences;
	private List<Method> excluded;
	private Method[] methods;
	
	private EquivalenceRepository() {
		equivalences = new ArrayList<EquivalentSequence>();
		excluded = new ArrayList<Method>();
		initTargetMethods();
	}
	
	private void initTargetMethods() {
		InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath());
		ClassLoader classloader = ic.getClassLoader();
		Class<?> c;
		try {
			c = Class.forName(ClassUtils.getCanonicalClassname(Options.I().getMethodSignature()), false, classloader);
		} catch (ClassNotFoundException e) {
			// infeasible, we already checked the classpath
			throw new GenerationException("Target class not found");
		}
		// get class methods
		methods = ClassUtils.getClassMethods(c);
	}

	public static EquivalenceRepository getInstance() {
		if (instance == null) {
			instance = new EquivalenceRepository();
		}
		return instance;
	}
	
	public void addEquivalence(EquivalentSequence eqSeq) {
		equivalences.add(eqSeq);
		
		// find methods to exclude
		BlockStmt body = eqSeq.getBody();
		CloneObjVisitor cov = new CloneObjVisitor();
		cov.visit(body, null);
		for (MethodCallExpr mce : cov.getMethods()) {
			String methodName = mce.getName();
			List<Method> eligible = new ArrayList<Method>();
			for (Method method : methods) {
				if (method.getName().equals(methodName)) {
					eligible.add(method);
				}
			}
			if (eligible.isEmpty()) {
				throw new SBESException("Unable to remove synthesized method. Terminating to avoid deadlock");
			}
			else if (eligible.size() == 0) {
				logger.info("Excluded method " + eligible.get(0).toString());
				excluded.add(eligible.get(0));
			}
			else {
				int args;
				if (mce.getArgs() == null) {
					args = 0;
				}
				else {
					args = mce.getArgs().size();
				}
				for (Method method : eligible) {
					if (method.getParameterTypes().length == args) {
						logger.info("Excluded method " + method.toString());
						excluded.add(method);
						break;
					}
				}
			}
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
	
	public boolean isRealSizeExcluded() {
		return false;
	}
	
	public boolean isCollectionSizeExcluded() {
		return false;
	}

	public void printEquivalences() {
		logger.info("Equivalent sequences synthesized:");
		for (EquivalentSequence eqSeq : equivalences) {
			logger.info(eqSeq.toString());
		}
	}
	
}