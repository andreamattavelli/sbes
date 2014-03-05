package sbes.stub.generator;

import japa.parser.ASTHelper;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.stmt.ExpressionStmt;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.stub.Stub;
import sbes.util.ClassUtils;

public class SecondStageGenericStubGenerator extends SecondStageStubGenerator {

	private String concreteClass;
	
	public SecondStageGenericStubGenerator(Stub stub, CarvingResult candidateES, String concreteClass) {
		super(stub, candidateES);
		this.concreteClass = concreteClass;
	}
	
	@Override
	protected ExpressionStmt createCloneObj(Method targetMethod) {
		List<Expression> methodParameters = new ArrayList<Expression>();
		methodParameters.add(new ThisExpr());
		Expression right = new MethodCallExpr(ASTHelper.createNameExpr("c"), "deepClone", methodParameters);
		List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
		vars.add(new VariableDeclarator(new VariableDeclaratorId("clone")));
		String className = ClassUtils.getSimpleClassname(Options.I().getMethodSignature()) + "<" + concreteClass + ">";
		Expression left = new VariableDeclarationExpr(ASTHelper.createReferenceType(className, 0), vars);
		AssignExpr assignment = new AssignExpr(left, right, Operator.assign);
		return new ExpressionStmt(assignment);
	}

}
