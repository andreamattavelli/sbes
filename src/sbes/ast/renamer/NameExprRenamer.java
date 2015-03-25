package sbes.ast.renamer;

import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

/**
 * Change all possible NameExpr nodes whose name returned by <code>getName()</code> method
 * matches a given string in input.
 */
public class NameExprRenamer extends VoidVisitorAdapter<Void> {
	
	private String oldName;
	private String newName;
	
	public NameExprRenamer(final String oldName, final String newName) {
		this.oldName = oldName;
		this.newName = newName;
	}
	
	@Override
	public void visit(final NameExpr n, final Void arg) {
		if (n.getName().equals(oldName)) {
			n.setName(newName);
		}
		super.visit(n, arg);
	}
	
}