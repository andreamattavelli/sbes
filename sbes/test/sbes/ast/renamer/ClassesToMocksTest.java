package sbes.ast.renamer;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import org.junit.Assert;
import org.junit.Test;

public class ClassesToMocksTest {

	private BlockStmt stmts;
	
	public void setUp(String code) {
		try {
			stmts = JavaParser.parseBlock(code);
		} catch (ParseException e) {
			Assert.fail();
		}
	}
	
	@Test
	public void test() {
		setUp(
				"{"+
				"Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0_0;"+
				"Integer integer1 = new Integer((int) integer0);"+
				"Integer integer2 = new Integer((int) integer1);"+
				"boolean boolean0 = stack_Stub0.add(integer2);"+
				"stack_Stub0.set_results(integer2);"+
				"stack_Stub0.method_under_test();"+
				"}");
		
		new ClassesToMocksRenamer().visit(stmts, null);
	}
	
	@Test
	public void test2() throws ParseException {
		BodyDeclaration body = JavaParser.parseBodyDeclaration(
				"public class Stack_Stub_2 {"+
						"private interface FakeVariable {"+
						"}"+
						"Stack<Integer> v_Stack1;"+
						"Stack<Integer> v_Stack2;"+
						"FakeVariable forceConservativeRepOk;"+
						"FakeVariable forceConservativeRepOk2;"+
						"FakeVariable forceConservativeRepOk3;"+
						"IntegerMock expected_result;"+
						"IntegerMock actual_result;"+
						"Exception e1;"+
						"Exception e2;"+
						"IntegerMock p0;"+
						"@ConservativeRepOk"+
						"boolean mirrorInitialConservative() {"+
						"if (Analysis.isResolved(this, \"v_Stack1\") | Analysis.isResolved(this, \"v_Stack2\"))"+
						"if (v_Stack1 == null ^ v_Stack2 == null)"+
						"return false;"+
						"else if (v_Stack1 != null & v_Stack2 != null)"+
						"return Stack.mirrorInitialConservative(v_Stack1, v_Stack2);"+
						"return true;"+
						"}"+
						"boolean mirrorFinalConservative() {"+
						"if (v_Stack1 == null ^ v_Stack2 == null)"+
						"return false;"+
						"else if (v_Stack1 != null & v_Stack2 != null)"+
						"return Stack.mirrorFinalConservative(v_Stack1, v_Stack2);"+
						"return true;"+
						"}"+
						"public void method_under_test() {"+
						"expected_result = null;"+
						"actual_result = null;"+
						"e1 = null;"+
						"e2 = null;"+
						"try {"+
						"expected_result = v_Stack1.push(p0);"+
						"} catch (Exception e) {"+
						"e1 = e;"+
						"}"+
						"try {"+
						"v_Stack2.addElement(p0);"+
						"actual_result = new IntegerMock(p0.intValue());"+
						"} catch (Exception e) {"+
						"e2 = e;"+
						"}"+
						"boolean ok = mirrorFinalConservative();"+
						"FakeVariable fake = forceConservativeRepOk;"+
						"Analysis.ass3rt(ok);"+
						"if (expected_result != null)"+
						"ok = expected_result.equals(actual_result);"+
						"else"+
						"ok = actual_result == null;"+
						"FakeVariable fake2 = forceConservativeRepOk2;"+
						"Analysis.ass3rt(ok);"+
						"if (e1 == null ^ e2 == null)"+
						"ok = false;"+
						"FakeVariable fake3 = forceConservativeRepOk3;"+
						"Analysis.ass3rt(ok);"+
						"}"+
						"}");
		
		ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration) body;
		new ClassesToMocksRenamer().visit(cid, null);
		System.out.println(cid);
	}
	
}
