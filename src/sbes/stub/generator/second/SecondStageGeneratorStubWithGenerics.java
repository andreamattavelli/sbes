package sbes.stub.generator.second;

import japa.parser.ASTHelper;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
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
import japa.parser.ast.type.ClassOrInterfaceType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sbes.exceptions.SBESException;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.Stub;
import sbes.util.ASTUtils;
import sbes.util.ClassUtils;
import sbes.util.GenericsUtils;
import sbes.util.ReflectionUtils;

public class SecondStageGeneratorStubWithGenerics extends SecondStageGeneratorStub {

	private static final Logger logger = new Logger(SecondStageGeneratorStubWithGenerics.class);

	private Map<TypeVariable<?>, String> genericToConcreteClasses;

	public SecondStageGeneratorStubWithGenerics(
			final List<TestScenario> scenarios, 
			final Stub stub,
			final CarvingResult candidateES,
			final List<FieldDeclaration> fields,
			final Map<TypeVariable<?>, String> genericToConcreteClasses) {
		super(scenarios, stub, candidateES, fields);
		this.genericToConcreteClasses = genericToConcreteClasses;
	}

	@Override
	protected TypeDeclaration getClassDeclaration(String className) {
		stubName = className + STUB_EXTENSION + "_2";

		// extends base class
		ClassOrInterfaceType extendClassDecl = new ClassOrInterfaceType(className + "<" + GenericsUtils.toGenericsString(genericToConcreteClasses) + ">");
		List<ClassOrInterfaceType> extendClasses = new ArrayList<ClassOrInterfaceType>();
		extendClasses.add(extendClassDecl);

		ClassOrInterfaceDeclaration classDecl = new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, stubName);
		classDecl.setExtends(extendClasses);

		return classDecl;
	}

	@Override
	protected MethodDeclaration getMethodUnderTest(Method targetMethod) {
		logger.debug("Adding method_under_test method with generics");
		MethodDeclaration method_under_test = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "method_under_test");

		Type[] genericParams = targetMethod.getGenericParameterTypes();
		Class<?>[] concreteParams = targetMethod.getParameterTypes();
		List<Parameter> param = getGenericParameterType(targetMethod, genericParams, concreteParams);
		method_under_test.setParameters(param);

		BlockStmt stmt = new BlockStmt();

		// Cloner c = new Cloner();
		ASTHelper.addStmt(stmt, createClonerObj());

		// CLASS clone = c.deepClone(this);
		ASTHelper.addStmt(stmt, createCloneObj(targetMethod));

		// RESULT_CLASS expected_result = this.METHOD
		ASTHelper.addStmt(stmt, createExpectedResult(targetMethod, param));

		// RESULT_CLASS actual_result = clone.CARVED_METHOD(S)
		List<Statement> stmts = createActualResult(targetMethod, candidateES, param);
		if (stmts.isEmpty()) {
			throw new SBESException("Unable to carve candidate: no statements!");
		}
		equivalence = stmts;
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

	protected ExpressionStmt createCloneObj(Method targetMethod) {
		List<Expression> methodParameters = new ArrayList<Expression>();
		methodParameters.add(new ThisExpr());
		Expression right = new MethodCallExpr(ASTHelper.createNameExpr("c"), "deepClone", methodParameters);
		List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
		vars.add(new VariableDeclarator(new VariableDeclaratorId("clone")));
		String className = ClassUtils.getSimpleClassname(Options.I().getTargetMethod());
		Expression left = new VariableDeclarationExpr(ASTHelper.createReferenceType(className + "<" + GenericsUtils.toGenericsString(genericToConcreteClasses) + ">", 0), vars);
		AssignExpr assignment = new AssignExpr(left, right, Operator.assign);
		return new ExpressionStmt(assignment);
	}

	@Override
	protected Statement createExpectedResult(final Method targetMethod, final List<Parameter> parameters) {
		Type[] genericParameters = targetMethod.getGenericParameterTypes();
		List<Expression> methodParameters = new ArrayList<Expression>();
		for (int i = 0; i < genericParameters.length; i++) {
			Type param = genericParameters[i];
			if (param instanceof Class<?>) {
				methodParameters.add(ASTHelper.createNameExpr(parameters.get(i).getId().getName()));
			}
			else {
				if (param instanceof TypeVariable<?>) {
					for (TypeVariable<?> type : genericToConcreteClasses.keySet()) {
						if (type.getName().equals(param.toString())) {
							methodParameters.add(ASTHelper.createNameExpr(parameters.get(i).getId().getName()));
						}
					}
				}
				else if (param instanceof ParameterizedType) {
					methodParameters.add(ASTHelper.createNameExpr(parameters.get(i).getId().getName()));
				}
			}
		}

		Expression right = new MethodCallExpr(new ThisExpr(), targetMethod.getName(), methodParameters);
		if (!targetMethod.getReturnType().equals(void.class)) {
			List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
			vars.add(new VariableDeclarator(new VariableDeclaratorId("expected_result")));
			String className = targetMethod.getGenericReturnType().toString();
			Set<TypeVariable<?>> types = genericToConcreteClasses.keySet();
			for (TypeVariable<?> typeVariable : types) {
				if (className.contains(typeVariable.toString())) {
					className = className.replaceAll(typeVariable.toString(), genericToConcreteClasses.get(typeVariable));
				}
			}
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

	protected String getActualResultType(Method targetMethod) {
		String className = targetMethod.getGenericReturnType().toString();
		Set<TypeVariable<?>> types = genericToConcreteClasses.keySet();
		for (TypeVariable<?> typeVariable : types) {
			if (className.contains(typeVariable.toString())) {
				className = className.replaceAll(typeVariable.toString(), genericToConcreteClasses.get(typeVariable));
			}
		}

		return className;
	}

	private List<Parameter> getGenericParameterType(Method targetMethod, Type[] genericParams, Class<?>[] concreteParams) {
		List<Parameter> toReturn = new ArrayList<Parameter>();
		for (int i = 0; i < genericParams.length; i++) {
			Type type = genericParams[i];
			VariableDeclaratorId id = new VariableDeclaratorId("p" + i);
			String typeClass;
			if (type instanceof TypeVariable<?>) {
				typeClass = type.toString();
				Set<TypeVariable<?>> types = genericToConcreteClasses.keySet();
				for (TypeVariable<?> typeVariable : types) {
					if (typeClass.contains(typeVariable.toString())) {
						typeClass = typeClass.replaceAll(typeVariable.toString(), genericToConcreteClasses.get(typeVariable));
					}
				}
			} 
			else if (type instanceof ParameterizedType) {
				typeClass = type.toString();
				Set<TypeVariable<?>> types = genericToConcreteClasses.keySet();
				for (TypeVariable<?> typeVariable : types) {
					if (typeClass.contains(typeVariable.toString())) {
						typeClass = typeClass.replaceAll(typeVariable.toString(), genericToConcreteClasses.get(typeVariable));
					}
				}
			}
			else {
				typeClass = concreteParams[i].getCanonicalName();
				typeClass = typeClass.indexOf(" ") >= 0 ? typeClass.split(" ")[1] : typeClass;
			}
			
			int dimensions = ReflectionUtils.getArrayDimensionCount(concreteParams[i]);
			Parameter p = new Parameter(ASTHelper.createReferenceType(typeClass, dimensions), id);
			toReturn.add(p);
		}

		return toReturn;
	}

}
