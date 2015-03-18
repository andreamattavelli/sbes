package sbes.scenario.generalizer;

import japa.parser.ast.stmt.BlockStmt;

import java.lang.reflect.Method;

import sbes.ast.renamer.ExpectedResultRenamer;
import sbes.logging.Logger;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;

public class TestScenarioGeneralizer extends AbstractGeneralizer  {

	private static final Logger logger = new Logger(TestScenarioGeneralizer.class);

	public TestScenario testToTestScenario(CarvingResult test) {
		logger.debug("Generalizing carved test");
		return super.generalizeToTestScenario(test);
	}
	
	@Override
	protected String getAndRenameExpectedResult(BlockStmt cloned, Method targetMethod, String methodName, int index) {
		ExpectedResultRenamer erv = new ExpectedResultRenamer(index, targetMethod.getParameterTypes().length);
		erv.visit(cloned, methodName);
		return erv.getExpectedState();
	}

}
