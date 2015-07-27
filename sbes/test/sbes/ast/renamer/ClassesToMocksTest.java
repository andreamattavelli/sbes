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
		Assert.assertTrue(stmts.toString().contains("IntegerMock "));
		Assert.assertTrue(stmts.toString().contains("IntegerMock("));
		Assert.assertFalse(stmts.toString().contains("Integer "));
		Assert.assertFalse(stmts.toString().contains("Integer("));
	}
	
	@Test
	public void test2() throws ParseException {
		BodyDeclaration body = JavaParser.parseBodyDeclaration(
				"public class Stack_Stub_2 {"+
						"private interface FakeVariable {"+
						"}"+
						"Stack<Integer> v_Stack1;"+
						"Stack<Integer> v_Stack2;"+
						"}");
		
		ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration) body;
		new ClassesToMocksRenamer().visit(cid, null);
		Assert.assertTrue(body.toString().contains("<IntegerMock>"));
		Assert.assertFalse(body.toString().contains("<Integer>"));
	}
	
	@Test
	public void test3() throws ParseException {
		setUp(
				"{"+
				"Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0_0;"+
				"int int0 = 2;"+
				"int int1 = 2;"+
				"Integer integer1 = stack_Stub0.set(int1, integer0);"+
				"Integer integer2 = new Integer(int0);"+
				"stack_Stub0.add((int) integer2, (Integer) int0);"+
				"Integer integer3 = Stack_Stub.ELEMENT_0_0;"+
				"stack_Stub0.set_results(integer3);"+
				"stack_Stub0.method_under_test();"+
				"}");
		
		new ClassesToMocksRenamer().visit(stmts, null);
//		Assert.assertTrue(stmts.toString().contains("IntegerMock "));
//		Assert.assertTrue(stmts.toString().contains("IntegerMock("));
//		Assert.assertFalse(stmts.toString().contains("Integer "));
//		Assert.assertFalse(stmts.toString().contains("Integer("));
		
		System.out.println(stmts.toString());
	}
	
}
