package sbes.ast;

import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

import sbes.util.ASTUtils;

public class CloneObjVisitor extends VoidVisitorAdapter<Void> {
	List<MethodCallExpr> methods = new ArrayList<MethodCallExpr>();
	
	public List<MethodCallExpr> getMethods() {
		return methods;
	}
	
	@Override
	public void visit(MethodCallExpr arg0, Void arg1) {
		if (arg0.getScope() != null) {
			String name = ASTUtils.getName(arg0.getScope());
			if (name != null && name.equals("clone")) {
				methods.add(arg0);
			}
		}
		super.visit(arg0, arg1);
	}
}