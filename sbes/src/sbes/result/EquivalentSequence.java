package sbes.result;

import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;

import java.util.List;

public class EquivalentSequence extends Result {

	public EquivalentSequence(List<Statement> stmts) {
		super(new BlockStmt(stmts));
	}
	
	@Override
	public String toString() {
		String toReturn = new String();
		for (Statement s : body.getStmts()) {
			toReturn += s.toString() + System.lineSeparator();
		}
		return toReturn;
	}

}
