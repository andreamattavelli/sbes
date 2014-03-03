package sbes.ast;

import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class ExtractVariablesFromTargetMethodVisitor extends VoidVisitorAdapter<String> {
	private List<String> dependencies;
	public ExtractVariablesFromTargetMethodVisitor() {
		dependencies = new ArrayList<String>();
	}
	public List<String> getDependencies() {
		return dependencies;
	}
	@Override
	public void visit(MethodCallExpr n, String methodName) {
		if (n.getName().equals(methodName)) {
			if (n.getArgs() != null) {
				for (Expression arg : n.getArgs()) {
					if (arg instanceof NameExpr) {
						dependencies.add(((NameExpr)arg).getName());
					}
					else if (arg instanceof CastExpr) {
						CastExpr ce = (CastExpr) arg;
						if (ce.getExpr() instanceof NameExpr) {
							dependencies.add(((NameExpr)ce.getExpr()).getName());
						}
					}
				}
			}
		}
		super.visit(n, methodName);
	}
}