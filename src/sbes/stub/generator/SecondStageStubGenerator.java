package sbes.stub.generator;

import japa.parser.ASTHelper;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.CloneVisitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import sbes.logging.Logger;
import sbes.stub.GenerationException;
import sbes.stub.Stub;
import sbes.testcase.CarvingResult;
import sbes.util.ASTUtils;

public class SecondStageStubGenerator extends StubGenerator {

	private static final Logger logger = new Logger(SecondStageStubGenerator.class);
	
	private CarvingResult candidateES;
	private Stub stub;
	
	public SecondStageStubGenerator(Stub stub, CarvingResult candidateES) {
		this.stub = stub;
		this.candidateES = candidateES;
	}

	@Override
	public Stub generateStub() {
		logger.info("Generating stub for second phase");
		Stub stub = super.generateStub(); 
		logger.info("Generating stub for second phase - done");
		return stub;
	}
	
	@Override
	protected List<ImportDeclaration> getImports() {
		List<ImportDeclaration> imports = new ArrayList<>();
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("sbes.distance.Distance"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("sbes.cloning.Cloner"), false, false));
		return imports;
	}
	
	@Override
	protected TypeDeclaration getClassDeclaration(String className) {
		stubName = className + STUB_EXTENSION + "_2";
		
		// extends base class
		ClassOrInterfaceType extendClassDecl = new ClassOrInterfaceType(className);
		List<ClassOrInterfaceType> extendClasses = new ArrayList<ClassOrInterfaceType>();
		extendClasses.add(extendClassDecl);
		
		ClassOrInterfaceDeclaration classDecl = new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, stubName);
		classDecl.setExtends(extendClasses);
		
		return classDecl;
	}

	@Override
	protected List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c) {
		return new ArrayList<BodyDeclaration>();
	}
	
	@Override
	protected List<BodyDeclaration> getStubConstructor(Method targetMethod, Class<?> c) {
		/*
		 * We must adhere to inheritance as defined in Java, which imposes to
		 * add constructor to a child if an explicit constructor is defined in
		 * the parent.
		 */
		List<BodyDeclaration> toReturn = new ArrayList<BodyDeclaration>(); 
		Constructor<?> constructors[] = c.getDeclaredConstructors();
		for (Constructor<?> constructor : constructors) {
			if (!constructor.isSynthetic()) {
				ConstructorDeclaration cons = new ConstructorDeclaration(constructor.getModifiers(), stubName);
				cons.setParameters(getParameterType(constructor.getParameterTypes()));
				List<Expression> methodParameters = ASTUtils.createParameters(cons.getParameters());
				
				BlockStmt stmt = new BlockStmt();
				ExplicitConstructorInvocationStmt ecis = new ExplicitConstructorInvocationStmt(false, null, methodParameters);
				ASTHelper.addStmt(stmt, ecis);
				cons.setBlock(stmt);
				toReturn.add(cons);
			}
		}
		
		return toReturn;
	}
	
	@Override
	protected List<BodyDeclaration> getAdditionalMethods(Method targetMethod, Method[] methods) {
		return new ArrayList<BodyDeclaration>();
	}

	@Override
	protected MethodDeclaration getMethodUnderTest(Method targetMethod) {
		logger.debug("Adding method_under_test method");
		MethodDeclaration method_under_test = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "method_under_test");
		
		Class<?>[] parameters = targetMethod.getParameterTypes();
		List<Parameter> param = getParameterType(parameters);
		method_under_test.setParameters(param);
		
		BlockStmt stmt = new BlockStmt();
		
		// Cloner c = new Cloner();
		ASTHelper.addStmt(stmt, createClonerObj());
		
		// CLASS clone = c.deepClone(this);
		ASTHelper.addStmt(stmt, createCloneObj(targetMethod));
		
		// RESULT_CLASS expected_result = this.METHOD
		ASTHelper.addStmt(stmt, createExpectedResult(targetMethod, param));
		
		// RESULT_CLASS actual_result = clone.CARVED_METHOD(S)
		List<Statement> stmts = createActualResult(targetMethod, candidateES);
		if (stmts.isEmpty()) {
			throw new GenerationException("Unable to carve candidate: no statements!");
		}
		stmt.getStmts().addAll(stmts);
		
		NameExpr distanceClass = ASTHelper.createNameExpr("Distance");
		String distanceMethod = "distance";
		
		// if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)
		Expression zeroDouble = new DoubleLiteralExpr("0.0d");
		List<Expression> distanceStateArgs = new ArrayList<Expression>();
		distanceStateArgs.add(new ThisExpr());
		distanceStateArgs.add(ASTHelper.createNameExpr("clone"));
		Expression state = new MethodCallExpr(distanceClass, distanceMethod, distanceStateArgs);
		BinaryExpr stateCondition = new BinaryExpr(state, zeroDouble, japa.parser.ast.expr.BinaryExpr.Operator.greater);
		
		List<Expression> distanceResultArgs = new ArrayList<Expression>();
		distanceResultArgs.add(ASTHelper.createNameExpr("expected_result"));
		distanceResultArgs.add(ASTHelper.createNameExpr("actual_result"));
		Expression result = new MethodCallExpr(distanceClass, distanceMethod, distanceResultArgs);
		BinaryExpr resultCondition = new BinaryExpr(result, zeroDouble, japa.parser.ast.expr.BinaryExpr.Operator.greater);
		
		BinaryExpr ifCondition = new BinaryExpr(resultCondition, stateCondition, japa.parser.ast.expr.BinaryExpr.Operator.or);
		IfStmt ifStmt = new IfStmt(ifCondition, new ExpressionStmt(ASTUtils.createSystemOut("Executed")), null);
		ASTHelper.addStmt(stmt, ifStmt);
		
		method_under_test.setBody(stmt);
		
		return method_under_test;
	}

	private ExpressionStmt createClonerObj() {
		VariableDeclaratorId clonerId = new VariableDeclaratorId("c");
		ClassOrInterfaceType clonerType = new ClassOrInterfaceType("Cloner");
		Expression clonerInit = new ObjectCreationExpr(null, clonerType, null);
		VariableDeclarator cloner = new VariableDeclarator(clonerId, clonerInit);
		List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
		vars.add(cloner);
		VariableDeclarationExpr clonerVde = new VariableDeclarationExpr(ASTHelper.createReferenceType("Cloner", 0), vars);
		return new ExpressionStmt(clonerVde);
	}
	
	private ExpressionStmt createCloneObj(Method targetMethod) {
		List<Expression> methodParameters = new ArrayList<Expression>();
		methodParameters.add(new ThisExpr());
		Expression right = new MethodCallExpr(ASTHelper.createNameExpr("c"), "deepClone", methodParameters);
		List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
		vars.add(new VariableDeclarator(new VariableDeclaratorId("clone")));
		String className = targetMethod.getDeclaringClass().getSimpleName();
		Expression left = new VariableDeclarationExpr(ASTHelper.createReferenceType(className, 0), vars);
		AssignExpr assignment = new AssignExpr(left, right, Operator.assign);
		return new ExpressionStmt(assignment);
	}
	
	private Statement createExpectedResult(Method targetMethod, List<Parameter> parameters) {
		List<Expression> methodParameters = new ArrayList<Expression>();
		for (Parameter parameter : parameters) {
			methodParameters.add(ASTHelper.createNameExpr(parameter.getId().getName()));
		}
		Expression right = new MethodCallExpr(new ThisExpr(), targetMethod.getName(), methodParameters);
		List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
		vars.add(new VariableDeclarator(new VariableDeclaratorId("expected_result")));
		String className = targetMethod.getReturnType().getCanonicalName();
		int arrayDimension = 0;
		if (targetMethod.getReturnType().isArray()) {
			arrayDimension = 1;
		}
		Expression left = new VariableDeclarationExpr(ASTHelper.createReferenceType(className, arrayDimension), vars);
		AssignExpr assignment = new AssignExpr(left, right, Operator.assign);
		return new ExpressionStmt(assignment);
	}
	
	private List<Statement> createActualResult(Method targetMethod, CarvingResult candidateES2) {
		List<Statement> stmts = new ArrayList<Statement>();
		
		BlockStmt carved = candidateES2.getBody();
		CloneVisitor visitor = new CloneVisitor();
		BlockStmt cloned = (BlockStmt) visitor.visit(carved, null);
		
		String stubName = stub.getStubName();
		String varName = null;
		
		// generalize carved test
		for (int i = 0; i < cloned.getStmts().size(); i++) {
			Statement stmt = cloned.getStmts().get(i);
			if (stmt instanceof ExpressionStmt) {
				ExpressionStmt estmt = (ExpressionStmt) stmt;
				if (estmt.getExpression() instanceof VariableDeclarationExpr) {
					/* Evosuite does not create variable declarations with multiple definitions,
					 * so we can safely assume to get element 0
					 */
					VariableDeclarationExpr vde = (VariableDeclarationExpr) estmt.getExpression();
					if (vde.getType().toString().equals(stubName)) {
						varName = vde.getVars().get(0).getId().getName();
						cloned.getStmts().remove(i);
						i--;
					}
					else if (vde.getVars().get(0).getInit().toString().contains(varName)) {
						int arrayDimension = 0;
						if (targetMethod.getReturnType().isArray()) {
							arrayDimension = 1;
						}
						vde.setType(ASTHelper.createReferenceType(targetMethod.getReturnType().getCanonicalName(), arrayDimension));
						
						vde.getVars().get(0).setId(new VariableDeclaratorId("actual_result"));
						
						Expression value = vde.getVars().get(0).getInit();
						if (value instanceof MethodCallExpr) {
							MethodCallExpr mce = (MethodCallExpr) value;
							mce.setScope(ASTHelper.createNameExpr("clone"));
						}
						else if (value instanceof CastExpr) {
							CastExpr cast = (CastExpr) value;
							MethodCallExpr mce = (MethodCallExpr) cast.getExpr(); // safe cast: in our case it is always a method call
							mce.setScope(ASTHelper.createNameExpr("clone"));
						}
					}
					else if (varName != null) {
						Expression expr = vde.getVars().get(0).getInit();
						if (expr instanceof MethodCallExpr) {
							MethodCallExpr mce = (MethodCallExpr) expr;
							if (mce.getScope() instanceof NameExpr) {
								NameExpr ne = (NameExpr) mce.getScope();
								if (ne.getName().equals(varName)) {
									mce.setScope(ASTHelper.createNameExpr("clone"));
								}
							}
						}
					}
				}
				else if (estmt.getExpression() instanceof MethodCallExpr) {
					if (varName == null) {
						continue;
					}
					MethodCallExpr mce = (MethodCallExpr) estmt.getExpression();
					if (mce.getName().equals("method_under_test") || mce.getName().equals("set_results")) {
						cloned.getStmts().remove(i);
						i--;
					}
					else if (mce.getScope() instanceof NameExpr) {
						NameExpr scopeName = (NameExpr) mce.getScope();
						if (scopeName.getName().equals(varName)) {
							mce.setScope(ASTHelper.createNameExpr("clone"));
						}
					}
				}
			}
		}
		
		stmts.addAll(cloned.getStmts());
		
		return stmts;
	}

	@Override
	protected MethodDeclaration getSetResultsMethod(Method targetMethod) {
		return null;
	}

}
