package sbes.ast;

import japa.parser.ASTHelper;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.lang.reflect.Method;

import sbes.stub.generator.FirstStageStubGenerator;
import sbes.util.ASTUtils;

public class CounterexampleExpectedResultVisitor extends VoidVisitorAdapter<Void> {
	private int index;
	private String expectedState;
	private Method targetMethod;
	
	public CounterexampleExpectedResultVisitor(Method targetMethod, int index) {
		this.targetMethod = targetMethod;
		this.index = index;
	}
	
	public String getExpectedState() {
		return expectedState;
	}

	@Override
	public void visit(MethodCallExpr arg0, Void methodName) {
		handleMethodCall(arg0);
		super.visit(arg0, methodName);
	}
	
	private void handleMethodCall(MethodCallExpr mce) {
		if (mce.getName().equals("method_under_test")) {
			// found class constructor, switch to EXPECTED_STATES
			mce.setName(targetMethod.getName());
			Expression target = new ArrayAccessExpr(ASTHelper.createNameExpr(FirstStageStubGenerator.EXPECTED_RESULT),
													ASTHelper.createNameExpr(Integer.toString(index)));
			
			if (!targetMethod.getReturnType().equals(void.class)) {
				expectedState = ASTUtils.getName(mce.getScope());
				mce.setScope(new ArrayAccessExpr(ASTHelper.createNameExpr(FirstStageStubGenerator.EXPECTED_STATE),
											ASTHelper.createNameExpr(Integer.toString(index))));
				ExpressionStmt estmt = (ExpressionStmt) mce.getParentNode();
				AssignExpr ae = new AssignExpr(target, mce, Operator.assign);
				estmt.setExpression(ae);
			}
		}
	}
}