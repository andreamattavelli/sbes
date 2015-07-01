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
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NullLiteralExpr;
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

import sbes.ast.ArrayStubRemoverVisitor;
import sbes.ast.renamer.NameExprRenamer;
import sbes.ast.renamer.StubRenamer;
import sbes.exceptions.GenerationException;
import sbes.logging.Logger;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.CounterexampleStub;
import sbes.stub.Stub;
import sbes.stub.generator.second.SecondStageGeneratorStub;

public class SecondStageGeneratorStubSE extends SecondStageGeneratorStub {

	private static final Logger logger = new Logger(SecondStageGeneratorStub.class);

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
			fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType(resultType, 0), "expected_result"));
			fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType(resultType, 0), "actual_result"));
		}
		// exception fields
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType("Exception", 0), "e1"));
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType("Exception", 0), "e2"));
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
		ASTHelper.addStmt(body, getTry(new BlockStmt(createActualResult(targetMethod, candidateES, param)), getCatchClause("e2")));

		//assume semi conservative
		ASTHelper.addStmt(body, getAnalysisMethod("assume", new MethodCallExpr(null, "listsMirrorEachOtherInitally_semiconservative_onShadowFields")));
		//assert conservative
		ASTHelper.addStmt(body, getAnalysisMethod("ass3rt", new MethodCallExpr(null, "listsMirrorEachOtherAtEnd_conservative")));
		//assert equal returns
		ASTHelper.addStmt(body, getAnalysisMethod("ass3rt", new BinaryExpr(ASTHelper.createNameExpr("expected_result"), ASTHelper.createNameExpr("actual_result"), BinaryExpr.Operator.equals)));
		//assert equal exceptions
		ASTHelper.addStmt(body, getIfException("e1", "e2"));
		ASTHelper.addStmt(body, getIfException("e2", "e1"));

		method_under_test.setBody(body);
		
		return method_under_test;
	}
	
	protected List<Statement> initVariables(Method targetMethod) {
		List<Statement> stmts = new ArrayList<Statement>();
		if (!targetMethod.getReturnType().isPrimitive()) {
			stmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("expected_result"), new NullLiteralExpr(), AssignExpr.Operator.assign)));
			stmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("actual_result"), new NullLiteralExpr(), AssignExpr.Operator.assign)));
		}
		stmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("e1"), new NullLiteralExpr(), AssignExpr.Operator.assign)));
		stmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("e2"), new NullLiteralExpr(), AssignExpr.Operator.assign)));
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
		Expression right = new MethodCallExpr(ASTHelper.createNameExpr("v_Stack1"), targetMethod.getName(), methodParameters);
		if (!targetMethod.getReturnType().equals(void.class)) {
			String className = targetMethod.getReturnType().getCanonicalName();
			int arrayDimension = 0;
			if (targetMethod.getReturnType().isArray()) {
				arrayDimension = 1;
			}
			Expression left = ASTHelper.createVariableDeclarationExpr(ASTHelper.createReferenceType(className, arrayDimension), "expected_result");
			AssignExpr assignment = new AssignExpr(left, right, Operator.assign);
			ASTHelper.addStmt(body, new ExpressionStmt(assignment));
		}
		else {
			ASTHelper.addStmt(body, new ExpressionStmt(right));
		}
		
		return getTry(body, getCatchClause("e1"));
	}
	
	@Override
	protected void stubToClone(BlockStmt cloned) {
		String stubName = stub.getStubName();
		String stubObjectName = null;
		for (int i = 0; i < cloned.getStmts().size(); i++) {
			Statement stmt = cloned.getStmts().get(i);
			if (stmt instanceof ExpressionStmt) {
				ExpressionStmt estmt = (ExpressionStmt) stmt;
				if (estmt.getExpression() instanceof VariableDeclarationExpr) {
					VariableDeclarationExpr vde = (VariableDeclarationExpr) estmt.getExpression();
					if (vde.getType().toString().equals(stubName)) {
						// found stub constructor: REMOVE!
						stubObjectName = vde.getVars().get(0).getId().getName();
						cloned.getStmts().remove(i);
						i--;
						break;
					}
				}
			}
		}

		if (stubObjectName == null) {
			throw new GenerationException("Stub object not found!");
		}

		new NameExprRenamer(stubObjectName, "v_Stack2").visit(cloned, null);
		new StubRenamer(stubName, stubName.substring(0, stubName.indexOf('_'))).visit(cloned, null);
		new ArrayStubRemoverVisitor().visit(cloned, null);
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
		ifExceptions.setCondition(new BinaryExpr(ASTHelper.createNameExpr(originalVar), new NullLiteralExpr(), BinaryExpr.Operator.equals));
		ifExceptions.setThenStmt(getAnalysisMethod("ass3rt", new BinaryExpr(ASTHelper.createNameExpr(checkVar), new NullLiteralExpr(), BinaryExpr.Operator.equals)));
		return ifExceptions;
	}

	protected Statement getAnalysisMethod(String methodName, Expression parameter) {
		MethodCallExpr mce = new MethodCallExpr(ASTHelper.createNameExpr("Analysis"), methodName);
		mce.setArgs(Arrays.asList(parameter));
		return new ExpressionStmt(mce);
	}

}
