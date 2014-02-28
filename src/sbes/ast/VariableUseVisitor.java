package sbes.ast;

import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class VariableUseVisitor extends VoidVisitorAdapter<Void> {
	private String variableId;
	private boolean alive;
	public VariableUseVisitor(String variableId) {
		this.variableId = variableId;
		this.alive = false;
	}
	public boolean isUsed() {
		return alive;
	}
	@Override
	public void visit(AssignExpr n, Void arg) {
		if (n.getValue() instanceof NameExpr) {
			NameExpr ne = (NameExpr) n.getValue();
			if (ne.getName().equals(variableId)) {
				alive = true;
			}
		}
		super.visit(n, arg);
	}
	@Override
	public void visit(MethodCallExpr arg0, Void arg1) {
		if (arg0.getScope() instanceof NameExpr) {
			NameExpr ne = (NameExpr) arg0.getScope();
			if (ne.getName().equals(variableId)) {
				alive = true;
			}
		}
		super.visit(arg0, arg1);
	}
}