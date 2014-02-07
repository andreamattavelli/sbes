package sbes.testcase;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sbes.Options;
import sbes.japa.MethodCallVisitor;
import sbes.japa.MethodVisitor;
import sbes.logging.Logger;
import sbes.util.ClassUtils;
import sbes.util.DirectoryUtils;

public class Carver {

	private static final Logger logger = new Logger(Carver.class);

	private final CarvingContext context;

	public Carver(final CarvingContext context) {
		this.context = context;
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
			String testPath = DirectoryUtils.toPath(context.getTestDirectory(), testClassname);
			CompilationUnit cu = JavaParser.parse(new File(testPath));
			
			MethodVisitor visitor = new MethodVisitor();
			visitor.visit(cu, null);
			for (MethodDeclaration method : visitor.getTests()) {
				if (isSearchedClass(cu)) {
					String methodSignature = ClassUtils.getMethodname(Options.I().getMethodSignature());
					String methodName = methodSignature.split("\\[")[0];
					String parameters = methodSignature.substring(methodSignature.indexOf("[") + 1, methodSignature.indexOf("]"));
					int parametersNumber = parameters.equals("") ? 0 : parameters.split(",").length;
					MethodCallVisitor callVisitor = new MethodCallVisitor(methodName, parametersNumber);
					callVisitor.visit(method, null);

					if (callVisitor.isFound()) {
						logger.debug(" * " + method.getName() + " contains explicit method invocation");
						bodies.add(new CarvingResult(method.getBody(), cu.getImports()));
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
		String simpleName = ClassUtils.getSimpleClassname(Options.I().getMethodSignature());
		if (stringCU.contains(canonicalName) || stringCU.contains(simpleName)) {
			return true;
		}
		return false;
	}
}