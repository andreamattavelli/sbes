package sbes.stub.generator.second.symbolic;

import japa.parser.ASTHelper;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sbes.exceptions.GenerationException;
import sbes.logging.Logger;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.Stub;
import sbes.util.GenericsUtils;
import sbes.util.ReflectionUtils;

public class SecondStageGeneratorStubWithGenericsSE extends SecondStageGeneratorStubSE {

	private static final Logger logger = new Logger(SecondStageGeneratorStubWithGenericsSE.class);
	
	private Map<TypeVariable<?>, String> genericToConcreteClasses;

	public SecondStageGeneratorStubWithGenericsSE(
			final List<TestScenario> scenarios, 
			final Stub stub,
			final CarvingResult candidateES,
			final Map<TypeVariable<?>, String> genericToConcreteClasses) {
		super(scenarios, stub, candidateES);
		this.genericToConcreteClasses = genericToConcreteClasses;
	}
	
	@Override
	protected List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c) {
		List<BodyDeclaration> fields = new ArrayList<>();
		fields.add(new ClassOrInterfaceDeclaration(Modifier.PRIVATE, true, "FakeVariable"));
		
		// class variables
		String referenceType = c.getSimpleName() + "<" + GenericsUtils.toGenericsString(genericToConcreteClasses) + ">";
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType(referenceType, 0), "v_" + c.getSimpleName() + "1"));
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType(referenceType, 0), "v_" + c.getSimpleName() + "2"));
		
		// fake variables
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType("FakeVariable", 0), "forceConservativeRepOk"));
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType("FakeVariable", 0), "forceConservativeRepOk2"));
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType("FakeVariable", 0), "forceConservativeRepOk3"));
		// return fields
		if (!targetMethod.getReturnType().equals(void.class)) {
			String resultType = getActualResultType(targetMethod);
			fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType(resultType, 0), EXP_RES));
			fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType(resultType, 0), ACT_RES));
		}
		// exception fields
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType("Exception", 0), "e1"));
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType("Exception", 0), "e2"));
		Type[] genericParams = targetMethod.getGenericParameterTypes();
		Class<?>[] concreteParams = targetMethod.getParameterTypes();
		List<Parameter> param = getGenericParameterType(targetMethod, genericParams, concreteParams);
		for (int i = 0; i < param.size(); i++) {
			Parameter p = param.get(i);
			fields.add(ASTHelper.createFieldDeclaration(0, p.getType(), p.getId().getName()));
		}
		return fields;
	}
	
	@Override
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
	
	protected List<Parameter> getGenericParameterType(Method targetMethod, Type[] genericParams, Class<?>[] concreteParams) {
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
	
	@Override
	protected MethodDeclaration getMethodUnderTest(Method targetMethod) {
		logger.debug("Adding method_under_test method");
		MethodDeclaration method_under_test = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "method_under_test");
		
		Type[] genericParams = targetMethod.getGenericParameterTypes();
		Class<?>[] concreteParams = targetMethod.getParameterTypes();
		List<Parameter> param = getGenericParameterType(targetMethod, genericParams, concreteParams);
		
		BlockStmt body = new BlockStmt();
		
		// variable init
		body.setStmts(new ArrayList<Statement>());
		body.getStmts().addAll(initVariables(targetMethod));
		
		//try-catch original method
		ASTHelper.addStmt(body, createExpectedResult(targetMethod, param));

		//try-catch candidate equivalence
		List<Statement> stmts = createActualResult(targetMethod, candidateES, param);
		if (stmts.isEmpty()) {
			throw new GenerationException("Unable to carve candidate: no statements!");
		}
		equivalence = stmts;
		ASTHelper.addStmt(body, getTry(new BlockStmt(stmts), getCatchClause("e2")));

		//assert conservative
		ASTHelper.addStmt(body, new ExpressionStmt(createOkVariable()));
		ASTHelper.addStmt(body, new ExpressionStmt(createFakeVariable("fake", "forceConservativeRepOk")));
		ASTHelper.addStmt(body, getAnalysisMethod("ass3rt", ASTHelper.createNameExpr("ok")));
		//assert equal returns
		if (!targetMethod.getReturnType().equals(void.class)) {
			IfStmt ifReturns = createCheckOnReturns(targetMethod);
			ASTHelper.addStmt(body, ifReturns);
			ASTHelper.addStmt(body, new ExpressionStmt(createFakeVariable("fake2", "forceConservativeRepOk2")));
			ASTHelper.addStmt(body, getAnalysisMethod("ass3rt", ASTHelper.createNameExpr("ok")));
		}
		//assert equal exceptions
		IfStmt ifExceptions = createCheckOnExceptions();
		ASTHelper.addStmt(body, ifExceptions);
		ASTHelper.addStmt(body, new ExpressionStmt(createFakeVariable("fake3", "forceConservativeRepOk3")));
		ASTHelper.addStmt(body, getAnalysisMethod("ass3rt", ASTHelper.createNameExpr("ok")));
		
		method_under_test.setBody(body);
		
		return method_under_test;
	}
	
}
