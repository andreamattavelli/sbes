package sbes.ast.renamer;

import japa.parser.JavaParser;
import japa.parser.ParseException;
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
		
		System.out.println(stmts);
	}
	
}
