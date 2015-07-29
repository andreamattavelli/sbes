package sbes.ast.inliner;

import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.stmt.BlockStmt;

import org.junit.Assert;
import org.junit.Test;

public class InlinerTest {

	@Test
	public void test() throws ParseException {
		BlockStmt block = JavaParser.parseBlock("{"+
				 "IntegerMock integer0 = v_Stack2.peek();"+
				 "actual_result = integer0;"+
				"}");
		
		Inliner inliner = new Inliner();
		VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId("integer0"));
		vd.setInit(new MethodCallExpr(ASTHelper.createNameExpr("v_Stack2"), "peek"));
		inliner.visit(block, vd);
		
		Assert.assertEquals("{IntegerMock integer0 = v_Stack2.peek();actual_result = v_Stack2.peek();}".replaceAll("\\s|\t|\n", ""), 
							block.toString().replaceAll("\\s|\t|\n", ""));
	}
	
}
