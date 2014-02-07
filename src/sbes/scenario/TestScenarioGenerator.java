package sbes.scenario;

import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.CloneVisitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sbes.Options;
import sbes.SBESException;
import sbes.evosuite.Evosuite;
import sbes.evosuite.EvosuiteTestScenarioStrategy;
import sbes.execution.ExecutionManager;
import sbes.execution.ExecutionResult;
import sbes.execution.WorkerException;
import sbes.logging.Logger;
import sbes.stub.generator.FirstPhaseStubStrategy;
import sbes.testcase.Carver;
import sbes.testcase.CarvingContext;
import sbes.testcase.CarvingResult;
import sbes.testcase.Compilation;
import sbes.testcase.CompilationContext;
import sbes.util.ASTUtils;
import sbes.util.ClassUtils;
import sbes.util.DirectoryUtils;

public class TestScenarioGenerator {
	
	private static final Logger logger = new Logger(TestScenarioGenerator.class);

	public static List<TestScenario> generateTestScenarios() throws SBESException {
		logger.info("Generating initial test scenarios");
		try {
			ExecutionResult result = generate();
			
			logger.debug("Check whether the generated test cases compile");
			if (!isCompilable(result)) {
				throw new SBESException("Unable to generate compilable test scenarios, give up!");
			}
			
			List<CarvingResult> carvedTests = carveTestScenarios(result);
			if (carvedTests.isEmpty()) {
				throw new SBESException("Unable to generate any test scenarios, give up!");
			}
			
			List<TestScenario> scenarios = testToArrayScenario(carvedTests);
			logger.info("Generated " + scenarios.size() + " initial test scenarios - Done");
			return scenarios;
		} catch (WorkerException e) {
			logger.fatal("Stopping test scenario generation: " + e.getMessage());
			throw new SBESException("Unable to generate initial test scenarios");
		}
	}

	private static boolean isCompilable(ExecutionResult result) {
		String signature = Options.I().getMethodSignature();
		String packagename = ClassUtils.getPackage(signature).replaceAll("\\.", File.separator);
		String testDirectory = DirectoryUtils.toPath(result.getOutputDir(), packagename);
		
		String classPath =	Options.I().getClassesPath() + File.pathSeparatorChar + 
							Options.I().getJunitPath() + File.pathSeparatorChar +
							Options.I().getEvosuitePath();
		
		return Compilation.compile(new CompilationContext(testDirectory, result.getFilename(), classPath));
	}

	private static  ExecutionResult generate() {
		ExecutionManager manager = new ExecutionManager();
		Evosuite evosuiteCommand = new EvosuiteTestScenarioStrategy(ClassUtils.getCanonicalClassname(Options.I().getMethodSignature()), 
																	ClassUtils.getMethodname(Options.I().getMethodSignature()));
		return manager.execute(evosuiteCommand);
	}
	
	private static List<CarvingResult> carveTestScenarios(ExecutionResult result) {
		String signature = Options.I().getMethodSignature();
		String packagename = ClassUtils.getPackage(signature).replaceAll("\\.", File.separator);
		String testDirectory = DirectoryUtils.toPath(result.getOutputDir(), packagename);
		
		CarvingContext context = new CarvingContext(testDirectory, result.getFilename());
		
		Carver carver = new Carver(context);
		return carver.carveBodyFromTests();
	}
	
	private static List<TestScenario> testToArrayScenario(List<CarvingResult> carvedTests) {
		logger.debug("Generalizing carved bodies to array-based test scenarios");
		List<TestScenario> scenarios = new ArrayList<TestScenario>();
		
		for (CarvingResult carvedTest : carvedTests) {
			scenarios.add(generalizeTestToScenario(carvedTest));
		}
		
		logger.debug("Generalization - done");
		return scenarios;
	}
	
	private static TestScenario generalizeTestToScenario(CarvingResult carvedTest) {
		logger.debug("Generalizing carved body");
		
		CloneVisitor cloner = new CloneVisitor();
		BlockStmt cloned = (BlockStmt) cloner.visit(carvedTest.getBody(), null);
		
		String className = ClassUtils.getSimpleClassname(Options.I().getMethodSignature());
		String methodName = ClassUtils.getMethodname(Options.I().getMethodSignature().split("\\[")[0]);
		for (int i = 0; i < cloned.getStmts().size(); i++) {
			Statement stmt = cloned.getStmts().get(i);
			if (stmt instanceof ExpressionStmt) {
				ExpressionStmt estmt = (ExpressionStmt) stmt;
				if (estmt.getExpression() instanceof VariableDeclarationExpr) {
					/* Evosuite does not create variable declarations with multiple definitions,
					 * so we can safely assume to get element 0
					 */
					VariableDeclarationExpr vde = (VariableDeclarationExpr) estmt.getExpression();
					if (vde.getType().toString().equals(className)) {
						Expression target = ASTUtils.createArrayAccess(FirstPhaseStubStrategy.EXPECTED_STATE, Integer.toString(0));
						Expression value = vde.getVars().get(0).getInit();
						AssignExpr ae = new AssignExpr(target, value, Operator.assign);
						cloned.getStmts().remove(i);
						cloned.getStmts().add(i, new ExpressionStmt(ae));
					}
					else if (vde.getVars().get(0).getInit().toString().contains(methodName)) {
						Expression target = ASTUtils.createArrayAccess(FirstPhaseStubStrategy.EXPECTED_RESULT, Integer.toString(0));
						Expression value = vde.getVars().get(0).getInit();
						AssignExpr ae = new AssignExpr(target, value, Operator.assign);
						cloned.getStmts().remove(i);
						cloned.getStmts().add(i, new ExpressionStmt(ae));
					}
				}
			}
		}
		
		return new TestScenario(cloned, carvedTest.getImports());
	}
	
}
