package sbes.scenario;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;

import java.util.ArrayList;

import org.junit.Test;

import sbes.ast.CounterexampleVisitor;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.result.TestScenario;
import sbes.util.ClassUtils;

public class CounterexampleRecusionTest {

	@Test
	public void test1() throws ParseException {
		BlockStmt block = JavaParser.parseBlock("{Stack_Stub_2 stack_Stub_2_0 = new Stack_Stub_2();"+
				"Object object0 = new Object();"+
				"Object[] objectArray0 = new Object[8];"+
				"objectArray0[3] = (Object) 0;"+
				"stack_Stub_2_0.elementData = objectArray0;"+
				"boolean boolean0 = stack_Stub_2_0.add(object0);"+
				"boolean boolean1 = stack_Stub_2_0.add(object0);"+
				"Integer integer0 = new Integer(0);"+
				"boolean boolean2 = stack_Stub_2_0.add((Object) integer0);"+
				"stack_Stub_2_0.method_under_test();}");
		
		Options.I().setMethodSignature("stack.util.java.Stack.peek()");
		
		CarvingResult counterexample = new CarvingResult(block, new ArrayList<ImportDeclaration>());
		
		cleanCounterexample(counterexample);
		TestScenario ts = TestScenarioGenerator.getInstance().carvedTestToScenario(counterexample);
		System.out.println(ts);
	}
	
	private void cleanCounterexample(CarvingResult counterexample) {
		String classname = ClassUtils.getSimpleClassname(Options.I().getMethodSignature());
		CounterexampleVisitor cv = new CounterexampleVisitor();
		cv.visit(counterexample.getBody(), classname);
		
		for (int i = 0; i < counterexample.getBody().getStmts().size(); i++) {
			ExpressionStmt estmt = (ExpressionStmt) counterexample.getBody().getStmts().get(i);
			if (estmt.getExpression() instanceof MethodCallExpr) {
				MethodCallExpr mce = (MethodCallExpr) estmt.getExpression();
				if (mce.getName().equals("method_under_test")) {
					counterexample.getBody().getStmts().remove(i);
					break;
				}
			}
		}
		
		for (int i = 0; i < counterexample.getImports().size(); i++) {
			ImportDeclaration importDecl = counterexample.getImports().get(i);
			if (importDecl.getName().getName().endsWith(classname + "_Stub_2")) {
				counterexample.getImports().remove(importDecl);
				i--;
			}
		}
		
	}
	
}
