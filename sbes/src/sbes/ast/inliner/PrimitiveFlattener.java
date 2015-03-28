package sbes.ast.inliner;

import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import sbes.util.ReflectionUtils;

public class PrimitiveFlattener  extends VoidVisitorAdapter<Void> {

	@Override
	public void visit(ObjectCreationExpr n, Void arg) {
		if (ReflectionUtils.primitivesStringRepresentation.contains(n.getType().getName())) {
			if (n.getArgs().get(0) instanceof ObjectCreationExpr) {
				ObjectCreationExpr oce = (ObjectCreationExpr) n.getArgs().get(0);
				n.getArgs().set(0, oce.getArgs().get(0));
			}
			else if (n.getArgs().get(0) instanceof CastExpr) {
				CastExpr ce = (CastExpr) n.getArgs().get(0);
				n.getArgs().set(0, ce.getExpr());
			}
		}
		
		super.visit(n, arg);
	}
	
}
