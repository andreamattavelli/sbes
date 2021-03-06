package sbes.stub.generator.second;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.Comment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;

import java.io.IOException;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import sbes.logging.Level;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.Stub;
import sbes.testcase.Compilation;
import sbes.testcase.CompilationContext;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SecondStageStubGeneratorTest {
	
	private List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
	private Stub stub;
	
	private void setUp(String classesPath, String methodSignature, String stubName) {
		Options.I().setClassesPath(classesPath);
		Options.I().setTargetMethod(methodSignature);
		Options.I().setLogLevel(Level.ERROR);
		Options.I().setDontResolveGenerics(false);
		List<TypeDeclaration> decls = new ArrayList<TypeDeclaration>(); 
		List<Comment> comments = new ArrayList<Comment>();
		stub = new Stub(new CompilationUnit(new PackageDeclaration(new NameExpr("foo")), imports, decls, comments), stubName);
	}
	
	protected void assertASTEquals(String actual, String expected) {
		assertEquals(expected.replaceAll("\\s|\t|\n", ""), actual.replaceAll("\\s|\t|\n", ""));
	}
	
	protected void assertCompiles(String packagename, String filename, String classesPath) {
		CompilationContext compilationContext = new CompilationContext(
				"./test/resources/compilation/" + packagename, 
				filename + ".java", 
				"./test/resources/compilation", 
				classesPath);
		
		boolean compilationSucceeded = Compilation.compile(compilationContext);
		assertTrue(compilationSucceeded);
	}
	
	@AfterClass
	public static void tearDown() {
		try {
			Path directory = Paths.get("./test/resources/compilation");
			if (!directory.toFile().exists()) {
				return;
			}
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test01() throws ParseException {
		setUp("./bin", "stack.util.Stack.push(Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{" + 
				"Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0;"+
				"stack_Stub0.addElement(integer0);"+
				"Integer[] integerArray0 = new Integer[1];"+
				"integerArray0[0] = integer0;"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();" + 
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
		"import sbes.distance.Distance;"+
		"import sbes.cloning.Cloner;"+
		"public class Stack_Stub_2 extends Stack<Integer> {"+
		"public Stack_Stub_2() {"+
		"super();"+
		"}"+
		"public void method_under_test(Integer p0) {"+
		"Cloner c = new Cloner();"+
		"Stack<Integer> clone = c.deepClone(this);"+
		"Integer expected_result = this.push(p0);"+
		"clone.addElement(p0);"+
		"Integer actual_result = p0;"+
		"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
		"System.out.println(\"Executed\");"+
		"}"+
		"}";
		assertASTEquals(actual, expected);
	}

	@Test
	public void test02() throws ParseException {
		setUp("./bin", "stack.util.Stack.push(Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{" +
				"Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer[] integerArray0 = new Integer[4];"+
				"Integer integer0 = Stack_Stub.ELEMENT_0;"+
				"integerArray0[0] = integer0;"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.addElement(integer0);"+
				"stack_Stub0.method_under_test();"+
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(Integer p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.push(p0);"+
				"clone.addElement(p0);"+
				"Integer actual_result = p0;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}

	@Test
	public void test03() throws ParseException {
		setUp("./bin", "stack.util.Stack.push(Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0;"+
				"boolean[] booleanArray0 = stack_Stub0.add(integer0);"+
				"Integer[] integerArray0 = new Integer[3];"+
				"integerArray0[0] = integer0;"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();"+
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(Integer p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.push(p0);"+
				"clone.add(p0);"+
				"Integer actual_result = p0;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test04() throws ParseException {
		setUp("./bin", "stack.util.Stack.push(Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0;"+
				"Integer[] integerArray0 = new Integer[5];"+
				"integerArray0[0] = integer0;"+
				"integerArray0[3] = integer0;"+
				"stack_Stub0.addElement(integerArray0[3]);"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(Integer p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.push(p0);"+
				"clone.addElement(p0);"+
				"Integer actual_result = p0;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test05() throws ParseException {
		setUp("./test/resources/gs-core-1.2.jar", "org.graphstream.graph.implementations.AbstractEdge.getNode0()", "AbstractEdge_Stub");

		BlockStmt body = JavaParser.parseBlock("{AbstractEdge_Stub abstractEdge_Stub0 = new AbstractEdge_Stub();"+
				"Node[] nodeArray0 = abstractEdge_Stub0.getSourceNode();"+
				"abstractEdge_Stub0.set_results(nodeArray0);"+
				"abstractEdge_Stub0.method_under_test();}");

		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("org.graphstream.graph.implementations.AbstractEdge"), false, false));
		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(new ArrayList<TestScenario>(), stub, candidateES);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("org/graphstream/graph/implementations", second.getStubName(), "./test/resources/gs-core-1.2.jar:./bin:./bin");
		
		String actual = second.getAst().toString();
		String expected = "package org.graphstream.graph.implementations;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import org.graphstream.graph.implementations.AbstractEdge;"+
				"public class AbstractEdge_Stub_2 extends AbstractEdge {"+
				"protected AbstractEdge_Stub_2(java.lang.String p0, org.graphstream.graph.implementations.AbstractNode p1, org.graphstream.graph.implementations.AbstractNode p2, boolean p3) {"+
				"super(p0, p1, p2, p3);"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"AbstractEdge clone = c.deepClone(this);"+
				"org.graphstream.graph.Node expected_result = this.getNode0();"+
				"org.graphstream.graph.Node actual_result = clone.getSourceNode();"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test06() throws ParseException {
		setUp("./bin", "stack.util.Stack.push(Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+ 
				"Integer integer0 = new Integer(0);"+ 
				"LinkedList<Integer> linkedList0 = new LinkedList<Integer>();"+ 
				"boolean[] booleanArray0 = stack_Stub0.retainAll(linkedList0);"+ 
				"Integer[] integerArray0 = new Integer[9];"+ 
				"integerArray0[0] = integer0;"+ 
				"stack_Stub0.set_results(integerArray0);"+ 
				"stack_Stub0.method_under_test();}");

		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.LinkedList"), false, false));
		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import java.util.LinkedList;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(Integer p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.push(p0);"+
				"LinkedList<Integer> linkedList0 = new LinkedList<Integer>();"+
				"clone.retainAll(linkedList0);"+
				"Integer actual_result = new Integer(0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test07() throws ParseException {
		setUp("./bin", "stack.util.Stack.firstElement()", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"int[] intArray0 = new int[5];"+
				"Integer[] integerArray0 = stack_Stub0.elementAt(intArray0);"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.firstElement();"+
				"Integer actual_result = clone.elementAt(0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test08() throws ParseException {
		setUp("./bin", "stack.util.Stack.firstElement()", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"int[] intArray0 = new int[10];"+
				"Integer[] integerArray0 = stack_Stub0.get(intArray0);"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.firstElement();"+
				"Integer actual_result = clone.get(0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test09() throws ParseException {
		setUp("./bin", "stack.util.Stack.peek()", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Integer.getInteger(\"1I21yRrt\");"+
				"Integer[] integerArray0 = new Integer[5];"+
				"integerArray0[0] = null;"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.peek();"+
				"Integer actual_result = null;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test10() throws ParseException {
		setUp("./bin", "stack.util.Stack.peek()", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+ 
											"Integer[] integerArray0 = new Integer[7];"+ 
											"Integer integer0 = Integer.valueOf(0);"+ 
											"integerArray0[0] = integer0;"+ 
											"stack_Stub0.set_results(integerArray0);"+ 
											"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.peek();"+
				"Integer actual_result = Integer.valueOf(0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test11() throws ParseException {
		setUp("./bin", "stack.util.Stack.peek()", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+ 
				"int[] intArray0 = new int[4];"+ 
				"Integer[] integerArray0 = stack_Stub0.remove(intArray0);"+ 
				"Integer[] integerArray1 = stack_Stub0.push((Integer) 0);"+ 
				"stack_Stub0.set_results(integerArray0);"+ 
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.peek();"+
				"Integer actual_result = clone.remove(0);"+
				"clone.push((Integer) 0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test12() throws ParseException {
		setUp("./bin", "stack.util.Stack.peek()", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"LinkedList<Integer> linkedList0 = new LinkedList<Integer>();"+
				"boolean[] booleanArray0 = stack_Stub0.retainAll(linkedList0);"+
				"Integer[] integerArray0 = stack_Stub0.push((Integer) 0);"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();}");

		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.LinkedList"), false, false));
		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import java.util.LinkedList;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.peek();"+
				"LinkedList<Integer> linkedList0 = new LinkedList<Integer>();"+
				"clone.retainAll(linkedList0);"+
				"Integer actual_result = clone.push((Integer) 0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test13() throws ParseException {
		setUp("./test/resources/gs-core-1.2.jar", "org.graphstream.graph.implementations.AbstractEdge.addAttribute(String,Object)", "AbstractEdge_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{AbstractEdge_Stub abstractEdge_Stub0 = new AbstractEdge_Stub();"+ 
				"Integer integer0 = new Integer(325);"+ 
				"Object[] objectArray0 = new Object[1];"+ 
				"objectArray0[0] = (Object) integer0;"+ 
				"abstractEdge_Stub0.changeAttribute(\"value\", objectArray0);"+ 
				"abstractEdge_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(new ArrayList<TestScenario>(), stub, candidateES);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("org/graphstream/graph/implementations", second.getStubName(), "./test/resources/gs-core-1.2.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = "package org.graphstream.graph.implementations;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class AbstractEdge_Stub_2 extends AbstractEdge {"+
				"protected AbstractEdge_Stub_2(java.lang.String p0, org.graphstream.graph.implementations.AbstractNode p1, org.graphstream.graph.implementations.AbstractNode p2, boolean p3) {"+
				"super(p0, p1, p2, p3);"+
				"}"+
				"public void method_under_test(java.lang.String p0, java.lang.Object[] p1) {"+
				"Cloner c = new Cloner();"+
				"AbstractEdge clone = c.deepClone(this);"+
				"this.addAttribute(p0, p1);"+
				"Object[] objectArray0 = new Object[1];"+
				"clone.changeAttribute(\"value\", objectArray0);"+
				"if (Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test14() throws ParseException {
		setUp("./bin", "stack.util.Stack.clear()", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"LinkedList<Object> linkedList0 = new LinkedList<Object>();"+
				"boolean[] booleanArray0 = stack_Stub0.retainAll(linkedList0);"+
				"stack_Stub0.method_under_test();}");

		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.LinkedList"), false, false));
		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import java.util.LinkedList;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"this.clear();"+
				"LinkedList<Object> linkedList0 = new LinkedList<Object>();"+
				"clone.retainAll(linkedList0);"+
				"if (Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test15() throws ParseException {
		setUp("./bin", "stack.util.Stack.clear()", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"int int0 = Integer.rotateRight(1, (-1));"+
				"int[] intArray0 = new int[2];"+
				"intArray0[0] = 2;"+
				"stack_Stub0.removeElementAt(intArray0);"+
				"boolean[] booleanArray0 = stack_Stub0.add((Integer) 2);"+
				"Integer[] integerArray0 = stack_Stub0.elementAt(intArray0);"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"this.clear();"+
				"clone.removeElementAt(2);"+
				"clone.add((Integer) 2);"+
				"clone.elementAt(2);"+
				"if (Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}

	@Test
	public void test16() throws ParseException {
		setUp("./bin", "stack.util.Stack.remove(int)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer[] integerArray0 = stack_Stub0.pop();"+
				"Integer[] integerArray1 = stack_Stub0.pop();"+
				"stack_Stub0.addElement((Integer) 2);"+
				"stack_Stub0.set_results(integerArray1);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(int p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.remove(p0);"+
				"clone.pop();"+
				"Integer actual_result = clone.pop();"+
				"clone.addElement((Integer) 2);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test17() throws ParseException {
		setUp("./bin", "stack.util.Stack.get(int)", "Stack_Stub");

		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"int int0 = Stack_Stub.ELEMENT_0_0;"+
				"Integer integer0 = stack_Stub0.remove(int0);"+
				"stack_Stub0.insertElementAt((Integer) int0, int0);"+
				"Integer integer1 = stack_Stub0.set(int0, integer0);"+
				"stack_Stub0.set_results(integer0);"+
				"stack_Stub0.method_under_test();"+
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(int p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.get(p0);"+
				"Integer actual_result = clone.remove(p0);"+
				"clone.insertElementAt( p0, p0);"+ //FIXME: clone.insertElementAt((Integer) p0, p0);
				"clone.set(p0, actual_result);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test18() throws ParseException {
		setUp("./bin", "stack.util.Stack.elementAt(int)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = new Integer(55);"+
				"Integer[] integerArray0 = stack_Stub0.push(integer0);"+
				"stack_Stub0.set_results(integerArray0);"+
				"Integer[] integerArray1 = stack_Stub0.pop();"+
				"stack_Stub0.method_under_test();"+
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(int p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.elementAt(p0);"+
				"Integer actual_result = clone.push(new Integer(55));"+
				"clone.pop();"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test19() throws ParseException {
		setUp("./bin", "stack.util.Stack.elementAt(int)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0_0;"+
				"Integer[] integerArray0 = new Integer[2];"+
				"Integer integer1 = Stack_Stub.ELEMENT_1_0;"+
				"integerArray0[1] = integer1;"+
				"integerArray0[0] = integer0;"+
				"Integer[] integerArray1 = stack_Stub0.get(integerArray0);"+
				"stack_Stub0.set_results(integerArray1);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(Integer p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.elementAt(p0);"+
				"Integer actual_result = clone.get(p0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test20() throws ParseException {
		setUp("./test/resources/gs-core-1.2.jar", "org.graphstream.graph.implementations.AbstractEdge.addAttribute(String,Object)", "AbstractEdge_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{AbstractEdge_Stub abstractEdge_Stub0 = new AbstractEdge_Stub();"+
				"String string0 = AbstractEdge_Stub.ELEMENT_0_0;"+
				"Object[] objectArray0 = AbstractEdge_Stub.ELEMENT_0_1;"+
				"abstractEdge_Stub0.changeAttribute(string0, objectArray0);"+
				"abstractEdge_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(new ArrayList<TestScenario>(), stub, candidateES);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("org/graphstream/graph/implementations", second.getStubName(), "./test/resources/gs-core-1.2.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package org.graphstream.graph.implementations;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class AbstractEdge_Stub_2 extends AbstractEdge {"+
				"protected AbstractEdge_Stub_2(java.lang.String p0, org.graphstream.graph.implementations.AbstractNode p1, org.graphstream.graph.implementations.AbstractNode p2, boolean p3) {"+
				"super(p0, p1, p2, p3);"+
				"}"+
				"public void method_under_test(java.lang.String p0, java.lang.Object[] p1) {"+
				"Cloner c = new Cloner();"+
				"AbstractEdge clone = c.deepClone(this);"+
				"this.addAttribute(p0, p1);"+
				"clone.changeAttribute(p0, p1);"+
				"if (Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test21() throws ParseException {
		setUp("./bin", "stack.util.Stack.push(Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0_0;"+
				"boolean boolean0 = stack_Stub0.add(integer0);"+
				"stack_Stub0.set_results(integer0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(Integer p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.push(p0);"+
				"clone.add(p0);"+
				"Integer actual_result = p0;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test22() throws ParseException {
		setUp("./bin", "stack.util.Stack.add(int,Object)", "Stack_Stub");

		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0_1;"+
				"LinkedList<Object> linkedList0 = new LinkedList<Object>();"+
				"boolean boolean0 = linkedList0.offerLast((Object) integer0);"+
				"int int0 = Stack_Stub.ELEMENT_0_0;"+
				"boolean[] booleanArray0 = stack_Stub0.addAll(int0, (Collection) linkedList0);"+
				"stack_Stub0.method_under_test();}");

		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.LinkedList"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.Collection"), false, false));
		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import java.util.LinkedList;"+
				"import java.util.Collection;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(int p0, Integer p1) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"this.add(p0, p1);"+
				"LinkedList<Object> linkedList0 = new LinkedList<Object>();"+
				"boolean boolean0 = linkedList0.offerLast((Object) p1);"+
				"clone.addAll(p0, (Collection) linkedList0);"+
				"if (Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test23() throws ParseException {
		setUp("./bin", "stack.util.Stack.push(Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0_0;"+
				"stack_Stub0.addElement(integer0);"+
				"Integer[] integerArray0 = new Integer[4];"+
				"integerArray0[0] = integer0;"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(Integer p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.push(p0);"+
				"clone.addElement(p0);"+
				"Integer actual_result = p0;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test24() throws ParseException {
		setUp("./test/resources/gs-core-1.2.jar", "org.graphstream.graph.Path.getEdgeCount()", "Path_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Path_Stub path_Stub0 = new Path_Stub();"+
				"AbstractEdge abstractEdge0 = (AbstractEdge)path_Stub0.popEdge();"+
				"path_Stub0.add((Edge) abstractEdge0);"+
				"SingleNode singleNode0 = (SingleNode)path_Stub0.popNode();"+
				"path_Stub0.add((Edge) abstractEdge0);"+
				"short short0 = ObjectStreamConstants.STREAM_VERSION;"+
				"path_Stub0.set_results((int) short0);"+
				"path_Stub0.method_under_test();}");

		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("org.graphstream.graph.implementations.AbstractEdge"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("org.graphstream.graph.implementations.SingleNode"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.io.ObjectStreamConstants"), false, false));
		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(new ArrayList<TestScenario>(), stub, candidateES);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("org/graphstream/graph", second.getStubName(), "./test/resources/gs-core-1.2.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package org.graphstream.graph;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import org.graphstream.graph.implementations.AbstractEdge;"+
				"import org.graphstream.graph.implementations.SingleNode;"+
				"import java.io.ObjectStreamConstants;"+
				"public class Path_Stub_2 extends Path {"+
				"public Path_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"Path clone = c.deepClone(this);"+
				"int expected_result = this.getEdgeCount();"+
				"AbstractEdge abstractEdge0 = (AbstractEdge) clone.popEdge();"+
				"clone.add((Edge) abstractEdge0);"+
				"SingleNode singleNode0 = (SingleNode) clone.popNode();"+
				"clone.add((Edge) abstractEdge0);"+
				"int actual_result = ObjectStreamConstants.STREAM_VERSION;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test25() throws ParseException {
		setUp("./test/resources/gs-core-1.2.jar", "org.graphstream.ui.geom.Vector2.x()", "Vector2_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Vector2_Stub vector2_Stub0 = new Vector2_Stub();"+
				"Point2 point2_0 = new Point2();"+
				"vector2_Stub0.scalarDiv(point2_0.x);"+
				"double[] doubleArray0 = vector2_Stub0.normalize();"+
				"vector2_Stub0.set_results(doubleArray0);"+
				"vector2_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(new ArrayList<TestScenario>(), stub, candidateES);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("org/graphstream/ui/geom", second.getStubName(), "./test/resources/gs-core-1.2.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package org.graphstream.ui.geom;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Vector2_Stub_2 extends Vector2 {"+
				"public Vector2_Stub_2(org.graphstream.ui.geom.Point2 p0, org.graphstream.ui.geom.Point2 p1) {"+
				"super(p0, p1);"+
				"}"+
				"public Vector2_Stub_2(org.graphstream.ui.geom.Point2 p0) {"+
				"super(p0);"+
				"}"+
				"public Vector2_Stub_2(org.graphstream.ui.geom.Vector2 p0) {"+
				"super(p0);"+
				"}"+
				"public Vector2_Stub_2(double p0, double p1) {"+
				"super(p0, p1);"+
				"}"+
				"public Vector2_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"Vector2 clone = c.deepClone(this);"+
				"double expected_result = this.x();"+
				"Point2 point2_0 = new Point2();"+
				"clone.scalarDiv(point2_0.x);"+
				"double actual_result = clone.normalize();"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test27() throws ParseException {
		setUp("./bin", "stack.util.Stack.remove(int)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"int int0 = Stack_Stub.ELEMENT_0_0;"+
				"Integer integer0 = stack_Stub0.get(int0);"+
				"Integer integer1 = new Integer((int) integer0);"+
				"stack_Stub0.addElement(integer1);"+
				"Integer integer2 = stack_Stub0.pop();"+
				"int int1 = Stack_Stub.ELEMENT_0_0;"+
				"stack_Stub0.removeElementAt(int1);"+
				"stack_Stub0.set_results(integer1);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(int p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.remove(p0);"+
				"Integer integer0 = clone.get(p0);"+
				"clone.addElement(new Integer((int) integer0));"+
				"clone.pop();"+
				"clone.removeElementAt(p0);"+
				"Integer actual_result = new Integer((int) integer0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test28() throws ParseException {
		setUp("./test/resources/gs-core-1.2.jar", "org.graphstream.graph.implementations.AbstractEdge.addAttribute(String,Object)", "AbstractEdge_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{AbstractEdge_Stub abstractEdge_Stub0 = new AbstractEdge_Stub();"+
				"String string0 = AbstractEdge_Stub.ELEMENT_0_0;"+
				"Integer integer0 = (Integer)abstractEdge_Stub0.getAttribute(string0);"+
				"abstractEdge_Stub0.set_results((Object) integer0);"+
				"abstractEdge_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(new ArrayList<TestScenario>(), stub, candidateES);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("org/graphstream/graph/implementations", second.getStubName(), "./test/resources/gs-core-1.2.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package org.graphstream.graph.implementations;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class AbstractEdge_Stub_2 extends AbstractEdge {"+
				"protected AbstractEdge_Stub_2(java.lang.String p0, org.graphstream.graph.implementations.AbstractNode p1, org.graphstream.graph.implementations.AbstractNode p2, boolean p3) {"+
				"super(p0, p1, p2, p3);"+
				"}"+
				"public void method_under_test(java.lang.String p0, java.lang.Object[] p1) {"+
				"Cloner c = new Cloner();"+
				"AbstractEdge clone = c.deepClone(this);"+
				"this.addAttribute(p0, p1);"+
				"Integer integer0 = (Integer) clone.getAttribute(p0);"+
				"if (Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test29() throws ParseException {
		setUp("./test/resources/gs-core-1.2.jar", "org.graphstream.graph.implementations.AbstractEdge.addAttribute(String,Object)", "AbstractEdge_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{AbstractEdge_Stub abstractEdge_Stub0 = new AbstractEdge_Stub();"+
				"String string0 = AbstractEdge_Stub.ELEMENT_0_0;"+
				"Integer integer0 = (Integer)abstractEdge_Stub0.getAttribute(string0);"+
				"abstractEdge_Stub0.set_results((Object) integer0);"+
				"abstractEdge_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(new ArrayList<TestScenario>(), stub, candidateES);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("org/graphstream/graph/implementations", second.getStubName(), "./test/resources/gs-core-1.2.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package org.graphstream.graph.implementations;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class AbstractEdge_Stub_2 extends AbstractEdge {"+
				"protected AbstractEdge_Stub_2(java.lang.String p0, org.graphstream.graph.implementations.AbstractNode p1, org.graphstream.graph.implementations.AbstractNode p2, boolean p3) {"+
				"super(p0, p1, p2, p3);"+
				"}"+
				"public void method_under_test(java.lang.String p0, java.lang.Object[] p1) {"+
				"Cloner c = new Cloner();"+
				"AbstractEdge clone = c.deepClone(this);"+
				"this.addAttribute(p0, p1);"+
				"Integer integer0 = (Integer) clone.getAttribute(p0);"+
				"if (Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test30() throws ParseException {
		setUp("./bin", "stack.util.Stack.add(Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0_0;"+
				"stack_Stub0.addElement(integer0);"+
				"boolean boolean0 = true;"+
				"stack_Stub0.set_results(boolean0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(Integer p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"boolean expected_result = this.add(p0);"+
				"clone.addElement(p0);"+
				"boolean actual_result = true;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test31() throws ParseException {
		setUp("./bin", "stack.util.Stack.set(int,Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0_1;"+
				"Integer integer1 = stack_Stub0.lastElement();"+
				"int int0 = 55;"+
				"int int1 = Stack_Stub.ELEMENT_0_0;"+
				"stack_Stub0.setSize(int1);"+
				"stack_Stub0.addElement(integer0);"+
				"stack_Stub0.addElement(integer1);"+
				"stack_Stub0.set_results((Integer) int0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(int p0, Integer p1) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.set(p0, p1);"+
				"Integer integer1 = clone.lastElement();"+
				"clone.setSize(p0);"+
				"clone.addElement(p1);"+
				"clone.addElement(integer1);"+
				"Integer actual_result = 55;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}

	@Test
	public void test32() throws ParseException {
		setUp("./bin", "stack.util.Stack.pop()", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer[] integerArray0 = stack_Stub0.lastElement();"+
				"Integer[] integerArray1 = stack_Stub0.remove(integerArray0);"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.pop();"+
				"Integer actual_result = clone.lastElement();"+
				"clone.remove(actual_result);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test33() throws ParseException {
		setUp("./bin", "stack.util.Stack.get(int)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"int int0 = (-2);"+
				"stack_Stub0.set_results((Integer) int0);"+
				"stack_Stub0.method_under_test();"+
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(int p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"Integer expected_result = this.get(p0);"+
				"Integer actual_result = -2;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test34() throws ParseException {
		setUp("./bin", "stack.util.Stack.remove(Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0_0;"+
				"boolean boolean0 = stack_Stub0.add(integer0);"+
				"boolean boolean1 = stack_Stub0.removeElement(integer0);"+
				"Integer integer1 = stack_Stub0.pop();"+
				"stack_Stub0.set_results(boolean0);"+
				"stack_Stub0.method_under_test();"+
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 extends Stack<Integer> {"+
				"public Stack_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(Integer p0) {"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(this);"+
				"boolean expected_result = this.remove(p0);"+
				"boolean actual_result = clone.add(p0);"+
				"clone.removeElement(p0);"+
				"clone.pop();"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test35() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.put(Object,Object)", "ArrayListMultimap_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap_Stub arrayListMultimap_Stub0 = new ArrayListMultimap_Stub();"+
				"Integer integer0 = ArrayListMultimap_Stub.ELEMENT_0_1;"+
				"ArrayDeque<LinkedList<BoundType>> arrayDeque0 = new ArrayDeque<LinkedList<BoundType>>();"+
				"LinkedList<BoundType> linkedList0 = new LinkedList<BoundType>();"+
				"boolean boolean0 = arrayDeque0.add(linkedList0);"+
				"ArrayDeque<String> arrayDeque1 = new ArrayDeque<String>();"+
				"String string0 = ArrayListMultimap_Stub.ELEMENT_0_0;"+
				"boolean boolean1 = arrayDeque1.offerLast(string0);"+
				"List<String> list0 = arrayListMultimap_Stub0.replaceValues(integer0, (Iterable<? extends String>) arrayDeque1);"+
				"arrayListMultimap_Stub0.set_results(boolean0);"+
				"arrayListMultimap_Stub0.method_under_test();"+
				"}");

		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.ArrayDeque"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.LinkedList"), false, false));
		
		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
		TypeVariable<?> v = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
		genericToConcrete.put(k, "Integer");
		genericToConcrete.put(v, "String");
		SecondStageGeneratorStubWithGenerics sssg = new SecondStageGeneratorStubWithGenerics(new ArrayList<TestScenario>(), 
													stub, candidateES, genericToConcrete);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");

		String actual = second.getAst().toString();
		String expected = 
				"package com.google.common.collect;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import java.util.ArrayDeque;"+
				"import java.util.LinkedList;"+
				"public class ArrayListMultimap_Stub_2 extends ArrayListMultimap<Integer, String> {"+
				"protected ArrayListMultimap_Stub_2(){super();}"+
				"public void method_under_test(Integer p0, String p1) {"+
				"Cloner c = new Cloner();"+
				"ArrayListMultimap<Integer, String> clone = c.deepClone(this);"+
				"boolean expected_result = this.put(p0, p1);"+
				"ArrayDeque<LinkedList<BoundType>> arrayDeque0 = new ArrayDeque<LinkedList<BoundType>>();"+
				"LinkedList<BoundType> linkedList0 = new LinkedList<BoundType>();"+
				"boolean actual_result = arrayDeque0.add(linkedList0);"+
				"ArrayDeque<String> arrayDeque1 = new ArrayDeque<String>();"+
				"String string0 = p1;"+
				"boolean boolean1 = arrayDeque1.offerLast(string0);"+
				"clone.replaceValues(p0, (Iterable<? extends String>) arrayDeque1);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test36() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.put(Object,Object)", "ArrayListMultimap_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap_Stub arrayListMultimap_Stub0 = new ArrayListMultimap_Stub();"+
				"Integer integer0 = ArrayListMultimap_Stub.ELEMENT_0_1;"+
				"String string0 = ArrayListMultimap_Stub.ELEMENT_0_0;"+
				"SingletonImmutableSet<String> singletonImmutableSet0 = new SingletonImmutableSet<String>(string0, (int) integer0);"+
				"List<String> list0 = arrayListMultimap_Stub0.replaceValues(integer0, (Iterable<? extends String>) singletonImmutableSet0);"+
				"boolean boolean0 = true;"+
				"arrayListMultimap_Stub0.set_results(boolean0);"+
				"arrayListMultimap_Stub0.method_under_test();"+
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
		TypeVariable<?> v = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
		genericToConcrete.put(k, "Integer");
		genericToConcrete.put(v, "String");
		SecondStageGeneratorStubWithGenerics sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package com.google.common.collect;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class ArrayListMultimap_Stub_2 extends ArrayListMultimap<Integer, String> {"+
				"protected ArrayListMultimap_Stub_2(){super();}"+
				"public void method_under_test(Integer p0, String p1) {"+
				"Cloner c = new Cloner();"+
				"ArrayListMultimap<Integer, String> clone = c.deepClone(this);"+
				"boolean expected_result = this.put(p0, p1);"+
				"String string0 = p1;"+
				"SingletonImmutableSet<String> singletonImmutableSet0 = new SingletonImmutableSet<String>(string0, (int) p0);"+
				"clone.replaceValues(p0, (Iterable<? extends String>) singletonImmutableSet0);"+
				"boolean actual_result = true;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test37() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.putAll(Object,Iterable)", "ArrayListMultimap_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap_Stub arrayListMultimap_Stub0 = new ArrayListMultimap_Stub();"+
				"Integer integer0 = ArrayListMultimap_Stub.ELEMENT_0_1;"+
				"String string0 = \"Lwtep\\\\V&\";"+
				"String string1 = \"RK\";"+
				"Integer integer1 = ArrayListMultimap_Stub.ELEMENT_0_1;"+
				"StackTraceElement stackTraceElement0 = new StackTraceElement(string0, string0, string1, (int) integer1);"+
				"String string2 = stackTraceElement0.getMethodName();"+
				"Integer integer2 = ArrayListMultimap_Stub.ELEMENT_0_1;"+
				"List<String> list0 = ArrayListMultimap_Stub.ELEMENT_0_0;"+
				"List<String> list1 = arrayListMultimap_Stub0.replaceValues(integer2, (Iterable<? extends String>) list0);"+
				"ArrayListMultimap<Integer, String> arrayListMultimap0 = arrayListMultimap_Stub0.create();"+
				"Object object0 = null;"+
				"arrayListMultimap0.trimToSize();"+
				"boolean boolean0 = arrayListMultimap_Stub0.remove(object0, object0);"+
				"boolean boolean1 = true;"+
				"boolean boolean2 = true;"+
				"arrayListMultimap_Stub0.set_results(boolean2);"+
				"arrayListMultimap_Stub0.method_under_test();"+
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
		TypeVariable<?> v = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
		genericToConcrete.put(k, "Integer");
		genericToConcrete.put(v, "String");
		SecondStageGeneratorStubWithGenerics sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package com.google.common.collect;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class ArrayListMultimap_Stub_2 extends ArrayListMultimap<Integer, String> {"+
				"protected ArrayListMultimap_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(Integer p0, java.lang.Iterable<? extends String> p1) {"+
				"Cloner c = new Cloner();"+
				"ArrayListMultimap<Integer, String> clone = c.deepClone(this);"+
				"boolean expected_result = this.putAll(p0, p1);"+
				"String string0 = \"Lwtep\\\\V&\";"+
				"String string1 = \"RK\";"+
				"StackTraceElement stackTraceElement0 = new StackTraceElement(string0, string0, string1, (int) p0);"+
				"String string2 = stackTraceElement0.getMethodName();"+
				"clone.replaceValues(p0, p1);"+
				"ArrayListMultimap<Integer, String> arrayListMultimap0 = clone.create();"+
				"Object object0 = null;"+
				"arrayListMultimap0.trimToSize();"+
				"clone.remove(object0, object0);"+
				"boolean actual_result = true;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test38() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.containsEntry(Object,Object)", "ArrayListMultimap_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap_Stub arrayListMultimap_Stub0 = new ArrayListMultimap_Stub();"+
				"boolean boolean0 = true;"+
				"arrayListMultimap_Stub0.set_results(boolean0);"+
				"arrayListMultimap_Stub0.method_under_test();"+
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
		TypeVariable<?> v = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
		genericToConcrete.put(k, "Integer");
		genericToConcrete.put(v, "String");
		SecondStageGeneratorStubWithGenerics sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package com.google.common.collect;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class ArrayListMultimap_Stub_2 extends ArrayListMultimap<Integer, String> {"+
				"protected ArrayListMultimap_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(java.lang.Object p0, java.lang.Object p1) {"+
				"Cloner c = new Cloner();"+
				"ArrayListMultimap<Integer, String> clone = c.deepClone(this);"+
				"boolean expected_result = this.containsEntry(p0, p1);"+
				"boolean actual_result = true;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test39() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.remove(Object,Object)", "ArrayListMultimap_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap_Stub arrayListMultimap_Stub0 = new ArrayListMultimap_Stub();"+
				"ArrayListMultimap_Stub arrayListMultimap_Stub1 = new ArrayListMultimap_Stub();"+
				"Object object0 = new Object();"+
				"List<String> list0 = arrayListMultimap_Stub1.removeAll(object0);"+
				"Integer integer0 = ArrayListMultimap_Stub.ELEMENT_0_0;"+
				"List<String> list1 = arrayListMultimap_Stub0.replaceValues(integer0, (Iterable<? extends String>) list0);"+
				"boolean boolean0 = true;"+
				"arrayListMultimap_Stub0.set_results(boolean0);"+
				"arrayListMultimap_Stub0.method_under_test();"+
				"}");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.List"), false, false));

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
		TypeVariable<?> v = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
		genericToConcrete.put(k, "Integer");
		genericToConcrete.put(v, "String");
		SecondStageGeneratorStubWithGenerics sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package com.google.common.collect;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import java.util.List;"+
				"public class ArrayListMultimap_Stub_2 extends ArrayListMultimap<Integer, String> {"+
				"protected ArrayListMultimap_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(Integer p0, java.lang.Object p1) {"+
				"Cloner c = new Cloner();"+
				"ArrayListMultimap<Integer, String> clone = c.deepClone(this);"+
				"boolean expected_result = this.remove(p0, p1);"+
				"ArrayListMultimap arrayListMultimap_Stub1 = new ArrayListMultimap();"+
				"Object object0 = new Object();"+
				"List<String> list0 = arrayListMultimap_Stub1.removeAll(object0);"+
				"clone.replaceValues(p0, (Iterable<? extends String>) list0);"+
				"boolean actual_result = true;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test40() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.create(Multimap)", "ArrayListMultimap_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap_Stub arrayListMultimap_Stub0 = new ArrayListMultimap_Stub();"+
				"ArrayListMultimap<Integer, String> arrayListMultimap0 = arrayListMultimap_Stub0.create();"+
				"LinkedListMultimap<Integer, String> linkedListMultimap0 = ArrayListMultimap_Stub.ELEMENT_0_0;"+
				"boolean boolean0 = arrayListMultimap0.putAll((Multimap) linkedListMultimap0);"+
				"arrayListMultimap_Stub0.set_results(arrayListMultimap0);"+
				"arrayListMultimap_Stub0.method_under_test();"+
				"}");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.List"), false, false));

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
		TypeVariable<?> v = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
		genericToConcrete.put(k, "Integer");
		genericToConcrete.put(v, "String");
		SecondStageGeneratorStubWithGenerics sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package com.google.common.collect;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import java.util.List;"+
				"public class ArrayListMultimap_Stub_2 extends ArrayListMultimap<Integer, String> {"+
				"protected ArrayListMultimap_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(com.google.common.collect.Multimap<? extends Integer, ? extends String> p0) {"+
				"Cloner c = new Cloner();"+
				"ArrayListMultimap<Integer, String> clone = c.deepClone(this);"+
				"com.google.common.collect.ArrayListMultimap<Integer, String> expected_result = this.create(p0);"+
				"com.google.common.collect.ArrayListMultimap<Integer, String> actual_result = clone.create();"+
				"boolean boolean0 = actual_result.putAll((Multimap) p0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test41() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.HashMultimap.removeAll(Object)", "HashMultimap_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"HashMultimap_Stub hashMultimap_Stub0 = new HashMultimap_Stub();"+
				"Integer integer0 = HashMultimap_Stub.ELEMENT_0_0;"+
				"Ordering<Object> ordering0 = Ordering.arbitrary();"+
				"TreeMultimap<String, String> treeMultimap0 = new TreeMultimap<String, String>(ordering0, ordering0);"+
				"SortedSet<String> sortedSet0 = treeMultimap0.keySet();"+
				"Integer integer1 = new Integer((int) integer0);"+
				"Set<String> set0 = hashMultimap_Stub0.replaceValues(integer1, (Iterable<? extends String>) sortedSet0);"+
				"hashMultimap_Stub0.set_results(set0);"+
				"hashMultimap_Stub0.method_under_test();"+
				"}");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("com.google.common.collect.HashMultimap_Stub"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("com.google.common.collect.Ordering"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("com.google.common.collect.TreeMultimap"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.Set"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.SortedSet"), false, false));

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
		TypeVariable<?> v = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
		genericToConcrete.put(k, "Integer");
		genericToConcrete.put(v, "String");
		SecondStageGeneratorStubWithGenerics sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package com.google.common.collect;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import com.google.common.collect.Ordering;"+
				"import com.google.common.collect.TreeMultimap;"+
				"import java.util.Set;"+
				"import java.util.SortedSet;"+
				"public class HashMultimap_Stub_2 extends HashMultimap<Integer, String> {"+
				"protected HashMultimap_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(java.lang.Object p0) {"+
				"Cloner c = new Cloner();"+
				"HashMultimap<Integer, String> clone = c.deepClone(this);"+
				"java.util.Set<String> expected_result = this.removeAll(p0);"+
				"Ordering<Object> ordering0 = Ordering.arbitrary();"+
				"TreeMultimap<String, String> treeMultimap0 = new TreeMultimap<String, String>(ordering0, ordering0);"+
				"SortedSet<String> sortedSet0 = treeMultimap0.keySet();"+
				"java.util.Set<String> actual_result = clone.replaceValues(new Integer((int) p0), (Iterable<? extends String>) sortedSet0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test42() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ConcurrentHashMultiset.add(Object,int)", "ConcurrentHashMultiset_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ConcurrentHashMultiset_Stub concurrentHashMultiset_Stub0 = new ConcurrentHashMultiset_Stub();"+
				"int int0 = ConcurrentHashMultiset_Stub.ELEMENT_0_0;"+
				"Integer integer0 = ConcurrentHashMultiset_Stub.ELEMENT_0_1;"+
				"Integer integer1 = new Integer((int) integer0);"+
				"int int1 = concurrentHashMultiset_Stub0.setCount(integer1, int0);"+
				"concurrentHashMultiset_Stub0.method_under_test();"+
				"}");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.List"), false, false));

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStubWithGenerics sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package com.google.common.collect;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import java.util.List;"+
				"public class ConcurrentHashMultiset_Stub_2 extends ConcurrentHashMultiset<Integer> {"+
				"ConcurrentHashMultiset_Stub_2(java.util.concurrent.ConcurrentMap p0) {"+
				"super(p0);"+
				"}"+
				"public void method_under_test(Integer p0, int p1) {"+
				"Cloner c = new Cloner();"+
				"ConcurrentHashMultiset<Integer> clone = c.deepClone(this);"+
				"int expected_result = this.add(p0, p1);"+
				"clone.setCount(new Integer((int) p0), p1);"+
				"int actual_result = 0;"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test43() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.HashBasedTable.clear()", "HashBasedTable_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"HashBasedTable_Stub hashBasedTable_Stub0 = new HashBasedTable_Stub();"+
				"Map<Integer, Map<String, Character>> map0 = hashBasedTable_Stub0.rowMap();"+
				"map0.clear();"+
				"BoundType boundType0 = BoundType.CLOSED;"+
				"hashBasedTable_Stub0.method_under_test();"+
				"}");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("com.google.common.collect.BoundType"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.Map"), false, false));

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> r = TypeVariableImpl.<GenericDeclaration>make(Object.class, "R", null, null);
		TypeVariable<?> c = TypeVariableImpl.<GenericDeclaration>make(Object.class, "C", null, null);
		TypeVariable<?> v = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
		genericToConcrete.put(r, "Integer");
		genericToConcrete.put(c, "String");
		genericToConcrete.put(v, "Character");
		SecondStageGeneratorStubWithGenerics sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package com.google.common.collect;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import com.google.common.collect.BoundType;"+
				"import java.util.Map;"+
				"public class HashBasedTable_Stub_2 extends HashBasedTable<Integer, String, Character> {"+
				"HashBasedTable_Stub_2(java.util.Map p0, com.google.common.collect.HashBasedTable.Factory p1) {"+
				"super(p0, p1);"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"HashBasedTable<Integer, String, Character> clone = c.deepClone(this);"+
				"this.clear();"+
				"Map<Integer, Map<String, Character>> map0 = clone.rowMap();"+
				"map0.clear();"+
				"if (Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test44() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.Lists.newArrayList(Object)", "Lists_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"Lists_Stub lists_Stub0 = new Lists_Stub();"+
				"Integer[] integerArray0 = Lists_Stub.ELEMENT_0_0;"+
				"int int0 = 2;"+
				"Integer integer0 = new Integer(int0);"+
				"Integer integer1 = new Integer((int) integer0);"+
				"Integer integer2 = new Integer((int) integer1);"+
				"int int1 = 0;"+
				"Integer integer3 = new Integer(int1);"+
				"Integer integer4 = new Integer((int) integer3);"+
				"Integer integer5 = new Integer((int) integer4);"+
				"RegularImmutableSet<BoundType> regularImmutableSet0 = new RegularImmutableSet<BoundType>(integerArray0, (int) integer2, integerArray0, (int) integer5);"+
				"UnmodifiableIterator<BoundType> unmodifiableIterator0 = regularImmutableSet0.iterator();"+
				"ArrayList arrayList0 = lists_Stub0.newArrayList((Iterator) unmodifiableIterator0);"+
				"lists_Stub0.set_results(arrayList0);"+
				"lists_Stub0.method_under_test();"+
				"}");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("com.google.common.collect.BoundType"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("com.google.common.collect.RegularImmutableSet"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("com.google.common.collect.UnmodifiableIterator"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.ArrayList"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.Iterator"), false, false));

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(new ArrayList<TestScenario>(), stub, candidateES);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package com.google.common.collect;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import com.google.common.collect.BoundType;"+
				"import com.google.common.collect.RegularImmutableSet;"+
				"import com.google.common.collect.UnmodifiableIterator;"+
				"import java.util.ArrayList;"+
				"import java.util.Iterator;"+
				"public class Lists_Stub_2 extends Lists {"+
				"protected Lists_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test(java.lang.Object[] p0) {"+
				"Cloner c = new Cloner();"+
				"Lists clone = c.deepClone(this);"+
				"java.util.ArrayList expected_result = this.newArrayList(p0);"+
				"RegularImmutableSet<BoundType> regularImmutableSet0 = new RegularImmutableSet<BoundType>(p0, (int) new Integer((int) new Integer((int) new Integer(2))), p0, (int) new Integer((int) new Integer((int) new Integer(0))));"+
				"UnmodifiableIterator<BoundType> unmodifiableIterator0 = regularImmutableSet0.iterator();"+
				"java.util.ArrayList actual_result = clone.newArrayList((Iterator) unmodifiableIterator0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test45() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.Sets.newHashSet()", "Sets_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"Sets_Stub sets_Stub0 = new Sets_Stub();"+
				"Set<Integer> set0 = sets_Stub0.newIdentityHashSet();"+
				"HashSet<Integer> hashSet0 = sets_Stub0.newHashSet((Iterable<? extends Integer>) set0);"+
				"sets_Stub0.set_results(hashSet0);"+
				"sets_Stub0.method_under_test();"+
				"}");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.HashSet"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.Set"), false, false));

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStubWithGenerics sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package com.google.common.collect;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import java.util.HashSet;"+
				"import java.util.Set;"+
				"public class Sets_Stub_2 extends Sets {"+
				"protected Sets_Stub_2() {"+
				"super();"+
				"}"+
				"public void method_under_test() {"+
				"Cloner c = new Cloner();"+
				"Sets clone = c.deepClone(this);"+
				"java.util.HashSet<Integer> expected_result = this.newHashSet();"+
				"Set<Integer> set0 = clone.newIdentityHashSet();"+
				"java.util.HashSet<Integer> actual_result = clone.newHashSet((Iterable<? extends Integer>) set0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test46() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.HashBasedTable.get(Object,Object)", "HashBasedTable_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"HashBasedTable_Stub hashBasedTable_Stub0 = new HashBasedTable_Stub();"+
				"char char0 = '&';"+
				"Integer integer0 = HashBasedTable_Stub.ELEMENT_0_0;"+
				"String string0 = HashBasedTable_Stub.ELEMENT_0_1;"+
				"Character character0 = hashBasedTable_Stub0.put(integer0, string0, (Character) char0);"+
				"Character character1 = new Character((char) character0);"+
				"String string1 = HashBasedTable_Stub.ELEMENT_0_1;"+
				"Character character2 = hashBasedTable_Stub0.put(integer0, string1, character0);"+
				"hashBasedTable_Stub0.set_results(character1);"+
				"hashBasedTable_Stub0.method_under_test();"+
				"}");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.Collection"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.Map"), false, false));

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> r = TypeVariableImpl.<GenericDeclaration>make(Object.class, "R", null, null);
		TypeVariable<?> v = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
		TypeVariable<?> c = TypeVariableImpl.<GenericDeclaration>make(Object.class, "C", null, null);
		genericToConcrete.put(r, "Integer");
		genericToConcrete.put(c, "String");
		genericToConcrete.put(v, "Character");
		SecondStageGeneratorStubWithGenerics sssg = new SecondStageGeneratorStubWithGenerics(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
		
		String actual = second.getAst().toString();
		String expected = 
				"package com.google.common.collect;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import java.util.Collection;"+
				"import java.util.Map;"+
				"public class HashBasedTable_Stub_2 extends HashBasedTable<Integer, String, Character> {"+
				"HashBasedTable_Stub_2(java.util.Map p0, com.google.common.collect.HashBasedTable.Factory p1) {"+
				"super(p0, p1);"+
				"}"+
				"public void method_under_test(Integer p0, String p1) {"+
				"Cloner c = new Cloner();"+
				"HashBasedTable<Integer, String, Character> clone = c.deepClone(this);"+
				"Character expected_result = this.get(p0, p1);"+
				"Character character0 = clone.put(p0, p1, (Character) '&');"+
				"clone.put(p0, p1, character0);"+
				"Character actual_result = new Character((char) character0);"+
				"if (Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(this, clone) > 0.0d)"+
				"System.out.println(\"Executed\");"+
				"}"+
				"}";
		assertASTEquals(actual, expected);
	}
	
}
