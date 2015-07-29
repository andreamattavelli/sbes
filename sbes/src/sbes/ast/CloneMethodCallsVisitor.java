package sbes.ast;

import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

import sbes.util.ASTUtils;

/**
 * Retrieve all method calls on clone objects.
 */
public class CloneMethodCallsVisitor extends VoidVisitorAdapter<Void> {
	
	private List<MethodCallExpr> methods = new ArrayList<MethodCallExpr>();
	
	public List<MethodCallExpr> getMethods() {
		return methods;
	}
	
	@Override
	public void visit(final MethodCallExpr arg0, final Void arg1) {
		if (arg0.getScope() != null) {
			String name = ASTUtils.getName(arg0.getScope());
			if (name != null && (name.equals("clone") || name.startsWith("v_"))) {
				methods.add(arg0);
			}
		}
		super.visit(arg0, arg1);
	}
}