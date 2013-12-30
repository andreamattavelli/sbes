package sbes.stub;

import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.ConditionalExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.PrimitiveType.Primitive;
import japa.parser.ast.type.Type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import sbes.Options;
import sbes.logging.Logger;
import sbes.util.ClassUtils;

public class Generator {

	private static final Logger logger = new Logger(Generator.class);
	private final ClassLoader classloader;
	
	private static final String NUM_SCENARIOS = "NUM_SCENARIOS";
	private static final String EXPECTED_STATE = "expected_states";
	private static final String EXPECTED_RESULT = "expected_results";
	private static final String ACTUAL_STATE = "actual_states";
	private static final String ACTUAL_RESULT = "actual_results";
	
	public Generator() {
		this.classloader = InternalClassloader.getInternalClassLoader();
	}
	
	public void generateStub() {
		checkClasspath();
		
		Class<?> c;
		try {
			c = Class.forName(ClassUtils.getClassname(Options.I().getMethodSignature()), false, this.classloader);
		} catch (ClassNotFoundException e) {
			// infeasible
			throw new GenerationException("");
		}
		
		Method[] methods = ClassUtils.getClassMethods(c);
		String methodName = ClassUtils.getMethodname(Options.I().getMethodSignature());
		
		// get target method from the list of class' methods
		Method targetMethod = null;
		String meth = methodName.split("\\[")[0];
		String args[] = methodName.split("\\[")[1].replaceAll("\\]", "").split(",");
		if (args.length == 1) {
			args = args[0].equals("") ? new String[0] : args;
		}
		for (Method m : methods) {
			if (m.getName().equals(meth) && m.getParameterTypes().length == args.length) {
				int i;
				for (i = 0; i < args.length; i++) {
					if (!m.getParameterTypes()[i].getCanonicalName().contains(args[i])) {
						break;
					}
				}
				if (i == args.length) {
					targetMethod = m;
					break;
				}
			}
		}
		if (targetMethod == null) {
			throw new GenerationException("Target method not found"); // failed to find method, give up
		}
		
		CompilationUnit cu = new CompilationUnit();
		
		// class name
		TypeDeclaration td = new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, c.getSimpleName() + "_Stub");
		List<TypeDeclaration> types = new ArrayList<TypeDeclaration>();
		types.add(td);
		cu.setTypes(types);
		
		// NUM_SCENARIOS
		VariableDeclarator num_scenarios = new VariableDeclarator(new VariableDeclaratorId(NUM_SCENARIOS), new IntegerLiteralExpr("1"));
		BodyDeclaration num_scenarios_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC, ASTHelper.INT_TYPE, num_scenarios);
		
		List<Expression> dimension = new ArrayList<Expression>();
		dimension.add(ASTHelper.createNameExpr(NUM_SCENARIOS));
		Type targetReturnType = ASTHelper.createReferenceType(targetMethod.getGenericReturnType().toString(), 0); //FIXME
		Type classType = ASTHelper.createReferenceType(c.getSimpleName(), 0);
		
		// stub helper arrays
		ArrayCreationExpr es_ace = new ArrayCreationExpr(classType, dimension, 0);
		VariableDeclarator expected_states = new VariableDeclarator(new VariableDeclaratorId(EXPECTED_STATE), es_ace);
		BodyDeclaration es_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC, ASTHelper.createReferenceType(c.getSimpleName(), 1), expected_states);
		
		ArrayCreationExpr er_ace = new ArrayCreationExpr(targetReturnType, dimension, 0);
		VariableDeclarator expected_results = new VariableDeclarator(new VariableDeclaratorId(EXPECTED_STATE), er_ace);
		BodyDeclaration er_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC, ASTHelper.createReferenceType(targetMethod.getGenericReturnType().toString(), 1), expected_results);
		
		ArrayCreationExpr as_ace = new ArrayCreationExpr(classType, dimension, 0);
		VariableDeclarator actual_states = new VariableDeclarator(new VariableDeclaratorId(ACTUAL_STATE), as_ace);
		BodyDeclaration as_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC, ASTHelper.createReferenceType(c.getSimpleName(), 1), actual_states);
		
		ArrayCreationExpr ar_ace = new ArrayCreationExpr(targetReturnType, dimension, 0);
		VariableDeclarator actual_results = new VariableDeclarator(new VariableDeclaratorId(ACTUAL_STATE), ar_ace);
		BodyDeclaration ar_bd = new FieldDeclaration(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC, ASTHelper.createReferenceType(targetMethod.getGenericReturnType().toString(), 1), actual_results);
		
		List<BodyDeclaration> members = new ArrayList<BodyDeclaration>();
		members.add(num_scenarios_bd);
		members.add(es_bd);
		members.add(er_bd);
		members.add(as_bd);
		members.add(ar_bd);
		
		
		for (Method method : methods) {
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
		
		td.setMembers(members);
		
		System.out.println(cu.toString());
		
	}
	
	private void checkClasspath() {
		logger.debug("Checking classpath");
		checkClasspath(ClassUtils.getClassname(Options.I().getMethodSignature()));
		// TODO: check evosuite jar existance
		logger.debug("Classpath OK");
	}

	private void checkClasspath(final String className) {
		try {
			Class.forName(className, false, this.classloader);
		} catch (ClassNotFoundException e) {
			logger.error("Could not find class under test: " + className);
			throw new GenerationException(e);
		}
	}
	
	private Type getReturnType(Method method) {
		java.lang.reflect.Type returnType = method.getGenericReturnType();
		
		if (returnType.toString().equals("void")) {
			return ASTHelper.createReferenceType(returnType.toString(), 0); //FIXME: check cardinality array
		}
		else {
			return ASTHelper.createReferenceType(returnType.toString(), 1); //FIXME: check cardinality array
		}
	}
	
	private List<Parameter> getParameterType(java.lang.reflect.Type[] parameters) {
		List<Parameter> toReturn = new ArrayList<Parameter>();
		for (int i = 0; i < parameters.length; i++) {
			java.lang.reflect.Type type = parameters[i];
			VariableDeclaratorId id = new VariableDeclaratorId("p" + i);
			Parameter p = new Parameter(ASTHelper.createReferenceType(type.toString(), 0), id); //FIXME: check cardinality array, type erasure, distinguish between primitive and reference types
			toReturn.add(p);
		}
		
		return toReturn;
	}
	
}
