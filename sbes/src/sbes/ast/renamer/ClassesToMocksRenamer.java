package sbes.ast.renamer;

import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class ClassesToMocksRenamer extends VoidVisitorAdapter<Void> {

	@Override
	public void visit(ReferenceType n, Void arg) {
		if (n.getType() instanceof ClassOrInterfaceType) {
			ClassOrInterfaceType coi = (ClassOrInterfaceType) n.getType();
			switch(coi.getName()) {
			case "Integer":
				coi.setName("IntegerMock");
				break;
			}
		}
		super.visit(n, arg);
	}
	
	@Override
	public void visit(ObjectCreationExpr n, Void arg) {
		switch(n.getType().getName()) {
		case "Integer":
			n.getType().setName("IntegerMock");
			checkAndFixParameters(n);
			break;
		}
		super.visit(n, arg);
	}

	private void checkAndFixParameters(ObjectCreationExpr n) {
		List<Expression> args = n.getArgs();
		for (int i = 0; i < args.size(); i++) {
			Expression expression = args.get(i);
			if (expression instanceof CastExpr) {
				CastExpr ce = (CastExpr) expression;
				if (ce.getType() instanceof PrimitiveType) {
					MethodCallExpr mce = new MethodCallExpr(ce.getExpr(), "intValue");
					args.set(i, mce);
				}
			}
		}
	}
	
}
