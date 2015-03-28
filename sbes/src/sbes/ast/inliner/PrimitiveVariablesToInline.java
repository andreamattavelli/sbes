package sbes.ast.inliner;

import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CharLiteralExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.IntegerLiteralMinValueExpr;
import japa.parser.ast.expr.LiteralExpr;
import japa.parser.ast.expr.LongLiteralExpr;
import japa.parser.ast.expr.LongLiteralMinValueExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

import sbes.util.ReflectionUtils;

public class PrimitiveVariablesToInline extends VoidVisitorAdapter<Void> {

	private List<VariableDeclarator> toInline = new ArrayList<>();
	
	public List<VariableDeclarator> getToInline() {
		return toInline;
	}
	
	@Override
	public void visit(VariableDeclarationExpr arg0, Void arg1) {
		if (arg0.getType() instanceof PrimitiveType) {
			VariableDeclarator vd = arg0.getVars().get(0); //safe
			if (isValid(vd.getInit())) {
				toInline.add(vd);
			}
		}
		else if (arg0.getType() instanceof ReferenceType) {
			ReferenceType rt = (ReferenceType) arg0.getType();
			if (ReflectionUtils.primitivesStringRepresentation.contains(rt.toString())) {
				VariableDeclarator vd = arg0.getVars().get(0); //safe
				if (isValid(vd.getInit())) {
					toInline.add(vd);
				}
			}
		}
		super.visit(arg0, arg1);
	}
	
	private boolean isValid(Expression expr) {
		if (expr instanceof FieldAccessExpr ||
			expr instanceof BooleanLiteralExpr ||
			expr instanceof CharLiteralExpr ||
			expr instanceof DoubleLiteralExpr ||
			expr instanceof IntegerLiteralExpr ||
			expr instanceof IntegerLiteralMinValueExpr ||
			expr instanceof LiteralExpr ||
			expr instanceof LongLiteralExpr ||
			expr instanceof LongLiteralMinValueExpr ||
			expr instanceof NullLiteralExpr ||
			expr instanceof ObjectCreationExpr) {
			return true;
		}
		return false;
	}
	
}
