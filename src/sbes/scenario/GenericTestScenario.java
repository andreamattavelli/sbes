package sbes.scenario;

import japa.parser.ast.stmt.BlockStmt;
import sbes.testcase.CarvingResult;

public class GenericTestScenario extends TestScenario {

	private String genericClass;
	
	public GenericTestScenario(CarvingResult carvedTest, BlockStmt scenarioBody, String genericClass) {
		super(carvedTest, scenarioBody);
		this.genericClass = genericClass;
	}
	
	public String getGenericClass() {
		return genericClass;
	}

}
