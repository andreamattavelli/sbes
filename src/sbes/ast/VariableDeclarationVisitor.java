package sbes.ast;

import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class VariableDeclarationVisitor extends VoidVisitorAdapter<Void> {
	private String variableId;
	private VariableDeclarationExpr variable;
	public VariableDeclarationVisitor(String variableId) {
		this.variableId = variableId;
		this.variable = null;
	}
	public VariableDeclarationExpr getVariable() {
		return variable;
	}
	@Override
	public void visit(VariableDeclarationExpr arg0, Void arg1) {
		// Safe since evosuite instantiate only one variable at a time
		VariableDeclarator vd = arg0.getVars().get(0);
		if (vd.getId().getName().equals(variableId)) {
			variable = arg0;
		}
	}
}