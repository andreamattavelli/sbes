package sbes.stub.generator;

import japa.parser.ASTHelper;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import sbes.util.MethodUtils;

public class FirstPhaseStrategy extends Generator {

	private static final String NUM_SCENARIOS = "NUM_SCENARIOS";
	private static final String EXPECTED_STATE = "expected_states";
	private static final String EXPECTED_RESULT = "expected_results";
	private static final String ACTUAL_STATE = "actual_states";
	private static final String ACTUAL_RESULT = "actual_results";
	
	private List<Expression> dimension;
	
	@Override
	protected TypeDeclaration getClassDeclaration(String className) {
		stubName = className + STUB_EXTENSION;
		return new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, stubName);
	}

	@Override
	protected List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c) {
		List<BodyDeclaration> declarations = new ArrayList<BodyDeclaration>();
		
		VariableDeclarator num_scenarios = new VariableDeclarator(new VariableDeclaratorId(NUM_SCENARIOS), new IntegerLiteralExpr("1"));
		BodyDeclaration num_scenarios_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC, ASTHelper.INT_TYPE, num_scenarios);
		
		dimension = new ArrayList<Expression>();
		dimension.add(ASTHelper.createNameExpr(NUM_SCENARIOS));
		Type targetReturnType = ASTHelper.createReferenceType(targetMethod.getGenericReturnType().toString(), 0); //FIXME
		Type classType = ASTHelper.createReferenceType(c.getSimpleName(), 0);
		
		// stub helper arrays
		ArrayCreationExpr es_ace = new ArrayCreationExpr(classType, dimension, 0);
		VariableDeclarator expected_states = new VariableDeclarator(new VariableDeclaratorId(EXPECTED_STATE), es_ace);
		BodyDeclaration es_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL, ASTHelper.createReferenceType(c.getSimpleName(), 1), expected_states);
		
		ArrayCreationExpr er_ace = new ArrayCreationExpr(targetReturnType, dimension, 0);
		VariableDeclarator expected_results = new VariableDeclarator(new VariableDeclaratorId(EXPECTED_RESULT), er_ace);
		BodyDeclaration er_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL, ASTHelper.createReferenceType(targetMethod.getGenericReturnType().toString(), 1), expected_results);
		
		ArrayCreationExpr as_ace = new ArrayCreationExpr(classType, dimension, 0);
		VariableDeclarator actual_states = new VariableDeclarator(new VariableDeclaratorId(ACTUAL_STATE), as_ace);
		BodyDeclaration as_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL, ASTHelper.createReferenceType(c.getSimpleName(), 1), actual_states);
		
		ArrayCreationExpr ar_ace = new ArrayCreationExpr(targetReturnType, dimension, 0);
		VariableDeclarator actual_results = new VariableDeclarator(new VariableDeclaratorId(ACTUAL_RESULT), ar_ace);
		BodyDeclaration ar_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL, ASTHelper.createReferenceType(targetMethod.getGenericReturnType().toString(), 1), actual_results);
		
		declarations.add(num_scenarios_bd);
		declarations.add(es_bd);
		declarations.add(er_bd);
		declarations.add(as_bd);
		declarations.add(ar_bd);
		
		return declarations;
	}
	
	@Override
	protected List<BodyDeclaration> additionalMethods(Method[] methods) {
		List<BodyDeclaration> members = new ArrayList<BodyDeclaration>();
		
		for (Method method : methods) {
			if (MethodUtils.methodFilter(method)) {
				continue;
			}
			Type returnType = getReturnType(method);
			MethodDeclaration md = new MethodDeclaration(method.getModifiers(), returnType, method.getName());
			
			//parameters
			List<Parameter> parameters = new ArrayList<Parameter>();
			parameters.addAll(getParameterType(method.getGenericParameterTypes()));
			md.setParameters(parameters);
			
			//body
			BlockStmt stmt = new BlockStmt();
			List<Statement> stmts = new ArrayList<Statement>();
			
			VariableDeclarationExpr res = ASTHelper.createVariableDeclarationExpr(returnType, "res");
			
			Type cleanReturnType = ASTHelper.createReferenceType(method.getGenericReturnType().toString(), 0);
			if (!returnType.toString().equals("void")) {
				ArrayCreationExpr ace = new ArrayCreationExpr(cleanReturnType, dimension, 0);
				AssignExpr resAssign = new AssignExpr(res, ace, Operator.assign);
				ExpressionStmt resStmt = new ExpressionStmt(resAssign);
				stmts.add(resStmt);
				
				// for loop
				List<Expression> init = new ArrayList<Expression>();
				List<VariableDeclarator> decls = new ArrayList<VariableDeclarator>();
				decls.add(new VariableDeclarator(new VariableDeclaratorId("i")));
				init.add(new AssignExpr(new VariableDeclarationExpr(ASTHelper.INT_TYPE, decls), new IntegerLiteralExpr("0"), Operator.assign));
				
				Expression condition = new BinaryExpr(ASTHelper.createNameExpr("i"), ASTHelper.createNameExpr(NUM_SCENARIOS), japa.parser.ast.expr.BinaryExpr.Operator.less);
				List<Expression> increment = new ArrayList<Expression>();
				increment.add(new UnaryExpr(ASTHelper.createNameExpr("i"), japa.parser.ast.expr.UnaryExpr.Operator.posIncrement));
				BlockStmt body = new BlockStmt();
				
				Expression left = new ArrayAccessExpr(ASTHelper.createNameExpr("res"), ASTHelper.createNameExpr("i"));
				List<Expression> call_args = new ArrayList<Expression>();
				if (!parameters.isEmpty()) {
					for (Parameter p : parameters) {
						call_args.add(ASTHelper.createNameExpr(p.getId().getName()));
					}
				}
				Expression right = new MethodCallExpr(new ArrayAccessExpr(ASTHelper.createNameExpr(ACTUAL_STATE), ASTHelper.createNameExpr("i")), method.getName(), call_args);
				AssignExpr call_result = new AssignExpr(left, right, Operator.assign);
				ASTHelper.addStmt(body, call_result);
				
				ForStmt forStmt = new ForStmt(init, 
											condition, 
											increment, 
											body);
				stmts.add(forStmt);
				
				ReturnStmt ret = new ReturnStmt(ASTHelper.createNameExpr("res"));
				stmts.add(ret);
				
			} else {
				// for loop
				//FIXME: remove code cloning..
				List<Expression> init = new ArrayList<Expression>();
				List<VariableDeclarator> decls = new ArrayList<VariableDeclarator>();
				decls.add(new VariableDeclarator(new VariableDeclaratorId("i")));
				init.add(new AssignExpr(new VariableDeclarationExpr(ASTHelper.INT_TYPE, decls), new IntegerLiteralExpr("0"), Operator.assign));
				
				Expression condition = new BinaryExpr(ASTHelper.createNameExpr("i"), ASTHelper.createNameExpr(NUM_SCENARIOS), japa.parser.ast.expr.BinaryExpr.Operator.less);
				List<Expression> increment = new ArrayList<Expression>();
				increment.add(new UnaryExpr(ASTHelper.createNameExpr("i"), japa.parser.ast.expr.UnaryExpr.Operator.posIncrement));
				BlockStmt body = new BlockStmt();
				
				List<Expression> call_args = new ArrayList<Expression>();
				if (!parameters.isEmpty()) {
					for (Parameter p : parameters) {
						call_args.add(ASTHelper.createNameExpr(p.getId().getName()));
					}
				}
				Expression right = new MethodCallExpr(new ArrayAccessExpr(ASTHelper.createNameExpr(ACTUAL_STATE), ASTHelper.createNameExpr("i")), method.getName(), call_args);
				ASTHelper.addStmt(body, right);
				
				ForStmt forStmt = new ForStmt(init, 
											condition, 
											increment, 
											body);
				stmts.add(forStmt);
			}

			stmt.setStmts(stmts);
			md.setBody(stmt);
			
			members.add(md);
		}
		return members;
	}
	
	@Override
	protected MethodDeclaration createSetResultsMethod(Method targetMethod) {
		MethodDeclaration set_results = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "set_results");
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter(ASTHelper.createReferenceType(getReturnType(targetMethod).toString(), 1), new VariableDeclaratorId("res")));
		set_results.setParameters(parameters);
		
		List<Expression> init = new ArrayList<Expression>();
		List<VariableDeclarator> decls = new ArrayList<VariableDeclarator>();
		decls.add(new VariableDeclarator(new VariableDeclaratorId("i")));
		init.add(new AssignExpr(new VariableDeclarationExpr(ASTHelper.INT_TYPE, decls), new IntegerLiteralExpr("0"), Operator.assign));
		Expression condition = new BinaryExpr(ASTHelper.createNameExpr("i"), ASTHelper.createNameExpr(NUM_SCENARIOS), japa.parser.ast.expr.BinaryExpr.Operator.less);
		List<Expression> increment = new ArrayList<Expression>();
		increment.add(new UnaryExpr(ASTHelper.createNameExpr("i"), japa.parser.ast.expr.UnaryExpr.Operator.posIncrement));
		BlockStmt forBody = new BlockStmt();
		Expression left = new ArrayAccessExpr(ASTHelper.createNameExpr(ACTUAL_RESULT), ASTHelper.createNameExpr("i"));
		Expression right = new ArrayAccessExpr(ASTHelper.createNameExpr("res"), ASTHelper.createNameExpr("i"));
		AssignExpr assignment = new AssignExpr(left, right, Operator.assign);
		ASTHelper.addStmt(forBody, assignment);
		
		ForStmt forStmt = new ForStmt(init, condition, increment, forBody);
		
		BlockStmt methodBody = new BlockStmt();
		ASTHelper.addStmt(methodBody, forStmt);
		set_results.setBody(methodBody);
		
		return set_results;
	}
	
	@Override
	protected MethodDeclaration createMethodUnderTest() {
		MethodDeclaration set_results = new MethodDeclaration(Modifier.PUBLIC, ASTHelper.VOID_TYPE, "method_under_test");
		BlockStmt stmt = new BlockStmt();
		
		List<Expression> args = new ArrayList<Expression>();
		args.add(new StringLiteralExpr("Executed"));
		MethodCallExpr syso = new MethodCallExpr(new FieldAccessExpr(ASTHelper.createNameExpr("System"), "out"), "println", args);
		
		BinaryExpr condition = null;
		for (int i = 0; i < 1; i++) { //FIXME: number of generated test scenarios	
			Expression left_res = new ArrayAccessExpr(ASTHelper.createNameExpr(EXPECTED_RESULT), ASTHelper.createNameExpr(Integer.toString(i)));
			Expression right_res = new ArrayAccessExpr(ASTHelper.createNameExpr(ACTUAL_RESULT), ASTHelper.createNameExpr(Integer.toString(i)));
			BinaryExpr resCondition = new BinaryExpr(left_res, right_res, japa.parser.ast.expr.BinaryExpr.Operator.equals);
			
			List<Expression> distance_args = new ArrayList<Expression>();
			distance_args.add(new ArrayAccessExpr(ASTHelper.createNameExpr(EXPECTED_STATE), ASTHelper.createNameExpr(Integer.toString(i))));
			distance_args.add(new ArrayAccessExpr(ASTHelper.createNameExpr(ACTUAL_STATE), ASTHelper.createNameExpr(Integer.toString(i))));
			
			Expression left_state = new MethodCallExpr(null, "distance", distance_args);
			Expression right_state = new IntegerLiteralExpr("0");
			BinaryExpr stateCondition = new BinaryExpr(left_state, right_state, japa.parser.ast.expr.BinaryExpr.Operator.equals);
			BinaryExpr newCondition = new BinaryExpr(resCondition, stateCondition, japa.parser.ast.expr.BinaryExpr.Operator.and);
			
			if (condition != null) {
				condition = new BinaryExpr(condition, newCondition, japa.parser.ast.expr.BinaryExpr.Operator.and);
			}
			else {
				condition = newCondition;
			}
		}
		
		IfStmt ifStmt = new IfStmt(condition, new ExpressionStmt(syso), null);
		ASTHelper.addStmt(stmt, ifStmt);
		set_results.setBody(stmt);
		
		return set_results;
	}

}
