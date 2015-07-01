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
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.AssignExpr;
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
		String resultType = getActualResultType(targetMethod);
		// return fields
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType(resultType, 0), "r1"));
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType(resultType, 0), "r2"));
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
		// add method_under_test
		MethodDeclaration md = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "method_under_test");
		BlockStmt body = new BlockStmt();
		List<Statement> stmts = new ArrayList<Statement>();
		// variable init
		stmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("r1"), new NullLiteralExpr(), AssignExpr.Operator.assign)));
		stmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("r2"), new NullLiteralExpr(), AssignExpr.Operator.assign)));
		stmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("e1"), new NullLiteralExpr(), AssignExpr.Operator.assign)));
		stmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("e2"), new NullLiteralExpr(), AssignExpr.Operator.assign)));
		//try-catch original method
		BlockStmt tryBodyOriginal = new BlockStmt();
		List<Statement> tryBodyOriginalStmts = new ArrayList<>();
		tryBodyOriginalStmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("r1"), ASTHelper.createNameExpr("XXXXXXXX"), AssignExpr.Operator.assign)));
		tryBodyOriginal.setStmts(tryBodyOriginalStmts);
		BlockStmt catchBodyOriginal = new BlockStmt();
		List<Statement> catchBodyOriginalStmts = new ArrayList<>();
		catchBodyOriginalStmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("e1"), ASTHelper.createNameExpr("e"), AssignExpr.Operator.assign)));
		catchBodyOriginal.setStmts(catchBodyOriginalStmts);
		TryStmt tryOriginal = getTry(tryBodyOriginal, catchBodyOriginal);
		stmts.add(tryOriginal);

		//try-catch candidate equivalence
		BlockStmt tryBodyCandidate = new BlockStmt();
		List<Statement> tryBodyCandidateStmts = new ArrayList<>();
		tryBodyCandidateStmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("r2"), ASTHelper.createNameExpr("YYYYYYYYY"), AssignExpr.Operator.assign)));
		tryBodyCandidate.setStmts(tryBodyCandidateStmts);
		BlockStmt catchBodyCandidate = new BlockStmt();
		List<Statement> catchBodyCandidateStmts = new ArrayList<>();
		catchBodyCandidateStmts.add(new ExpressionStmt(new AssignExpr(ASTHelper.createNameExpr("e2"), ASTHelper.createNameExpr("e"), AssignExpr.Operator.assign)));
		catchBodyCandidate.setStmts(catchBodyCandidateStmts);
		TryStmt tryCandidate = getTry(tryBodyCandidate, catchBodyCandidate);
		stmts.add(tryCandidate);

		//assume semi conservative
		stmts.add(getAnalysisMethod("assume", new MethodCallExpr(null, "listsMirrorEachOtherInitally_semiconservative_onShadowFields")));

		//assert conservative
		stmts.add(getAnalysisMethod("ass3rt", new MethodCallExpr(null, "listsMirrorEachOtherAtEnd_conservative")));

		//assert equal returns
		stmts.add(getAnalysisMethod("ass3rt", new BinaryExpr(ASTHelper.createNameExpr("r1"), ASTHelper.createNameExpr("r2"), BinaryExpr.Operator.equals)));

		//assert equal exceptions
		stmts.add(getIfException("e1", "e2"));
		stmts.add(getIfException("e2", "e1"));

		body.setStmts(stmts);
		md.setBody(body);
		return md;
	}
	
	private IfStmt getIfException(String originalVar, String checkVar) {
		IfStmt ifExceptions = new IfStmt();
		ifExceptions.setCondition(new BinaryExpr(ASTHelper.createNameExpr(originalVar), new NullLiteralExpr(), BinaryExpr.Operator.equals));
		ifExceptions.setThenStmt(getAnalysisMethod("ass3rt", new BinaryExpr(ASTHelper.createNameExpr(checkVar), new NullLiteralExpr(), BinaryExpr.Operator.equals)));
		return ifExceptions;
	}

	private Statement getAnalysisMethod(String methodName, Expression parameter) {
		MethodCallExpr mce = new MethodCallExpr(ASTHelper.createNameExpr("Analysis"), methodName);
		mce.setArgs(Arrays.asList(parameter));
		return new ExpressionStmt(mce);
	}

	private TryStmt getTry(BlockStmt tryBody, BlockStmt catchBody) {
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

}
