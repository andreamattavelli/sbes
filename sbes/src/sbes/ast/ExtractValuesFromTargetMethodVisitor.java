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
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.PrimitiveType.Primitive;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import sbes.util.ReflectionUtils;

public class ExtractValuesFromTargetMethodVisitor extends VoidVisitorAdapter<String> {
	private int index;
	private Method targetMethod;
	private List<FieldDeclaration> fields;

	public ExtractValuesFromTargetMethodVisitor(final int index, final Method targetMethod) {
		this.index = index;
		this.targetMethod = targetMethod;
		fields = new ArrayList<FieldDeclaration>();
	}

	public List<FieldDeclaration> getFields() {
		return fields;
	}

	@Override
	public void visit(final MethodCallExpr n, final String methodName) {
		if (n != null && n.getName() != null && n.getName().equals(methodName) && 
				(targetMethod.getParameterTypes().length == 0 || n.getArgs().size() == targetMethod.getParameterTypes().length)) {
			if (n.getArgs() != null) {
				for (int i = 0; i < n.getArgs().size(); i++) {
					Expression arg = n.getArgs().get(i);
					handleArgument(n, arg, i);
				}
			}
		}
		super.visit(n, methodName);
	}

	private void handleArgument(final MethodCallExpr n, final Expression arg, final int i) {
		if (arg instanceof IntegerLiteralExpr) {
			Class<?> parameterType = targetMethod.getParameterTypes()[i];
			n.getArgs().set(i, new NameExpr("ELEMENT_" + index + "_" + fields.size()));
			
			Type integer;
			if (parameterType.getCanonicalName().contains("Object")) {
				integer = new ClassOrInterfaceType("Integer");
			}
			else {
				integer = new PrimitiveType(Primitive.Int);
			}
			Expression int_init = arg;
			VariableDeclarator variable_int = new VariableDeclarator(new VariableDeclaratorId("ELEMENT_" + index + "_" + fields.size()), int_init);
			FieldDeclaration fd_int = new FieldDeclaration(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, integer, variable_int);
			fields.add(fd_int);
			
		} else if (arg instanceof StringLiteralExpr) {
			n.getArgs().set(i, new NameExpr("ELEMENT_" + index + "_" + fields.size()));
			ClassOrInterfaceType string = new ClassOrInterfaceType("String");
			List<Expression> args = new ArrayList<Expression>();
			args.add(arg);
			Expression init = new ObjectCreationExpr(null, string, args);
			VariableDeclarator variable = new VariableDeclarator(new VariableDeclaratorId("ELEMENT_" + index + "_" + fields.size()), init);
			FieldDeclaration fd = new FieldDeclaration(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, string, variable);
			fields.add(fd);
		} else if (arg instanceof ObjectCreationExpr) { 
			ObjectCreationExpr oce = (ObjectCreationExpr) arg;
			if (ReflectionUtils.primitivesStringRepresentation.contains(oce.getType().toString())) {
				handleArgument(n, oce.getArgs().get(0), i);
			}
			else {
				// we need to check its arguments, let's put it in the TODOLIST
			}
		} else if (arg instanceof CastExpr) {
			CastExpr ce = (CastExpr) arg;
			handleArgument(n, ce.getExpr(), i);
		}
	}
}