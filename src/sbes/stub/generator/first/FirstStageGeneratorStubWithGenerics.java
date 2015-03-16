package sbes.stub.generator.first;

import japa.parser.ASTHelper;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sbes.exceptions.GenerationException;
import sbes.logging.Logger;
import sbes.result.EquivalenceRepository;
import sbes.scenario.GenericTestScenario;
import sbes.scenario.TestScenario;
import sbes.util.ASTUtils;
import sbes.util.AsmParameterNames;
import sbes.util.MethodUtils;

public class FirstStageGeneratorStubWithGenerics extends FirstStageGeneratorStub {

	private static final Logger logger = new Logger(FirstStageGeneratorStubWithGenerics.class);
	
	private String concreteClass;
	
	public FirstStageGeneratorStubWithGenerics(List<TestScenario> scenarios) {
		super(scenarios);
		checkConcreteClasses();
	}
	
	private void checkConcreteClasses() {
		Map<String, Integer> concretes = new HashMap<String, Integer>();
		for (TestScenario scenario : scenarios) {
			if (scenario instanceof GenericTestScenario) {
				GenericTestScenario generic = (GenericTestScenario) scenario;
				if (concretes.containsKey(generic.getGenericClass())) {
					Integer i = concretes.get(generic.getGenericClass());
					concretes.put(generic.getGenericClass().get(0), ++i); //FIXME
				}
				else {
					concretes.put(generic.getGenericClass().get(0), 1); //FIXME
				}
			}
		}
		if (concretes.keySet().size() > 1) {
			logger.warn("Many concrete types for the same generic type. Choosing the most used one");
			throw new GenerationException("Many concrete types for the same generic type. NOT IMPLEMENTED YET");
		}
		else {
			for (String concrete : concretes.keySet()) { //trick
				concreteClass = concrete;
				break;
			}
		}
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
			declarations.add(ASTUtils.createStubHelperArray(ASTUtils.getReturnConcreteType(generics, concreteClass, targetMethod).toString(), EXPECTED_RESULT));
			declarations.add(ASTUtils.createStubHelperArray(ASTUtils.getReturnConcreteType(generics, concreteClass, targetMethod).toString(), ACTUAL_RESULT));
		}
		declarations.add(ASTUtils.createGenericStubHelperArray(c.getCanonicalName(), concreteClass, EXPECTED_STATE));
		declarations.add(ASTUtils.createGenericStubHelperArray(c.getCanonicalName(), concreteClass, ACTUAL_STATE));
		
		for (TestScenario scenario : scenarios) {
			declarations.addAll(scenario.getInputAsFields());
		}
		
		logger.debug("Adding class fields - done");
		
		return declarations;
	}
	
	@Override
	protected List<BodyDeclaration> getAdditionalMethods(Method targetMethod, Method[] methods) {
		logger.debug("Adding original class method wrappers");
		
		boolean collectionReturn = false;
		boolean isSizePresent = false;
		
		List<BodyDeclaration> members = new ArrayList<BodyDeclaration>();
		methods = preventMethodBloat(targetMethod, methods);
		for (Method method : methods) {
			if (MethodUtils.methodFilter(method)) {
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
			
			Type returnType = ASTUtils.getReturnConcreteType(generics, concreteClass, method);
			Type returnStubType = ASTUtils.getReturnConcreteTypeAsArray(generics, concreteClass, method);
			MethodDeclaration md;
			if (scenarios.size() > 1) {
				md = new MethodDeclaration(method.getModifiers() & Modifier.TRANSIENT & Modifier.VOLATILE | Modifier.PUBLIC, returnStubType, method.getName());
			}
			else {
				md = new MethodDeclaration(method.getModifiers() & Modifier.TRANSIENT & Modifier.VOLATILE | Modifier.PUBLIC, returnType, method.getName());
			}
			
			//parameters
			List<Parameter> parameters = new ArrayList<Parameter>();
			parameters.addAll(getParameterType(method.getParameterTypes(), method.getGenericParameterTypes(), paramsNames));
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
	
	protected List<Parameter> getParameterType(Class<?>[] parameterTypes, java.lang.reflect.Type[] genericParameterTypes, String paramNames[]) {
		List<Parameter> toReturn = new ArrayList<Parameter>();
		for (int i = 0; i < parameterTypes.length; i++) {
			VariableDeclaratorId id;
			if (paramNames.length > i) {
				id = new VariableDeclaratorId(paramNames[i]);
			}
			else {
				id = new VariableDeclaratorId("p" + i);
			}
			String typeClass;
			boolean found = false;
			if (genericParameterTypes.length > i) {
				for (TypeVariable<?> generic : generics) {
					if (generic.toString().equals(genericParameterTypes[i].toString())) {
						found = true;
					}
				}
			}
			if (found) {
				typeClass = concreteClass;
			}
			else if (parameterTypes[i].getSimpleName().equals("Object")) {
				typeClass = concreteClass;
			} else {
				typeClass = parameterTypes[i].getCanonicalName();
				typeClass = typeClass.indexOf(" ") >= 0 ? typeClass.split(" ")[1] : typeClass;
			}
			Parameter p;
			if (scenarios.size() > 1 && paramNames.length > i  && AsmParameterNames.isSizeParam(paramNames[i])) {
				p = new Parameter(ASTHelper.createReferenceType(typeClass, 1), id);
			}
			else {
				p = new Parameter(ASTHelper.createReferenceType(typeClass, 0), id);
			}
			toReturn.add(p);
		}
		return toReturn;
	}
	
	@Override
	protected MethodDeclaration getSetResultsMethod(Method targetMethod) {
		logger.debug("Adding set_results method");
		Type returnType = ASTUtils.getReturnType(targetMethod);
		if (returnType.toString().equals("void")) {
			logger.debug("Original method's return value is void, stopping");
			return null;
		}
		else if (returnType.toString().equals("java.lang.Object")) {
			returnType = ASTHelper.createReferenceType(concreteClass, 0);
		}
		
		MethodDeclaration set_results = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "set_results");
		List<Parameter> parameters = new ArrayList<Parameter>();
		if (scenarios.size() > 1) {
			parameters.add(new Parameter(ASTHelper.createReferenceType(returnType.toString(), 1), new VariableDeclaratorId("res")));
		}
		else {
			parameters.add(new Parameter(ASTHelper.createReferenceType(returnType.toString(), 0), new VariableDeclaratorId("res")));
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
	
	public String getConcreteClass() {
		return concreteClass;
	}

}