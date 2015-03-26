package sbes.ast.renamer;

import japa.parser.ASTHelper;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import sbes.stub.generator.first.FirstStageGeneratorStub;
import sbes.util.ASTUtils;

public class ExpectedResultRenamer extends VoidVisitorAdapter<String> {
	private final int index;
	private final int parameters;
	private final boolean isStatic;
	
	private String expectedState;
	private boolean found;

	public ExpectedResultRenamer(final int index, final int parameters, final boolean isStatic) {
		this.index = index;
		this.parameters = parameters;
		this.isStatic = isStatic;
		
		this.found = false;
	}
	
	public String getExpectedState() {
		return expectedState;
	}

	@Override
	public void visit(final VariableDeclarationExpr n, final String methodName) {
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
	
	@Override
	public void visit(final MethodCallExpr arg0, final String methodName) {
		if (!found) {
			handleMethodCall(null, methodName, arg0);
		}
		super.visit(arg0, methodName);
	}
	
	private void handleMethodCall(final VariableDeclarationExpr n, final String methodName, final MethodCallExpr mce) {
		if (mce.getName().equals(methodName) && (parameters == 0 || mce.getArgs().size() == parameters)) {
			found = true;
			// found class constructor, switch to EXPECTED_STATES
			Expression target = new ArrayAccessExpr(ASTHelper.createNameExpr(FirstStageGeneratorStub.EXPECTED_RESULT),
													ASTHelper.createNameExpr(Integer.toString(index)));
			
			if (mce.getScope() != null) {
				if (!isStatic && !Character.isUpperCase(ASTUtils.getName(mce.getScope()).toCharArray()[0])) {
					expectedState = ASTUtils.getName(mce.getScope());
					mce.setScope(new ArrayAccessExpr(ASTHelper.createNameExpr(FirstStageGeneratorStub.EXPECTED_STATE),
												ASTHelper.createNameExpr(Integer.toString(index))));
				}
			}
			
			if (n != null) {
				AssignExpr ae = new AssignExpr(target, mce, Operator.assign);
				ExpressionStmt estmt = (ExpressionStmt) n.getParentNode();
				estmt.setExpression(ae);
			}
		}
	}
}