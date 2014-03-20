package sbes.ast;

import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class ObjToObjVisitor extends VoidVisitorAdapter<Void> {
	
	private int index;
	
	public ObjToObjVisitor(int index) {
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