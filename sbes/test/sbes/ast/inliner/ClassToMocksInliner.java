package sbes.ast.inliner;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.stmt.BlockStmt;

import org.junit.Assert;
import org.junit.Test;

public class ClassToMocksInliner {

	@Test
	public void test() throws ParseException {
		BlockStmt block = JavaParser.parseBlock("{new IntegerMock(new IntegerMock(new IntegerMock(p0.intValue()).intValue()).intValue());}");
		
		ClassesToMocksInliner cmi = new ClassesToMocksInliner();
		cmi.visit(block, null);
		 while (cmi.isModified()) {
			 cmi.reset();
			 cmi.visit(block, null);
		}
		
		Assert.assertEquals("{new IntegerMock(p0.intValue());}".replaceAll("\\s|\t|\n", ""), block.toString().replaceAll("\\s|\t|\n", ""));
	}
	
	@Test
	public void test2() throws ParseException {
		BlockStmt block = JavaParser.parseBlock("{IntegerMock i = new IntegerMock(new IntegerMock(new IntegerMock(p0.intValue()).intValue()).intValue());}");
		
		ClassesToMocksInliner cmi = new ClassesToMocksInliner();
		cmi.visit(block, null);
		 while (cmi.isModified()) {
			 cmi.reset();
			 cmi.visit(block, null);
		}
		
		Assert.assertEquals("{IntegerMock i = p0;}".replaceAll("\\s|\t|\n", ""), block.toString().replaceAll("\\s|\t|\n", ""));
	}
	
}
