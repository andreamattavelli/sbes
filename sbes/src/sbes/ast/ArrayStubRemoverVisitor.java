package sbes.ast;

import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import sbes.util.ASTUtils;

/**
 * Remove references to arrays to clean up the counterexample code from the
 * syntax used in the stub
 */
public class ArrayStubRemoverVisitor extends VoidVisitorAdapter<Void> {

	@Override
	public void visit(final VariableDeclarationExpr arg0, final Void arg1) {
		Expression e = arg0.getVars().get(0).getInit();
		if (e instanceof MethodCallExpr) {
			MethodCallExpr mce = (MethodCallExpr) e;
			if (mce.getScope() instanceof NameExpr) {
				String scopeName = ASTUtils.getName(mce.getScope());
				if (scopeName != null && scopeName.equals("clone")) {
					Type type = arg0.getType();
					if (type instanceof ReferenceType) {
						ReferenceType rt = (ReferenceType) type;
						int arrayCount = rt.getArrayCount();
						if (arrayCount > 0) {
							rt.setArrayCount(arrayCount - 1);
						}
					}
				}
			}
		}
		super.visit(arg0, arg1);
	}

}