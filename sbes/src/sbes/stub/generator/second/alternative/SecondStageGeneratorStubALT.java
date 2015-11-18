package sbes.stub.generator.second.alternative;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import japa.parser.ASTHelper;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;
import sbes.exceptions.GenerationException;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.CounterexampleStub;
import sbes.stub.Stub;
import sbes.stub.generator.second.SecondStageGeneratorStub;
import sbes.util.ASTUtils;
import sbes.util.ClassUtils;
import sbes.util.ReflectionUtils;

public class SecondStageGeneratorStubALT extends SecondStageGeneratorStub {

	private static final Logger logger = new Logger(SecondStageGeneratorStubALT.class);
	
	protected final static String classParameterName = "instance";
	protected String className;

	public SecondStageGeneratorStubALT(List<TestScenario> scenarios, Stub stub, CarvingResult candidateES) {
		super(scenarios, stub, candidateES);
	}	

	@Override
	public Stub generateStub() {
		logger.info("Generating stub for second phase with alternative structure");
		Stub stub = super.generateStub();
		CounterexampleStub counterexampleStub = new CounterexampleStub(stub.getAst(), stub.getStubName(), equivalence);
		logger.info("Generating stub for second phase with alternative structure - done");
		return counterexampleStub;
	}

	@Override
	protected TypeDeclaration getClassDeclaration(Class<?> c) {
		stubName = c.getSimpleName() + STUB_EXTENSION + "_2";
		className = c.getCanonicalName();
		return new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, stubName);
	}

	@Override
	protected List<BodyDeclaration> getStubConstructor(Method targetMethod, Class<?> c) {
		return new ArrayList<BodyDeclaration>();
	}

	@Override
	protected MethodDeclaration getMethodUnderTest(Method targetMethod) {
		logger.debug("Adding method_under_test method");
		MethodDeclaration method_under_test = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "method_under_test");

		Class<?>[] parameters = targetMethod.getParameterTypes();
		List<Parameter> param = getParameterType(parameters, targetMethod);
		method_under_test.setParameters(param);

		BlockStmt stmt = new BlockStmt();

		// Cloner c = new Cloner();
		ASTHelper.addStmt(stmt, createClonerObj());

		// CLASS clone = c.deepClone(this);
		ASTHelper.addStmt(stmt, createCloneObj(targetMethod));

		// RESULT_CLASS expected_result = this.METHOD
		ASTHelper.addStmt(stmt, createExpectedResult(targetMethod, param.subList(1, param.size())));

		// RESULT_CLASS actual_result = clone.CARVED_METHOD(S)
		List<Statement> stmts = createActualResult(targetMethod, candidateES, param.subList(1, param.size()));
		if (stmts.isEmpty()) {
			throw new GenerationException("Unable to carve candidate: no statements!");
		}
		equivalence = stmts;
		stmt.getStmts().addAll(stmts);

		method_under_test.setParameters(param);

		NameExpr distanceClass = ASTHelper.createNameExpr("Distance");
		String distanceMethod = "distance";

		// if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)
		Expression zeroDouble = new DoubleLiteralExpr("0.0d");
		List<Expression> distanceStateArgs = new ArrayList<Expression>();
		distanceStateArgs.add(new ThisExpr());
		distanceStateArgs.add(ASTHelper.createNameExpr("clone"));
		Expression state = new MethodCallExpr(distanceClass, distanceMethod, distanceStateArgs);
		BinaryExpr stateCondition = new BinaryExpr(state, zeroDouble, japa.parser.ast.expr.BinaryExpr.Operator.greater);

		BinaryExpr ifCondition;
		if (!targetMethod.getReturnType().equals(void.class)) {
			List<Expression> distanceResultArgs = new ArrayList<Expression>();
			distanceResultArgs.add(ASTHelper.createNameExpr("expected_result"));
			distanceResultArgs.add(ASTHelper.createNameExpr("actual_result"));
			Expression result = new MethodCallExpr(distanceClass, distanceMethod, distanceResultArgs);
			BinaryExpr resultCondition = new BinaryExpr(result, zeroDouble, japa.parser.ast.expr.BinaryExpr.Operator.greater);

			ifCondition = new BinaryExpr(resultCondition, stateCondition, japa.parser.ast.expr.BinaryExpr.Operator.or);
		}
		else {
			ifCondition = stateCondition;
		}

		IfStmt ifStmt = new IfStmt(ifCondition, new ExpressionStmt(ASTUtils.createSystemOut("Executed")), null);
		ASTHelper.addStmt(stmt, ifStmt);

		method_under_test.setBody(stmt);

		return method_under_test;
	}

	@Override
	protected ExpressionStmt createCloneObj(Method targetMethod) {
		List<Expression> methodParameters = new ArrayList<Expression>();
		methodParameters.add(ASTHelper.createNameExpr(classParameterName));
		Expression right = new MethodCallExpr(ASTHelper.createNameExpr("c"), "deepClone", methodParameters);
		List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
		vars.add(new VariableDeclarator(new VariableDeclaratorId("clone")));
		String className = ClassUtils.getSimpleClassname(Options.I().getTargetMethod());
		Expression left = new VariableDeclarationExpr(ASTHelper.createReferenceType(className, 0), vars);
		AssignExpr assignment = new AssignExpr(left, right, Operator.assign);
		return new ExpressionStmt(assignment);
	}
	
	@Override
	protected Statement createExpectedResult(Method targetMethod, final List<Parameter> parameters) {
		List<Expression> methodParameters = new ArrayList<Expression>();
		for (int i = 0; i < parameters.size(); i++) {
			Parameter parameter = parameters.get(i);
			methodParameters.add(ASTHelper.createNameExpr(parameter.getId().getName()));
		}
		Expression right = new MethodCallExpr(ASTHelper.createNameExpr(classParameterName), targetMethod.getName(), methodParameters);
		if (!targetMethod.getReturnType().equals(void.class)) {
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
		return new ExpressionStmt(right);
	}

	@Override
	protected List<Parameter> getParameterType(Class<?>[] parameters, Method targetMethod) {
		List<Parameter> toReturn = new ArrayList<Parameter>();
		toReturn.add(new Parameter(ASTHelper.createReferenceType(className, 0), new VariableDeclaratorId(classParameterName)));
		for (int i = 0; i < parameters.length; i++) {
			Class<?> type = parameters[i];
			VariableDeclaratorId id = new VariableDeclaratorId("p" + i);
			String typeClass = type.getCanonicalName();
			typeClass = typeClass.indexOf(" ") >= 0 ? typeClass.split(" ")[1]: typeClass;
			if (targetMethod.isVarArgs() && i == parameters.length - 1) {
				typeClass = typeClass.replace("[]", "");
			}
			int cardinality = ReflectionUtils.arrayCardinality(type);
			Parameter p = new Parameter(ASTHelper.createReferenceType(typeClass, cardinality), id);
			toReturn.add(p);
		}

		return toReturn;
	}

}
