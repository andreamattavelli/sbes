package sbes.scenario;

import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.CloneVisitor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sbes.SBESException;
import sbes.evosuite.Evosuite;
import sbes.evosuite.EvosuiteTestScenario;
import sbes.execution.ExecutionManager;
import sbes.execution.ExecutionResult;
import sbes.execution.WorkerException;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.stub.generator.FirstStageStubGenerator;
import sbes.testcase.Carver;
import sbes.testcase.CarvingContext;
import sbes.testcase.CarvingResult;
import sbes.testcase.Compilation;
import sbes.testcase.CompilationContext;
import sbes.util.ASTUtils;
import sbes.util.ClassUtils;
import sbes.util.EvosuiteUtils;
import sbes.util.IOUtils;

public class TestScenarioGenerator {

	private static final Logger logger = new Logger(TestScenarioGenerator.class);

	private static TestScenarioGenerator instance;

	private List<TestScenario> scenarios;

	private TestScenarioGenerator() {
		scenarios = new ArrayList<TestScenario>();
	}

	public static TestScenarioGenerator getInstance() {
		if (instance == null) {
			instance = new TestScenarioGenerator();
		}
		return instance;
	}

	public void generateTestScenarios() throws SBESException {
		logger.info("Generating initial test scenarios");
		try {
			ExecutionResult result = generate();

			logger.debug("Check whether the generation was successful");
			if (result.getExitStatus() != 0 && !EvosuiteUtils.succeeded(result.getStdout(), result.getStderr())) {
				throw new SBESException("Generation failed due " + result.getStdout() + System.lineSeparator() + result.getStderr());
			}

			logger.debug("Check whether the generated test cases compile");
			if (!isCompilable(result)) {
				throw new SBESException("Unable to generate compilable test scenarios, give up!");
			}

			List<CarvingResult> carvedTests = carveTestScenarios(result);
			if (carvedTests.isEmpty()) {
				throw new SBESException("Unable to generate any test scenarios, give up!");
			}

			testToArrayScenario(carvedTests);

			logger.info("Generated " + scenarios.size() + " initial test scenarios - Done");
		} catch (WorkerException e) {
			logger.fatal("Stopping test scenario generation: " + e.getMessage());
			throw new SBESException("Unable to generate initial test scenarios");
		}
	}

	private boolean isCompilable(ExecutionResult result) {
		String signature = Options.I().getMethodSignature();
		String packagename = IOUtils.fromCanonicalToPath(ClassUtils.getPackage(signature));
		String testDirectory = IOUtils.concatPath(result.getOutputDir(), packagename);

		String classPath =	Options.I().getClassesPath() + File.pathSeparatorChar + 
				Options.I().getJunitPath() + File.pathSeparatorChar +
				Options.I().getEvosuitePath();

		return Compilation.compile(new CompilationContext(testDirectory, result.getFilename(), result.getOutputDir(), classPath));
	}

	private  ExecutionResult generate() {
		ExecutionManager manager = new ExecutionManager();
		Evosuite evosuiteCommand = new EvosuiteTestScenario(ClassUtils.getCanonicalClassname(Options.I().getMethodSignature()), 
				ClassUtils.getMethodname(Options.I().getMethodSignature()));
		return manager.execute(evosuiteCommand);
	}

	private List<CarvingResult> carveTestScenarios(ExecutionResult result) {
		String signature = Options.I().getMethodSignature();
		String packagename = IOUtils.fromCanonicalToPath(ClassUtils.getPackage(signature));
		String testDirectory = IOUtils.concatPath(result.getOutputDir(), packagename);

		CarvingContext context = new CarvingContext(testDirectory, result.getFilename());

		Carver carver = new Carver(context, true);
		return carver.carveBodyFromTests();
	}

	private void testToArrayScenario(List<CarvingResult> carvedTests) {
		logger.debug("Generalizing carved bodies to array-based test scenarios");
		for (CarvingResult carvedTest : carvedTests) {
			TestScenario ts = generalizeTestToScenario(carvedTest);
			if (ts != null) {
				scenarios.add(ts);
			}
		}
		logger.debug("Generalization - done");
	}

	private TestScenario generalizeTestToScenario(CarvingResult carvedTest) {
		logger.debug("Generalizing carved body");

		CloneVisitor cloner = new CloneVisitor();
		BlockStmt cloned = (BlockStmt) cloner.visit(carvedTest.getBody(), null);
		List<Statement> actualStatements = new ArrayList<Statement>();

		String className = ClassUtils.getSimpleClassname(Options.I().getMethodSignature());
		String methodName = ClassUtils.getMethodname(Options.I().getMethodSignature().split("\\[")[0]);

		Map<String, String> transformationMap = new HashMap<String, String>();
		VariableDeclaratorId var = null;
		int index = scenarios.size();
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
						// EXPECTED_STATE
						var = vde.getVars().get(0).getId();
						Expression target = ASTUtils.createArrayAccess(FirstStageStubGenerator.EXPECTED_STATE, Integer.toString(index));
						Expression value = vde.getVars().get(0).getInit();
						AssignExpr ae = new AssignExpr(target, value, Operator.assign);
						cloned.getStmts().remove(i);
						cloned.getStmts().add(i, new ExpressionStmt(ae));

						Expression target_act = ASTUtils.createArrayAccess(FirstStageStubGenerator.ACTUAL_STATE, Integer.toString(index));
						AssignExpr ae_act = new AssignExpr(target_act, value, Operator.assign);
						actualStatements.add(new ExpressionStmt(ae_act));
					}
					else if (vde.getVars().get(0).getInit().toString().contains(methodName)) {
						// EXPECTED_RESULT = EXPECTED_STATE.METHOD
						Expression target = ASTUtils.createArrayAccess(FirstStageStubGenerator.EXPECTED_RESULT, Integer.toString(index));
						Expression value = vde.getVars().get(0).getInit();
						if (value instanceof MethodCallExpr) {
							MethodCallExpr mce = (MethodCallExpr) value;
							mce.setScope(ASTUtils.createArrayAccess(FirstStageStubGenerator.EXPECTED_STATE, Integer.toString(index)));
							if (isArgumentNull(mce.getArgs())) {
								// if the arguments are null, we discard the test since it is not meaningful
								return null;
							}
							handleArguments(transformationMap, mce.getArgs());
						}
						else if (value instanceof CastExpr) {
							CastExpr cast = (CastExpr) value;
							MethodCallExpr mce = (MethodCallExpr) cast.getExpr(); // safe cast: in our case it is always a method call
							mce.setScope(ASTUtils.createArrayAccess(FirstStageStubGenerator.EXPECTED_STATE, Integer.toString(index)));
							handleArguments(transformationMap, mce.getArgs());
						}
						AssignExpr ae = new AssignExpr(target, value, Operator.assign);
						cloned.getStmts().remove(i);
						cloned.getStmts().add(i, new ExpressionStmt(ae));
					}
					/*
					 * If we have multiple scenarios we have to handle multiple
					 * declarations (EvoSuite use a fix pattern for names)
					 */
					VariableDeclaratorId vdi = vde.getVars().get(0).getId();
					String newName = vdi.getName() + "_" + scenarios.size();
					transformationMap.put(vdi.getName(), newName);
					vdi.setName(newName);

					if (vde.getVars().get(0).getInit() instanceof ObjectCreationExpr) {
						ObjectCreationExpr oce = (ObjectCreationExpr) vde.getVars().get(0).getInit();
						handleArguments(transformationMap, oce.getArgs());
					}
				}
				else if (estmt.getExpression() instanceof MethodCallExpr) {
					MethodCallExpr mce = (MethodCallExpr) estmt.getExpression();
					if (mce.getScope() instanceof NameExpr) {
						NameExpr scopeName = (NameExpr) mce.getScope();
						if (var != null && scopeName.getName().equals(var.getName())) {
							CloneVisitor mceCloner = new CloneVisitor();
							MethodCallExpr clonedMce = (MethodCallExpr) mceCloner.visit(mce, null);

							mce.setScope(ASTUtils.createArrayAccess(FirstStageStubGenerator.EXPECTED_STATE, Integer.toString(index)));
							clonedMce.setScope(ASTUtils.createArrayAccess(FirstStageStubGenerator.ACTUAL_STATE, Integer.toString(index)));
							actualStatements.add(new ExpressionStmt(clonedMce));
						}
						else {
							/*
							 * If we have multiple scenarios we have to handle multiple
							 * declarations (EvoSuite use a fix pattern for names)
							 */
							if (transformationMap.containsKey(scopeName.getName())) {
								scopeName.setName(transformationMap.get(scopeName.getName()));
							}
						}
					}
					handleArguments(transformationMap, mce.getArgs());
				}
			}
		}

		cloned.getStmts().addAll(actualStatements);

		return new TestScenario(carvedTest, cloned);
	}
	
	private boolean isArgumentNull(List<Expression> args) {
		if (args == null) {
			return false;
		}
		/*
		 * we should use more powerful dynamic analyses to understand the value
		 * of a variable
		 */
		for (Expression arg : args) {
			if (arg instanceof NameExpr) {
				NameExpr ne = (NameExpr) arg;
				if (!ne.getName().equals("null")) {
					return false;
				}
			} else if (arg instanceof CastExpr) {
				CastExpr cast = (CastExpr) arg;
				if (cast.getExpr() instanceof NameExpr) {
					NameExpr ne = (NameExpr) cast.getExpr();
					if (!ne.getName().equals("null")) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private void handleArguments(Map<String, String> transformationMap, List<Expression> args) {
		if (args == null) {
			return;
		}

		for (Expression arg : args) {
			if (arg instanceof NameExpr) {
				NameExpr ne = (NameExpr) arg;
				if (transformationMap.containsKey(ne.getName())) {
					ne.setName(transformationMap.get(ne.getName()));
				}
			} else if (arg instanceof CastExpr) {
				CastExpr cast = (CastExpr) arg;
				if (cast.getExpr() instanceof NameExpr) {
					NameExpr ne = (NameExpr) cast.getExpr();
					if (transformationMap.containsKey(ne.getName())) {
						ne.setName(transformationMap.get(ne.getName()));
					}
				}
			}
		}
	}

	public TestScenario carvedTestToScenario(CarvingResult carvedTest) {
		TestScenario scenario = generalizeTestToScenario(carvedTest);
		scenarios.add(scenario);
		return scenario;
	}

	public List<TestScenario> getScenarios() {
		return scenarios;
	}

}
