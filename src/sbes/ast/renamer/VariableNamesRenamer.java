package sbes.ast.renamer;

import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

/**
 * Renames variable to insert a constant to avoid name collisions with previous
 * test scenarios
 */
public class VariableNamesRenamer extends VoidVisitorAdapter<Void> {
	
	private int index;
	
	public VariableNamesRenamer(int index) {
		this.index = index;
	}
	
	@Override
	public void visit(NameExpr n, Void arg) {
		if (!Character.isUpperCase(n.getName().charAt(0))) {
			n.setName(n.getName() + "_" + index);
		}
		super.visit(n, arg);
	}
	
	@Override
	public void visit(VariableDeclaratorId n, Void arg) {
		if (!Character.isUpperCase(n.getName().charAt(0))) {
			n.setName(n.getName() + "_" + index);
		}
		super.visit(n, arg);
	}
	
}