package sbes.testcase;

import java.util.List;

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.stmt.BlockStmt;

public class CarvingResult {

	private final BlockStmt body;
	private final List<ImportDeclaration> imports;

	public CarvingResult(final BlockStmt body, final List<ImportDeclaration> imports) {
		this.body = body;
		this.imports = imports;
	}

	public BlockStmt getBody() {
		return body;
	}

	public List<ImportDeclaration> getImports() {
		return imports;
	}

	@Override
	public String toString() {
		return body.toString();
	}
	
}
