package sbes.result;

import japa.parser.ast.stmt.BlockStmt;

public class Result {

	protected final BlockStmt body;
	
	public Result(BlockStmt body) {
		this.body = body;
	}
	
	public BlockStmt getBody() {
		return body;
	}
	
	@Override
	public String toString() {
		return body.toString();
	}
	
}
