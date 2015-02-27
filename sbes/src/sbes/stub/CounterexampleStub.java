package sbes.stub;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.stmt.Statement;

import java.util.List;

import sbes.result.EquivalentSequence;

public class CounterexampleStub extends Stub {

	private EquivalentSequence equivalence;
	
	public CounterexampleStub(CompilationUnit cu, String stubName, List<Statement> stmts) {
		super(cu, stubName);
		this.equivalence = new EquivalentSequence(stmts);
	}
	
	public EquivalentSequence getEquivalence() {
		return equivalence;
	}

}
