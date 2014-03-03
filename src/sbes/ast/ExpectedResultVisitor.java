package sbes.ast;

import sbes.stub.generator.FirstStageStubGenerator;
import sbes.util.ASTUtils;
import japa.parser.ASTHelper;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class ExpectedResultVisitor extends VoidVisitorAdapter<String> {
	private int index;
	private String expectedState;

	public ExpectedResultVisitor(int index) {
		this.index = index;
	}
	
	public String getExpectedState() {
		return expectedState;
	}

	@Override
	public void visit(VariableDeclarationExpr n, String methodName) {
		VariableDeclarator vd = n.getVars().get(0);
		if (vd.getInit() instanceof MethodCallExpr) {
			MethodCallExpr mce = (MethodCallExpr) vd.getInit();
			handleMethodCall(n, methodName, mce);
		}
		else if (vd.getInit() instanceof CastExpr) {
			CastExpr ce = (CastExpr) vd.getInit();
			if (ce.getExpr() instanceof MethodCallExpr) {
				handleMethodCall(n, methodName, (MethodCallExpr) ce.getExpr());
			}
		}
		super.visit(n, methodName);
	}
	
	private void handleMethodCall(VariableDeclarationExpr n, String methodName, MethodCallExpr mce) {
		if (mce.getName().equals(methodName)) {
			// found class constructor, switch to EXPECTED_STATES
			Expression target = new ArrayAccessExpr(ASTHelper.createNameExpr(FirstStageStubGenerator.EXPECTED_RESULT),
													ASTHelper.createNameExpr(Integer.toString(index)));
			
			if (mce.getScope() != null) {
				expectedState = ASTUtils.getName(mce.getScope());
				mce.setScope(new ArrayAccessExpr(ASTHelper.createNameExpr(FirstStageStubGenerator.EXPECTED_STATE),
											ASTHelper.createNameExpr(Integer.toString(index))));
			}

			AssignExpr ae = new AssignExpr(target, mce, Operator.assign);
			ExpressionStmt estmt = (ExpressionStmt) n.getParentNode();
			estmt.setExpression(ae);
		}
	}
}