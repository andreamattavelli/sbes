package sbes.ast;

import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ExtractValuesFromTargetMethodVisitor extends VoidVisitorAdapter<String> {
	private List<FieldDeclaration> fields;
	public ExtractValuesFromTargetMethodVisitor() {
		fields = new ArrayList<FieldDeclaration>();
	}
	public List<FieldDeclaration> getFields() {
		return fields;
	}
	@Override
	public void visit(MethodCallExpr n, String methodName) {
		if (n.getName().equals(methodName)) {
			if (n.getArgs() != null) {
				for (int i = 0; i < n.getArgs().size(); i++) {
					Expression arg = n.getArgs().get(i);
					handleArgument(n, arg, i);
				}
			}
		}
		super.visit(n, methodName);
	}
	
	private void handleArgument(MethodCallExpr n, Expression arg, int i) {
		if (arg instanceof IntegerLiteralExpr) {
			n.getArgs().set(i, new NameExpr("ELEMENT_" + fields.size()));
			ClassOrInterfaceType integer = new ClassOrInterfaceType("Integer");
			List<Expression> args = new ArrayList<Expression>();
			args.add(arg);
			Expression init = new ObjectCreationExpr(null, integer, args);
			VariableDeclarator variable = new VariableDeclarator(new VariableDeclaratorId("ELEMENT_" + fields.size()), init);
			FieldDeclaration fd = new FieldDeclaration(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, integer, variable);
			fields.add(fd);
		}
		else if (arg instanceof StringLiteralExpr) {
			n.getArgs().set(i, new NameExpr("ELEMENT_" + fields.size()));
			ClassOrInterfaceType integer = new ClassOrInterfaceType("String");
			List<Expression> args = new ArrayList<Expression>();
			args.add(arg);
			Expression init = new ObjectCreationExpr(null, integer, args);
			VariableDeclarator variable = new VariableDeclarator(new VariableDeclaratorId("ELEMENT_" + fields.size()), init);
			FieldDeclaration fd = new FieldDeclaration(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, integer, variable);
			fields.add(fd);
		}
		else if (arg instanceof CastExpr) {
			CastExpr ce = (CastExpr) arg;
			handleArgument(n, ce.getExpr(), i);
		}
	}
}