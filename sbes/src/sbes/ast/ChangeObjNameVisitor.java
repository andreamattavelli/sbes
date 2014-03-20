package sbes.ast;

import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class ChangeObjNameVisitor extends VoidVisitorAdapter<Void> {
	private String oldName;
	private String newName;
	public ChangeObjNameVisitor(String oldName, String newName) {
		this.oldName = oldName;
		this.newName = newName;
	}
	@Override
	public void visit(NameExpr n, Void arg) {
		if (n.getName().equals(oldName)) {
			n.setName(newName);
		}
		super.visit(n, arg);
	}
}