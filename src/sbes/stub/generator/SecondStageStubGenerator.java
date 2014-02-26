package sbes.stub.generator;

import japa.parser.ASTHelper;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.CloneVisitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sbes.logging.Logger;
import sbes.stub.GenerationException;
import sbes.stub.Stub;
import sbes.testcase.CarvingResult;
import sbes.util.ASTUtils;

public class SecondStageStubGenerator extends StubGenerator {

	private static final Logger logger = new Logger(SecondStageStubGenerator.class);
	
	private CarvingResult candidateES;
	private Stub stub;
	
	public SecondStageStubGenerator(Stub stub, CarvingResult candidateES) {
		this.stub = stub;
		this.candidateES = candidateES;
	}

	@Override
	public Stub generateStub() {
		logger.info("Generating stub for second phase");
		Stub stub = super.generateStub(); 
		logger.info("Generating stub for second phase - done");
		return stub;
	}
	
	@Override
	protected List<ImportDeclaration> getImports() {
		List<ImportDeclaration> imports = new ArrayList<>();
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("sbes.distance.Distance"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("sbes.cloning.Cloner"), false, false));
		return imports;
	}
	
	@Override
	protected TypeDeclaration getClassDeclaration(String className) {
		stubName = className + STUB_EXTENSION + "_2";
		
		// extends base class
		ClassOrInterfaceType extendClassDecl = new ClassOrInterfaceType(className);
		List<ClassOrInterfaceType> extendClasses = new ArrayList<ClassOrInterfaceType>();
		extendClasses.add(extendClassDecl);
		
		ClassOrInterfaceDeclaration classDecl = new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, stubName);
		classDecl.setExtends(extendClasses);
		
		return classDecl;
	}

	@Override
	protected List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c) {
		return new ArrayList<BodyDeclaration>();
	}
	
	@Override
	protected List<BodyDeclaration> getStubConstructor(Method targetMethod, Class<?> c) {
		/*
		 * We must adhere to inheritance as defined in Java, which imposes to
		 * add constructor to a child if an explicit constructor is defined in
		 * the parent.
		 */
		List<BodyDeclaration> toReturn = new ArrayList<BodyDeclaration>(); 
		Constructor<?> constructors[] = c.getDeclaredConstructors();
		for (Constructor<?> constructor : constructors) {
			if (!constructor.isSynthetic()) {
				ConstructorDeclaration cons = new ConstructorDeclaration(constructor.getModifiers(), stubName);
				cons.setParameters(getParameterType(constructor.getParameterTypes()));
				List<Expression> methodParameters = ASTUtils.createParameters(cons.getParameters());
				
				BlockStmt stmt = new BlockStmt();
				ExplicitConstructorInvocationStmt ecis = new ExplicitConstructorInvocationStmt(false, null, methodParameters);
				ASTHelper.addStmt(stmt, ecis);
				cons.setBlock(stmt);
				toReturn.add(cons);
			}
		}
		
		return toReturn;
	}
	
	@Override
	protected List<BodyDeclaration> getAdditionalMethods(Method targetMethod, Method[] methods) {
		return new ArrayList<BodyDeclaration>();
	}

	@Override
	protected MethodDeclaration getMethodUnderTest(Method targetMethod) {
		logger.debug("Adding method_under_test method");
		MethodDeclaration method_under_test = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "method_under_test");
		
		Class<?>[] parameters = targetMethod.getParameterTypes();
		List<Parameter> param = getParameterType(parameters);
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
		
		List<Expression> distanceResultArgs = new ArrayList<Expression>();
		distanceResultArgs.add(ASTHelper.createNameExpr("expected_result"));
		distanceResultArgs.add(ASTHelper.createNameExpr("actual_result"));
		Expression result = new MethodCallExpr(distanceClass, distanceMethod, distanceResultArgs);
		BinaryExpr resultCondition = new BinaryExpr(result, zeroDouble, japa.parser.ast.expr.BinaryExpr.Operator.greater);
		
		BinaryExpr ifCondition = new BinaryExpr(resultCondition, stateCondition, japa.parser.ast.expr.BinaryExpr.Operator.or);
		IfStmt ifStmt = new IfStmt(ifCondition, new ExpressionStmt(ASTUtils.createSystemOut("Executed")), null);
		ASTHelper.addStmt(stmt, ifStmt);
		
		method_under_test.setBody(stmt);
		
		return method_under_test;
	}

	private ExpressionStmt createClonerObj() {
		VariableDeclaratorId clonerId = new VariableDeclaratorId("c");
		ClassOrInterfaceType clonerType = new ClassOrInterfaceType("Cloner");
		Expression clonerInit = new ObjectCreationExpr(null, clonerType, null);
		VariableDeclarator cloner = new VariableDeclarator(clonerId, clonerInit);
		List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
		vars.add(cloner);
		VariableDeclarationExpr clonerVde = new VariableDeclarationExpr(ASTHelper.createReferenceType("Cloner", 0), vars);
		return new ExpressionStmt(clonerVde);
	}
	
	private ExpressionStmt createCloneObj(Method targetMethod) {
		List<Expression> methodParameters = new ArrayList<Expression>();
		methodParameters.add(new ThisExpr());
		Expression right = new MethodCallExpr(ASTHelper.createNameExpr("c"), "deepClone", methodParameters);
		List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
		vars.add(new VariableDeclarator(new VariableDeclaratorId("clone")));
		String className = targetMethod.getDeclaringClass().getSimpleName();
		Expression left = new VariableDeclarationExpr(ASTHelper.createReferenceType(className, 0), vars);
		AssignExpr assignment = new AssignExpr(left, right, Operator.assign);
		return new ExpressionStmt(assignment);
	}
	
	private Statement createExpectedResult(Method targetMethod, List<Parameter> parameters) {
		List<Expression> methodParameters = new ArrayList<Expression>();
		for (Parameter parameter : parameters) {
			methodParameters.add(ASTHelper.createNameExpr(parameter.getId().getName()));
		}
		Expression right = new MethodCallExpr(new ThisExpr(), targetMethod.getName(), methodParameters);
		List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
		vars.add(new VariableDeclarator(new VariableDeclaratorId("expected_result")));
		String className = targetMethod.getReturnType().getCanonicalName();
		int arrayDimension = 0;
		if (targetMethod.getReturnType().isArray()) {
			arrayDimension = 1;
		}
		Expression left = new VariableDeclarationExpr(ASTHelper.createReferenceType(className, arrayDimension), vars);
		AssignExpr assignment = new AssignExpr(left, right, Operator.assign);
		return new ExpressionStmt(assignment);
	}
	
	private List<Statement> createActualResult(Method targetMethod, CarvingResult candidateES2, List<Parameter> param) {
		List<Statement> stmts = new ArrayList<Statement>();
		
		BlockStmt carved = candidateES2.getBody();
		CloneVisitor visitor = new CloneVisitor();
		BlockStmt cloned = (BlockStmt) visitor.visit(carved, null);
		
		Map<String, Integer> vars = new HashMap<String, Integer>();
		List<Integer> toRemove = new ArrayList<Integer>();
		
		String stubName = stub.getStubName();
		String stubObjectName = null;
		
		// generalize carved test
		for (int i = 0; i < cloned.getStmts().size(); i++) {
			Statement stmt = cloned.getStmts().get(i);
			if (stmt instanceof ExpressionStmt) {
				ExpressionStmt estmt = (ExpressionStmt) stmt;
				
				// VARIABLE
				if (estmt.getExpression() instanceof VariableDeclarationExpr) {
					/* Evosuite does not create variable declarations with multiple definitions,
					 * so we can safely assume to get element 0
					 */
					VariableDeclarationExpr vde = (VariableDeclarationExpr) estmt.getExpression();
					if (vde.getType().toString().equals(stubName)) {
						// STUB CONSTRUCTOR ===> remove
						stubObjectName = vde.getVars().get(0).getId().getName();
						toRemove.add(i);
					}
					else if (vde.getVars().get(0).getInit().toString().contains(stubObjectName)) {
						// ACTUAL_RESULT
						int arrayDimension = 0;
						if (targetMethod.getReturnType().isArray()) {
							arrayDimension = 1;
						}
						vde.setType(ASTHelper.createReferenceType(targetMethod.getReturnType().getCanonicalName(), arrayDimension));
						
						vde.getVars().get(0).setId(new VariableDeclaratorId("actual_result"));
						
						Expression value = vde.getVars().get(0).getInit();
						if (value instanceof MethodCallExpr) {
							MethodCallExpr mce = (MethodCallExpr) value;
							mce.setScope(ASTHelper.createNameExpr("clone"));
						}
						else if (value instanceof CastExpr) {
							CastExpr cast = (CastExpr) value;
							MethodCallExpr mce = (MethodCallExpr) cast.getExpr(); // safe cast: in our case it is always a method call
							mce.setScope(ASTHelper.createNameExpr("clone"));
						}
					}
					else if (stubObjectName != null) {
						Expression expr = vde.getVars().get(0).getInit();
						if (expr instanceof MethodCallExpr) {
							MethodCallExpr mce = (MethodCallExpr) expr;
							if (mce.getScope() instanceof NameExpr) {
								NameExpr ne = (NameExpr) mce.getScope();
								if (ne.getName().equals(stubObjectName)) {
									mce.setScope(ASTHelper.createNameExpr("clone"));
								}
							}
						}
					}
					vars.put(vde.getVars().get(0).getId().getName(), i);
				}
				// METHOD CALL
				else if (estmt.getExpression() instanceof MethodCallExpr) {
					if (stubObjectName == null) {
						continue;
					}
					MethodCallExpr mce = (MethodCallExpr) estmt.getExpression();
					if (mce.getName().equals("method_under_test")) {
						toRemove.add(i);
					}
					else if (mce.getName().equals("set_results")) {
						for (Expression arg : mce.getArgs()) {
							if (arg instanceof NameExpr) {
								NameExpr ne = (NameExpr) arg;
								if (vars.containsKey(ne.getName())) {
									int index = vars.get(ne.getName());
									Statement s = cloned.getStmts().get(index);
									ExpressionStmt es = (ExpressionStmt) s;
									if (es.getExpression() instanceof VariableDeclarationExpr) {
										VariableDeclarationExpr v = (VariableDeclarationExpr) es.getExpression();
										Expression e = v.getVars().get(0).getInit();
										if (e instanceof FieldAccessExpr) {
											FieldAccessExpr fae = (FieldAccessExpr) e;
											if (fae.getField().startsWith("ELEMENT_")) {
												ne.setName(param.get(0).getId().getName());
												toRemove.add(index);
											}
										}
										else if (e instanceof ArrayCreationExpr) {
											for (int j = index + 1; j < i; j++) {
												ExpressionStmt arr = (ExpressionStmt) cloned.getStmts().get(j);
												if (arr.getExpression() instanceof AssignExpr) {
													AssignExpr ae = (AssignExpr) arr.getExpression();
													if (ae.getTarget() instanceof ArrayAccessExpr) {
														ArrayAccessExpr aae = (ArrayAccessExpr) ae.getTarget();
														if (((NameExpr)aae.getName()).getName().equals(v.getVars().get(0).getId().getName())) {
															NameExpr value = (NameExpr) ae.getValue();
															if (vars.containsKey(value.getName())) {
																ExpressionStmt var = (ExpressionStmt) cloned.getStmts().get(vars.get(value.getName()));
																if (var.getExpression() instanceof VariableDeclarationExpr) {
																    VariableDeclarationExpr v2 = (VariableDeclarationExpr) var.getExpression();
																    Expression e2 = v2.getVars().get(0).getInit();
																    if (e2 instanceof FieldAccessExpr) {
																        FieldAccessExpr fae2 = (FieldAccessExpr) e2;
																        if (fae2.getField().startsWith("ELEMENT_")) {
																            VariableDeclarationExpr actual = new VariableDeclarationExpr();
																            actual.setType(param.get(0).getType());
																            VariableDeclarator actualVal = new VariableDeclarator();
																            actualVal.setId(new VariableDeclaratorId("actual_result"));
																            actualVal.setInit(new NameExpr(param.get(0).getId().getName()));
																            List<VariableDeclarator> vdes = new ArrayList<VariableDeclarator>();
																            vdes.add(actualVal);
																            actual.setVars(vdes);
																            cloned.getStmts().set(i, new ExpressionStmt(actual));
																            toRemove.add(index);
																            toRemove.add(j);
																        }
																    }
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
					else if (mce.getScope() instanceof NameExpr) {
						NameExpr scopeName = (NameExpr) mce.getScope();
						if (scopeName.getName().equals(stubObjectName)) {
							mce.setScope(ASTHelper.createNameExpr("clone"));
						}
						for (Expression arg : mce.getArgs()) {
							if (arg instanceof NameExpr) {
								NameExpr ne = (NameExpr) arg;
								if (vars.containsKey(ne.getName())) {
									int index = vars.get(ne.getName());
									Statement s = cloned.getStmts().get(index);
									ExpressionStmt es = (ExpressionStmt) s;
									if (es.getExpression() instanceof VariableDeclarationExpr) {
										VariableDeclarationExpr v = (VariableDeclarationExpr) es.getExpression();
										Expression e = v.getVars().get(0).getInit();
										if (e instanceof FieldAccessExpr) {
											FieldAccessExpr fae = (FieldAccessExpr) e;
											if (fae.getField().startsWith("ELEMENT_")) {
												ne.setName(param.get(0).getId().getName());
												toRemove.add(index);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		Collections.sort(toRemove);
		int count = 0;
		for (int rem : toRemove) {
			cloned.getStmts().remove(rem - (count++));
		}
		
		stmts.addAll(cloned.getStmts());
		
		return stmts;
	}

	@Override
	protected MethodDeclaration getSetResultsMethod(Method targetMethod) {
		return null;
	}

}