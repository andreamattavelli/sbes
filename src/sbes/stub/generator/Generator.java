package sbes.stub.generator;

import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.type.Type;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sbes.Options;
import sbes.logging.Logger;
import sbes.stub.GenerationException;
import sbes.stub.InternalClassloader;
import sbes.util.ClassUtils;

public abstract class Generator {

	private static final Logger logger = new Logger(Generator.class);
	
	protected static final String STUB_EXTENSION = "_Stub"; 
	
	private final ClassLoader classloader;
	protected String stubName;
	
	public Generator() {
		this.classloader = InternalClassloader.getInternalClassLoader();
	}
	
	public void generateStub() {
		checkClasspath();
		
		Class<?> c;
		try {
			c = Class.forName(ClassUtils.getClassname(Options.I().getMethodSignature()), false, classloader);
		} catch (ClassNotFoundException e) {
			// infeasible
			throw new GenerationException("Target class not found");
		}
		
		Method[] methods = ClassUtils.getClassMethods(c);
		String methodName = ClassUtils.getMethodname(Options.I().getMethodSignature());
		
		// get target method from the list of class' methods
		Method targetMethod = findTargetMethod(methods, methodName);
		
		CompilationUnit cu = new CompilationUnit();
		
		// class name
		TypeDeclaration td = getClassDeclaration(c.getSimpleName());
		List<TypeDeclaration> types = new ArrayList<TypeDeclaration>();
		types.add(td);
		cu.setTypes(types);
		
		// class fields
		List<BodyDeclaration> members = new ArrayList<BodyDeclaration>();
		members.addAll(getClassFields(targetMethod, c));
		
		members.addAll(additionalMethods(methods));
		
		// set_results artificial method
		members.add(createSetResultsMethod(targetMethod));
		members.add(createMethodUnderTest());
		
		td.setMembers(members);
		
		dumpTestCase(cu, c.getSimpleName() + "_Stub");
	}
	
	// ---------- ABSTRACT METHODS ----------
	protected abstract List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c);
	protected abstract TypeDeclaration getClassDeclaration(String className);
	protected abstract MethodDeclaration createMethodUnderTest();
	protected abstract MethodDeclaration createSetResultsMethod(Method targetMethod);
	protected abstract List<BodyDeclaration> additionalMethods(Method[] methods);
	
	
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
	
	private void dumpTestCase(CompilationUnit cu, String name) {
		BufferedWriter out = null;
		try {
			String filename = name + ".java";
			out = new BufferedWriter(new FileWriter(filename));
			out.write(cu.toString());
			out.close();
		} catch(IOException e) {
			logger.error("Unable to dump stub due to: " + e.getMessage());
		} finally{
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Unable to correctly dump stub due to: " + e.getMessage());
				}
			}
		}
	}

	private void checkClasspath() {
		logger.debug("Checking classpath");
		checkClasspath(ClassUtils.getClassname(Options.I().getMethodSignature()));
		// TODO: check evosuite jar existance
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
		java.lang.reflect.Type returnType = method.getGenericReturnType();
		
		if (returnType.toString().equals("void")) {
			return ASTHelper.createReferenceType(returnType.toString(), 0); //FIXME: check cardinality array
		}
		else {
			return ASTHelper.createReferenceType(returnType.toString(), 1); //FIXME: check cardinality array
		}
	}
	
	protected List<Parameter> getParameterType(java.lang.reflect.Type[] parameters) {
		List<Parameter> toReturn = new ArrayList<Parameter>();
		for (int i = 0; i < parameters.length; i++) {
			java.lang.reflect.Type type = parameters[i];
			VariableDeclaratorId id = new VariableDeclaratorId("p" + i);
			String typeClass = type.toString();
			typeClass = typeClass.indexOf(" ") >= 0 ? typeClass.split(" ")[1]: typeClass;
			Parameter p = new Parameter(ASTHelper.createReferenceType(typeClass, 0), id); //FIXME: check cardinality array, type erasure, distinguish between primitive and reference types
			toReturn.add(p);
		}
		
		return toReturn;
	}

}
