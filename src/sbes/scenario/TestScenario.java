package sbes.scenario;

import japa.parser.ast.stmt.BlockStmt;
import sbes.testcase.CarvingResult;

public class TestScenario extends CarvingResult {
	
	protected BlockStmt scenarioBody;
	
	public TestScenario(CarvingResult carvedTest, BlockStmt scenarioBody) {
		super(carvedTest.getBody(), carvedTest.getImports());
		this.scenarioBody = scenarioBody;
	}
	
	public BlockStmt getScenario() {
		return scenarioBody;
	}
	
}
