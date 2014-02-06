package sbes.scenario;

import java.util.List;

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.stmt.BlockStmt;
import sbes.testcase.CarvingResult;

public class TestScenario extends CarvingResult {
	
	private BlockStmt arrayzedBody;
	
	public TestScenario(BlockStmt body, List<ImportDeclaration> imports) {
		super(body, imports);
	}
	
	public TestScenario(BlockStmt body, BlockStmt arrayzedBody, List<ImportDeclaration> imports) {
		super(body, imports);
		this.arrayzedBody = arrayzedBody;
	}
	
	public BlockStmt getArrayzedBody() {
		return arrayzedBody;
	}
	
}
