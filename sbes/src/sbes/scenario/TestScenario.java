package sbes.scenario;

import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.util.List;

import sbes.result.CarvingResult;

public class TestScenario extends CarvingResult {
	
	protected BlockStmt scenarioBody;
	protected List<FieldDeclaration> inputs;
	
	public TestScenario(CarvingResult carvedTest, BlockStmt scenarioBody, List<FieldDeclaration> inputs) {
		super(carvedTest.getBody(), carvedTest.getImports());
		this.scenarioBody = scenarioBody;
		this.inputs = inputs;
	}
	
	public BlockStmt getScenario() {
		return scenarioBody;
	}
	
	public List<FieldDeclaration> getInputs() {
		return inputs;
	}
	
}
