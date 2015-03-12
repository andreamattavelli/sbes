package sbes.ast;

import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

/**
 * Change all possible NameExpr nodes whose name returned by <code>getName()</code> method
 * matches a given string in input.
 */
public class NameExprRenamerVisitor extends VoidVisitorAdapter<Void> {
	
	private String oldName;
	private String newName;
	
	public NameExprRenamerVisitor(String oldName, String newName) {
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