package sbes.scenario;

import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import sbes.result.CarvingResult;

public class TestScenarioWithGenerics extends TestScenario {

	private Map<TypeVariable<?>, String> genericToConcreteClasses;

	public TestScenarioWithGenerics(CarvingResult carvedTest, BlockStmt scenarioBody, 
									List<FieldDeclaration> inputs, Map<TypeVariable<?>, String> genericToConcreteClasses) {
		super(carvedTest, scenarioBody, inputs);
		this.genericToConcreteClasses = genericToConcreteClasses;
	}

	public Map<TypeVariable<?>, String> getGenericToConcreteClasses() {
		return genericToConcreteClasses;
	}

}
