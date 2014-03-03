package sbes.stub;

import java.util.List;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.stmt.Statement;

public class CounterexampleStub extends Stub {

	private List<Statement> equivalence;
	
	public CounterexampleStub(CompilationUnit cu, String stubName, List<Statement> stmts) {
		super(cu, stubName);
		this.equivalence = stmts;
	}
	
	public List<Statement> getEquivalence() {
		return equivalence;
	}

}
