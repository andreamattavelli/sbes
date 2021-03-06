package sbes.scenario.generalizer;

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
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import sbes.ast.ExpectedStateVisitor;
import sbes.ast.ExtractValuesFromTargetMethodVisitor;
import sbes.ast.ExtractVariablesFromTargetMethodVisitor;
import sbes.ast.GenericClassVisitor;
import sbes.ast.renamer.ActualStateRenamer;
import sbes.ast.renamer.ExpectedStateRenamer;
import sbes.ast.renamer.InputFieldRenamer;
import sbes.ast.renamer.VariableNamesRenamer;
import sbes.exceptions.SBESException;
import sbes.execution.InternalClassloader;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.scenario.TestScenarioRepository;
import sbes.scenario.TestScenarioWithGenerics;
import sbes.stub.generator.first.FirstStageGeneratorStub;
import sbes.util.ClassUtils;
import sbes.util.ReflectionUtils;

public abstract class AbstractGeneralizer {

	protected TestScenario generalizeToTestScenario(final CarvingResult carvedTest) {
		int index = TestScenarioRepository.I().getScenarios().size();

		BlockStmt cloned = (BlockStmt) new CloneVisitor().visit(carvedTest.getBody(), null);
		List<Statement> actualStatements = new ArrayList<Statement>();

		String className = ClassUtils.getSimpleClassname(Options.I().getTargetMethod());
		String methodName = ClassUtils.getMethodname(Options.I().getTargetMethod().split("\\(")[0]);

		Class<?> c;
		try {
			InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath());
			c = Class.forName(ClassUtils.getCanonicalClassname(Options.I().getTargetMethod()), false, ic.getClassLoader());
		} catch (ClassNotFoundException e) {
			// infeasible, we already checked the classpath
			throw new SBESException("Target class not found");
		}

		// get class' methods
		Method[] methods = ReflectionUtils.getClassMethods(c);
		// get method signature
		String methodSignature = ClassUtils.getMethodname(Options.I().getTargetMethod());
		// get target method from the list of class' methods
		Method targetMethod = ReflectionUtils.findTargetMethod(methods, methodSignature);
		// get generic types defined
		TypeVariable<?>[] genericTypes = c.getTypeParameters();
		if ((genericTypes == null || genericTypes.length == 0) && Modifier.isStatic(targetMethod.getModifiers())) {
			genericTypes = targetMethod.getReturnType().getTypeParameters();
		}

		// PHASE 0: transform variable names to avoid collisions among different scenarios
		new VariableNamesRenamer(index).visit(cloned, null);

		// PHASE 1: get concrete class used, if any generic class is involved
		GenericClassVisitor gccv = new GenericClassVisitor(className);
		gccv.visit(cloned, null);
		List<String> concreteClasses = gccv.getConcreteClasses();
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		for (int i = 0; i < concreteClasses.size(); i++) {
			genericToConcrete.put(genericTypes[i], concreteClasses.get(i));
		}

		// PHASE 2: find and substitute expected result
		String objName = getAndRenameExpectedResult(cloned, targetMethod, methodName, index);

		// PHASE 3: find and substitute expected state
		new ExpectedStateVisitor(index, objName).visit(cloned, getConcreteClass(className, concreteClasses));
		new ExpectedStateRenamer(objName, FirstStageGeneratorStub.EXPECTED_STATE, Integer.toString(index)).visit(cloned, null);
		// create actual state
		ActualStateRenamer asv = new ActualStateRenamer(FirstStageGeneratorStub.EXPECTED_STATE, Integer.toString(index), methodName);
		asv.visit(cloned, null);
		actualStatements.addAll(asv.getActualStates());

		// PHASE 4: extract candidate call parameters to fields (with all dependencies)
		List<FieldDeclaration> inputs = extractParametersToInputs(cloned, methodName, targetMethod);

		cloned.getStmts().addAll(actualStatements);

		if (!Options.I().dontResolveGenerics() && concreteClasses != null && concreteClasses.size() > 0) {
			return new TestScenarioWithGenerics(carvedTest, cloned, inputs, genericToConcrete);
		} else {
			return new TestScenario(carvedTest, cloned, inputs);
		}
	}
	
	protected abstract String getAndRenameExpectedResult(final BlockStmt cloned, final Method targetMethod, final String methodName, final int index);
	
	protected List<FieldDeclaration> extractParametersToInputs(final BlockStmt cloned, final String methodName, final Method targetMethod) {
		int index = TestScenarioRepository.I().getScenarios().size();
		List<String> varsToExtract = new ArrayList<String>();
		List<VariableDeclarationExpr> varsToField = new ArrayList<VariableDeclarationExpr>();
		List<FieldDeclaration> fields = new ArrayList<FieldDeclaration>();
		
		// extract dependencies from target method
		ExtractValuesFromTargetMethodVisitor evmv = new ExtractValuesFromTargetMethodVisitor(index, targetMethod);
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
			InputFieldRenamer snv = new InputFieldRenamer();
			snv.visit(vde, varMap);

			// create field
			FieldDeclaration fd = new FieldDeclaration(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, vde.getType(), vde.getVars());
			fields.add(fd);
		}
		// substitute dependency names
		InputFieldRenamer snv = new InputFieldRenamer();
		snv.visit(cloned, varMap);
		
		return fields;
	}
	
	protected List<String> extractMethodDependencies(final Expression init) {
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

	protected String getConcreteClass(final String className, final List<String> concreteClasses) {
		if (concreteClasses != null && concreteClasses.size() > 0) {
			return className + "<" + concreteClasses.toString().replace("[", "").replace("]", "") + ">";
		}
		else {
			return className;
		}
	}
	
}
