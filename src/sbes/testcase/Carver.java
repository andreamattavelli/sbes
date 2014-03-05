package sbes.testcase;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sbes.SBESException;
import sbes.ast.MethodCallVisitor;
import sbes.ast.MethodVisitor;
import sbes.execution.InternalClassloader;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.util.ClassUtils;
import sbes.util.IOUtils;

public class Carver {

	private static final Logger logger = new Logger(Carver.class);

	private final CarvingContext context;
	private final boolean strictCheck;

	public Carver(final CarvingContext context, final boolean strictCheck) {
		this.context = context;
		this.strictCheck = strictCheck;
	}

	public List<CarvingResult> carveBodyFromTests() {
		logger.info("Carving body from test cases" );
		List<CarvingResult> tests = carveTests();
		if (tests.size() == 1) {
			logger.info("Carved 1 body" );
		}
		else {
			logger.info("Carved " + tests.size() + " bodies" );
		}
		return tests;
	}

	private List<CarvingResult> carveTests() {
		logger.debug("Carving bodies");
		List<CarvingResult> bodies = new ArrayList<CarvingResult>();
		try {
			String testClassname = context.getTestFilename();
			logger.debug("Loading generated test: " + testClassname);
			String testPath = IOUtils.concatPath(context.getTestDirectory(), testClassname);
			CompilationUnit cu = JavaParser.parse(new File(testPath));
			
			MethodVisitor visitor = new MethodVisitor();
			visitor.visit(cu, null);
			for (MethodDeclaration method : visitor.getTests()) {
				if (isSearchedClass(cu)) {
					String methodSignature = ClassUtils.getMethodname(Options.I().getMethodSignature());
					String methodName = methodSignature.split("\\(")[0];
					String parameters = methodSignature.substring(methodSignature.indexOf("(") + 1, methodSignature.indexOf(")"));
					int parametersNumber = parameters.equals("") ? 0 : parameters.split(",").length;
					MethodCallVisitor callVisitor = new MethodCallVisitor(methodName, parametersNumber);
					callVisitor.visit(method, null);

					if (!strictCheck || (strictCheck && callVisitor.isFound())) {
						if (strictCheck) {
							logger.debug(" * " + method.getName() + " contains explicit method invocation");
						}
						bodies.add(new CarvingResult(method.getBody(), getCleanImports(cu)));
					}
					else {
						logger.debug(" * " + method.getName() + " does not contain explicit method invocation.");
					}
				}
				else {
					logger.debug(" * " + method.getName() + " does not contain explicit class invocation.");
				}
			}
		} catch (ParseException | IOException e) {
			logger.error(e.getMessage());
		}
		logger.debug("Carving bodies - done");
		return bodies;
	}
	
	private boolean isSearchedClass(CompilationUnit cu) {
		String stringCU = cu.toString();
		String canonicalName = ClassUtils.getCanonicalClassname(Options.I().getMethodSignature());
		String simpleName = ClassUtils.getSimpleClassnameFromCanonical(canonicalName);
		if (stringCU.contains(canonicalName) || stringCU.contains(simpleName)) {
			return true;
		}
		return false;
	}
	
	private List<ImportDeclaration> getCleanImports(CompilationUnit cu) {
		List<ImportDeclaration> cleanImports = new ArrayList<ImportDeclaration>();
		if (cu.getImports() != null) {
			for (int i = 0; i < cu.getImports().size(); i++) {
				String name = cu.getImports().get(i).getName().toString();
				if (!name.equals("org.evosuite") && !name.startsWith("org.junit")) {
					cleanImports.add(cu.getImports().get(i));
				}
			}
		}
		return cleanImports;
	}
	
	private String getClassToSearch() {
		String classname = ClassUtils.getCanonicalClassname(Options.I().getMethodSignature());
		String methodname = ClassUtils.getMethodname(Options.I().getMethodSignature());
		
		Method target = null;
		
		Class<?> c;
		try {
			InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath());
			c = Class.forName(classname, false, ic.getClassLoader());
			String method = methodname.split("\\(")[0];
			String args[] = methodname.split("\\(")[1].replaceAll("\\)", "").split(",");
			if (args.length == 1) {
				args = args[0].equals("") ? new String[0] : args;
			}
			for (Method m : c.getMethods()) {
				if (m.getName().equals(method) && m.getParameterTypes().length == args.length) {
					int i;
					for (i = 0; i < args.length; i++) {
						if (!m.getParameterTypes()[i].getCanonicalName().contains(args[i])) {
							break;
						}
					}
					if (i == args.length) {
						target = m;
						break;
					}
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error("Unable to find class", e);
			throw new SBESException("Unable to find class");
		}
		
		if (target == null) {
			throw new SBESException("Unable to find method");
		}
		else {
			return target.getDeclaringClass().getCanonicalName();
		}
	}
	
}
