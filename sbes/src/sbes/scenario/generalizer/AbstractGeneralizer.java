package sbes.scenario.generalizer;

import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sbes.ast.ExtractValuesFromTargetMethodVisitor;
import sbes.ast.ExtractVariablesFromTargetMethodVisitor;
import sbes.ast.renamer.InputFieldRenamer;

public abstract class AbstractGeneralizer {

	protected static List<FieldDeclaration> extractParametersToInputs(BlockStmt cloned, String methodName, Method targetMethod, int index) {
		List<String> varsToExtract = new ArrayList<String>();
		List<VariableDeclarationExpr> varsToField = new ArrayList<VariableDeclarationExpr>();
		List<FieldDeclaration> fields = new ArrayList<FieldDeclaration>();
		
		// extract dependencies from target method
		ExtractValuesFromTargetMethodVisitor evmv = new ExtractValuesFromTargetMethodVisitor(index, targetMethod);
		evmv.visit(cloned, methodName);
		fields.addAll(evmv.getFields());
		
		ExtractVariablesFromTargetMethodVisitor epv = new ExtractVariablesFromTargetMethodVisitor();
		epv.visit(cloned, methodName);
		varsToExtract.addAll(epv.getDependencies());
		
		// recursively extract all variable dependencies
		while (!varsToExtract.isEmpty()) {
			String variableId = varsToExtract.remove(0);
			for (int i = 0; i < cloned.getStmts().size(); i++) {
				ExpressionStmt estmt = (ExpressionStmt) cloned.getStmts().get(i);
				if (estmt.getExpression() instanceof VariableDeclarationExpr) {
					VariableDeclarationExpr vde = (VariableDeclarationExpr) estmt.getExpression();
					VariableDeclarator vd = vde.getVars().get(0);
					if (vd.getId().getName().equals(variableId)) {
						varsToField.add(vde);
						if (vd.getInit() instanceof MethodCallExpr || vd.getInit() instanceof ObjectCreationExpr) {
							varsToExtract.addAll(extractMethodDependencies(vd.getInit()));
						}
						cloned.getStmts().remove(i);
						i--;
					}
				}
			}
		}
		
		Collections.reverse(varsToField);

		Map<String, String> varMap = new HashMap<String, String>();
		for (int i = 0; i < varsToField.size(); i++) {
			// substitute name
			VariableDeclarationExpr vde = varsToField.get(i);
			String newName = "ELEMENT_" + index + "_" + fields.size();
			varMap.put(vde.getVars().get(0).getId().getName(), newName);
			vde.getVars().get(0).getId().setName(newName);

			// substitute dependency names
			InputFieldRenamer snv = new InputFieldRenamer();
			snv.visit(vde, varMap);

			// create field
			FieldDeclaration fd = new FieldDeclaration(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, vde.getType(), vde.getVars());
			fields.add(fd);
		}
		// substitute dependency names
		InputFieldRenamer snv = new InputFieldRenamer();
		snv.visit(cloned, varMap);
		
		return fields;
	}
	
	protected static List<String> extractMethodDependencies(Expression init) {
		List<String> dependencies = new ArrayList<String>();
		List<Expression> args = new ArrayList<Expression>();
		if (init instanceof MethodCallExpr) {
			MethodCallExpr mce = (MethodCallExpr) init;
			if (mce.getScope() != null) {
				args.add(mce.getScope());
			}
			if (mce.getArgs() != null) {
				args.addAll(mce.getArgs());
			}
		}
		else if (init instanceof ObjectCreationExpr) {
			ObjectCreationExpr oce = (ObjectCreationExpr) init;
			if (oce.getArgs() != null) {
				args.addAll(oce.getArgs());
			}
		}
		else if (init instanceof CastExpr) {
			
		}
		for (Expression expression : args) {
			if (expression instanceof NameExpr) {
				dependencies.add(((NameExpr)expression).getName());
			}
			else if (expression instanceof CastExpr) {
				CastExpr ce = (CastExpr) expression;
				if (ce.getExpr() instanceof NameExpr) {
					dependencies.add(((NameExpr)ce.getExpr()).getName());
				}
			}
		}
		return dependencies;
	}

	protected static String getConcreteClass(String className, List<String> concreteClasses) {
		if (concreteClasses != null && concreteClasses.size() > 0) {
			return className + "<" + concreteClasses.toString().replace("[", "").replace("]", "") + ">";
		}
		else {
			return className;
		}
	}
	
}
