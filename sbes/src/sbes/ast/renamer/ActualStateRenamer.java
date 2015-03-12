package sbes.ast.renamer;

import japa.parser.ast.Node;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

import sbes.stub.generator.first.FirstStageGeneratorStub;
import sbes.util.ASTUtils;

public class ActualStateRenamer extends VoidVisitorAdapter<Void> {

	private String expectedState;
	private String index;
	private String methodName;
	List<Statement> actualStates; 
	
	public ActualStateRenamer(String expectedState, String index, String methodName) {
		this.expectedState = expectedState;
		this.index = index;
		this.methodName = methodName;
		this.actualStates = new ArrayList<Statement>();
	}
	
	public List<Statement> getActualStates() {
		return actualStates;
	}
	
	@Override
	public void visit(ArrayAccessExpr n, Void arg) {
		String name_ = ASTUtils.getName(n.getName());
		String index_ = ASTUtils.getName(n.getIndex());
		if (name_ != null && index_ != null) {
			if (name_.equals(expectedState) && index_.equals(index)) {
				handleParent(n.getParentNode());
			}
		}
		super.visit(n, arg);
	}
	
	private void handleParent(Node n) {
		if (n instanceof AssignExpr) {
			AssignExpr ae = (AssignExpr) n;
			AssignExpr actual = new AssignExpr();
			actual.setValue(ae.getValue());
			actual.setOperator(Operator.assign);
			actual.setTarget(new ArrayAccessExpr(new NameExpr(FirstStageGeneratorStub.ACTUAL_STATE), new NameExpr(index)));
			actualStates.add(new ExpressionStmt(actual));
		}
		else if (n instanceof MethodCallExpr) {
			MethodCallExpr mce = (MethodCallExpr) n;
			if (!mce.getName().equals(methodName)) {
				MethodCallExpr actual = new MethodCallExpr();
				actual.setScope(new ArrayAccessExpr(new NameExpr(FirstStageGeneratorStub.ACTUAL_STATE), new NameExpr(index)));
				actual.setName(mce.getName());
				actual.setArgs(mce.getArgs());
				actualStates.add(new ExpressionStmt(actual));
			}
		}
	}

}
