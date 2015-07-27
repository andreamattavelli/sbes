package sbes.ast.inliner;

import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class ClassesToMocksInliner extends VoidVisitorAdapter<Void> {

	private boolean modified;
	
	public ClassesToMocksInliner() {
		modified = false;
	}
	
	public boolean isModified() {
		return modified;
	}
	
	public void reset() {
		modified = false;
	}
	
	@Override
	public void visit(ObjectCreationExpr n, Void arg) {
		if (n.getType().getName().equals("IntegerMock")) {
			List<Expression> args = n.getArgs();
			for (int i = 0; i < args.size(); i++) {
				Expression expression = args.get(i);
				if (expression instanceof MethodCallExpr) {
					MethodCallExpr mce = (MethodCallExpr) expression;
					if (mce.getScope() instanceof ObjectCreationExpr) {
						ObjectCreationExpr oce = (ObjectCreationExpr) mce.getScope();
						n.getArgs().set(i, oce.getArgs().get(0));
						modified = true;
					}
				}
				else if (expression instanceof ObjectCreationExpr) {
					ObjectCreationExpr oce = (ObjectCreationExpr) expression;
					n.getArgs().set(i, oce.getArgs().get(0));
					modified = true;
				}
			}
		}
		super.visit(n, arg);
	}
	
}
