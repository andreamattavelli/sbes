package sbes.stub.generator;

import japa.parser.ASTHelper;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import sbes.logging.Logger;
import sbes.result.CarvingResult;
import sbes.stub.GenerationException;
import sbes.stub.Stub;
import sbes.util.ASTUtils;
import sbes.util.ReflectionUtils;

public class SecondStageGenericStubGenerator extends SecondStageStubGenerator {
	
	private static final Logger logger = new Logger(SecondStageGenericStubGenerator.class);

	private String concreteClass;
	
	public SecondStageGenericStubGenerator(Stub stub, CarvingResult candidateES, String concreteClass) {
		super(stub, candidateES);
		this.concreteClass = concreteClass;
	}
	
	@Override
	protected TypeDeclaration getClassDeclaration(String className) {
		stubName = className + STUB_EXTENSION + "_2";
		String genericDecl = "<E extends " + concreteClass + ">";
		
		// extends base class
		ClassOrInterfaceType extendClassDecl = new ClassOrInterfaceType(className + "<E>");
		List<ClassOrInterfaceType> extendClasses = new ArrayList<ClassOrInterfaceType>();
		extendClasses.add(extendClassDecl);
		
		ClassOrInterfaceDeclaration classDecl = new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, stubName + genericDecl);
		classDecl.setExtends(extendClasses);
		
		return classDecl;
	}
	
	@Override
	protected MethodDeclaration getMethodUnderTest(Method targetMethod) {
		logger.debug("Adding method_under_test method");
		MethodDeclaration method_under_test = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "method_under_test");
		
		Type[] genericParams = targetMethod.getGenericParameterTypes();
		Class<?>[] concreteParams = targetMethod.getParameterTypes();
		List<Parameter> param = getGenericParameterType(genericParams, concreteParams);
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
			throw new GenerationException("Unable to carve candidate: no statements!");
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

	private List<Parameter> getGenericParameterType(Type[] genericParams, Class<?>[] concreteParams) {
		List<Parameter> toReturn = new ArrayList<Parameter>();
		for (int i = 0; i < genericParams.length; i++) {
			Type type = genericParams[i];
			VariableDeclaratorId id = new VariableDeclaratorId("p" + i);
			String typeClass;
			if (type instanceof TypeVariable<?>) {
				typeClass = concreteClass;
			} 
			else {
				typeClass = concreteParams[i].getCanonicalName();
			}
			typeClass = typeClass.indexOf(" ") >= 0 ? typeClass.split(" ")[1] : typeClass;
			int dimensions = ReflectionUtils.getArrayDimensionCount(concreteParams[i]);
			Parameter p = new Parameter(ASTHelper.createReferenceType(typeClass, dimensions), id);
			toReturn.add(p);
		}

		return toReturn;
	}

}
