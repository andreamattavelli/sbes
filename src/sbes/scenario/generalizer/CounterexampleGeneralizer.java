package sbes.scenario.generalizer;

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.CloneVisitor;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import sbes.ast.ExpectedStateVisitor;
import sbes.ast.GenericClassVisitor;
import sbes.ast.renamer.ActualStateRenamer;
import sbes.ast.renamer.ClassOrInterfaceRenamer;
import sbes.ast.renamer.ExpectedResultCounterexampleRenamer;
import sbes.ast.renamer.ExpectedStateRenamer;
import sbes.ast.renamer.VariableNamesRenamer;
import sbes.exceptions.GenerationException;
import sbes.execution.InternalClassloader;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.scenario.TestScenarioRepository;
import sbes.scenario.TestScenarioWithGenerics;
import sbes.stub.generator.first.FirstStageGeneratorStub;
import sbes.util.ClassUtils;

public class CounterexampleGeneralizer extends AbstractGeneralizer {

	private static final Logger logger = new Logger(TestScenarioGeneralizer.class);
	
	public static TestScenario counterexampleToTestScenario(CarvingResult carvedCounterexample) {
		cleanCounterexample(carvedCounterexample);
		int index = TestScenarioRepository.I().getScenarios().size();
		TestScenario scenario = generalizeCounterexampleToScenario(index, carvedCounterexample);
		return scenario;
	}
	
	private static void cleanCounterexample(CarvingResult counterexample) {
		String classname = ClassUtils.getSimpleClassname(Options.I().getMethodSignature());
		ClassOrInterfaceRenamer cv = new ClassOrInterfaceRenamer(classname + "_Stub_2", classname);
		cv.visit(counterexample.getBody(), null);
		
		for (int i = 0; i < counterexample.getImports().size(); i++) {
			ImportDeclaration importDecl = counterexample.getImports().get(i);
			if (importDecl.getName().getName().endsWith(classname + "_Stub_2")) {
				counterexample.getImports().remove(importDecl);
				i--;
			}
		}
	}
	
	private static TestScenario generalizeCounterexampleToScenario(int index, CarvingResult carvedTest) {
		logger.debug("Generalizing carved body");
		
		Class<?> c;
		try {
			InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath());
			c = Class.forName(ClassUtils.getCanonicalClassname(Options.I().getMethodSignature()), false, ic.getClassLoader());
		} catch (ClassNotFoundException e) {
			// infeasible, we already checked the classpath
			throw new GenerationException("Target class not found");
		}
		
		// get class' methods
		Method[] methods = ClassUtils.getClassMethods(c);
		// get method signature
		String methodSignature = ClassUtils.getMethodname(Options.I().getMethodSignature());
		// get target method from the list of class' methods
		Method targetMethod = ClassUtils.findTargetMethod(methods, methodSignature);
		// get generic types defined
		TypeVariable<?>[] genericTypes = c.getTypeParameters();

		CloneVisitor cloner = new CloneVisitor();
		BlockStmt cloned = (BlockStmt) cloner.visit(carvedTest.getBody(), null);
		List<Statement> actualStatements = new ArrayList<Statement>();

		String className = ClassUtils.getSimpleClassname(Options.I().getMethodSignature());
		String methodName = ClassUtils.getMethodname(Options.I().getMethodSignature().split("\\(")[0]);

		// PHASE 0: transform variable names to avoid collisions among different scenarios
		VariableNamesRenamer oov = new VariableNamesRenamer(index);
		oov.visit(cloned, null);
		
		// PHASE 1: get concrete class used, if any generic class is involved
		GenericClassVisitor gccv = new GenericClassVisitor(className);
		gccv.visit(cloned, null);
		List<String> concreteClasses = gccv.getConcreteClasses();
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		for (int i = 0; i < concreteClasses.size(); i++) {
			genericToConcrete.put(genericTypes[i], concreteClasses.get(i));
		}
		
		// PHASE 2: find and substitute expected result
		ExpectedResultCounterexampleRenamer cerv = new ExpectedResultCounterexampleRenamer(targetMethod, index);
		cerv.visit(cloned, null);
		String objName = cerv.getExpectedState();
		
		// PHASE 3: find and substitute expected state
		ExpectedStateVisitor esv = new ExpectedStateVisitor(index, objName);
		esv.visit(cloned, getConcreteClass(className, concreteClasses));
		ExpectedStateRenamer oesv = new ExpectedStateRenamer(objName, FirstStageGeneratorStub.EXPECTED_STATE, Integer.toString(index));
		oesv.visit(cloned, null);
		// create actual state
		ActualStateRenamer asv = new ActualStateRenamer(FirstStageGeneratorStub.EXPECTED_STATE, Integer.toString(index), methodName);
		asv.visit(cloned, null);
		actualStatements.addAll(asv.getActualStates());
		
		// PHASE 4: extract candidate call parameters to fields (with all dependencies)
		List<FieldDeclaration> inputs = extractParametersToInputs(cloned, methodName, targetMethod, index);
		
		cloned.getStmts().addAll(actualStatements);
		
		if (concreteClasses != null && concreteClasses.size() > 0) {
			return new TestScenarioWithGenerics(carvedTest, cloned, inputs, genericToConcrete);
		} else {
			return new TestScenario(carvedTest, cloned, inputs);
		}
	}
	
}
