package sbes.japa;

import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class MethodCallVisitor extends VoidVisitorAdapter<Void> {
	private final String methodName;
	private final int parameters;
	private boolean found;
	public MethodCallVisitor(final String methodName, final int parameters) {
		this.methodName = methodName;
		this.parameters = parameters;
		this.found = false;
	}
	@Override
	public void visit(final MethodCallExpr n, final Void arg) {
		if (n.getName().equals(this.methodName)) {
			int args = n.getArgs() == null ? 0 : n.getArgs().size();
			if (args == parameters) {
				this.found = true;
			}
		}
		else {
			super.visit(n, arg);
		}
	}
	public boolean isFound() {
		return this.found;
	}
}