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
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.BinaryExpr;
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
import java.util.List;

import sbes.ast.ArrayCellDeclarationVisitor;
import sbes.ast.EquivalentSequenceCallVisitor;
import sbes.ast.MethodCallVisitor;
import sbes.ast.StubArrayVariableRemoverVisitor;
import sbes.ast.StubObjToCloneObjVisitor;
import sbes.ast.VariableDeclarationVisitor;
import sbes.ast.VariableUseVisitor;
import sbes.logging.Logger;
import sbes.result.CarvingResult;
import sbes.stub.CounterexampleStub;
import sbes.stub.GenerationException;
import sbes.stub.Stub;
import sbes.util.ASTUtils;

public class SecondStageStubGenerator extends StubGenerator {

	private static final Logger logger = new Logger(SecondStageStubGenerator.class);
	
	private CarvingResult candidateES;
	private List<Statement> equivalence;
	private Stub stub;
	
	public SecondStageStubGenerator(Stub stub, CarvingResult candidateES) {
		this.stub = stub;
		this.candidateES = candidateES;
	}

	@Override
	public Stub generateStub() {
		logger.info("Generating stub for second phase");
		Stub stub = super.generateStub();
		CounterexampleStub counterexampleStub = new CounterexampleStub(stub.getAst(), stub.getStubName(), equivalence);
		logger.info("Generating stub for second phase - done");
		return counterexampleStub;
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
		equivalence = stmts;
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
		
		//PHASE 0: clean carved result by removing method_under_test
		removeMethodUnderTest(cloned);
		//PHASE 1: remove accesses to the stub object and replace them to accesses to the clone object
		stubObjToCloneObj(cloned);
		//PHASE 2: identify equivalent sequence parameters
		identifyEquivalentSequenceParameters(cloned, param);
		//PHASE 3: identify actual_result
		identifyActualResult(cloned, targetMethod, param);
		//PHASE 4: remove dead code
		deadCodeElimination(cloned);
		
		stmts.addAll(cloned.getStmts());
		
		return stmts;
	}

	/*
	 * PHASE 0: remove method_under_test
	 */
	private void removeMethodUnderTest(BlockStmt cloned) {
		for (int i = cloned.getStmts().size() - 1; i > 0; i--) {
			Statement stmt = cloned.getStmts().get(i);
			if (stmt instanceof ExpressionStmt) {
				ExpressionStmt estmt = (ExpressionStmt) stmt;
				if (estmt.getExpression() instanceof MethodCallExpr) {
					MethodCallExpr mce = (MethodCallExpr) estmt.getExpression();
					if (mce.getNameExpr().getName().equals("method_under_test")) {
						cloned.getStmts().remove(i);
						break;
					}
				}
			}
		}
	}

	/*
	 * PHASE 1: remove stub constructor and rename all occurrences of the
	 * stub object to the cloned object
	 */
	private void stubObjToCloneObj(BlockStmt cloned) {
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
		
		StubObjToCloneObjVisitor visitor = new StubObjToCloneObjVisitor(stubObjectName);
		visitor.visit(cloned, null);
		
		StubArrayVariableRemoverVisitor msv = new StubArrayVariableRemoverVisitor();
		msv.visit(cloned, null);
	}
	
	/*
	 * 	PHASE 2: identify parameters in all the calls on clone object and 
	 * 	identify their source.
	 *  this means that, if they are references to the static inputs in the
	 *  first stub, we need to match an input in the current stub
	 */
	private void identifyEquivalentSequenceParameters(BlockStmt cloned, List<Parameter> param) {
		EquivalentSequenceCallVisitor escv = new EquivalentSequenceCallVisitor();
		escv.visit(cloned, null);
		for (MethodCallExpr methodCall : escv.getDependencies()) {
			analyzeParameters(cloned, param, methodCall);
		}
	}

	private void analyzeParameters(BlockStmt cloned, List<Parameter> param, MethodCallExpr methodCall) {
		for (int i = 0; i < methodCall.getArgs().size(); i++) { 
			Expression arg = methodCall.getArgs().get(i);
			VariableDeclarationExpr vde = null;
			
			String name = null;
			if (arg instanceof ArrayAccessExpr) {
				ArrayAccessExpr aae = (ArrayAccessExpr) arg;
				ArrayCellDeclarationVisitor acdv = new ArrayCellDeclarationVisitor(ASTUtils.getName(aae.getName()), aae.getIndex().toString());
				acdv.visit(cloned, null);
				name = ASTUtils.getName(acdv.getValue());
			}
			else {
				name = ASTUtils.getName(arg);
			}
			
			if (name != null) {
				VariableDeclarationVisitor visitor = new VariableDeclarationVisitor(name);
				visitor.visit(cloned, null);
				vde = visitor.getVariable();
			}
			
			if (vde != null) {
				Expression init = vde.getVars().get(0).getInit();
				if (init instanceof FieldAccessExpr) {
					FieldAccessExpr fae = (FieldAccessExpr) init;
					if (fae.getField().startsWith("ELEMENT_")) {
						// it is an input
						methodCall.getArgs().set(i, ASTHelper.createNameExpr(param.get(0).getId().getName())); // FIXME: check inputs
					}
				}
				else if (init instanceof ArrayCreationExpr) {
					// we should check what is inside the array
					throw new UnsupportedOperationException("Creation of a second stage stub with array parameter non yet supported");
				}
			}
		}
	}
	
	/*
	 * PHASE 3: search for set_results and then resolve the definition of the
	 * variable used as input
	 */
	private void identifyActualResult(BlockStmt cloned, Method targetMethod, List<Parameter> param) {
		MethodCallVisitor mcv = new MethodCallVisitor("set_results", 1);
		mcv.visit(cloned, null);
		MethodCallExpr mce = mcv.getMethodCall();
		
		Expression arg = mce.getArgs().get(0);
		VariableDeclarationExpr vde = null;
		String name = ASTUtils.getName(arg);
		String resultType = targetMethod.getReturnType().getCanonicalName();
		int arrayDimension = 0;
		if (targetMethod.getReturnType().isArray()) {
			arrayDimension = 1;
		}
		
		if (name != null) {
			VariableDeclarationVisitor visitor = new VariableDeclarationVisitor(name);
			visitor.visit(cloned, null);
			vde = visitor.getVariable();
		}
		
		if (vde != null) {
			Expression init = vde.getVars().get(0).getInit();
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
						if (internalInit instanceof FieldAccessExpr) {
							FieldAccessExpr fae = (FieldAccessExpr) internalInit;
							if (fae.getField().startsWith("ELEMENT_")) {
								// it is an input
								List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
								VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
								vd.setInit(new NameExpr(param.get(0).getId().getName()));
								vars.add(vd);
								VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
								cloned.getStmts().add(new ExpressionStmt(actualResult));
							}
						}
						else if (internalInit instanceof ObjectCreationExpr) {
							ObjectCreationExpr oce = (ObjectCreationExpr) internalInit;
							List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
							VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
							vd.setInit(oce);
							vars.add(vd);
							VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
							cloned.getStmts().add(new ExpressionStmt(actualResult));
						}
					}
				}
			}
			else if (init instanceof MethodCallExpr) {
				// the actual_result is the return value
				vde.setType(ASTHelper.createReferenceType(resultType, arrayDimension));
				vde.getVars().get(0).getId().setName("actual_result");
			}
		}
	}
	
	/*
	 * PHASE 4: dead code elimination: remove everything not necessary
	 */
	private void deadCodeElimination(BlockStmt cloned) {
		for (int i = cloned.getStmts().size() - 1; i >= 0; i--) {
			Statement stmt = cloned.getStmts().get(i);
			if (stmt instanceof ExpressionStmt) {
				ExpressionStmt estmt = (ExpressionStmt) stmt;
				if (estmt.getExpression() instanceof MethodCallExpr) {
					MethodCallExpr mce = (MethodCallExpr) estmt.getExpression();
					if (mce.getName().equals("set_results")) {
						cloned.getStmts().remove(i);
						i = i == cloned.getStmts().size() ? i : i++;
					}
				}
				else if (estmt.getExpression() instanceof VariableDeclarationExpr) {
					VariableDeclarationExpr vde = (VariableDeclarationExpr) estmt.getExpression();
					VariableDeclarator vd = vde.getVars().get(0);
					if (vd.getId().getName().equals("actual_result")) {
						continue;
					}
					else if (vd.getInit() instanceof FieldAccessExpr) {
						// if synthesis input, remove it
						FieldAccessExpr fae = (FieldAccessExpr) vd.getInit();
						if (fae.getField().startsWith("ELEMENT_")) {
							cloned.getStmts().remove(i);
							i = i == cloned.getStmts().size() ? i : i++;
						}
					}
					else if (vd.getInit() instanceof MethodCallExpr) {
						// heuristic
						continue;
					}
					else {
						// check use
						String varName = vd.getId().getName();
						VariableUseVisitor vu = new VariableUseVisitor(varName);
						vu.visit(cloned, null);
						if (!vu.isUsed()) {
							removeDeadAssignments(cloned, i, varName);
							cloned.getStmts().remove(i);
							i = i == cloned.getStmts().size() ? i : i++;
						}
					}
				}
			}
		}
	}

	private void removeDeadAssignments(BlockStmt cloned, int i, String varName) {
		for (int j = i; j < cloned.getStmts().size(); j++) {
			Statement s = cloned.getStmts().get(j);
			if (s instanceof ExpressionStmt) {
				ExpressionStmt es = (ExpressionStmt) s;
				if (es.getExpression() instanceof AssignExpr) {
					AssignExpr ae = (AssignExpr) es.getExpression();
					if (ae.getTarget() instanceof NameExpr) {
						NameExpr neae = (NameExpr) ae.getTarget();
						if (neae.getName().equals(varName)) {
							cloned.getStmts().remove(j);
							j--;
						}
					}					
					else if (ae.getTarget() instanceof ArrayAccessExpr) {
						ArrayAccessExpr aae = (ArrayAccessExpr) ae.getTarget();
						if (aae.getName() instanceof NameExpr) {
							NameExpr neae = (NameExpr) aae.getName();
							if (neae.getName().equals(varName)) {
								cloned.getStmts().remove(j);
								j--;
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	protected MethodDeclaration getSetResultsMethod(Method targetMethod) {
		return null;
	}

}
