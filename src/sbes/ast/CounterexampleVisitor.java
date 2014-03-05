package sbes.ast;

import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class CounterexampleVisitor extends VoidVisitorAdapter<String> {

	@Override
	public void visit(ClassOrInterfaceType arg0, String arg1) {
		if (arg0.getName().equals(arg1 + "_Stub_2")) {
			arg0.setName(arg1);
		}
		super.visit(arg0, arg1);
	}
	
}
