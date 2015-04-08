package sbes.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CharLiteralExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.NullLiteralExpr;

import java.util.Vector;

import org.junit.Test;

public class ASTUtilsTest {

	@Test
	public void test() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(boolean.class);
		assertNotNull(e);
		assertEquals(BooleanLiteralExpr.class, e.getClass());
		BooleanLiteralExpr ble = (BooleanLiteralExpr) e;
		assertEquals(false, ble.getValue());
	}
	
	@Test
	public void test2() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(byte.class);
		assertNotNull(e);
		assertEquals(IntegerLiteralExpr.class, e.getClass());
		IntegerLiteralExpr ile = (IntegerLiteralExpr) e;
		assertEquals("0", ile.getValue());
	}
	
	@Test
	public void test3() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(char.class);
		assertNotNull(e);
		assertEquals(CharLiteralExpr.class, e.getClass());
		CharLiteralExpr cle = (CharLiteralExpr) e;
		assertEquals(Character.toString('\u0000'), cle.getValue());
	}
	
	@Test
	public void test4() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(double.class);
		assertNotNull(e);
		assertEquals(DoubleLiteralExpr.class, e.getClass());
		DoubleLiteralExpr dle = (DoubleLiteralExpr) e;
		assertEquals("0.0d", dle.getValue());
	}
	
	@Test
	public void test5() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(float.class);
		assertNotNull(e);
		assertEquals(DoubleLiteralExpr.class, e.getClass());
		DoubleLiteralExpr dle = (DoubleLiteralExpr) e;
		assertEquals("0.0f", dle.getValue());
	}

	@Test
	public void test6() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(int.class);
		assertNotNull(e);
		assertEquals(IntegerLiteralExpr.class, e.getClass());
		IntegerLiteralExpr ile = (IntegerLiteralExpr) e;
		assertEquals("0", ile.getValue());
	}
	
	@Test
	public void test7() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(long.class);
		assertNotNull(e);
		assertEquals(IntegerLiteralExpr.class, e.getClass());
		IntegerLiteralExpr ile = (IntegerLiteralExpr) e;
		assertEquals("0L", ile.getValue());
	}
	
	@Test
	public void test8() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(short.class);
		assertNotNull(e);
		assertEquals(IntegerLiteralExpr.class, e.getClass());
		IntegerLiteralExpr ile = (IntegerLiteralExpr) e;
		assertEquals("0", ile.getValue());
	}
	
	@Test
	public void test9() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(Boolean.class);
		assertNotNull(e);
		assertEquals(BooleanLiteralExpr.class, e.getClass());
		BooleanLiteralExpr ble = (BooleanLiteralExpr) e;
		assertEquals(false, ble.getValue());
	}
	
	@Test
	public void test10() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(Byte.class);
		assertNotNull(e);
		assertEquals(IntegerLiteralExpr.class, e.getClass());
		IntegerLiteralExpr ile = (IntegerLiteralExpr) e;
		assertEquals("0", ile.getValue());
	}
	
	@Test
	public void test11() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(Character.class);
		assertNotNull(e);
		assertEquals(CharLiteralExpr.class, e.getClass());
		CharLiteralExpr cle = (CharLiteralExpr) e;
		assertEquals(Character.toString('\u0000'), cle.getValue());
	}
	
	@Test
	public void test12() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(Double.class);
		assertNotNull(e);
		assertEquals(DoubleLiteralExpr.class, e.getClass());
		DoubleLiteralExpr dle = (DoubleLiteralExpr) e;
		assertEquals("0.0d", dle.getValue());
	}
	
	@Test
	public void test13() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(Float.class);
		assertNotNull(e);
		assertEquals(DoubleLiteralExpr.class, e.getClass());
		DoubleLiteralExpr dle = (DoubleLiteralExpr) e;
		assertEquals("0.0f", dle.getValue());
	}

	@Test
	public void test14() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(Integer.class);
		assertNotNull(e);
		assertEquals(IntegerLiteralExpr.class, e.getClass());
		IntegerLiteralExpr ile = (IntegerLiteralExpr) e;
		assertEquals("0", ile.getValue());
	}
	
	@Test
	public void test15() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(Long.class);
		assertNotNull(e);
		assertEquals(IntegerLiteralExpr.class, e.getClass());
		IntegerLiteralExpr ile = (IntegerLiteralExpr) e;
		assertEquals("0L", ile.getValue());
	}
	
	@Test
	public void test16() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(Short.class);
		assertNotNull(e);
		assertEquals(IntegerLiteralExpr.class, e.getClass());
		IntegerLiteralExpr ile = (IntegerLiteralExpr) e;
		assertEquals("0", ile.getValue());
	}
	
	@Test
	public void test17() {
		Expression e = ASTUtils.getDefaultPrimitiveValue(Vector.class);
		assertNotNull(e);
		assertEquals(NullLiteralExpr.class, e.getClass());
	}
	
}
