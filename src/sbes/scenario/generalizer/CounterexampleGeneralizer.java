package sbes.scenario.generalizer;

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.lang.reflect.Method;

import sbes.ast.renamer.ClassOrInterfaceRenamer;
import sbes.ast.renamer.ExpectedResultCounterexampleRenamer;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.util.ClassUtils;

public class CounterexampleGeneralizer extends AbstractGeneralizer {

	private static final Logger logger = new Logger(CounterexampleGeneralizer.class);

	public TestScenario counterexampleToTestScenario(CarvingResult carvedCounterexample) {
		logger.debug("Generalizing carved counterexample");
		cleanCounterexample(carvedCounterexample);
		return super.generalizeToTestScenario(carvedCounterexample);
	}

	@Override
	protected String getAndRenameExpectedResult(BlockStmt cloned, Method targetMethod, String methodName, int index) {
		ExpectedResultCounterexampleRenamer cerv = new ExpectedResultCounterexampleRenamer(targetMethod, index);
		cerv.visit(cloned, null);
		return cerv.getExpectedState();
	}
	
	private void cleanCounterexample(CarvingResult counterexample) {
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

}
