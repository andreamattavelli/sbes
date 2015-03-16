package sbes.scenario;

import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.util.List;

import sbes.result.CarvingResult;

public class GenericTestScenario extends TestScenario {

	private List<String> genericClass;
	
	public GenericTestScenario(CarvingResult carvedTest, BlockStmt scenarioBody, List<FieldDeclaration> inputs, List<String> concreteClasses) {
		super(carvedTest, scenarioBody, inputs);
		this.genericClass = concreteClasses;
	}
	
	public List<String> getGenericClass() {
		return genericClass;
	}

}
