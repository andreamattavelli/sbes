package sbes.ast;

import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import sbes.util.ASTUtils;

/**
 * Check if a given variable is alive or not
 */
public class VariableUseVisitor extends VoidVisitorAdapter<Void> {
	
	private String variableId;
	private boolean alive;

	public VariableUseVisitor(final String variableId) {
		this.variableId = variableId;
		this.alive = false;
	}

	public boolean isUsed() {
		return alive;
	}

	@Override
	public void visit(final AssignExpr n, final Void arg) {
		if (n.getValue() instanceof NameExpr) {
			NameExpr ne = (NameExpr) n.getValue();
			if (ne.getName().equals(variableId)) {
				alive = true;
			}
		}
		super.visit(n, arg);
	}

	@Override
	public void visit(final MethodCallExpr arg0, final Void arg1) {
		if (arg0.getScope() instanceof NameExpr) {
			NameExpr ne = (NameExpr) arg0.getScope();
			if (ne.getName().equals(variableId)) {
				alive = true;
			}
			if (arg0.getArgs() != null) {
				for (Expression arg : arg0.getArgs()) {
					String name = ASTUtils.getName(arg);
					if (name != null && name.equals(variableId)) {
						alive = true;
						break;
					}
				}
			}
		}
		super.visit(arg0, arg1);
	}

	@Override
	public void visit(final ObjectCreationExpr arg0, final Void arg1) {
		if (arg0.getArgs() != null) {
			for (Expression arg : arg0.getArgs()) {
				String name = ASTUtils.getName(arg);
				if (name != null && name.equals(variableId)) {
					alive = true;
					break;
				}
			}
		}
		super.visit(arg0, arg1);
	}
	
}