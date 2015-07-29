package sbes.ast.inliner;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.stmt.BlockStmt;

import org.junit.Assert;
import org.junit.Test;

import sbes.ast.renamer.ClassesToMocksRenamer;

public class ClassToMocksInlinerTest {

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

	@Test
	public void test3() throws ParseException {
		BlockStmt block = JavaParser.parseBlock("{boolean boolean0 = v_Stack2.add(new IntegerMock(p0.intValue()));}");

		ClassesToMocksInliner cmi = new ClassesToMocksInliner();
		cmi.visit(block, null);
		while (cmi.isModified()) {
			cmi.reset();
			cmi.visit(block, null);
		}

		Assert.assertEquals("{boolean boolean0 = v_Stack2.add(p0);}".replaceAll("\\s|\t|\n", ""), block.toString().replaceAll("\\s|\t|\n", ""));
	}

	@Test
	public void test4() throws ParseException {
		BlockStmt block = JavaParser.parseBlock("{actual_result = new IntegerMock(integer0.intValue());}");

		ClassesToMocksInliner cmi = new ClassesToMocksInliner();
		cmi.visit(block, null);
		while (cmi.isModified()) {
			cmi.reset();
			cmi.visit(block, null);
		}

		Assert.assertEquals("{actual_result = integer0;}".replaceAll("\\s|\t|\n", ""), block.toString().replaceAll("\\s|\t|\n", ""));
	}

	@Test
	public void test5() throws ParseException {
		BlockStmt block = JavaParser.parseBlock(
				"{"+
				"v_Stack2.insertElementAt(new Integer(p0), (int) new Integer(p0));"+
				"Integer integer1 = v_Stack2.peek();"+
				"v_Stack2.setElementAt(integer1, (int) new Integer(p0));"+
				"actual_result = v_Stack2.pop();"+
				"}");

		new ClassesToMocksRenamer().visit(block, null);
		ClassesToMocksInliner cmi = new ClassesToMocksInliner();
		cmi.visit(block, null);
		while (cmi.isModified()) {
			cmi.reset();
			cmi.visit(block, null);
		}

		Assert.assertEquals("{v_Stack2.insertElementAt(new IntegerMock(p0), p0);IntegerMock integer1 = v_Stack2.peek();v_Stack2.setElementAt(integer1, p0);actual_result = v_Stack2.pop();}".replaceAll("\\s|\t|\n", ""), block.toString().replaceAll("\\s|\t|\n", ""));
	}
	
	@Test
	public void test6() throws ParseException {
		BlockStmt block = JavaParser.parseBlock(
				"{"+
				"boolean boolean0 = v_Stack2.add(new Integer((int) new Integer((int) p0)));"+
				"actual_result = new Integer((int) new Integer((int) p0));"+
				"}");

		new ClassesToMocksRenamer().visit(block, null);
		ClassesToMocksInliner cmi = new ClassesToMocksInliner();
		cmi.visit(block, null);
		while (cmi.isModified()) {
			cmi.reset();
			cmi.visit(block, null);
		}
		System.out.println(block);
//		Assert.assertEquals("{v_Stack2.insertElementAt(new IntegerMock(p0), p0);IntegerMock integer1 = v_Stack2.peek();v_Stack2.setElementAt(integer1, p0);actual_result = v_Stack2.pop();}".replaceAll("\\s|\t|\n", ""), block.toString().replaceAll("\\s|\t|\n", ""));
	}

}
