package sbes.ast.renamer;

import japa.parser.ASTHelper;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class ExpectedStateRenamer extends VoidVisitorAdapter<Void> {

	private String objName;
	private String expectedName;
	private String index;
	
	public ExpectedStateRenamer(final String objName, final String expectedName, final String index) {
		this.objName = objName;
		this.expectedName = expectedName;
		this.index = index;
	}
	
	@Override
	public void visit(final NameExpr n, final Void arg) {
		if (n.getName().equals(objName)) {
			if (n.getParentNode() instanceof MethodCallExpr) {
				MethodCallExpr mce = (MethodCallExpr) n.getParentNode();
				ArrayAccessExpr aae = new ArrayAccessExpr(ASTHelper.createNameExpr(expectedName), ASTHelper.createNameExpr(index));
				mce.setScope(aae);
			}
			else if (n.getParentNode() instanceof FieldAccessExpr) {
				FieldAccessExpr fae = (FieldAccessExpr) n.getParentNode();
				ArrayAccessExpr aae = new ArrayAccessExpr(ASTHelper.createNameExpr(expectedName), ASTHelper.createNameExpr(index));
				fae.setScope(aae);
			}
		}
		super.visit(n, arg);
	}
	
}
