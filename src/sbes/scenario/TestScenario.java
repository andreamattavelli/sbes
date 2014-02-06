package sbes.scenario;

import java.util.List;

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.stmt.BlockStmt;
import sbes.testcase.CarvingResult;

public class TestScenario extends CarvingResult {

	public TestScenario(BlockStmt body, List<ImportDeclaration> imports) {
		super(body, imports);
	}

}
