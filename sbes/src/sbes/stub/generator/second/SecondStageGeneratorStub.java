package sbes.stub.generator.second;

import japa.parser.ASTHelper;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.FieldDeclaration;
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
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.ConditionalExpr;
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
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.PrimitiveType.Primitive;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.CloneVisitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import sbes.ast.ArrayCellDeclarationVisitor;
import sbes.ast.ArrayDeclarationVisitor;
import sbes.ast.ArrayStubRemoverVisitor;
import sbes.ast.CloneMethodCallsVisitor;
import sbes.ast.EquivalentSequenceCallVisitor;
import sbes.ast.MethodCallVisitor;
import sbes.ast.VariableDeclarationVisitor;
import sbes.ast.VariableUseVisitor;
import sbes.ast.inliner.FieldVariablesToInline;
import sbes.ast.inliner.Inliner;
import sbes.ast.inliner.PrimitiveVariablesToInline;
import sbes.ast.renamer.NameExprRenamer;
import sbes.ast.renamer.StubRenamer;
import sbes.exceptions.GenerationException;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.CounterexampleStub;
import sbes.stub.Stub;
import sbes.stub.generator.AbstractStubGenerator;
import sbes.util.ASTUtils;
import sbes.util.ClassUtils;
import sbes.util.ReflectionUtils;

public class SecondStageGeneratorStub extends AbstractStubGenerator {

	private static final Logger logger = new Logger(SecondStageGeneratorStub.class);

	protected CarvingResult candidateES;
	protected List<Statement> equivalence;
	protected List<FieldDeclaration> fields;
	protected Stub stub;

	public SecondStageGeneratorStub(final List<TestScenario> scenarios, Stub stub, CarvingResult candidateES, List<FieldDeclaration> fields) {
		super(scenarios);
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
		for (ImportDeclaration importDeclaration : candidateES.getImports()) {
			if (!imports.contains(importDeclaration) && !importDeclaration.getName().getName().contains("Stub")) {
				imports.add(importDeclaration);
			}
		}
		return imports;
	}

	@Override
	protected TypeDeclaration getClassDeclaration(Class<?> c) {
		stubName = c.getSimpleName() + STUB_EXTENSION + "_2";

		// extends base class
		ClassOrInterfaceType extendClassDecl = new ClassOrInterfaceType(c.getSimpleName());
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
			if (ReflectionUtils.canUse(constructor)) {
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
	protected List<BodyDeclaration> getAdditionalMethods(Method targetMethod, Method[] methods, Class<?> c) {
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

		method_under_test.setParameters(param);

		NameExpr distanceClass = ASTHelper.createNameExpr("Distance");
		String distanceMethod = "distance";

		// if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)
		Expression zeroDouble = new DoubleLiteralExpr("0.0d");
		List<Expression> distanceStateArgs = new ArrayList<Expression>();
		distanceStateArgs.add(new ThisExpr());
		distanceStateArgs.add(ASTHelper.createNameExpr("clone"));
		Expression state = new MethodCallExpr(distanceClass, distanceMethod, distanceStateArgs);
		BinaryExpr stateCondition = new BinaryExpr(state, zeroDouble, japa.parser.ast.expr.BinaryExpr.Operator.greater);

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

		IfStmt ifStmt = new IfStmt(ifCondition, new ExpressionStmt(ASTUtils.createSystemOut("Executed")), null);
		ASTHelper.addStmt(stmt, ifStmt);

		method_under_test.setBody(stmt);

		return method_under_test;
	}

	protected ExpressionStmt createClonerObj() {
		VariableDeclaratorId clonerId = new VariableDeclaratorId("c");
		ClassOrInterfaceType clonerType = new ClassOrInterfaceType("Cloner");
		Expression clonerInit = new ObjectCreationExpr(null, clonerType, null);
		VariableDeclarator cloner = new VariableDeclarator(clonerId, clonerInit);
		List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
		vars.add(cloner);
		VariableDeclarationExpr clonerVde = new VariableDeclarationExpr(ASTHelper.createReferenceType("Cloner", 0), vars);
		return new ExpressionStmt(clonerVde);
	}

	protected ExpressionStmt createCloneObj(Method targetMethod) {
		List<Expression> methodParameters = new ArrayList<Expression>();
		methodParameters.add(new ThisExpr());
		Expression right = new MethodCallExpr(ASTHelper.createNameExpr("c"), "deepClone", methodParameters);
		List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
		vars.add(new VariableDeclarator(new VariableDeclaratorId("clone")));
		String className = ClassUtils.getSimpleClassname(Options.I().getTargetMethod());
		Expression left = new VariableDeclarationExpr(ASTHelper.createReferenceType(className, 0), vars);
		AssignExpr assignment = new AssignExpr(left, right, Operator.assign);
		return new ExpressionStmt(assignment);
	}

	protected Statement createExpectedResult(Method targetMethod, final List<Parameter> parameters) {
		List<Expression> methodParameters = new ArrayList<Expression>();
		for (Parameter parameter : parameters) {
			methodParameters.add(ASTHelper.createNameExpr(parameter.getId().getName()));
		}
		Expression right = new MethodCallExpr(new ThisExpr(), targetMethod.getName(), methodParameters);
		if (!targetMethod.getReturnType().equals(void.class)) {
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
		return new ExpressionStmt(right);
	}

	protected List<Statement> createActualResult(Method targetMethod, CarvingResult candidateES2, final List<Parameter> param) {
		List<Statement> stmts = new ArrayList<Statement>();

		BlockStmt carved = candidateES2.getBody();
		CloneVisitor visitor = new CloneVisitor();
		BlockStmt cloned = (BlockStmt) visitor.visit(carved, null);

		//PHASE 0: clean carved result by removing method_under_test
		removeMethodUnderTest(cloned);
		//PHASE 1: remove accesses to the stub object and replace them to accesses to the clone object
		stubToClone(cloned);
		//PHASE 2: identify equivalent sequence parameters
		identifyEquivalentSequenceParameters(cloned, param);
		//PHASE 3: identify actual_result
		if (!targetMethod.getReturnType().equals(void.class)) {
			identifyActualResult(cloned, targetMethod, param);
		}
		//PHASE 4: check clone.METHOD arguments, if array prune it
		pruneArrayParameters(cloned, targetMethod);
		//PHASE 5: remove dead code
		deadCodeElimination(cloned);
		
		PrimitiveVariablesToInline pvi = new PrimitiveVariablesToInline();
		pvi.visit(cloned, null);
		
		FieldVariablesToInline fvi = new FieldVariablesToInline();
		fvi.visit(cloned, null);
		
		for (VariableDeclarator vd : pvi.getToInline()) {
			new Inliner().visit(cloned, vd);
		}
		
		for (VariableDeclarator vd : fvi.getToInline()) {
			new Inliner().visit(cloned, vd);
		}

		boolean modified = true;
		while (modified) {
			modified = false;
			for (int i = 0; i < cloned.getStmts().size(); i++) {
				Statement stmt = cloned.getStmts().get(i);
				if (stmt instanceof ExpressionStmt) {
					Expression e = ((ExpressionStmt) stmt).getExpression();
					if (e instanceof VariableDeclarationExpr) {
						VariableDeclarationExpr vde = (VariableDeclarationExpr) e;
						VariableDeclarator var = vde.getVars().get(0); // safe

						if (var.getId().getName().contains("_result")) {
							continue;
						}

						VariableUseVisitor vuv = new VariableUseVisitor(var.getId().getName());
						vuv.visit(cloned, null);
						if (!vuv.isUsed() && 
								(!var.getInit().toString().contains("clone") &&!var.getInit().toString().contains("_result")) &&
								unusedObject(var, cloned)) {
							cloned.getStmts().remove(i);
							i--;
							modified = true;
						}
					}
				}
			}
		}

		stmts.addAll(cloned.getStmts());

		return stmts;
	}

	private boolean unusedObject(VariableDeclarator var, BlockStmt cloned) {
		if (var.getInit() instanceof MethodCallExpr) {
			MethodCallExpr mce = (MethodCallExpr) var.getInit();
			if (Character.isUpperCase(mce.getScope().toString().charAt(0))) {
				return true; // static call
			}
			else {
				VariableUseVisitor vuv = new VariableUseVisitor(mce.getScope().toString());
				vuv.visit(cloned, null);
				if (!vuv.isUsed()) {
					return true;
				}
			}
			return false;
		}
		return true;
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
					}
				}
			}
		}
	}

	/*
	 * PHASE 1: remove stub constructor and rename all occurrences of the
	 * stub object to the cloned object
	 */
	private void stubToClone(BlockStmt cloned) {
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

		new NameExprRenamer(stubObjectName, "clone").visit(cloned, null);
		new StubRenamer(stubName, stubName.substring(0, stubName.indexOf('_'))).visit(cloned, null);
		new ArrayStubRemoverVisitor().visit(cloned, null);
	}

	/*
	 * 	PHASE 2: identify parameters in all the calls on clone object and 
	 * 	identify their source.
	 *  this means that, if they are references to the static inputs in the
	 *  first stub, we need to match an input in the current stub
	 */
	private void identifyEquivalentSequenceParameters(BlockStmt cloned, final List<Parameter> param) {
		EquivalentSequenceCallVisitor escv = new EquivalentSequenceCallVisitor();
		escv.visit(cloned, null);
		for (MethodCallExpr methodCall : escv.getDependencies()) {
			analyzeParameters(cloned, param, methodCall);
		}
		for (VariableDeclarationExpr vde : escv.getAssignments()) {
			Expression init = vde.getVars().get(0).getInit();
			if (init instanceof FieldAccessExpr) {
				FieldAccessExpr fae = (FieldAccessExpr) init;
				if (fae.getField().startsWith("ELEMENT_")) {
					// it is an input
					boolean modified = false;
					for (Parameter p : param) {
						if (vde.getType().toString().contains(ClassUtils.getSimpleClassnameFromCanonical(p.getType().toString()))) {
							vde.getVars().get(0).setInit(ASTHelper.createNameExpr(p.getId().getName()));
							modified = true;
							break;
						}
					}
					if (!modified && param.size() == 1) {
						vde.getVars().get(0).setInit(ASTHelper.createNameExpr(param.get(0).getId().getName()));
					}
				}
			}
		}
	}

	private void analyzeParameters(BlockStmt cloned, final List<Parameter> param, MethodCallExpr methodCall) {
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
						int index = 0;
						for (int j = 0; j < param.size(); j++) {
							Parameter p = param.get(j);
							if (p.getType().toString().contains(vde.getType().toString().replace("[]", ""))) {
								index = j;
								break;
							}
							else if (p.getType().toString().contains("java.lang.Iterable") && 
									(vde.getType().toString().contains("Collection") || vde.getType().toString().contains("List") )) {
								index = j;
								break;
							}
 						}
						methodCall.getArgs().set(i, ASTHelper.createNameExpr(param.get(index).getId().getName()));
						// now we have to ensure that the type of the parameter
						// in method_under_test corresponds to this type
						Type testType = vde.getType();
						Type paramType = param.get(index).getType();
						if (!paramType.toString().contains(testType.toString())) {
							if (paramType.toString().contains("Object") || testType.toString().contains("Object")){
								// mismatch! we rely on the type found in the test
								param.get(index).setType(testType);
							}
						}
					}
				}
				else if (init instanceof ArrayCreationExpr) {
					// we should check what is inside the array
					ArrayDeclarationVisitor vuv = new ArrayDeclarationVisitor(name);
					vuv.visit(cloned, methodCall.getName());
					if (!vuv.isUsed()) {
						methodCall.getArgs().set(i, new IntegerLiteralExpr("0")); // we assume it is and integer
					}
					else {
						ArrayCellDeclarationVisitor acdv = new ArrayCellDeclarationVisitor(name, "0");
						acdv.visit(cloned, null);
						Expression value = acdv.getValue();
						if (value instanceof NameExpr || value instanceof CastExpr) {
							String actual_name = ASTUtils.getName(value);
							VariableDeclarationVisitor vdv = new VariableDeclarationVisitor(actual_name);
							vdv.visit(cloned, null);
							VariableDeclarationExpr actual_vde = vdv.getVariable();
							if (actual_vde != null) {
								Expression actual_init = actual_vde.getVars().get(0).getInit();
								if (actual_init instanceof FieldAccessExpr) {
									FieldAccessExpr actual_fae = (FieldAccessExpr) actual_init;
									if (actual_fae.getField().startsWith("ELEMENT_")) {
										// it is an input
										String indexString = actual_fae.getField().substring(actual_fae.getField().lastIndexOf('_') + 1);
										int index = Integer.valueOf(indexString);
										methodCall.getArgs().set(i, ASTHelper.createNameExpr(param.get(index).getId().getName()));
										// now we have to ensure that the type of the parameter
										// in method_under_test corresponds to this type
										Type testType = vde.getType();
										Type paramType = param.get(index).getType();
										if (!testType.toString().equals(paramType.toString())) {
											// mismatch! we rely on the type found in the test
											if (testType instanceof ReferenceType) {
												ReferenceType rt = (ReferenceType) testType;
												if (rt.getArrayCount() > 0) {
													rt.setArrayCount(rt.getArrayCount() - 1);
												}
												param.get(index).setType(rt);
											}
											else {
												param.get(index).setType(testType);
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

	/*
	 * PHASE 3: search for set_results and then resolve the definition of the
	 * variable used as input
	 */
	private void identifyActualResult(BlockStmt cloned, Method targetMethod, final List<Parameter> param) {
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
				List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
				VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
				vd.setInit(defaultValue);
				vars.add(vd);
				VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
				cloned.getStmts().add(new ExpressionStmt(actualResult));
			}
			else {
				// it is an object
				List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
				VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
				vd.setInit(new NullLiteralExpr());
				vars.add(vd);
				VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
				cloned.getStmts().add(new ExpressionStmt(actualResult));
			}
			return;
		}

		VariableDeclarationExpr vde = null;
		Expression arg = mce.getArgs().get(0);
		if (arg instanceof FieldAccessExpr) {
			FieldAccessExpr fae = (FieldAccessExpr) arg;
			List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
			VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
			vd.setInit(fae);
			vars.add(vd);
			VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
			cloned.getStmts().add(new ExpressionStmt(actualResult));
			return;
		}
		else if (arg instanceof LiteralExpr) {
			LiteralExpr le = (LiteralExpr) arg;
			List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
			VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
			vd.setInit(le);
			vars.add(vd);
			VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
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
										List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
										VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
										vd.setInit(new NameExpr(param.get(0).getId().getName()));
										vars.add(vd);
										VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
										cloned.getStmts().add(new ExpressionStmt(actualResult));
										break;
									}
								}
							}
							else if (internalInit instanceof FieldAccessExpr) {
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
							else if (internalInit instanceof MethodCallExpr) {
								List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
								VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
								vd.setInit(internalInit);
								vars.add(vd);
								VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
								cloned.getStmts().add(new ExpressionStmt(actualResult));
							}
						}
					}
					else if (e instanceof CastExpr) {
						Expression exp = ((CastExpr) e).getExpr();
						if (exp instanceof IntegerLiteralExpr) {
							List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
							VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
							vd.setInit(e);
							vars.add(vd);
							VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
							cloned.getStmts().add(new ExpressionStmt(actualResult));
						}
					}
					else if (e instanceof NullLiteralExpr) {
						List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
						VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
						vd.setInit(e);
						vars.add(vd);
						VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
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
							List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
							VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
							vd.setInit(new NameExpr(param.get(0).getId().getName()));
							vars.add(vd);
							VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
							cloned.getStmts().add(new ExpressionStmt(actualResult));
							break;
						}
					}
				}
				else if (init instanceof CastExpr) {
					List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
					VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
					vd.setInit(((CastExpr) init).getExpr());
					vars.add(vd);
					VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof BooleanLiteralExpr) {
					List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
					VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
					vd.setInit(init);
					vars.add(vd);
					VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof IntegerLiteralExpr) {
					List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
					VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
					vd.setInit(init);
					vars.add(vd);
					VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof DoubleLiteralExpr) {
					List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
					VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
					vd.setInit(init);
					vars.add(vd);
					VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof StringLiteralExpr) {
					List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
					VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
					vd.setInit(init);
					vars.add(vd);
					VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
				}
				else if (init instanceof FieldAccessExpr) {
					FieldAccessExpr fae = (FieldAccessExpr) init;
					if (fae.getField().startsWith("ELEMENT_")) {
						// it is an input
						List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
						VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
						vd.setInit(new NameExpr(param.get(0).getId().getName()));
						vars.add(vd);
						VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
						cloned.getStmts().add(new ExpressionStmt(actualResult));
					}
					else {
						List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
						VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
						vd.setInit(fae);
						vars.add(vd);
						VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
						cloned.getStmts().add(new ExpressionStmt(actualResult));
					}
				}
				else if (init instanceof ObjectCreationExpr) {
					ObjectCreationExpr oce = (ObjectCreationExpr) init;
					List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
					VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
					vd.setInit(oce);
					vars.add(vd);
					VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
					return;
				}
				else if (init instanceof UnaryExpr) {
					UnaryExpr ue = (UnaryExpr) init;
					List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
					VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("actual_result"));
					vd.setInit(ue);
					vars.add(vd);
					VariableDeclarationExpr actualResult = new VariableDeclarationExpr(ASTHelper.createReferenceType(resultType, arrayDimension), vars);
					cloned.getStmts().add(new ExpressionStmt(actualResult));
					return;

				}
				NameExprRenamer conv = new NameExprRenamer(varName, "actual_result");
				conv.visit(cloned, null);
			}
		}
	}

	protected String getActualResultType(Method targetMethod) {
		return targetMethod.getReturnType().getCanonicalName();
	}

	/*
	 * PHASE 4: remove spurious parameters from method calls due to array-based stub
	 */
	private void pruneArrayParameters(BlockStmt cloned, Method targetMethod) {
		// get all calls on clone obj, therefore a stub obj
		CloneMethodCallsVisitor cov = new CloneMethodCallsVisitor();
		cov.visit(cloned, null);
		List<MethodCallExpr> methods = cov.getMethods();
		for (MethodCallExpr methodCallExpr : methods) {
			// if it has no parameters, we are not interested in it
			if (methodCallExpr.getName().equals("set_results")) {
				continue;
			}
			if (methodCallExpr.getArgs() == null) {
				continue;
			}
			// otherwise, we have to check if it takes in input an array
			// if so, we have to get the first value used
			for (int i = 0; i < methodCallExpr.getArgs().size(); i++) {
				Expression arg = methodCallExpr.getArgs().get(i);
				if (arg instanceof NameExpr) {
					VariableDeclarationVisitor vdv = new VariableDeclarationVisitor(((NameExpr) arg).getName());
					vdv.visit(cloned, null);
					VariableDeclarationExpr vde = vdv.getVariable();
					if (vde == null) {
						continue;
					}
					else {
						if (vde.getType() instanceof ReferenceType) {
							ReferenceType rt = (ReferenceType) vde.getType();
							if (rt.getArrayCount() > 0) {
								Class<?> c = targetMethod.getDeclaringClass();
								Method[] ms = c.getMethods();
								for (Method method : ms) {
									if (method.getName().equals(methodCallExpr.getName()) && 
											method.getParameterTypes().length == methodCallExpr.getArgs().size()) {
										// it should be the correct method
										if (!method.getParameterTypes()[i].isArray()) {
											String variableId = vde.getVars().get(0).getId().getName();
											ArrayCellDeclarationVisitor acdv = new ArrayCellDeclarationVisitor(variableId, "0");
											acdv.visit(cloned, null);
											Expression value = acdv.getValue();
											if (value != null) {
												methodCallExpr.getArgs().set(i, value);
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

	/*
	 * PHASE 5: dead code elimination: remove everything not necessary
	 */
	private void deadCodeElimination(BlockStmt cloned) {
		boolean changed = false;
		do {
			changed = false;
			for (int i = cloned.getStmts().size() - 1; i >= 0; i--) {
				Statement stmt = cloned.getStmts().get(i);
				if (stmt instanceof ExpressionStmt) {
					ExpressionStmt estmt = (ExpressionStmt) stmt;
					if (estmt.getExpression() instanceof MethodCallExpr) {
						MethodCallExpr mce = (MethodCallExpr) estmt.getExpression();
						if (mce.getName().equals("set_results")) {
							cloned.getStmts().remove(i);
							i = i == cloned.getStmts().size() ? i : i++;
							changed = true;
						}
					}
					else if (estmt.getExpression() instanceof VariableDeclarationExpr) {
						VariableDeclarationExpr vde = (VariableDeclarationExpr) estmt.getExpression();
						VariableDeclarator vd = vde.getVars().get(0);
						if (vde.getVars().get(0).getInit() instanceof MethodCallExpr) {
							MethodCallExpr mce = (MethodCallExpr) vde.getVars().get(0).getInit();
							if (mce.getName().equals("realSize")) {
								ConditionalExpr ce = new ConditionalExpr();

								BinaryExpr condition = new BinaryExpr();
								condition.setLeft(new MethodCallExpr(mce.getScope(), "size"));
								condition.setRight(new IntegerLiteralExpr("0"));
								condition.setOperator(japa.parser.ast.expr.BinaryExpr.Operator.greater);
								ce.setCondition(condition);

								BinaryExpr subtraction = new BinaryExpr();
								subtraction.setLeft(new MethodCallExpr(mce.getScope(), "size"));
								subtraction.setRight(new IntegerLiteralExpr("1"));
								subtraction.setOperator(japa.parser.ast.expr.BinaryExpr.Operator.minus);
								ce.setThenExpr(subtraction);

								ce.setElseExpr(new IntegerLiteralExpr("0"));

								vde.getVars().get(0).setInit(ce);
								vde.setType(new PrimitiveType(Primitive.Int));
							}
							else if (mce.getName().equals("collectionSize")) {
								String name = ASTUtils.getName(mce.getArgs().get(0));
								vde.getVars().get(0).setInit(new MethodCallExpr(new NameExpr(name), "size"));
								vde.setType(new PrimitiveType(Primitive.Int));
							}
						}

						if (vd.getId().getName().equals("actual_result")) {
							continue;
						}
						else if (vd.getInit() instanceof FieldAccessExpr) {
							// if synthesis input, remove it
							FieldAccessExpr fae = (FieldAccessExpr) vd.getInit();
							if (fae.getField().startsWith("ELEMENT_")) {
								cloned.getStmts().remove(i);
								i = i == cloned.getStmts().size() ? i : i++;
								changed = true;
							}
							continue;
						}
						else if (vd.getInit() instanceof CastExpr) {
							CastExpr ce = (CastExpr) vd.getInit();
							if (ce.getExpr() instanceof MethodCallExpr) {
								continue;	
							}
						}
						else if (vd.getInit() instanceof MethodCallExpr) {
							MethodCallExpr mce = (MethodCallExpr) vd.getInit();
							if (ASTUtils.getName(mce.getScope()).equals("clone")) {
								String varName = vd.getId().getName();
								VariableUseVisitor vu = new VariableUseVisitor(varName);
								vu.visit(cloned, null);
								if (!vu.isUsed()) {
									ExpressionStmt exprStmt = (ExpressionStmt) vde.getParentNode();
									exprStmt.setExpression(mce);
								}
							}
							if (mce.getArgs() != null) {
								for (int j = 0; j < mce.getArgs().size(); j++) {
									Expression arg = mce.getArgs().get(j);
									if (arg instanceof CastExpr) {
										CastExpr ce = (CastExpr) arg;
										if (ce.getExpr() instanceof ArrayAccessExpr) {
											ArrayAccessExpr aae = (ArrayAccessExpr) ce.getExpr();
											String variableName = ASTUtils.getName(aae.getName());
											String index = ((IntegerLiteralExpr)aae.getIndex()).getValue();
											ArrayCellDeclarationVisitor acdv = new ArrayCellDeclarationVisitor(variableName, index);
											acdv.visit(cloned, null);
											Expression value = acdv.getValue();
											if (value == null) {
												// no def
												mce.getArgs().set(j, new IntegerLiteralExpr("0"));
											}
										}
									}
								}
							}
							continue;
						}

						// check use
						String varName = vd.getId().getName();
						VariableUseVisitor vu = new VariableUseVisitor(varName);
						vu.visit(cloned, null);
						if (!vu.isUsed()) {
							removeDeadAssignments(cloned, i, varName);
							cloned.getStmts().remove(i);
							i = i == cloned.getStmts().size() ? i : i++;
							changed = true;
						}
					}
					else if (estmt.getExpression() instanceof AssignExpr) {
						cloned.getStmts().remove(i);
						i = i == cloned.getStmts().size() ? i : i++;
						changed = true;
					}
				}
			}
		} while (changed);
	}

	private void removeDeadAssignments(BlockStmt cloned, int i, String varName) {
		for (int j = i; j < cloned.getStmts().size(); j++) {
			ExpressionStmt es = (ExpressionStmt) cloned.getStmts().get(j);
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

	protected List<Parameter> getParameterType(Class<?>[] parameters, Method targetMethod) {
		List<Parameter> toReturn = new ArrayList<Parameter>();
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

	@Override
	protected MethodDeclaration getSetResultsMethod(Method targetMethod) {
		return null;
	}

}
