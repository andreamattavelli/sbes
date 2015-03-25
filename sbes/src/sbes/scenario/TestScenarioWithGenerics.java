package sbes.scenario;

import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import sbes.result.CarvingResult;

public class TestScenarioWithGenerics extends TestScenario {

	private Map<TypeVariable<?>, String> genericToConcreteClasses;

	public TestScenarioWithGenerics(final CarvingResult carvedTest, final BlockStmt scenarioBody, 
									final List<FieldDeclaration> inputs, final Map<TypeVariable<?>, String> genericToConcreteClasses) {
		super(carvedTest, scenarioBody, inputs);
		this.genericToConcreteClasses = genericToConcreteClasses;
	}

	public Map<TypeVariable<?>, String> getGenericToConcreteClasses() {
		return genericToConcreteClasses;
	}

}
