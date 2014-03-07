package sbes.scenario;

import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.VariableDeclarator;
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sbes.ast.ActualStateVisitor;
import sbes.ast.CounterexampleExpectedResultVisitor;
import sbes.ast.ExpectedStateVisitor;
import sbes.ast.ExtractValuesFromTargetMethodVisitor;
import sbes.ast.ExtractVariablesFromTargetMethodVisitor;
import sbes.ast.GenericToConcreteClassVisitor;
import sbes.ast.ObjToExpectedStateVisitor;
import sbes.ast.ObjToObjVisitor;
import sbes.ast.SubstituteNameVisitor;
import sbes.execution.InternalClassloader;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.result.TestScenario;
import sbes.stub.GenerationException;
import sbes.stub.generator.FirstStageStubGenerator;
import sbes.util.ClassUtils;

public class CounterexampleGeneralizer {

private static final Logger logger = new Logger(TestScenarioGeneralizer.class);
	
	private int index;
	
	public CounterexampleGeneralizer(int scenarioSize) {
		this.index = scenarioSize;
	}
	
	public TestScenario generalizeCounterexampleToScenario(CarvingResult carvedTest) {
		logger.debug("Generalizing carved body");
		
		InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath());
		ClassLoader classloader = ic.getClassLoader();
		
		Class<?> c;
		try {
			c = Class.forName(ClassUtils.getCanonicalClassname(Options.I().getMethodSignature()), false, classloader);
		} catch (ClassNotFoundException e) {
			// infeasible, we already checked the classpath
			throw new GenerationException("Target class not found");
		}
		
		// get class' methods
		Method[] methods = ClassUtils.getClassMethods(c);
		// get method signature
		String methodSignature = ClassUtils.getMethodname(Options.I().getMethodSignature());
		// get target method from the list of class' methods
		Method targetMethod = findTargetMethod(methods, methodSignature);

		CloneVisitor cloner = new CloneVisitor();
		BlockStmt cloned = (BlockStmt) cloner.visit(carvedTest.getBody(), null);
		List<Statement> actualStatements = new ArrayList<Statement>();

		String className = ClassUtils.getSimpleClassname(Options.I().getMethodSignature());
		String methodName = ClassUtils.getMethodname(Options.I().getMethodSignature().split("\\(")[0]);

		// PHASE 0: transform variable names to avoid collisions among different scenarios
		ObjToObjVisitor oov = new ObjToObjVisitor(index);
		oov.visit(cloned, null);
		
		// PHASE 1: get concrete class used, if any generic class is involved
		GenericToConcreteClassVisitor gccv = new GenericToConcreteClassVisitor(className);
		gccv.visit(cloned, null);
		String concreteClass = gccv.getConcreteClass();
		
		// PHASE 2: find and substitute expected result
		CounterexampleExpectedResultVisitor cerv = new CounterexampleExpectedResultVisitor(targetMethod, index);
		cerv.visit(cloned, null);
		String objName = cerv.getExpectedState();
		
		// PHASE 2: find and substitute expected state
		ExpectedStateVisitor esv = new ExpectedStateVisitor(index, objName);
		esv.visit(cloned, getConcreteClass(className, concreteClass));
		ObjToExpectedStateVisitor oesv = new ObjToExpectedStateVisitor(objName, FirstStageStubGenerator.EXPECTED_STATE, Integer.toString(index));
		oesv.visit(cloned, null);
		// create actual state
		ActualStateVisitor asv = new ActualStateVisitor(FirstStageStubGenerator.EXPECTED_STATE, Integer.toString(index), methodName);
		asv.visit(cloned, null);
		actualStatements.addAll(asv.getActualStates());
		
		// PHASE 4: extract candidate call parameters to fields (with all dependencies)
		List<FieldDeclaration> inputs = extractParametersToInputs(cloned, methodName);
		
		cloned.getStmts().addAll(actualStatements);
		
		if (concreteClass != null) {
			return new GenericTestScenario(carvedTest, cloned, inputs, concreteClass);
		} else {
			return new TestScenario(carvedTest, cloned, inputs);
		}
	}
	
	private List<FieldDeclaration> extractParametersToInputs(BlockStmt cloned, String methodName) {
		List<String> varsToExtract = new ArrayList<String>();
		List<VariableDeclarationExpr> varsToField = new ArrayList<VariableDeclarationExpr>();
		List<FieldDeclaration> fields = new ArrayList<FieldDeclaration>();
		
		// extract dependencies from target method
		ExtractValuesFromTargetMethodVisitor evmv = new ExtractValuesFromTargetMethodVisitor(index);
		evmv.visit(cloned, methodName);
		fields.addAll(evmv.getFields());
		
		ExtractVariablesFromTargetMethodVisitor epv = new ExtractVariablesFromTargetMethodVisitor();
		epv.visit(cloned, methodName);
		varsToExtract.addAll(epv.getDependencies());
		
		// recursively extract all variable dependencies
		while (!varsToExtract.isEmpty()) {
			String variableId = varsToExtract.remove(0);
			for (int i = 0; i < cloned.getStmts().size(); i++) {
				ExpressionStmt estmt = (ExpressionStmt) cloned.getStmts().get(i);
				if (estmt.getExpression() instanceof VariableDeclarationExpr) {
					VariableDeclarationExpr vde = (VariableDeclarationExpr) estmt.getExpression();
					VariableDeclarator vd = vde.getVars().get(0);
					if (vd.getId().getName().equals(variableId)) {
						varsToField.add(vde);
						if (vd.getInit() instanceof MethodCallExpr || vd.getInit() instanceof ObjectCreationExpr) {
							varsToExtract.addAll(extractMethodDependencies(vd.getInit()));
						}
						cloned.getStmts().remove(i);
						i--;
					}
				}
			}
		}
		
		Collections.reverse(varsToField);

		Map<String, String> varMap = new HashMap<String, String>();
		for (int i = 0; i < varsToField.size(); i++) {
			// substitute name
			VariableDeclarationExpr vde = varsToField.get(i);
			String newName = "ELEMENT_" + index + "_" + fields.size();
			varMap.put(vde.getVars().get(0).getId().getName(), newName);
			vde.getVars().get(0).getId().setName(newName);

			// substitute dependency names
			SubstituteNameVisitor snv = new SubstituteNameVisitor();
			snv.visit(vde, varMap);

			// create field
			FieldDeclaration fd = new FieldDeclaration(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, vde.getType(), vde.getVars());
			fields.add(fd);
		}
		// substitute dependency names
		SubstituteNameVisitor snv = new SubstituteNameVisitor();
		snv.visit(cloned, varMap);
		
		return fields;
	}
	
	private List<String> extractMethodDependencies(Expression init) {
		List<String> dependencies = new ArrayList<String>();
		List<Expression> args = new ArrayList<Expression>();
		if (init instanceof MethodCallExpr) {
			MethodCallExpr mce = (MethodCallExpr) init;
			if (mce.getScope() != null) {
				args.add(mce.getScope());
			}
			if (mce.getArgs() != null) {
				args.addAll(mce.getArgs());
			}
		}
		else if (init instanceof ObjectCreationExpr) {
			ObjectCreationExpr oce = (ObjectCreationExpr) init;
			if (oce.getArgs() != null) {
				args.addAll(oce.getArgs());
			}
		}
		else if (init instanceof CastExpr) {
			
		}
		for (Expression expression : args) {
			if (expression instanceof NameExpr) {
				dependencies.add(((NameExpr)expression).getName());
			}
			else if (expression instanceof CastExpr) {
				CastExpr ce = (CastExpr) expression;
				if (ce.getExpr() instanceof NameExpr) {
					dependencies.add(((NameExpr)ce.getExpr()).getName());
				}
			}
		}
		return dependencies;
	}

	private String getConcreteClass(String className, String concreteClass) {
		if (concreteClass != null) {
			return className + "<" + concreteClass + ">";
		}
		else {
			return className;
		}
	}
	
	private Method findTargetMethod(Method[] methods, String methodName) {
		Method targetMethod = null;
		String method = methodName.split("\\(")[0];
		String args[] = methodName.split("\\(")[1].replaceAll("\\)", "").split(",");
		if (args.length == 1) {
			args = args[0].equals("") ? new String[0] : args;
		}
		for (Method m : methods) {
			if (m.getName().equals(method) && m.getParameterTypes().length == args.length) {
				int i;
				for (i = 0; i < args.length; i++) {
					if (!m.getParameterTypes()[i].getCanonicalName().contains(args[i])) {
						break;
					}
				}
				if (i == args.length) {
					targetMethod = m;
					break;
				}
			}
		}
		if (targetMethod == null) {
			throw new GenerationException("Target method not found"); // failed to find method, give up
		}
		return targetMethod;
	}
	
}
