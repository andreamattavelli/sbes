package sbes.stub.generator;

import japa.parser.ASTHelper;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import sbes.Options;
import sbes.logging.Logger;
import sbes.scenario.TestScenario;
import sbes.stub.Stub;
import sbes.util.ASTUtils;
import sbes.util.MethodUtils;

public class FirstStageStubGenerator extends StubGenerator {

	private static final Logger logger = new Logger(FirstStageStubGenerator.class);
	
	public static final String NUM_SCENARIOS = "NUM_SCENARIOS";
	public static final String EXPECTED_STATE = "expected_states";
	public static final String EXPECTED_RESULT = "expected_results";
	public static final String ACTUAL_STATE = "actual_states";
	public static final String ACTUAL_RESULT = "actual_results";

	private List<TestScenario> scenarios;
	
	public FirstStageStubGenerator(List<TestScenario> scenarios) {
		this.scenarios = scenarios;
	}
	
	@Override
	public Stub generateStub() {
		logger.info("Generating stub for first phase");
		Stub stub = super.generateStub(); 
		logger.info("Generating stub for first phase - done");
		return stub;
	}
	
	@Override
	protected List<ImportDeclaration> getImports() {
		List<ImportDeclaration> imports = new ArrayList<>();
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("sbes.distance.Distance"), false, false));
		for (TestScenario scenario : scenarios) {
			List<ImportDeclaration> scenarioImports  = scenario.getImports();
			for (ImportDeclaration importDeclaration : scenarioImports) {
				if (!imports.contains(importDeclaration)) {
					imports.add(importDeclaration);
				}
			}
		}
		return imports;
	}
	
	@Override
	protected TypeDeclaration getClassDeclaration(String className) {
		stubName = className + STUB_EXTENSION;
		return new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, stubName);
	}

	@Override
	protected List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c) {
		logger.debug("Adding class fields");
		List<BodyDeclaration> declarations = new ArrayList<BodyDeclaration>();
		
		VariableDeclarator num_scenarios = ASTUtils.createDeclarator(NUM_SCENARIOS, new IntegerLiteralExpr(Integer.toString(scenarios.size())));
		BodyDeclaration num_scenarios_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC, ASTHelper.INT_TYPE, num_scenarios);
		declarations.add(num_scenarios_bd);
		
		// stub helper arrays
		declarations.add(ASTUtils.createStubHelperArray(c.getCanonicalName(), EXPECTED_STATE));
		declarations.add(ASTUtils.createStubHelperArray(ASTUtils.getReturnType(targetMethod).toString(), EXPECTED_RESULT));
		declarations.add(ASTUtils.createStubHelperArray(c.getCanonicalName(), ACTUAL_STATE));
		declarations.add(ASTUtils.createStubHelperArray(ASTUtils.getReturnType(targetMethod).toString(), ACTUAL_RESULT));
		
		logger.debug("Adding class fields - done");
		
		return declarations;
	}
	
	@Override
	protected List<BodyDeclaration> getStubConstructor(Method targetMethod, Class<?> c) {
		List<BodyDeclaration> constructors = new ArrayList<BodyDeclaration>();
		ConstructorDeclaration constructor = new ConstructorDeclaration(Modifier.PUBLIC, stubName);
		List<Statement> statements = new ArrayList<Statement>();
		BlockStmt body = new BlockStmt();
		for (int i = 0; i < scenarios.size(); i++) {
			statements.addAll(scenarios.get(i).getScenario().getStmts());
		}
		body.setStmts(statements);
		constructor.setBlock(body);
		constructors.add(constructor);
		return constructors;
	}
	
	@Override
	protected List<BodyDeclaration> getAdditionalMethods(Method targetMethod, Method[] methods) {
		logger.debug("Adding original class method wrappers");
		
		List<BodyDeclaration> members = new ArrayList<BodyDeclaration>();
		
		methods = preventMethodBloat(targetMethod, methods);
		
		for (Method method : methods) {
			if (MethodUtils.methodFilter(method)) {
				continue;
			}
			else if (method.equals(targetMethod)) {
				continue;
			}
			
			Type returnType = ASTUtils.getReturnType(method);
			Type returnStubType = ASTUtils.getReturnTypeAsArray(method);
			MethodDeclaration md = new MethodDeclaration(method.getModifiers() & Modifier.TRANSIENT & Modifier.VOLATILE, returnStubType, method.getName());
			
			//parameters
			List<Parameter> parameters = new ArrayList<Parameter>();
			parameters.addAll(getParameterType(method.getParameterTypes()));
			md.setParameters(parameters);
			
			//body
			BlockStmt stmt = new BlockStmt();
			List<Statement> stmts = new ArrayList<Statement>();
			
			VariableDeclarationExpr res = ASTHelper.createVariableDeclarationExpr(returnStubType, "res");
			
			// for loop
			List<Expression> init = ASTUtils.createForInit("i", ASTHelper.INT_TYPE, new IntegerLiteralExpr("0"), Operator.assign);
			Expression compare = ASTUtils.createForCondition("i", NUM_SCENARIOS, japa.parser.ast.expr.BinaryExpr.Operator.less);
			List<Expression> update = ASTUtils.createForIncrement("i", japa.parser.ast.expr.UnaryExpr.Operator.posIncrement);
			
			// for loop body
			List<Expression> methodParameters = ASTUtils.createParameters(parameters);
			Expression right = new MethodCallExpr(ASTUtils.createArrayAccess(ACTUAL_STATE, "i"), method.getName(), methodParameters);
			
			BlockStmt body = new BlockStmt();
			if (returnStubType.toString().equals("void")) {
				// return type void - no need for return array
				ASTHelper.addStmt(body, right);
			}
			else {
				// return type non void - need to build a return array
				List<Expression> arraysDimension = ASTUtils.getArraysDimension();
				ArrayCreationExpr ace = new ArrayCreationExpr(returnType, arraysDimension, 0);
				if (returnType instanceof ReferenceType) {
					ReferenceType rtype = (ReferenceType) returnType;
					if (rtype.getArrayCount() > 0) {
						ace = new ArrayCreationExpr(rtype.getType(), arraysDimension, rtype.getArrayCount());
					}
				}
				AssignExpr resAssign = new AssignExpr(res, ace, Operator.assign);
				ExpressionStmt resStmt = new ExpressionStmt(resAssign);
				stmts.add(resStmt);
				
				Expression left = ASTUtils.createArrayAccess("res", "i");
				AssignExpr callResult = new AssignExpr(left, right, Operator.assign);
				ASTHelper.addStmt(body, callResult);
			}
			
			ForStmt forStmt = new ForStmt(init, compare, update, body);
			stmts.add(forStmt);
			
			if (!returnStubType.toString().equals("void")) {
				ReturnStmt ret = new ReturnStmt(ASTHelper.createNameExpr("res"));
				stmts.add(ret);
			}

			stmt.setStmts(stmts);
			md.setBody(stmt);
			
			members.add(md);
		}
		
		logger.debug("Generated " + members.size() + " class method wrappers");
		logger.debug("Adding original class method wrappers - done");
		
		return members;
	}

	private Method[] preventMethodBloat(Method targetMethod, Method[] methods) {
		if (methods.length > Options.I().getMethodBloatFactor()) {
			List<Method> toReturn = new ArrayList<Method>();
			for (Method method : methods) {
				if (method.getDeclaringClass().equals(Object.class) ||
						method.getName().equals("toArray") ||
						method.getName().contains("iterator") ||
						method.getName().contains("Iterator")) {
					continue;
				}
				toReturn.add(method);
			}
			return toReturn.toArray(new Method[0]);
		}
		else {
			return methods;
		}
	}

	@Override
	protected MethodDeclaration getSetResultsMethod(Method targetMethod) {
		logger.debug("Adding set_results method");
		Type returnType = ASTUtils.getReturnType(targetMethod);
		if (returnType.toString().equals("void")) {
			logger.debug("Original method's return value is void, stopping");
			return null;
		}
		
		MethodDeclaration set_results = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "set_results");
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter(ASTHelper.createReferenceType(ASTUtils.getReturnType(targetMethod).toString(), 1), new VariableDeclaratorId("res")));
		set_results.setParameters(parameters);
		
		List<Expression> init = ASTUtils.createForInit("i", ASTHelper.INT_TYPE, new IntegerLiteralExpr("0"), Operator.assign);
		Expression compare = ASTUtils.createForCondition("i", NUM_SCENARIOS, japa.parser.ast.expr.BinaryExpr.Operator.less);
		List<Expression> update = ASTUtils.createForIncrement("i", japa.parser.ast.expr.UnaryExpr.Operator.posIncrement);
		
		BlockStmt forBody = new BlockStmt();
		Expression left = ASTUtils.createArrayAccess(ACTUAL_RESULT, "i");
		Expression right = ASTUtils.createArrayAccess("res", "i");
		AssignExpr assignment = new AssignExpr(left, right, Operator.assign);
		ASTHelper.addStmt(forBody, assignment);
		
		ForStmt forStmt = new ForStmt(init, compare, update, forBody);
		
		BlockStmt methodBody = new BlockStmt();
		ASTHelper.addStmt(methodBody, forStmt);
		set_results.setBody(methodBody);
		
		logger.debug("Adding set_results method - done");
		
		return set_results;
	}
	
	@Override
	protected MethodDeclaration getMethodUnderTest(Method targetMethod) {
		logger.debug("Adding method_under_test method");
		MethodDeclaration set_results = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "method_under_test");
		
		BlockStmt stmt = new BlockStmt();
		BinaryExpr condition = null;
		for (int i = 0; i < scenarios.size(); i++) {
			Expression zeroDouble = new DoubleLiteralExpr("0.0d");
			
			NameExpr distanceClass = ASTHelper.createNameExpr("Distance");
			String distanceMethod = "distance";
			
			// Distance.distance(expected_states[0], actual_states[0]) == 0.0d
			List<Expression> distanceStateArgs = new ArrayList<Expression>();
			distanceStateArgs.add(ASTUtils.createArrayAccess(EXPECTED_STATE, Integer.toString(i)));
			distanceStateArgs.add(ASTUtils.createArrayAccess(ACTUAL_STATE, Integer.toString(i)));
			Expression state = new MethodCallExpr(distanceClass, distanceMethod, distanceStateArgs);
			BinaryExpr stateCondition = new BinaryExpr(state, zeroDouble, japa.parser.ast.expr.BinaryExpr.Operator.equals);
			
			// Distance.distance(expected_results[0], actual_results[0]) == 0.0d
			List<Expression> distanceResultArgs = new ArrayList<Expression>();
			distanceResultArgs.add(ASTUtils.createArrayAccess(EXPECTED_RESULT, Integer.toString(i)));
			distanceResultArgs.add(ASTUtils.createArrayAccess(ACTUAL_RESULT, Integer.toString(i)));
			Expression result = new MethodCallExpr(distanceClass, distanceMethod, distanceResultArgs);
			BinaryExpr resultCondition = new BinaryExpr(result, zeroDouble, japa.parser.ast.expr.BinaryExpr.Operator.equals);
			
			// concatenate conditions
			BinaryExpr newCondition = new BinaryExpr(resultCondition, stateCondition, japa.parser.ast.expr.BinaryExpr.Operator.and);
			if (condition != null) {
				condition = new BinaryExpr(condition, newCondition, japa.parser.ast.expr.BinaryExpr.Operator.and);
			}
			else {
				condition = newCondition;
			}
		}
		
		IfStmt ifStmt = new IfStmt(condition, new ExpressionStmt(ASTUtils.createSystemOut("Executed")), null);
		ASTHelper.addStmt(stmt, ifStmt);
		set_results.setBody(stmt);
		
		logger.debug("Adding method_under_test method - done");
		
		return set_results;
	}

}
