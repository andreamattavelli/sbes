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
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.CloneVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sbes.logging.Logger;
import sbes.option.Options;
import sbes.stub.generator.FirstStageStubGenerator;
import sbes.testcase.CarvingResult;
import sbes.util.ASTUtils;
import sbes.util.ClassUtils;

public class TestScenarioGeneralizer {
	
	private static final Logger logger = new Logger(TestScenarioGeneralizer.class);
	
	private int index;
	
	public TestScenarioGeneralizer(int scenarioSize) {
		this.index = scenarioSize;
	}

	public TestScenario generalizeTestToScenario(CarvingResult carvedTest) {
		logger.debug("Generalizing carved body");

		CloneVisitor cloner = new CloneVisitor();
		BlockStmt cloned = (BlockStmt) cloner.visit(carvedTest.getBody(), null);
		List<Statement> actualStatements = new ArrayList<Statement>();

		String className = ClassUtils.getSimpleClassname(Options.I().getMethodSignature());
		String methodName = ClassUtils.getMethodname(Options.I().getMethodSignature().split("\\[")[0]);
		String genericClass = null;

		Map<String, String> transformationMap = new HashMap<String, String>();
		String varName = null;
		for (int i = 0; i < cloned.getStmts().size(); i++) {
			Statement stmt = cloned.getStmts().get(i);
			if (stmt instanceof ExpressionStmt) {
				ExpressionStmt estmt = (ExpressionStmt) stmt;
				if (estmt.getExpression() instanceof VariableDeclarationExpr) {
					/* Evosuite does not create variable declarations with multiple definitions,
					 * so we can safely assume to get element 0
					 */
					VariableDeclarationExpr vde = (VariableDeclarationExpr) estmt.getExpression();
					if (vde.getType().toString().startsWith(className)) {
						/*
						 * We need to use startsWith to support correctly
						 * generic classes, e.g. Stack<Integer>, Stack<? extends Collection<String>>
						 */
						// EXPECTED_STATE
						genericClass = extractConcreteClass(vde);
						varName = vde.getVars().get(0).getId().getName();
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
					else if (varName != null) {
						Expression expr = vde.getVars().get(0).getInit();
						if (expr instanceof MethodCallExpr) {
							MethodCallExpr mce = (MethodCallExpr) expr;
							if (mce.getScope() instanceof NameExpr) {
								NameExpr ne = (NameExpr) mce.getScope();
								if (ne.getName().equals(varName)) {
									mce.setScope(ASTUtils.createArrayAccess(FirstStageStubGenerator.EXPECTED_STATE, Integer.toString(index)));
								}
							}
						}
					}
					/*
					 * If we have multiple scenarios we have to handle multiple
					 * declarations (EvoSuite use a fix pattern for names)
					 */
					VariableDeclaratorId vdi = vde.getVars().get(0).getId();
					String newName = vdi.getName() + "_" + index;
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
						if (varName != null && scopeName.getName().equals(varName)) {
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
		
		if (genericClass != null) {
			return new GenericTestScenario(carvedTest, cloned, genericClass);
		} else {
			return new TestScenario(carvedTest, cloned);
		}
	}
	
	private String extractConcreteClass(VariableDeclarationExpr vde) {
		Type type = vde.getType();
		if (type instanceof ReferenceType) {
			ReferenceType rt = (ReferenceType) type;
			Type innerType = rt.getType();
			if (innerType instanceof ClassOrInterfaceType) {
				ClassOrInterfaceType coit = (ClassOrInterfaceType) innerType;
				if (coit.getTypeArgs() != null) {
					return coit.getTypeArgs().toString().replaceAll("\\[", "").replaceAll("\\]", "");
				}
			}
		}
		return null;
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
	
}
