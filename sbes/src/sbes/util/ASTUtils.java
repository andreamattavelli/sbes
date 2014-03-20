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
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import sbes.stub.generator.FirstStageStubGenerator;

public class ASTUtils {

	public static Type getReturnType(Method method) {
		if (method.getReturnType().isArray()) {
			return ASTHelper.createReferenceType(method.getReturnType().getComponentType().getCanonicalName(), 
													ReflectionUtils.getArrayDimensionCount(method.getReturnType()));
		}
		else {
			return ASTHelper.createReferenceType(method.getReturnType().getCanonicalName(), 0);
		}
	}
	
	public static Type getReturnConcreteType(TypeVariable<?>[] generics, String concreteClass, Method method) {
		String canonicalName = "";
		int arrayDimension = 0;
		
		if (method.getReturnType().isArray()) {
			arrayDimension = ReflectionUtils.getArrayDimensionCount(method.getReturnType());
		}
		
		String[] genericString = ArrayUtils.toArrayString(generics);
		
		String genericReturn = method.getGenericReturnType().toString();
		if (ArrayUtils.contains(genericString, genericReturn)) {
			canonicalName = concreteClass;
		}
		else {
			canonicalName = method.getReturnType().getCanonicalName();
		}

		return ASTHelper.createReferenceType(canonicalName, arrayDimension);
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
	
	public static Type getReturnConcreteTypeAsArray(TypeVariable<?>[] generics, String concreteClass, Method method) {
		Class<?> returnType = method.getReturnType();
		
		if (returnType.getSimpleName().equals("void")) {
			return ASTHelper.createReferenceType(returnType.getCanonicalName(), 0);
		}
		else {
			ReferenceType t = (ReferenceType) getReturnConcreteType(generics, concreteClass, method);
			t.setArrayCount(t.getArrayCount() + 1);
			return t;
		}
	}	
	
	public static List<Expression> getArraysDimension() {
		List<Expression> arraysDimension = new ArrayList<Expression>();
		arraysDimension.add(ASTHelper.createNameExpr(FirstStageStubGenerator.NUM_SCENARIOS));
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
	
	public static BodyDeclaration createGenericStubHelperArray(String classType, String concreteClass, String varId) {
		ArrayCreationExpr es_ace = new ArrayCreationExpr(ASTHelper.createReferenceType(classType, 0), ASTUtils.getArraysDimension(), 0);
		VariableDeclarator expected_states = ASTUtils.createDeclarator(varId, es_ace);
		BodyDeclaration es_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL, ASTHelper.createReferenceType(classType + "<"+concreteClass+">", 1), expected_states);
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
	
	public static List<Expression> createParameters(List<Parameter> parameters, String[] paramNames, boolean manyScenarios) {
		List<Expression> astParameters = new ArrayList<Expression>();
		if (!parameters.isEmpty()) {
			for (int i = 0; i < parameters.size(); i++) {
				Parameter p = parameters.get(i);
				if (manyScenarios && paramNames.length > i && AsmParameterNames.isSizeParam(paramNames[i])) {
					ArrayAccessExpr aae = new ArrayAccessExpr(ASTHelper.createNameExpr(paramNames[i]), new NameExpr("i_"));
//					MethodCallExpr mce = new MethodCallExpr(aae, "intValue");
					astParameters.add(aae);
				}
				else {
					astParameters.add(ASTHelper.createNameExpr(p.getId().getName()));
				}
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
	
	public static String getName(Expression exp) {
		if (exp instanceof NameExpr) {
			return getName((NameExpr) exp);
		}
		else if (exp instanceof CastExpr) {
			CastExpr ce = (CastExpr) exp;
			if (ce.getExpr() instanceof NameExpr) {
				return getName((NameExpr) ce.getExpr());
			}
		}
		else if (exp instanceof FieldAccessExpr) {
			FieldAccessExpr fae = (FieldAccessExpr) exp;
			return getName(fae.getScope());
		}
		else if (exp instanceof ArrayAccessExpr) {
			ArrayAccessExpr aae = (ArrayAccessExpr) exp;
			return getName(aae.getName());
		}
		return null;
	}
	
	private static String getName(NameExpr ne) {
		return ne.getName();
	}
}
