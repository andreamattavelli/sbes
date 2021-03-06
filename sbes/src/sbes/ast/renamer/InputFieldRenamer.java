package sbes.ast.renamer;

import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.Map;

public class InputFieldRenamer extends VoidVisitorAdapter<Map<String, String>> {
	
	@Override
	public void visit(final NameExpr n, final Map<String, String> arg) {
		if (arg.containsKey(n.getName())) {
			n.setName(arg.get(n.getName()));
		}
		super.visit(n, arg);
	}
	
}