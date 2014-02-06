package sbes.testcase;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sbes.Options;
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
		String className = ClassUtils.getCanonicalClassname(Options.I().getMethodSignature());
		if (!stringCU.contains(className)) {
			className = className.substring(className.lastIndexOf(".") + 1);
			if (stringCU.contains(className)) {
				return true;
			}
		}
		else {
			return true;
		}
		return false;
	}

//	private CompilationUnit createMainFromTest(final CompilationUnit testCU, final MethodDeclaration test) {
//		CompilationUnit cu = new CompilationUnit();
//		cu.setPackage(testCU.getPackage());
//		cu.setImports(testCU.getImports());
//		ClassOrInterfaceDeclaration type = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "");//ClassUtils.getTestClassname(this.equivalence));
//		ASTHelper.addTypeDeclaration(cu, type);
//		MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "main");
//		method.setModifiers(ModifierSet.addModifier(method.getModifiers(), ModifierSet.STATIC));
//		ASTHelper.addMember(type, method);
//		Parameter param = ASTHelper.createParameter(ASTHelper.createReferenceType("String", 1), "args");
//		ASTHelper.addParameter(method, param);
//		method.setBody(test.getBody());
//		//add method dependencies
//		List<MethodDeclaration> dependencies = analyzeMethodDependencies(testCU, test);
//		for (MethodDeclaration methodDeclaration : dependencies) {
//			ASTHelper.addMember(type, methodDeclaration);
//		}
//		return cu;
//	}
//	
//	private List<MethodDeclaration> analyzeMethodDependencies(final CompilationUnit testCU, final MethodDeclaration test) {
//		MethodVisitor methodVisitor = new MethodVisitor();
//		methodVisitor.visit(testCU, null);
//		List<MethodDeclaration> methods = methodVisitor.getTests();
//		MethodCalls mc = new MethodCalls(methods);
//		mc.visit(test, null);
//		return mc.getDependencies();
//	}

}

class TestGeneratorRemoveVisitor extends VoidVisitorAdapter<Void> {
	@Override
	public void visit(final ClassOrInterfaceDeclaration n, final Void arg) {
		n.setAnnotations(new ArrayList<AnnotationExpr>());
		super.visit(n, arg);
	}
	@Override
	public void visit(final CompilationUnit n, final Void arg) {
		if (n.getImports() != null) {
			for (int i = 0; i < n.getImports().size(); i++) {
				String name = n.getImports().get(i).getName().toString();
				if (name.equals("org.evosuite.junit.EvoSuiteRunner") || name.startsWith("org.junit")) {
					n.getImports().remove(i);
					i--;
				}
			}
		}
		super.visit(n, arg);
	}
}

class MethodVisitor extends VoidVisitorAdapter<Void> {
	private final List<MethodDeclaration> tests;
	public MethodVisitor() {
		this.tests = new ArrayList<MethodDeclaration>();
	}
	@Override
	public void visit(final MethodDeclaration n, final Void arg) {
		if (!n.getName().equals("initEvoSuiteFramework")) {
			this.tests.add(n);
		}
		super.visit(n, arg);
	}
	public List<MethodDeclaration> getTests() {
		return this.tests;
	}
}

class JUnitRemoveVisitor extends VoidVisitorAdapter<Void> {
	@Override
	public void visit(final MethodDeclaration n, final Void arg) {
		if (n.getBody() != null) {
			List<Statement> stmts = n.getBody().getStmts();
			if (stmts != null) {
				for (int i = 0; i < stmts.size(); i++) {
					Statement stmt = stmts.get(i);
					//failure
					if (stmt instanceof TryStmt) {
						List<Statement> tryStmts = ((TryStmt)stmt).getTryBlock().getStmts();
						for (int j = 0; j < tryStmts.size(); j++) {
							Statement tryStmt = tryStmts.get(j);
							if (tryStmt instanceof ExpressionStmt) {
								ExpressionStmt estmt = (ExpressionStmt) tryStmt;
								if (estmt.getExpression() instanceof MethodCallExpr) {
									MethodCallExpr mce = (MethodCallExpr) estmt.getExpression();
									if (mce.getName().equals("fail")) {
										tryStmts.remove(j);
										j--;
									}
								}
							}
						}
					}
					//assert*
					else if (stmt instanceof ExpressionStmt) {
						ExpressionStmt estmt = (ExpressionStmt) stmt;
						if (estmt.getExpression() instanceof MethodCallExpr) {
							MethodCallExpr mce = (MethodCallExpr) estmt.getExpression();
							if (mce.getName().startsWith("assert")) {
								stmts.remove(i);
								i--;
							}
						}
					}
				}
			}
		}

		super.visit(n, arg);
	}
}

class MethodCallVisitor extends VoidVisitorAdapter<Void> {
	private final String methodName;
	private final int parameters;
	private boolean found;
	public MethodCallVisitor(final String methodName, final int parameters) {
		this.methodName = methodName;
		this.parameters = parameters;
		this.found = false;
	}
	@Override
	public void visit(final MethodCallExpr n, final Void arg) {
		if (n.getName().equals(this.methodName)) {
			int args = n.getArgs() == null ? 0 : n.getArgs().size();
			if (args == parameters) {
				this.found = true;
			}
		}
		else {
			super.visit(n, arg);
		}
	}
	public boolean isFound() {
		return this.found;
	}
}

class MethodCalls extends VoidVisitorAdapter<Void> {
	private Map<String, MethodDeclaration> methods;
	private List<MethodDeclaration> dependencies;
	public MethodCalls(List<MethodDeclaration> declarations) {
		methods = new HashMap<String, MethodDeclaration>();
		for (MethodDeclaration methodDeclaration : declarations) {
			methods.put(methodDeclaration.getName(), methodDeclaration);
		}
		dependencies = new ArrayList<MethodDeclaration>();
	}
	@Override
	public void visit(MethodCallExpr n, Void arg) {
		if (methods.containsKey(n.getName())) {
			dependencies.add(methods.get(n.getName()));
		}
		super.visit(n, arg);
	}
	public List<MethodDeclaration> getDependencies() {
		return dependencies;
	}
}
