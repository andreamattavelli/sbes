package sbes.stub.generator;

import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.type.Type;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sbes.Options;
import sbes.logging.Logger;
import sbes.stub.GenerationException;
import sbes.stub.InternalClassloader;
import sbes.stub.Stub;
import sbes.util.ClassUtils;
import sbes.util.ReflectionUtils;

public abstract class Generator {

	private static final Logger logger = new Logger(Generator.class);
	
	protected static final int TEST_SCENARIOS = 1; //FIXME
	protected static final String STUB_EXTENSION = "_Stub"; 
	
	private final ClassLoader classloader;
	protected String stubName;
	
	public Generator() {
		this.classloader = InternalClassloader.getInternalClassLoader();
	}
	
	public Stub generateStub() {
		// check classpath: if the class is not found it raise an exception
		checkClasspath();
		
		Class<?> c;
		try {
			c = Class.forName(ClassUtils.getClassname(Options.I().getMethodSignature()), false, classloader);
		} catch (ClassNotFoundException e) {
			// infeasible, we already checked the classpath
			throw new GenerationException("Target class not found");
		}
		
		// get class' methods
		Method[] methods = ClassUtils.getClassMethods(c);
		// get method signature
		String methodSignature = ClassUtils.getMethodname(Options.I().getMethodSignature());
		// get target method from the list of class' methods
		Method targetMethod = findTargetMethod(methods, methodSignature);
		
		
		// GENERATE STUB
		CompilationUnit cu = new CompilationUnit();
		cu.setImports(getImports());
		
		// class name
		TypeDeclaration stubClass = getClassDeclaration(c.getSimpleName());
		ASTHelper.addTypeDeclaration(cu, stubClass);
		
		// class members
		List<BodyDeclaration> members = new ArrayList<BodyDeclaration>();
		
		// class fields (only phase 1)
		members.addAll(getClassFields(targetMethod, c));
		
		// original methods (only phase 1)
		members.addAll(getAdditionalMethods(targetMethod, methods));
		
		// artificial methods
		BodyDeclaration setResult = getSetResultsMethod(targetMethod); 
		if (setResult != null) {
			// only phase 1
			members.add(setResult);
		}
		members.add(getMethodUnderTest());
		
		stubClass.setMembers(members);
		
		return new Stub(cu, stubName);
	}

	// ---------- ABSTRACT STRATEGY METHODS ----------
	protected abstract List<ImportDeclaration> getImports();
	protected abstract TypeDeclaration getClassDeclaration(String className);
	protected abstract List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c);
	protected abstract List<BodyDeclaration> getAdditionalMethods(Method targetMethod, Method[] methods);
	protected abstract MethodDeclaration getSetResultsMethod(Method targetMethod);
	protected abstract MethodDeclaration getMethodUnderTest();
	
	
	// ---------- HELPER METHODS ----------
	private Method findTargetMethod(Method[] methods, String methodName) {
		Method targetMethod = null;
		String method = methodName.split("\\[")[0];
		String args[] = methodName.split("\\[")[1].replaceAll("\\]", "").split(",");
		if (args.length == 1) {
			args = args[0].equals("") ? new String[0] : args;
		}
		for (Method m : methods) {
			if (m.getName().equals(method) && m.getParameterTypes().length == args.length) {
				int i;
				for (i = 0; i < args.length; i++) {
					if (!m.getParameterTypes()[i].getCanonicalName().contains(args[i])) {
						break;
					}
				}
				if (i == args.length) {
					targetMethod = m;
					break;
				}
			}
		}
		if (targetMethod == null) {
			throw new GenerationException("Target method not found"); // failed to find method, give up
		}
		return targetMethod;
	}

	private void checkClasspath() {
		logger.debug("Checking classpath");
		checkClasspath(ClassUtils.getClassname(Options.I().getMethodSignature()));
		logger.debug("Classpath OK");
	}

	private void checkClasspath(final String className) {
		try {
			Class.forName(className, false, this.classloader);
		} catch (ClassNotFoundException e) {
			logger.error("Could not find class under test: " + className);
			throw new GenerationException(e);
		}
	}
	
	protected Type getReturnType(Method method) {
		if (method.getReturnType().isArray()) {
			return ASTHelper.createReferenceType(method.getReturnType().getComponentType().getCanonicalName(), ReflectionUtils.getArrayDimensionCount(method.getReturnType()));
		}
		else { 
			return ASTHelper.createReferenceType(method.getReturnType().getCanonicalName(), 0);
		}
	}
	
	protected Type getReturnTypeAsArray(Method method) {
		Class<?> returnType = method.getReturnType();
		
		if (returnType.getSimpleName().equals("void")) {
			return ASTHelper.createReferenceType(returnType.getCanonicalName(), 0);
		}
		else {
			return ASTHelper.createReferenceType(returnType.getCanonicalName(), 1);
		}
	}
	
	protected List<Parameter> getParameterType(Class<?>[] parameters) {
		List<Parameter> toReturn = new ArrayList<Parameter>();
		for (int i = 0; i < parameters.length; i++) {
			Class<?> type = parameters[i];
			VariableDeclaratorId id = new VariableDeclaratorId("p" + i);
			String typeClass = type.getCanonicalName();
			typeClass = typeClass.indexOf(" ") >= 0 ? typeClass.split(" ")[1]: typeClass;
			Parameter p = new Parameter(ASTHelper.createReferenceType(typeClass, 0), id); //FIXME: check cardinality array, type erasure, distinguish between primitive and reference types
			toReturn.add(p);
		}
		
		return toReturn;
	}

}
