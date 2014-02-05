package sbes.util;

import japa.parser.ASTHelper;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import sbes.stub.generator.FirstPhaseStubStrategy;

public class ASTUtils {

	public static Type getReturnType(Method method) {
		if (method.getReturnType().isArray()) {
			return ASTHelper.createReferenceType(method.getReturnType().getComponentType().getCanonicalName(), ReflectionUtils.getArrayDimensionCount(method.getReturnType()));
		}
		else { 
			return ASTHelper.createReferenceType(method.getReturnType().getCanonicalName(), 0);
		}
	}
	
	public static Type getReturnTypeAsArray(Method method) {
		Class<?> returnType = method.getReturnType();
		
		if (returnType.getSimpleName().equals("void")) {
			return ASTHelper.createReferenceType(returnType.getCanonicalName(), 0);
		}
		else {
			return ASTHelper.createReferenceType(returnType.getCanonicalName(), 1);
		}
	}
	
	
	public static List<Expression> getArraysDimension() {
		List<Expression> arraysDimension = new ArrayList<Expression>();
		arraysDimension.add(ASTHelper.createNameExpr(FirstPhaseStubStrategy.NUM_SCENARIOS));
		return arraysDimension;
	}
	
	public static VariableDeclarator createDeclarator(String id, Expression expression) {
		return new VariableDeclarator(new VariableDeclaratorId(id), expression);
	}
	
	public static Expression createArrayAccess(String arrayId, String indexId) {
		return new ArrayAccessExpr(ASTHelper.createNameExpr(arrayId), ASTHelper.createNameExpr(indexId));
	}
	
	public static BodyDeclaration createStubHelperArray(String classType, String varId) {
		ArrayCreationExpr es_ace = new ArrayCreationExpr(ASTHelper.createReferenceType(classType, 0), ASTUtils.getArraysDimension(), 0);
		VariableDeclarator expected_states = ASTUtils.createDeclarator(varId, es_ace);
		BodyDeclaration es_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL, ASTHelper.createReferenceType(classType, 1), expected_states);
		return es_bd;
	}
	
	public static List<Expression> createParameters(List<Parameter> parameters) {
		List<Expression> astParameters = new ArrayList<Expression>();
		if (!parameters.isEmpty()) {
			for (Parameter p : parameters) {
				astParameters.add(ASTHelper.createNameExpr(p.getId().getName()));
			}
		}
		return astParameters;
	}
	
	public static List<Expression> createForInit(String varId, Type varType, Expression varInit, Operator operator) {
		List<Expression> init = new ArrayList<Expression>();
		List<VariableDeclarator> decls = new ArrayList<VariableDeclarator>();
		decls.add(new VariableDeclarator(new VariableDeclaratorId(varId)));
		init.add(new AssignExpr(new VariableDeclarationExpr(varType, decls), varInit, operator));
		return init;
	}
	
	public static Expression createForCondition(String varIdLeft, String varIdRight, japa.parser.ast.expr.BinaryExpr.Operator operator) {
		return new BinaryExpr(ASTHelper.createNameExpr(varIdLeft), ASTHelper.createNameExpr(varIdRight), operator);
	}
	
	public static List<Expression> createForIncrement(String varId, japa.parser.ast.expr.UnaryExpr.Operator operator) {
		List<Expression> increment = new ArrayList<Expression>();
		increment.add(new UnaryExpr(ASTHelper.createNameExpr(varId), operator));
		return increment;
	}
	
	public static MethodCallExpr createSystemOut(String message) {
		List<Expression> args = new ArrayList<Expression>();
		args.add(new StringLiteralExpr(message));
		return new MethodCallExpr(new FieldAccessExpr(ASTHelper.createNameExpr("System"), "out"), "println", args);
	}
}
