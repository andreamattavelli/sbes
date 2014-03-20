package sbes.ast;

import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import sbes.util.ASTUtils;

public class ArrayCellDeclarationVisitor extends VoidVisitorAdapter<Void> {
	private String variableId;
	private String index;
	private Expression value;
	public ArrayCellDeclarationVisitor(String variableId, String index) {
		this.variableId = variableId;
		this.index = index;
		this.value = null;
	}
	public Expression getValue() {
		return value;
	}
	@Override
	public void visit(AssignExpr n, Void arg) {
		if (n.getTarget() instanceof ArrayAccessExpr) {
			ArrayAccessExpr aae = (ArrayAccessExpr) n.getTarget();
			if (ASTUtils.getName(aae.getName()).equals(variableId) && aae.getIndex().toString().equals(index)) {
				value = n.getValue();
			}
		}
		super.visit(n, arg);
	}
}