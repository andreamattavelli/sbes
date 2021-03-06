package sbes.stub.generator.first;

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
import japa.parser.ast.expr.ArrayAccessExpr;
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
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.EquivalenceRepository;
import sbes.scenario.TestScenario;
import sbes.stub.Stub;
import sbes.stub.generator.AbstractStubGenerator;
import sbes.util.ASTUtils;
import sbes.util.AsmParameterNames;
import sbes.util.ReflectionUtils;

public class FirstStageGeneratorStub extends AbstractStubGenerator {

	private static final Logger logger = new Logger(FirstStageGeneratorStub.class);
	
	public static final String NUM_SCENARIOS = "NUM_SCENARIOS";
	public static final String EXPECTED_STATE = "expected_states";
	public static final String EXPECTED_RESULT = "expected_results";
	public static final String ACTUAL_STATE = "actual_states";
	public static final String ACTUAL_RESULT = "actual_results";
	
	public FirstStageGeneratorStub(final List<TestScenario> scenarios) {
		super(scenarios);
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
	protected TypeDeclaration getClassDeclaration(Class<?> c) {
		stubName = c.getSimpleName() + STUB_EXTENSION;
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
		if (!targetMethod.getReturnType().equals(void.class)) {
			declarations.add(ASTUtils.createStubHelperArray(ASTUtils.getReturnType(targetMethod).toString(), EXPECTED_RESULT));
			declarations.add(ASTUtils.createStubHelperArray(ASTUtils.getReturnType(targetMethod).toString(), ACTUAL_RESULT));
		}
		declarations.add(ASTUtils.createStubHelperArray(c.getCanonicalName(), EXPECTED_STATE));
		declarations.add(ASTUtils.createStubHelperArray(c.getCanonicalName(), ACTUAL_STATE));
		
		for (TestScenario scenario : scenarios) {
			declarations.addAll(scenario.getInputAsFields());
		}
		
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
			List<Statement> scenarioStmt = scenarios.get(i).getScenario().getStmts();
			for (Statement statement : scenarioStmt) {
				if (statement != null) {
					statements.add(statement);
				}
			}
		}
		body.setStmts(statements);
		constructor.setBlock(body);
		constructors.add(constructor);
		return constructors;
	}
	
	@Override
	protected List<BodyDeclaration> getAdditionalMethods(Method targetMethod, Method[] methods, Class<?> c) {
		logger.debug("Adding original class method wrappers");
		
		boolean collectionReturn = false;
		boolean isSizePresent = false;
		
		List<BodyDeclaration> members = new ArrayList<BodyDeclaration>();
		methods = preventMethodBloat(targetMethod, methods);
		for (Method method : methods) {
			if (ReflectionUtils.methodFilter(method)) {
				continue;
			}
			else if (method.equals(targetMethod)) {
				continue;
			}
			else if (EquivalenceRepository.getInstance().isExcluded(method)) {
				logger.debug("Excluded from stub: " + method.toString());
				continue;
			}
			
			if (Collection.class.isAssignableFrom(method.getReturnType())) {
				collectionReturn = true;
			}
			else if (method.getName().equals("size")) {
				isSizePresent = true;
			}
			
			String paramsNames[] = AsmParameterNames.getParameterNames(method);
			
			Type returnType = ASTUtils.getReturnType(method);
			Type returnStubType = ASTUtils.getReturnTypeAsArray(method);
			MethodDeclaration md;
			if (scenarios.size() > 1) {
				md = new MethodDeclaration(method.getModifiers() & Modifier.TRANSIENT & Modifier.VOLATILE | Modifier.PUBLIC, returnStubType, method.getName());
			}
			else {
				md = new MethodDeclaration(method.getModifiers() & Modifier.TRANSIENT & Modifier.VOLATILE | Modifier.PUBLIC, returnType, method.getName());
			}
			
			
			//parameters
			List<Parameter> parameters = new ArrayList<Parameter>();
			parameters.addAll(getParameterType(method.getParameterTypes(), paramsNames, method.isVarArgs()));
			md.setParameters(parameters);
			
			//body
			BlockStmt stmt = new BlockStmt();
			List<Statement> stmts = new ArrayList<Statement>();
			VariableDeclarationExpr res = ASTHelper.createVariableDeclarationExpr(returnStubType, "res");
			
			// for loop
			List<Expression> init = ASTUtils.createForInit("i_", ASTHelper.INT_TYPE, new IntegerLiteralExpr("0"), Operator.assign);
			Expression compare = ASTUtils.createForCondition("i_", NUM_SCENARIOS, japa.parser.ast.expr.BinaryExpr.Operator.less);
			List<Expression> update = ASTUtils.createForIncrement("i_", japa.parser.ast.expr.UnaryExpr.Operator.posIncrement);
			
			// for loop body
			List<Expression> methodParameters = ASTUtils.createParameters(parameters, paramsNames, scenarios.size() > 1);
			Expression right;
			if (scenarios.size() > 1) {
				right = new MethodCallExpr(ASTUtils.createArrayAccess(ACTUAL_STATE, "i_"), method.getName(), methodParameters);
			}
			else {
				right = new MethodCallExpr(ASTUtils.createArrayAccess(ACTUAL_STATE, "0"), method.getName(), methodParameters);
			}

			
			BlockStmt body = new BlockStmt();
			if (returnStubType.toString().equals("void")) {
				// return type void - no need for return array
				ASTHelper.addStmt(body, right);
			}
			else if (scenarios.size() > 1) {
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
				
				Expression left = ASTUtils.createArrayAccess("res", "i_");
				AssignExpr callResult = new AssignExpr(left, right, Operator.assign);
				ASTHelper.addStmt(body, callResult);
			}
			
			if (scenarios.size() > 1) {
				ForStmt forStmt = new ForStmt(init, compare, update, body);
				stmts.add(forStmt);
			}
			
			if (!returnStubType.toString().equals("void")) {
				if (scenarios.size() > 1) {
					ReturnStmt ret = new ReturnStmt(ASTHelper.createNameExpr("res"));
					stmts.add(ret);
				}
				else {
					ReturnStmt ret = new ReturnStmt(right);
					stmts.add(ret);
				}
			}
			else {
				if (scenarios.size() == 1) {
					md.setBody(body);
				}
				else {
					stmt.setStmts(stmts);
					md.setBody(stmt);
				}
			}

			if (!returnStubType.toString().equals("void")) {
				stmt.setStmts(stmts);
				md.setBody(stmt);
			}
			
			members.add(md);
		}
		
		if (isSizePresent) {
			addRealSize(members);
		}
		if (collectionReturn) {
			addCollectionSize(members);
		}
		
		logger.debug("Generated " + members.size() + " class method wrappers");
		logger.debug("Adding original class method wrappers - done");
		
		return members;
	}
	
	protected void addRealSize(List<BodyDeclaration> members) {
		if (scenarios.size() > 1) {
			Type returnStubType = ASTHelper.createReferenceType("Integer", 1);
			MethodDeclaration md = new MethodDeclaration(Modifier.PUBLIC, returnStubType, "realSize");

			//body
			BlockStmt stmt = new BlockStmt();
			List<Statement> stmts = new ArrayList<Statement>();

			VariableDeclarationExpr res = ASTHelper.createVariableDeclarationExpr(returnStubType, "res");

			// for loop
			List<Expression> init = ASTUtils.createForInit("i", ASTHelper.INT_TYPE, new IntegerLiteralExpr("0"), Operator.assign);
			Expression compare = ASTUtils.createForCondition("i", NUM_SCENARIOS, japa.parser.ast.expr.BinaryExpr.Operator.less);
			List<Expression> update = ASTUtils.createForIncrement("i", japa.parser.ast.expr.UnaryExpr.Operator.posIncrement);

			MethodCallExpr mce = new MethodCallExpr(ASTUtils.createArrayAccess("actual_states", "i"), "size");

			// for loop body
			Expression right = new BinaryExpr(mce, new IntegerLiteralExpr("1"), japa.parser.ast.expr.BinaryExpr.Operator.minus);

			BlockStmt body = new BlockStmt();
			List<Expression> arraysDimension = ASTUtils.getArraysDimension();
			ArrayCreationExpr ace = new ArrayCreationExpr(ASTHelper.createReferenceType("Integer", 0), arraysDimension, 0);
			AssignExpr resAssign = new AssignExpr(res, ace, Operator.assign);
			ExpressionStmt resStmt = new ExpressionStmt(resAssign);
			stmts.add(resStmt);

			Expression left = ASTUtils.createArrayAccess("res", "i");
			AssignExpr callResult = new AssignExpr(left, right, Operator.assign);
			ASTHelper.addStmt(body, callResult);

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
		else {
			Type returnStubType = ASTHelper.createReferenceType("int", 0);
			MethodDeclaration md = new MethodDeclaration(Modifier.PUBLIC, returnStubType, "realSize");
			
			MethodCallExpr mce = new MethodCallExpr(ASTUtils.createArrayAccess("actual_states", "0"), "size");
			Expression right = new BinaryExpr(mce, new IntegerLiteralExpr("1"), japa.parser.ast.expr.BinaryExpr.Operator.minus);
			//body
			BlockStmt stmt = new BlockStmt();
			List<Statement> stmts = new ArrayList<Statement>();
			ReturnStmt ret = new ReturnStmt(right);
			stmts.add(ret);

			stmt.setStmts(stmts);
			md.setBody(stmt);
			
			members.add(md);
		}
	}
	
	protected void addCollectionSize(List<BodyDeclaration> members) {
		Type returnStubType;
		if (scenarios.size() > 1) {
			returnStubType = ASTHelper.createReferenceType("int", 1);
			MethodDeclaration md = new MethodDeclaration(Modifier.PUBLIC, returnStubType, "collectionSize");
			List<Parameter> parameters = new ArrayList<Parameter>();
			parameters.add(new Parameter(ASTHelper.createReferenceType("java.util.Collection", 1), new VariableDeclaratorId("p0")));
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
			Expression right = new MethodCallExpr(new ArrayAccessExpr(new NameExpr("p0"), new NameExpr("i")), "size");

			BlockStmt body = new BlockStmt();
			List<Expression> arraysDimension = ASTUtils.getArraysDimension();
			ArrayCreationExpr ace = new ArrayCreationExpr(ASTHelper.createReferenceType("int", 0), arraysDimension, 0);
			AssignExpr resAssign = new AssignExpr(res, ace, Operator.assign);
			ExpressionStmt resStmt = new ExpressionStmt(resAssign);
			stmts.add(resStmt);

			Expression left = ASTUtils.createArrayAccess("res", "i");
			AssignExpr callResult = new AssignExpr(left, right, Operator.assign);
			ASTHelper.addStmt(body, callResult);
			
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
		else {
			returnStubType = ASTHelper.createReferenceType("int", 0);
			MethodDeclaration md = new MethodDeclaration(Modifier.PUBLIC, returnStubType, "collectionSize");
			List<Parameter> parameters = new ArrayList<Parameter>();
			parameters.add(new Parameter(ASTHelper.createReferenceType("java.util.Collection", 0), new VariableDeclaratorId("p0")));
			md.setParameters(parameters);
			
			//body
			BlockStmt stmt = new BlockStmt();
			List<Statement> stmts = new ArrayList<Statement>();
			ReturnStmt ret = new ReturnStmt(new MethodCallExpr(new NameExpr("p0"), "size"));
			stmts.add(ret);

			stmt.setStmts(stmts);
			md.setBody(stmt);
			
			members.add(md);
		}
	}

	protected List<Parameter> getParameterType(Class<?>[] parameters, String paramNames[], boolean isVarArgs) {
		List<Parameter> toReturn = new ArrayList<Parameter>();
		for (int i = 0; i < parameters.length; i++) {
			Class<?> type = parameters[i];
			VariableDeclaratorId id;
			if (paramNames.length > i) {
				id = new VariableDeclaratorId(paramNames[i]);
			}
			else {
				id = new VariableDeclaratorId("p" + i);
			}
			String typeClass = type.getCanonicalName();
			typeClass = typeClass.indexOf(" ") >= 0 ? typeClass.split(" ")[1]: typeClass;
			if (i == parameters.length - 1 && isVarArgs) {
				typeClass = typeClass.replace("[]", "");
			}
			Parameter p;
			if (scenarios.size() > 1 && paramNames.length > i  && AsmParameterNames.isSizeParam(paramNames[i])) {
				p = new Parameter(ASTHelper.createReferenceType(typeClass, 1), id);
			}
			else {
				p = new Parameter(ASTHelper.createReferenceType(typeClass, 0), id);
			}
			if (i == parameters.length - 1 && isVarArgs) {
				p.setVarArgs(isVarArgs);
			}
			toReturn.add(p);
		}
		
		return toReturn;
	}

	protected Method[] preventMethodBloat(Method targetMethod, Method[] methods) {
		if (methods.length > Options.I().getMethodBloatFactor()) {
			logger.debug("Preventing method bloat");
			List<Method> toReturn = new ArrayList<Method>();
			for (Method method : methods) {
				if (method.getDeclaringClass().equals(Object.class) ||
						method.getName().equals("toArray") ||
						method.getName().contains("iterator") ||
						method.getName().contains("Iterator") ||
						method.getName().contains("capacity") ||
						method.getName().contains("Capacity") ||
						method.getName().contains("copyInto") ||
						method.getName().contains("trimToSize") ||
						Enumeration.class.isAssignableFrom(method.getReturnType())) {
					logger.debug(" * Removed method " + method);
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
		if (scenarios.size() > 1) {
			parameters.add(new Parameter(ASTHelper.createReferenceType(ASTUtils.getReturnType(targetMethod).toString(), 1), new VariableDeclaratorId("res")));
		}
		else {
			parameters.add(new Parameter(ASTHelper.createReferenceType(ASTUtils.getReturnType(targetMethod).toString(), 0), new VariableDeclaratorId("res")));
		}
		set_results.setParameters(parameters);
		
		List<Expression> init = ASTUtils.createForInit("i", ASTHelper.INT_TYPE, new IntegerLiteralExpr("0"), Operator.assign);
		Expression compare = ASTUtils.createForCondition("i", NUM_SCENARIOS, japa.parser.ast.expr.BinaryExpr.Operator.less);
		List<Expression> update = ASTUtils.createForIncrement("i", japa.parser.ast.expr.UnaryExpr.Operator.posIncrement);
		
		BlockStmt forBody = new BlockStmt();
		Expression left;
		if (scenarios.size() > 1) {
			left = ASTUtils.createArrayAccess(ACTUAL_RESULT, "i");
		}
		else {
			left = ASTUtils.createArrayAccess(ACTUAL_RESULT, "0");
		}
		Expression right;
		if (scenarios.size() > 1) {
			right = ASTUtils.createArrayAccess("res", "i");
		}
		else {
			right = new NameExpr("res");
		}
		AssignExpr assignment = new AssignExpr(left, right, Operator.assign);
		ASTHelper.addStmt(forBody, assignment);
		
		if (scenarios.size() > 1) {
			ForStmt forStmt = new ForStmt(init, compare, update, forBody);
			BlockStmt methodBody = new BlockStmt();
			ASTHelper.addStmt(methodBody, forStmt);
			set_results.setBody(methodBody);
		}
		else {
			set_results.setBody(forBody);
		}
		
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
			Expression state = null;
			BinaryExpr stateCondition = null;
			if (!Modifier.isStatic(targetMethod.getModifiers())) {
				List<Expression> distanceStateArgs = new ArrayList<Expression>();
				distanceStateArgs.add(ASTUtils.createArrayAccess(EXPECTED_STATE, Integer.toString(i)));
				distanceStateArgs.add(ASTUtils.createArrayAccess(ACTUAL_STATE, Integer.toString(i)));
				state = new MethodCallExpr(distanceClass, distanceMethod, distanceStateArgs);
				stateCondition = new BinaryExpr(state, zeroDouble, japa.parser.ast.expr.BinaryExpr.Operator.equals);
			}
			
			// Distance.distance(expected_results[0], actual_results[0]) == 0.0d
			if (!targetMethod.getReturnType().equals(void.class)) {
				List<Expression> distanceResultArgs = new ArrayList<Expression>();
				distanceResultArgs.add(ASTUtils.createArrayAccess(EXPECTED_RESULT, Integer.toString(i)));
				distanceResultArgs.add(ASTUtils.createArrayAccess(ACTUAL_RESULT, Integer.toString(i)));
				Expression result = new MethodCallExpr(distanceClass, distanceMethod, distanceResultArgs);
				BinaryExpr resultCondition = new BinaryExpr(result, zeroDouble, japa.parser.ast.expr.BinaryExpr.Operator.equals);
				
				// concatenate conditions
				BinaryExpr newCondition;
				if (stateCondition == null) {
					newCondition = resultCondition;
				}
				else {
					newCondition = new BinaryExpr(resultCondition, stateCondition, japa.parser.ast.expr.BinaryExpr.Operator.and);
				}
				if (condition != null) {
					condition = new BinaryExpr(condition, newCondition, japa.parser.ast.expr.BinaryExpr.Operator.and);
				}
				else {
					condition = newCondition;
				}
			}
			else {
				// concatenate conditions
				if (condition != null) {
					condition = new BinaryExpr(condition, stateCondition, japa.parser.ast.expr.BinaryExpr.Operator.and);
				}
				else {
					condition = stateCondition;
				}
			}
		}
		
		IfStmt ifStmt = new IfStmt(condition, new ExpressionStmt(ASTUtils.createSystemOut("Executed")), null);
		ASTHelper.addStmt(stmt, ifStmt);
		set_results.setBody(stmt);
		
		logger.debug("Adding method_under_test method - done");
		
		return set_results;
	}

}
