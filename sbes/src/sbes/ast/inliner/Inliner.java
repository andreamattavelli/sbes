package sbes.ast.inliner;

import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class Inliner extends VoidVisitorAdapter<VariableDeclarator> {

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
	
	@Override
	public void visit(MethodCallExpr n, VariableDeclarator arg) {
		if (n.getArgs() != null) {
			for (int j = 0; j < n.getArgs().size(); j++) {
				Expression expr = n.getArgs().get(j);
				if (expr instanceof NameExpr) {
					NameExpr ne = (NameExpr) expr;
					if (ne.getName().equals(arg.getId().getName())) {
						n.getArgs().set(j, arg.getInit());
					}
				}
			}
		}
		super.visit(n, arg);
	}
	
	@Override
	public void visit(ObjectCreationExpr n, VariableDeclarator arg) {
		if (n.getArgs() != null) {
			for (int j = 0; j < n.getArgs().size(); j++) {
				Expression expr = n.getArgs().get(j);
				if (expr instanceof NameExpr) {
					NameExpr ne = (NameExpr) expr;
					if (ne.getName().equals(arg.getId().getName())) {
						n.getArgs().set(j, arg.getInit());
					}
				}
			}
		}
		super.visit(n, arg);
	}
	
	@Override
	public void visit(AssignExpr n, VariableDeclarator arg) {
		if (n.getValue() instanceof NameExpr) {
			NameExpr ne = (NameExpr) n.getValue();
			if (ne.getName().equals(arg.getId().getName())) {
				n.setValue(arg.getInit());
			}
		}
		super.visit(n, arg);
	}
	
}
