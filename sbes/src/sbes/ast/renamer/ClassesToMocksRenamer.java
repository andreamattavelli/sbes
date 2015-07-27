package sbes.ast.renamer;

import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class ClassesToMocksRenamer extends VoidVisitorAdapter<Void> {	
	
	@Override
	public void visit(FieldDeclaration arg0, Void arg1) {
		super.visit(arg0, arg1);
	}
	
	@Override
	public void visit(ReferenceType n, Void arg) {
		if (n.getType() instanceof ClassOrInterfaceType) {
			ClassOrInterfaceType coi = (ClassOrInterfaceType) n.getType();
			if (coi.getName().equals("Integer")) {
				coi.setName("IntegerMock");
			}
			else if (coi.getName().contains("<Integer>")) {
				coi.setName(coi.getName().replace("<Integer>", "<IntegerMock>"));
			}
		}
		super.visit(n, arg);
	}
	
	@Override
	public void visit(ObjectCreationExpr n, Void arg) {
		switch(n.getType().getName()) {
		case "Integer":
			n.getType().setName("IntegerMock");
			checkAndFixParameters(n.getArgs());
			break;
		}
		super.visit(n, arg);
	}
	
	@Override
	public void visit(MethodCallExpr arg0, Void arg1) {
		checkAndFixParameters(arg0.getArgs());
		super.visit(arg0, arg1);
	}

	private void checkAndFixParameters(List<Expression> args) {
		if (args == null) {
			return;
		}
		for (int i = 0; i < args.size(); i++) {
			Expression expression = args.get(i);
			if (expression instanceof CastExpr) {
				CastExpr ce = (CastExpr) expression;
				if (ce.getType() instanceof PrimitiveType) {
					MethodCallExpr mce = new MethodCallExpr(ce.getExpr(), "intValue");
					args.set(i, mce);
				}
				else if (ce.getType() instanceof ReferenceType) {
					ReferenceType rt = (ReferenceType) ce.getType();
					if (rt.getType() instanceof ClassOrInterfaceType) {
						ClassOrInterfaceType coi = (ClassOrInterfaceType) rt.getType();
						if (coi.getName().equals("Integer") || coi.getName().equals("IntegerMock")) {
							ObjectCreationExpr oce = new ObjectCreationExpr();
							oce.setType(new ClassOrInterfaceType("IntegerMock"));
							List<Expression> arguments = new ArrayList<>();
							arguments.add(ce.getExpr());
							oce.setArgs(arguments);
							args.set(i, oce);
						}
					}
				}
			}
		}
	}
	
}
