package sbes.scenario.generalizer;

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.stmt.BlockStmt;

import java.lang.reflect.Method;

import sbes.ast.inliner.FieldVariablesToInline;
import sbes.ast.inliner.Inliner;
import sbes.ast.inliner.PrimitiveVariablesToInline;
import sbes.ast.inliner.StringVariablesToInline;
import sbes.ast.renamer.ClassOrInterfaceRenamer;
import sbes.ast.renamer.ExpectedResultCounterexampleRenamer;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.util.ClassUtils;

public class CounterexampleGeneralizer extends AbstractGeneralizer {

	private static final Logger logger = new Logger(CounterexampleGeneralizer.class);

	public TestScenario counterexampleToTestScenario(final CarvingResult carvedCounterexample) {
		logger.debug("Generalizing carved counterexample");
		cleanCounterexample(carvedCounterexample);
		return super.generalizeToTestScenario(carvedCounterexample);
	}

	@Override
	protected String getAndRenameExpectedResult(final BlockStmt cloned, final Method targetMethod, final String methodName, final int index) {
		ExpectedResultCounterexampleRenamer cerv = new ExpectedResultCounterexampleRenamer(targetMethod, index);
		cerv.visit(cloned, null);
		return cerv.getExpectedState();
	}
	
	private void cleanCounterexample(final CarvingResult counterexample) {
		String classname = ClassUtils.getSimpleClassname(Options.I().getTargetMethod());
		ClassOrInterfaceRenamer cv = new ClassOrInterfaceRenamer(classname + "_Stub_2", classname);
		cv.visit(counterexample.getBody(), null);

		for (int i = 0; i < counterexample.getImports().size(); i++) {
			ImportDeclaration importDecl = counterexample.getImports().get(i);
			if (importDecl.getName().getName().endsWith(classname + "_Stub_2")) {
				counterexample.getImports().remove(importDecl);
				i--;
			}
		}
		
		PrimitiveVariablesToInline pvi = new PrimitiveVariablesToInline();
		pvi.visit(counterexample.getBody(), null);
		
		StringVariablesToInline svi = new StringVariablesToInline();
		svi.visit(counterexample.getBody(), null);
		
		FieldVariablesToInline fvi = new FieldVariablesToInline();
		fvi.visit(counterexample.getBody(), null);
		
		for (VariableDeclarator vd : pvi.getToInline()) {
			new Inliner().visit(counterexample.getBody(), vd);
		}
		for (VariableDeclarator vd : svi.getToInline()) {
			new Inliner().visit(counterexample.getBody(), vd);
		}
		for (VariableDeclarator vd : fvi.getToInline()) {
			new Inliner().visit(counterexample.getBody(), vd);
		}
	}

}
