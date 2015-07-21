package sbes.stub.generator.second.symbolic;

import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.MultiTypeParameter;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sbes.ast.ArrayCellDeclarationVisitor;
import sbes.ast.MethodCallVisitor;
import sbes.ast.VariableDeclarationVisitor;
import sbes.ast.renamer.NameExprRenamer;
import sbes.exceptions.GenerationException;
import sbes.logging.Logger;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.CounterexampleStub;
import sbes.stub.Stub;
import sbes.stub.generator.second.SecondStageGeneratorStub;
import sbes.util.ASTUtils;
import sbes.util.ReflectionUtils;

public class SecondStageGeneratorStubSE extends SecondStageGeneratorStub {

	private static final Logger logger = new Logger(SecondStageGeneratorStub.class);
	
	protected static final String ACT_RES	= "actual_result";
	protected static final String EXP_RES	= "expected_result";
	protected static final String V_STACK1 = "v_Stack1";
	protected static final String V_STACK2 = "v_Stack2";
	protected static final String MIRROR_CONSERVATIVE = "mirrorFinalConservative";
	protected static final NullLiteralExpr NULL_EXPR = new NullLiteralExpr();

	protected CarvingResult candidateES;
	protected List<Statement> equivalence;
	protected Stub stub;
	protected CompilationUnit cu;

	public SecondStageGeneratorStubSE(final List<TestScenario> scenarios, Stub stub, CarvingResult candidateES) {
		super(scenarios, stub, candidateES);
		this.stub = stub;
		this.candidateES = candidateES;
	}

	@Override
	public Stub generateStub() {
		try {
			cu = JavaParser.parse(new File("./Symbolic_Stub_Template.java"));
		} catch (ParseException | IOException e) {
			throw new GenerationException("Unable to find symbolc execution stub for second stage!", e);
		}

		logger.info("Generating stub for second phase");
		Stub stub = super.generateStub();
		CounterexampleStub counterexampleStub = new CounterexampleStub(stub.getAst(), stub.getStubName(), equivalence);
		logger.info("Generating stub for second phase - done");
		return counterexampleStub;
	}

	@Override
	protected List<ImportDeclaration> getImports() {
		return cu.getImports();
	}
	
	@Override
	protected TypeDeclaration getClassDeclaration(Class<?> c) {
		stubName = c.getSimpleName() + STUB_EXTENSION + "_2";
		ClassOrInterfaceDeclaration classDecl = new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, stubName);
		return classDecl;
	}

	@Override
	protected List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c) {
		List<BodyDeclaration> fields = new ArrayList<>();
		// return fields
		if (!targetMethod.getReturnType().equals(void.class)) {
			String resultType = getActualResultType(targetMethod);
			fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType(resultType, 0), EXP_RES));
			fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType(resultType, 0), ACT_RES));
		}
		// exception fields
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType("Exception", 0), "e1"));
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType("Exception", 0), "e2"));
		Class<?>[] parameters = targetMethod.getParameterTypes();
		List<Parameter> param = getParameterType(parameters, targetMethod);
		for (int i = 0; i < param.size(); i++) {
			Parameter p = param.get(i);
			fields.add(ASTHelper.createFieldDeclaration(0, p.getType(), p.getId().getName()));
		}
		return fields;
	}

	@Override
	protected List<BodyDeclaration> getStubConstructor(Method targetMethod, Class<?> c) {
		return new ArrayList<>();
	}

	@Override
	protected List<BodyDeclaration> getAdditionalMethods(Method targetMethod, Method[] methods, Class<?> c) {
		return cu.getTypes().get(0).getMembers();
	}

	@Override
	protected MethodDeclaration getSetResultsMethod(Method targetMethod) {
		return null;
	}

	@Override
	protected MethodDeclaration getMethodUnderTest(Method targetMethod) {
		logger.debug("Adding method_under_test method");
		MethodDeclaration method_under_test = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "method_under_test");
		
		Class<?>[] parameters = targetMethod.getParameterTypes();
		List<Parameter> param = getParameterType(parameters, targetMethod);
		
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
		IfStmt ifReturns = createCheckOnReturns(targetMethod);
		ASTHelper.addStmt(body, ifReturns);
		ASTHelper.addStmt(body, new ExpressionStmt(createFakeVariable("fake2", "forceConservativeRepOk2")));
		ASTHelper.addStmt(body, getAnalysisMethod("ass3rt", ASTHelper.createNameExpr("ok")));
		//assert equal exceptions
		IfStmt ifExceptions = createCheckOnExceptions();
		ASTHelper.addStmt(body, ifExceptions);
		ASTHelper.addStmt(body, new ExpressionStmt(createFakeVariable("fake3", "forceConservativeRepOk3")));
		ASTHelper.addStmt(body, getAnalysisMethod("ass3rt", ASTHelper.createNameExpr("ok")));
		
		method_under_test.setBody(body);
		
		return method_under_test;
	}

	protected VariableDeclarationExpr createOkVariable() {
		VariableDeclarationExpr okVar = ASTHelper.createVariableDeclarationExpr(ASTHelper.BOOLEAN_TYPE, "ok");
		okVar.getVars().get(0).setInit(new MethodCallExpr(null, MIRROR_CONSERVATIVE));
		return okVar;
	}
	
	protected VariableDeclarationExpr createFakeVariable(String varName, String forceName) {
		VariableDeclarationExpr fakeVar1 = ASTHelper.createVariableDeclarationExpr(ASTHelper.createReferenceType("FakeVariable", 0), varName);
		fakeVar1.getVars().get(0).setInit(ASTHelper.createNameExpr(forceName));
		return fakeVar1;
	}
	
	protected IfStmt createCheckOnExceptions() {
		IfStmt ifExceptions = new IfStmt();
		ifExceptions.setCondition(new BinaryExpr(
				new BinaryExpr(ASTHelper.createNameExpr("e1"), NULL_EXPR, BinaryExpr.Operator.equals), 
				new BinaryExpr(ASTHelper.createNameExpr("e2"), NULL_EXPR, BinaryExpr.Operator.equals), 
				BinaryExpr.Operator.xor));
		ifExceptions.setThenStmt(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("ok"), new BooleanLiteralExpr(false), AssignExpr.Operator.assign)));
		return ifExceptions;
	}

	protected IfStmt createCheckOnReturns(Method targetMethod) {
		IfStmt ifReturns = new IfStmt();
		if (targetMethod.getReturnType().isPrimitive()) {
			ifReturns.setCondition(new BinaryExpr(ASTHelper.createNameExpr(EXP_RES), ASTHelper.createNameExpr(ACT_RES), BinaryExpr.Operator.notEquals));
			ifReturns.setThenStmt(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("ok"), new BooleanLiteralExpr(false), AssignExpr.Operator.assign)));
		}
		else {
			ifReturns.setCondition(new BinaryExpr(ASTHelper.createNameExpr(EXP_RES), NULL_EXPR, BinaryExpr.Operator.notEquals));
			MethodCallExpr mce = new MethodCallExpr(ASTHelper.createNameExpr(EXP_RES), "equals");
			List<Expression> args = new ArrayList<>();
			args.add(ASTHelper.createNameExpr(ACT_RES));
			mce.setArgs(args);
			ifReturns.setThenStmt(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("ok"), mce, AssignExpr.Operator.assign)));
			BinaryExpr mce2 = new BinaryExpr(ASTHelper.createNameExpr(ACT_RES), NULL_EXPR, BinaryExpr.Operator.equals);
			ifReturns.setElseStmt(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("ok"), mce2, AssignExpr.Operator.assign)));
		}
		return ifReturns;
	}
	
	protected List<Statement> initVariables(Method targetMethod) {
		List<Statement> stmts = new ArrayList<Statement>();
		if (!targetMethod.getReturnType().isPrimitive()) {
			stmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr(EXP_RES), NULL_EXPR, AssignExpr.Operator.assign)));
			stmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr(ACT_RES), NULL_EXPR, AssignExpr.Operator.assign)));
		}
		stmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("e1"), NULL_EXPR, AssignExpr.Operator.assign)));
		stmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("e2"), NULL_EXPR, AssignExpr.Operator.assign)));
		return stmts;
	}
	
	@Override
	protected TryStmt createExpectedResult(Method targetMethod, final List<Parameter> parameters) {
		BlockStmt body = new BlockStmt();
		body.setStmts(new ArrayList<Statement>());
		List<Expression> methodParameters = new ArrayList<Expression>();
		for (Parameter parameter : parameters) {
			methodParameters.add(ASTHelper.createNameExpr(parameter.getId().getName()));
		}
		Expression right = new MethodCallExpr(ASTHelper.createNameExpr(V_STACK1), targetMethod.getName(), methodParameters);
		if (!targetMethod.getReturnType().equals(void.class)) {
			AssignExpr assignment = new AssignExpr(ASTHelper.createNameExpr(EXP_RES), right, Operator.assign);
			ASTHelper.addStmt(body, new ExpressionStmt(assignment));
		}
		else {
			ASTHelper.addStmt(body, new ExpressionStmt(right));
		}
		
		return getTry(body, getCatchClause("e1"));
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
			Expression defaultValue;
			if (ReflectionUtils.isPrimitive(returnType)) {
				defaultValue = ASTUtils.getDefaultPrimitiveValue(returnType);
			}
			else {
				// it is an object
				defaultValue = new NullLiteralExpr();
			}
			AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), defaultValue, Operator.assign);
			cloned.getStmts().add(new ExpressionStmt(actualResult));
			return;
		}

		VariableDeclarationExpr vde = null;
		Expression arg = mce.getArgs().get(0);
		if (arg instanceof FieldAccessExpr) {
			FieldAccessExpr fae = (FieldAccessExpr) arg;
			AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), fae, Operator.assign);
			cloned.getStmts().add(new ExpressionStmt(actualResult));
			return;
		}
		else if (arg instanceof LiteralExpr) {
			LiteralExpr le = (LiteralExpr) arg;
			AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), le, Operator.assign);
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
										AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), new NameExpr(param.get(0).getId().getName()), Operator.assign);
										cloned.getStmts().add(new ExpressionStmt(actualResult));
										break;
									}
								}
							}
							else if (internalInit instanceof FieldAccessExpr) {
								FieldAccessExpr fae = (FieldAccessExpr) internalInit;
								if (fae.getField().startsWith("ELEMENT_")) {
									// it is an input
									AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), new NameExpr(param.get(0).getId().getName()), Operator.assign);
									cloned.getStmts().add(new ExpressionStmt(actualResult));
								}
							}
							else if (internalInit instanceof ObjectCreationExpr) {
								ObjectCreationExpr oce = (ObjectCreationExpr) internalInit;
								AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), oce, Operator.assign);
								cloned.getStmts().add(new ExpressionStmt(actualResult));
							}
							else if (internalInit instanceof MethodCallExpr) {
								AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), internalInit, Operator.assign);
								cloned.getStmts().add(new ExpressionStmt(actualResult));
							}
						}
					}
					else if (e instanceof CastExpr) {
						Expression exp = ((CastExpr) e).getExpr();
						if (exp instanceof IntegerLiteralExpr) {
							AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), e, Operator.assign);
							cloned.getStmts().add(new ExpressionStmt(actualResult));
						}
					}
					else if (e instanceof NullLiteralExpr) {
						AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), e, Operator.assign);
						cloned.getStmts().add(new ExpressionStmt(actualResult));
					}
				}
				else if (init instanceof MethodCallExpr) {
					// the actual_result is the return value
					vde.setType(ASTHelper.createReferenceType(resultType, arrayDimension));
					vde.getVars().get(0).getId().setName("actual_result");
				}
				else if (init instanceof NameExpr) {
					NameExpr valueName = (NameExpr) init;
					for (Parameter parameter : param) {
						if(parameter.getId().getName().equals(valueName.getName())) {
							// it is an input
							AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), new NameExpr(param.get(0).getId().getName()), Operator.assign);
							cloned.getStmts().add(new ExpressionStmt(actualResult));
							break;
						}
					}
				}
				else if (init instanceof CastExpr) {
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), ((CastExpr) init).getExpr(), Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof BooleanLiteralExpr) {
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), init, Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof IntegerLiteralExpr) {
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), init, Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof DoubleLiteralExpr) {
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), init, Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof StringLiteralExpr) {
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), init, Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof FieldAccessExpr) {
					FieldAccessExpr fae = (FieldAccessExpr) init;
					if (fae.getField().startsWith("ELEMENT_")) {
						// it is an input
						AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), new NameExpr(param.get(0).getId().getName()), Operator.assign);
						cloned.getStmts().add(new ExpressionStmt(actualResult));
					}
					else {
						AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), fae, Operator.assign);
						cloned.getStmts().add(new ExpressionStmt(actualResult));
					}
				}
				else if (init instanceof ObjectCreationExpr) {
					ObjectCreationExpr oce = (ObjectCreationExpr) init;
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), oce, Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
					return;
				}
				else if (init instanceof UnaryExpr) {
					UnaryExpr ue = (UnaryExpr) init;
					AssignExpr actualResult = new AssignExpr(ASTHelper.createNameExpr("actual_result"), ue, Operator.assign);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
					return;
				}
				NameExprRenamer conv = new NameExprRenamer(varName, "actual_result");
				conv.visit(cloned, null);
			}
		}
	}
	
	@Override
	protected void stubToClone(BlockStmt cloned) {
		super.stubToClone(cloned);
		new NameExprRenamer("clone", V_STACK2).visit(cloned, null);
	}
	
	protected BlockStmt getCatchClause(String exceptionVar) {
		BlockStmt catchBodyOriginal = new BlockStmt();
		List<Statement> catchBodyOriginalStmts = new ArrayList<>();
		catchBodyOriginalStmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr(exceptionVar), ASTHelper.createNameExpr("e"), AssignExpr.Operator.assign)));
		catchBodyOriginal.setStmts(catchBodyOriginalStmts);
		return catchBodyOriginal;
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
	
	protected IfStmt getIfException(String originalVar, String checkVar) {
		IfStmt ifExceptions = new IfStmt();
		ifExceptions.setCondition(new BinaryExpr(ASTHelper.createNameExpr(originalVar), NULL_EXPR, BinaryExpr.Operator.equals));
		ifExceptions.setThenStmt(getAnalysisMethod("ass3rt", new BinaryExpr(ASTHelper.createNameExpr(checkVar), NULL_EXPR, BinaryExpr.Operator.equals)));
		return ifExceptions;
	}

	protected Statement getAnalysisMethod(String methodName, Expression parameter) {
		MethodCallExpr mce = new MethodCallExpr(ASTHelper.createNameExpr("Analysis"), methodName);
		mce.setArgs(Arrays.asList(parameter));
		return new ExpressionStmt(mce);
	}

}
