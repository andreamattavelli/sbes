package sbes.scenario;

import java.util.List;

import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.stmt.BlockStmt;
import sbes.testcase.CarvingResult;

public class GenericTestScenario extends TestScenario {

	private String genericClass;
	
	public GenericTestScenario(CarvingResult carvedTest, BlockStmt scenarioBody, List<FieldDeclaration> inputs, String genericClass) {
		super(carvedTest, scenarioBody, inputs);
		this.genericClass = genericClass;
	}
	
	public String getGenericClass() {
		return genericClass;
	}

}
