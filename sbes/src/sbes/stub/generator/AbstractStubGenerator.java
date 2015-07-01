package sbes.stub.generator;

import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclaratorId;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import sbes.exceptions.SBESException;
import sbes.execution.InternalClassloader;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.scenario.TestScenario;
import sbes.stub.Stub;
import sbes.util.ClassUtils;
import sbes.util.ReflectionUtils;

public abstract class AbstractStubGenerator {

	private static final Logger logger = new Logger(AbstractStubGenerator.class);
	
	protected static final String STUB_EXTENSION = "_Stub"; 
	
	protected final ClassLoader classloader;
	protected TypeVariable<?>[] generics;
	protected String stubName;
	protected final List<TestScenario> scenarios;
	
	public AbstractStubGenerator(final List<TestScenario> scenarios) {
		InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath());
		this.classloader = ic.getClassLoader();
		this.scenarios = scenarios;
	}
	
	public Stub generateStub() {
		Class<?> c;
		try {
			c = Class.forName(ClassUtils.getCanonicalClassname(Options.I().getTargetMethod()), false, classloader);
		} catch (ClassNotFoundException e) {
			// infeasible, we already checked the classpath
			throw new SBESException("Target class not found");
		}
		
		// get generic types (if any)
		generics = c.getTypeParameters();
		
		// get class' methods
		Method[] methods = ReflectionUtils.getClassMethods(c);
		// get method signature
		String methodSignature = ClassUtils.getMethodname(Options.I().getTargetMethod());
		// get target method from the list of class' methods
		Method targetMethod = ReflectionUtils.findTargetMethod(methods, methodSignature);
		
		logger.debug("Found " + methods.length + " methods for class " + c.getCanonicalName());
		logger.debug("Generating stub for target method " + targetMethod.toGenericString());
		
		// GENERATE STUB
		CompilationUnit cu = new CompilationUnit();
		cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(c.getPackage().getName())));
		cu.setImports(getImports());
		
		// class name
		TypeDeclaration stubClass = getClassDeclaration(c);
		ASTHelper.addTypeDeclaration(cu, stubClass);
		
		// class members
		List<BodyDeclaration> members = new ArrayList<BodyDeclaration>();
		
		// class fields (only phase 1)
		members.addAll(getClassFields(targetMethod, c));
		
		// stub constructor
		members.addAll(getStubConstructor(targetMethod, c));
		
		// original methods (only phase 1)
		members.addAll(getAdditionalMethods(targetMethod, methods, c));
		
		// artificial methods
		BodyDeclaration setResult = getSetResultsMethod(targetMethod); 
		// set_results
		if (setResult != null) {
			// only phase 1
			members.add(setResult);
		}
		// method_under_test
		members.add(getMethodUnderTest(targetMethod));
		
		stubClass.setMembers(members);
		
		return new Stub(cu, stubName);
	}

	// ---------- ABSTRACT STRATEGY METHODS ----------
	protected abstract List<ImportDeclaration> getImports();
	protected abstract TypeDeclaration getClassDeclaration(Class<?> c);
	protected abstract List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c);
	protected abstract List<BodyDeclaration> getStubConstructor(Method targetMethod, Class<?> c);
	protected abstract List<BodyDeclaration> getAdditionalMethods(Method targetMethod, Method[] methods, Class<?> c);
	protected abstract MethodDeclaration getSetResultsMethod(Method targetMethod);
	protected abstract MethodDeclaration getMethodUnderTest(Method targetMethod);
	
	
	// ---------- HELPER METHODS ----------
	public List<TestScenario> getScenarios() {
		return scenarios;
	}
	
	protected List<Parameter> getParameterType(final Class<?>[] parameters) {
		List<Parameter> toReturn = new ArrayList<Parameter>();
		for (int i = 0; i < parameters.length; i++) {
			Class<?> type = parameters[i];
			VariableDeclaratorId id = new VariableDeclaratorId("p" + i);
			String typeClass = type.getCanonicalName();
			typeClass = typeClass.indexOf(" ") >= 0 ? typeClass.split(" ")[1]: typeClass;
			//FIXME: check cardinality array
			Parameter p = new Parameter(ASTHelper.createReferenceType(typeClass, 0), id);
			toReturn.add(p);
		}
		
		return toReturn;
	}

}
