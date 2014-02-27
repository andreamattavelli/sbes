package sbes.ast;

import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class StubObjToCloneObjVisitor extends VoidVisitorAdapter<Void> {
	
	private String stubObjName;
	
	public StubObjToCloneObjVisitor(String stubObjName) {
		this.stubObjName = stubObjName;
	}
	
	@Override
	public void visit(NameExpr n, Void arg) {
		if (n.getName().equals(stubObjName)) {
			n.setName("clone");
		}
		super.visit(n, arg);
	}
	
}