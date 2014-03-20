package sbes.result;

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.util.List;

public class CarvingResult extends Result {

	private final List<ImportDeclaration> imports;
	
	public CarvingResult(final BlockStmt body, final List<ImportDeclaration> imports) {
		super(body);
		this.imports = imports;
	}
	
	public List<ImportDeclaration> getImports() {
		return imports;
	}
}
