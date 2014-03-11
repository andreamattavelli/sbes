package sbes.ast;

import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class EquivalentSequenceCallVisitor extends VoidVisitorAdapter<Void> {
	List<MethodCallExpr> dependencies;
	List<VariableDeclarationExpr> assignments;
	
	public EquivalentSequenceCallVisitor() {
		dependencies = new ArrayList<MethodCallExpr>();
		assignments = new ArrayList<VariableDeclarationExpr>();
	}
	
	public List<MethodCallExpr> getDependencies() {
		return dependencies;
	}
	
	public List<VariableDeclarationExpr> getAssignments() {
		return assignments;
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
	
	@Override
	public void visit(VariableDeclarationExpr arg0, Void arg1) {
		if (arg0.getVars().get(0).getInit() instanceof FieldAccessExpr) {
			FieldAccessExpr fae = (FieldAccessExpr) arg0.getVars().get(0).getInit();
			if (fae.getField().startsWith("ELEMENT_")) {
				assignments.add(arg0);
			}
		}
		super.visit(arg0, arg1);
	}
	
}