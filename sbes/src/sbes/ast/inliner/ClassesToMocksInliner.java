package sbes.ast.inliner;

import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
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
	
	@Override
	public void visit(AssignExpr n, Void arg) {
		if (n.getValue() instanceof ObjectCreationExpr) {
			ObjectCreationExpr oce = (ObjectCreationExpr) n.getValue();
			if (oce.getType().getName().equals("IntegerMock") && oce.getArgs().get(0) instanceof MethodCallExpr) {
				MethodCallExpr mce = (MethodCallExpr) oce.getArgs().get(0);
				if (mce.getScope() instanceof NameExpr) {
					NameExpr ne = (NameExpr) mce.getScope();
					if (ne.getName().startsWith("p")) {
						n.setValue(ne);
					}
				}
			}
		}
		super.visit(n, arg);
	}
	
	@Override
	public void visit(VariableDeclarationExpr n, Void arg) {
		if (n.getVars().get(0).getInit() instanceof ObjectCreationExpr) {
			ObjectCreationExpr oce = (ObjectCreationExpr) n.getVars().get(0).getInit();
			if (oce.getType().getName().equals("IntegerMock") && oce.getArgs().get(0) instanceof MethodCallExpr) {
				MethodCallExpr mce = (MethodCallExpr) oce.getArgs().get(0);
				if (mce.getScope() instanceof NameExpr) {
					NameExpr ne = (NameExpr) mce.getScope();
					if (ne.getName().startsWith("p")) {
						n.getVars().get(0).setInit(ne);
					}
				}
			}
		}
		super.visit(n, arg);
	}
	
}
