package sbes.ast;

import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class EquivalentSequenceCallVisitor extends VoidVisitorAdapter<Void> {
	List<MethodCallExpr> dependencies;
	
	public EquivalentSequenceCallVisitor() {
		dependencies = new ArrayList<MethodCallExpr>();
	}
	
	public List<MethodCallExpr> getDependencies() {
		return dependencies;
	}
	
	@Override
	public void visit(MethodCallExpr arg0, Void arg1) {
		if (!arg0.getName().equals("set_results") && arg0.getScope() != null && 
				arg0.getArgs() != null && arg0.getScope() instanceof NameExpr) {
			NameExpr ne = (NameExpr) arg0.getScope();
			if (ne.getName().equals("clone")) {
				dependencies.add(arg0);
			}
		}
		
		super.visit(arg0, arg1);
	}
}