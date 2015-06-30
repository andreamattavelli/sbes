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
public class SecondStageStubGeneratorTestWithoutGenerics {
	
	private List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
	private Stub stub;
	
	private void setUp(String classesPath, String methodSignature, String stubName) {
		Options.I().setClassesPath(classesPath);
		Options.I().setTargetMethod(methodSignature);
		Options.I().setLogLevel(Level.ERROR);
		Options.I().setDontResolveGenerics(true);
		List<TypeDeclaration> decls = new ArrayList<TypeDeclaration>(); 
		List<Comment> comments = new ArrayList<Comment>();
		stub = new Stub(new CompilationUnit(new PackageDeclaration(new NameExpr("foo")), imports, decls, comments), stubName);	}
	
	protected void assertASTEquals(String actual, String expected) {
		assertEquals(expected.replaceAll("\\s|\t|\n", ""), actual.replaceAll("\\s|\t|\n", ""));	}
	
	protected void assertCompiles(String packagename, String filename, String classesPath) {
		CompilationContext compilationContext = new CompilationContext(
				"./test/resources/compilation/" + packagename, 
				filename + ".java", 
				"./test/resources/compilation", 
				classesPath);
		
		boolean compilationSucceeded = Compilation.compile(compilationContext);
		assertTrue(compilationSucceeded);	}
	
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
		}	}
	
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");	}

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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
	}
	
	@Test
	public void test22() throws ParseException {
		setUp("./bin", "stack.util.Stack.add(int,Object)", "Stack_Stub");

		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Object integer0 = Stack_Stub.ELEMENT_0_1;"+
				"LinkedList<Object> linkedList0 = new LinkedList<Object>();"+
				"boolean boolean0 = linkedList0.offerLast((Object) integer0);"+
				"int int0 = Stack_Stub.ELEMENT_0_0;"+
				"boolean[] booleanArray0 = stack_Stub0.addAll(int0, (Collection) linkedList0);"+
				"stack_Stub0.method_under_test();}");

		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.LinkedList"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.Collection"), false, false));
		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
	}
	
	@Test
	public void test27() throws ParseException {
		setUp("./bin", "stack.util.Stack.remove(int)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Object int0 = Stack_Stub.ELEMENT_0_0;"+
				"Object integer0 = stack_Stub0.get(int0);"+
				"Object integer1 = new Integer((int) integer0);"+
				"stack_Stub0.addElement(integer1);"+
				"Object integer2 = stack_Stub0.pop();"+
				"int int1 = Stack_Stub.ELEMENT_0_0;"+
				"stack_Stub0.removeElementAt(int1);"+
				"stack_Stub0.set_results(integer1);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
	}
	
	@Test
	public void test31() throws ParseException {
		setUp("./bin", "stack.util.Stack.set(int,Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Object integer0 = Stack_Stub.ELEMENT_0_1;"+
				"Object integer1 = stack_Stub0.lastElement();"+
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
	}
	
	@Test
	public void test35() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.put(Object,Object)", "ArrayListMultimap_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap_Stub arrayListMultimap_Stub0 = new ArrayListMultimap_Stub();"+
				"Object integer0 = ArrayListMultimap_Stub.ELEMENT_0_1;"+
				"ArrayDeque<LinkedList<BoundType>> arrayDeque0 = new ArrayDeque<LinkedList<BoundType>>();"+
				"LinkedList<BoundType> linkedList0 = new LinkedList<BoundType>();"+
				"boolean boolean0 = arrayDeque0.add(linkedList0);"+
				"ArrayDeque arrayDeque1 = new ArrayDeque();"+
				"Object string0 = ArrayListMultimap_Stub.ELEMENT_0_0;"+
				"boolean boolean1 = arrayDeque1.offerLast(string0);"+
				"List<String> list0 = arrayListMultimap_Stub0.replaceValues(integer0, (Iterable) arrayDeque1);"+
				"arrayListMultimap_Stub0.set_results(boolean0);"+
				"arrayListMultimap_Stub0.method_under_test();"+
				"}");

		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.ArrayDeque"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.LinkedList"), false, false));
		
		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(new ArrayList<TestScenario>(), stub, candidateES);
		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");	
	}
	
	@Test
	public void test36() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.put(Object,Object)", "ArrayListMultimap_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap_Stub arrayListMultimap_Stub0 = new ArrayListMultimap_Stub();"+
				"Integer integer0 = ArrayListMultimap_Stub.ELEMENT_0_1;"+
				"Object string0 = ArrayListMultimap_Stub.ELEMENT_0_0;"+
				"SingletonImmutableSet singletonImmutableSet0 = new SingletonImmutableSet(string0, (int) integer0);"+
				"List<String> list0 = arrayListMultimap_Stub0.replaceValues(integer0, (Iterable<? extends String>) singletonImmutableSet0);"+
				"boolean boolean0 = true;"+
				"arrayListMultimap_Stub0.set_results(boolean0);"+
				"arrayListMultimap_Stub0.method_under_test();"+
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
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

		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
	@Test
	public void test42() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ConcurrentHashMultiset.add(Object,int)", "ConcurrentHashMultiset_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ConcurrentHashMultiset_Stub concurrentHashMultiset_Stub0 = new ConcurrentHashMultiset_Stub();"+
				"int int0 = ConcurrentHashMultiset_Stub.ELEMENT_0_0;"+
				"Object integer0 = ConcurrentHashMultiset_Stub.ELEMENT_0_1;"+
				"Object integer1 = new Integer((int) integer0);"+
				"int int1 = concurrentHashMultiset_Stub0.setCount(integer1, int0);"+
				"concurrentHashMultiset_Stub0.method_under_test();"+
				"}");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.List"), false, false));

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStub(
				new ArrayList<TestScenario>(), stub, candidateES);

		Stub second = sssg.generateStub();
		
		second.dumpStub("./test/resources/compilation");
		assertCompiles("com/google/common/collect", second.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
}
