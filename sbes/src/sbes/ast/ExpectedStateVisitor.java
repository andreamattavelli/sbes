package sbes.ast;

import japa.parser.ASTHelper;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.visitor.CloneVisitor;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import sbes.stub.generator.first.FirstStageStubGenerator;

public class ExpectedStateVisitor extends VoidVisitorAdapter<String> {
	private int index;
	private String objName;
	
	private ExpressionStmt actualState;
	
	public ExpectedStateVisitor(int index, String objName) {
		this.index = index;
		this.objName = objName;
	}
	
	public ExpressionStmt getActualState() {
		return actualState;
	}
	
	@Override
	public void visit(VariableDeclarationExpr n, String concreteClassName) {
		if (n.getType().toString().equals(concreteClassName)) {
			VariableDeclarator vd = n.getVars().get(0);
			if (vd.getId().getName().equals(objName)) {
				if (vd.getInit() instanceof ObjectCreationExpr) {
					// create actual state on the base of the expected one				
					actualState = createActualState(n);

					// found class constructor, switch to EXPECTED_STATES
					Expression target = new ArrayAccessExpr(ASTHelper.createNameExpr(FirstStageStubGenerator.EXPECTED_STATE), 
							ASTHelper.createNameExpr(Integer.toString(index)));
					AssignExpr ae = new AssignExpr(target, vd.getInit(), Operator.assign);
					ExpressionStmt estmt = (ExpressionStmt) n.getParentNode();
					estmt.setExpression(ae);
				}
				else if (vd.getInit() instanceof MethodCallExpr) {
					// create actual state on the base of the expected one
					actualState = createActualState(n);
					
					// found class constructor, switch to EXPECTED_STATES
					Expression target = new ArrayAccessExpr(ASTHelper.createNameExpr(FirstStageStubGenerator.EXPECTED_STATE), 
							ASTHelper.createNameExpr(Integer.toString(index)));
					AssignExpr ae = new AssignExpr(target, vd.getInit(), Operator.assign);
					ExpressionStmt estmt = (ExpressionStmt) n.getParentNode();
					estmt.setExpression(ae);
				}
			}
		}
		
		super.visit(n, concreteClassName);
	}

	private ExpressionStmt createActualState(VariableDeclarationExpr arg0) {
		CloneVisitor cv = new CloneVisitor();
		VariableDeclarationExpr actual = (VariableDeclarationExpr) cv.visit(arg0, null);
		
		Expression target = new ArrayAccessExpr(ASTHelper.createNameExpr(FirstStageStubGenerator.ACTUAL_STATE), 
												ASTHelper.createNameExpr(Integer.toString(index)));
		AssignExpr ae = new AssignExpr(target, actual.getVars().get(0).getInit(), Operator.assign);
		ExpressionStmt estmt = new ExpressionStmt(ae);
		
		return estmt;
	}
}