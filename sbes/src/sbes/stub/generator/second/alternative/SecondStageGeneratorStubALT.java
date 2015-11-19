package sbes.stub.generator.second.alternative;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import japa.parser.ASTHelper;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.MultiTypeParameter;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.LiteralExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.CatchClause;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.type.Type;
import sbes.ast.ArrayCellDeclarationVisitor;
import sbes.ast.MethodCallVisitor;
import sbes.ast.VariableDeclarationVisitor;
import sbes.ast.renamer.NameExprRenamer;
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
		
		for (ExpressionStmt exprStmt : createVariables(targetMethod)) {
			ASTHelper.addStmt(stmt, exprStmt);
		}

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
		ASTHelper.addStmt(stmt, createActualResultTry(stmts));

		method_under_test.setParameters(param);

		NameExpr distanceClass = ASTHelper.createNameExpr("Distance");
		String distanceMethod = "distance";

		// if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)
		Expression zeroDouble = new DoubleLiteralExpr("0.0d");
		List<Expression> distanceStateArgs = new ArrayList<Expression>();
		distanceStateArgs.add(ASTHelper.createNameExpr(classParameterName));
		distanceStateArgs.add(ASTHelper.createNameExpr("clone"));
		Expression state = new MethodCallExpr(distanceClass, distanceMethod, distanceStateArgs);
		BinaryExpr stateCondition = new BinaryExpr(state, zeroDouble, japa.parser.ast.expr.BinaryExpr.Operator.greater);
		
		BinaryExpr exceptionsCondition = new BinaryExpr(
				new BinaryExpr(ASTHelper.createNameExpr("e1"), new NullLiteralExpr(), BinaryExpr.Operator.equals), 
				new BinaryExpr(ASTHelper.createNameExpr("e2"), new NullLiteralExpr(), BinaryExpr.Operator.equals), 
				BinaryExpr.Operator.xor);

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
		
		ifCondition = new BinaryExpr(exceptionsCondition, ifCondition, japa.parser.ast.expr.BinaryExpr.Operator.or);

		IfStmt ifStmt = new IfStmt(ifCondition, new ExpressionStmt(ASTUtils.createSystemOut("Executed")), null);
		ASTHelper.addStmt(stmt, ifStmt);

		method_under_test.setBody(stmt);

		return method_under_test;
	}
	
	protected List<ExpressionStmt> createVariables(Method targetMethod) {
		List<ExpressionStmt> toReturn = new ArrayList<ExpressionStmt>();
		VariableDeclaratorId exception1 = new VariableDeclaratorId("e1");
		VariableDeclaratorId exception2 = new VariableDeclaratorId("e2");		
		VariableDeclarator exception1Decl = new VariableDeclarator(exception1, new NullLiteralExpr());
		VariableDeclarator exception2Decl = new VariableDeclarator(exception2, new NullLiteralExpr());
		VariableDeclarationExpr exception1DeclExpr = new VariableDeclarationExpr(ASTHelper.createReferenceType("Exception", 0), Arrays.asList(exception1Decl));
		VariableDeclarationExpr exception2DeclExpr = new VariableDeclarationExpr(ASTHelper.createReferenceType("Exception", 0), Arrays.asList(exception2Decl));
		toReturn.add(new ExpressionStmt(exception1DeclExpr));
		toReturn.add(new ExpressionStmt(exception2DeclExpr));
		
		if (!targetMethod.getReturnType().equals(void.class)) {
			VariableDeclarator expected_resultDecl = new VariableDeclarator(new VariableDeclaratorId("expected_result"), new NullLiteralExpr());
			VariableDeclarator actual_resultDecl = new VariableDeclarator(new VariableDeclaratorId("actual_result"), new NullLiteralExpr());
			String className = targetMethod.getReturnType().getCanonicalName();
			int arrayDimension = 0;
			if (targetMethod.getReturnType().isArray()) {
				arrayDimension = 1;
			}
			Expression expected_result = new VariableDeclarationExpr(ASTHelper.createReferenceType(className, arrayDimension), Arrays.asList(expected_resultDecl));
			Expression actual_result = new VariableDeclarationExpr(ASTHelper.createReferenceType(className, arrayDimension), Arrays.asList(actual_resultDecl));
			toReturn.add(new ExpressionStmt(expected_result));
			toReturn.add(new ExpressionStmt(actual_result));
		}
		
		return toReturn;
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
		Expression tryExpr;
		Expression right = new MethodCallExpr(ASTHelper.createNameExpr(classParameterName), targetMethod.getName(), methodParameters);
		if (!targetMethod.getReturnType().equals(void.class)) {
			tryExpr = new AssignExpr(ASTHelper.createNameExpr("expected_result"), right, Operator.assign);
		}
		else {
			tryExpr = right;
		}
		
		// try block
		List<Statement> stmts = new ArrayList<>();
		stmts.add(new ExpressionStmt(tryExpr));

		return getTry(new BlockStmt(stmts), getCatchClause("e1"));
	}
	
	private Statement createActualResultTry(List<Statement> stmts) {
		return getTry(new BlockStmt(stmts), getCatchClause("e2"));
	}
	
	protected TryStmt getTry(BlockStmt tryBody, BlockStmt catchBody) {
		TryStmt tryStmt = new TryStmt();
		
		// try block
		tryStmt.setTryBlock(tryBody);
		CatchClause cc = new CatchClause();
		List<AnnotationExpr> annExpr = new ArrayList<>();
		List<Type> types = new ArrayList<>();
		types.add(ASTHelper.createReferenceType("Exception", 0));
		cc.setExcept(new MultiTypeParameter(0, annExpr, types, new VariableDeclaratorId("e")));
		// catch block
		cc.setCatchBlock(catchBody);
		tryStmt.setCatchs(Arrays.asList(cc));
		// resources used
		tryStmt.setResources(new ArrayList<VariableDeclarationExpr>());
		
		return tryStmt;
	}
	
	protected BlockStmt getCatchClause(String exceptionVar) {
		BlockStmt catchBodyOriginal = new BlockStmt();
		List<Statement> catchBodyOriginalStmts = new ArrayList<>();
		catchBodyOriginalStmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr(exceptionVar), ASTHelper.createNameExpr("e"), AssignExpr.Operator.assign)));
		catchBodyOriginal.setStmts(catchBodyOriginalStmts);
		return catchBodyOriginal;
	}
	
	@Override
	protected void identifyActualResult(BlockStmt cloned, Method targetMethod, final List<Parameter> param) {
		MethodCallVisitor mcv = new MethodCallVisitor("set_results", 1);
		mcv.visit(cloned, null);
		MethodCallExpr mce = mcv.getMethodCall();

		String resultType = getActualResultType(targetMethod);
		int arrayDimension = 0;
		if (targetMethod.getReturnType().isArray()) {
			arrayDimension = 1;
		}
		
		if (mce == null) {
			logger.debug("There is no set_results method, thus it MUST be a default value");
			Class<?> returnType = targetMethod.getReturnType();
			if (ReflectionUtils.isPrimitive(returnType)) {
				Expression defaultValue = ASTUtils.getDefaultPrimitiveValue(returnType);
				AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), defaultValue, AssignExpr.Operator.assign);
				cloned.getStmts().add(new ExpressionStmt(actualResult));
			}
			else {
				// it is an object
				AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), new NullLiteralExpr(), AssignExpr.Operator.assign);
				cloned.getStmts().add(new ExpressionStmt(actualResult));
			}
			return;
		}

		VariableDeclarationExpr vde = null;
		Expression arg = mce.getArgs().get(0);
		if (arg instanceof FieldAccessExpr) {
			FieldAccessExpr fae = (FieldAccessExpr) arg;
			AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), fae, AssignExpr.Operator.assign);
			cloned.getStmts().add(new ExpressionStmt(actualResult));
			return;
		}
		else if (arg instanceof LiteralExpr) {
			LiteralExpr le = (LiteralExpr) arg;
			AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), le, AssignExpr.Operator.assign);
			cloned.getStmts().add(new ExpressionStmt(actualResult));
			return;
		}
		else {
			String name = ASTUtils.getName(arg);
			if (name != null) {
				VariableDeclarationVisitor visitor = new VariableDeclarationVisitor(name);
				visitor.visit(cloned, null);
				vde = visitor.getVariable();
			}

			if (vde != null) {
				String varName = vde.getVars().get(0).getId().getName();
				Expression init = vde.getVars().get(0).getInit();
				if (init instanceof EnclosedExpr) {
					init = ((EnclosedExpr) init).getInner();
				}

				if (init instanceof ArrayCreationExpr) {
					// we should check what is inside the array
					ArrayCellDeclarationVisitor acdv = new ArrayCellDeclarationVisitor(name, Integer.toString(0));
					acdv.visit(cloned, null);
					Expression e = acdv.getValue();
					if (e instanceof NameExpr) {
						String internalName = ASTUtils.getName(e);
						if (internalName != null) {
							VariableDeclarationVisitor visitor = new VariableDeclarationVisitor(internalName);
							visitor.visit(cloned, null);
							VariableDeclarationExpr internalVde = visitor.getVariable();
							Expression internalInit = internalVde.getVars().get(0).getInit();
							if (internalInit instanceof NameExpr) {
								NameExpr valueName = (NameExpr) internalInit;
								for (Parameter parameter : param) {
									if(parameter.getId().getName().equals(valueName.getName())) {
										// it is an input
										AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), new NameExpr(param.get(0).getId().getName()), AssignExpr.Operator.assign);
										cloned.getStmts().add(new ExpressionStmt(actualResult));
										break;
									}
								}
							}
							else if (internalInit instanceof FieldAccessExpr) {
								FieldAccessExpr fae = (FieldAccessExpr) internalInit;
								if (fae.getField().startsWith("ELEMENT_")) {
									// it is an input
									AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), new NameExpr(param.get(0).getId().getName()), AssignExpr.Operator.assign);
									cloned.getStmts().add(new ExpressionStmt(actualResult));
								}
							}
							else if (internalInit instanceof ObjectCreationExpr) {
								ObjectCreationExpr oce = (ObjectCreationExpr) internalInit;
								AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), oce, AssignExpr.Operator.assign);
								cloned.getStmts().add(new ExpressionStmt(actualResult));
							}
							else if (internalInit instanceof MethodCallExpr) {
								AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), internalInit, AssignExpr.Operator.assign);
								cloned.getStmts().add(new ExpressionStmt(actualResult));
							}
						}
					}
					else if (e instanceof CastExpr) {
						Expression exp = ((CastExpr) e).getExpr();
						if (exp instanceof IntegerLiteralExpr) {
							AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), e, AssignExpr.Operator.assign);
							cloned.getStmts().add(new ExpressionStmt(actualResult));
						}
					}
					else if (e instanceof NullLiteralExpr) {
						AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), e, AssignExpr.Operator.assign);
						cloned.getStmts().add(new ExpressionStmt(actualResult));
					}
				}
				else if (init instanceof MethodCallExpr) {
					// the actual_result is the return value
					vde.setType(ASTHelper.createReferenceType(resultType, arrayDimension));
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), vde.getVars().get(0).getInit(), AssignExpr.Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
					vde.getVars().get(0).setInit(new NullLiteralExpr()); // dead code analysis will do the job
				}
				else if (init instanceof NameExpr) {
					NameExpr valueName = (NameExpr) init;
					for (Parameter parameter : param) {
						if(parameter.getId().getName().equals(valueName.getName())) {
							// it is an input
							AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), new NameExpr(param.get(0).getId().getName()), AssignExpr.Operator.assign);
							cloned.getStmts().add(new ExpressionStmt(actualResult));
							break;
						}
					}
				}
				else if (init instanceof CastExpr) {
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), ((CastExpr) init).getExpr(), AssignExpr.Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof BooleanLiteralExpr) {
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), init, AssignExpr.Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof IntegerLiteralExpr) {
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), init, AssignExpr.Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof DoubleLiteralExpr) {
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), init, AssignExpr.Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof StringLiteralExpr) {
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), init, AssignExpr.Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof FieldAccessExpr) {
					FieldAccessExpr fae = (FieldAccessExpr) init;
					if (fae.getField().startsWith("ELEMENT_")) {
						// it is an input
						AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), new NameExpr(param.get(0).getId().getName()), AssignExpr.Operator.assign);
						cloned.getStmts().add(new ExpressionStmt(actualResult));
					}
					else {
						AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), fae, AssignExpr.Operator.assign);
						cloned.getStmts().add(new ExpressionStmt(actualResult));
					}
				}
				else if (init instanceof ObjectCreationExpr) {
					ObjectCreationExpr oce = (ObjectCreationExpr) init;
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), oce, AssignExpr.Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
					return;
				}
				else if (init instanceof UnaryExpr) {
					UnaryExpr ue = (UnaryExpr) init;
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), ue, AssignExpr.Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
					return;

				}
				NameExprRenamer conv = new NameExprRenamer(varName, "actual_result");
				conv.visit(cloned, null);
			}
		}
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
