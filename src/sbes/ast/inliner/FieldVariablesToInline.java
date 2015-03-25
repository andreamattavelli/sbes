package sbes.ast.inliner;

import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

import sbes.util.ReflectionUtils;

public class FieldVariablesToInline extends VoidVisitorAdapter<Void> {

	private List<VariableDeclarator> toInline = new ArrayList<>();
	
	public List<VariableDeclarator> getToInline() {
		return toInline;
	}
	
	@Override
	public void visit(VariableDeclarationExpr arg0, Void arg1) {
		if (arg0.getType() instanceof ReferenceType) {
			ReferenceType rt = (ReferenceType) arg0.getType();
			if (!ReflectionUtils.primitivesStringRepresentation.contains(rt.toString()) &&
					!rt.toString().equals("String")) {
				VariableDeclarator vd = arg0.getVars().get(0); //safe
				if (isValid(vd.getInit())) {
					toInline.add(vd);
				}
			}
		}
		super.visit(arg0, arg1);
	}
	
	private boolean isValid(Expression expr) {
		if (expr instanceof FieldAccessExpr) {
			return true;
		}
		return false;
	}
	
}
