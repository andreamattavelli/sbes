package sbes.ast.inliner;

import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class Inliner extends VoidVisitorAdapter<VariableDeclarator> {

	@Override
	public void visit(MethodCallExpr arg0, VariableDeclarator arg1) {
		if (arg0.getArgs() != null) {
			for (int j = 0; j < arg0.getArgs().size(); j++) {
				Expression expr = arg0.getArgs().get(j);
				if (expr instanceof NameExpr) {
					NameExpr ne = (NameExpr) expr;
					if (ne.getName().equals(arg1.getId().getName())) {
						arg0.getArgs().set(j, arg1.getInit());
					}
				}
			}
		}
		super.visit(arg0, arg1);
	}
	
	@Override
	public void visit(CastExpr n, VariableDeclarator arg) {
		if (n.getExpr() instanceof NameExpr) {
			NameExpr ne = (NameExpr) n.getExpr();
			if (ne.getName().equals(arg.getId().getName())) {
				n.setExpr(arg.getInit());
			}
		}
		super.visit(n, arg);
	}
	
}
