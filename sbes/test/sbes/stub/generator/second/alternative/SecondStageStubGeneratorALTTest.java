package sbes.stub.generator.second.alternative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.Comment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;
import sbes.logging.Level;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.Stub;
import sbes.stub.generator.second.SecondStageGeneratorStub;
import sbes.testcase.Compilation;
import sbes.testcase.CompilationContext;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SecondStageStubGeneratorALTTest {
	
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenericsALT(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"public class Stack_Stub_2 {"+
				"public void method_under_test(stack.util.Stack<Integer> instance, Integer p0) {"+
				"Exception e1 = null;"+
				"Exception e2 = null;"+
				"java.lang.Object expected_result = null;"+
				"java.lang.Object actual_result = null;"+
				"Cloner c = new Cloner();"+
				"Stack<Integer> clone = c.deepClone(instance);"+
				"try {"+
				"expected_result = instance.push(p0);"+
				"} catch (Exception e) {"+
				"e1 = e;"+
				"}"+
				"try {"+
				"clone.addElement(p0);"+
				"actual_result = p0;"+
				"} catch (Exception e) {"+
				"e2 = e;"+
				"}"+
				"if (e1 == null ^ e2 == null || Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(instance, clone) > 0.0d)"+
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
		SecondStageGeneratorStub sssg = new SecondStageGeneratorStubWithGenericsALT(
				new ArrayList<TestScenario>(), stub, candidateES,
				genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
						"import sbes.distance.Distance;"+
						"import sbes.cloning.Cloner;"+
						"public class Stack_Stub_2 {"+
						"public void method_under_test(stack.util.Stack<Integer> instance, Integer p0) {"+
						"Exception e1 = null;"+
						"Exception e2 = null;"+
						"boolean expected_result = false;"+
						"boolean actual_result = false;"+
						"Cloner c = new Cloner();"+
						"Stack<Integer> clone = c.deepClone(instance);"+
						"try {"+
						"expected_result = instance.add(p0);"+
						"} catch (Exception e) {"+
						"e1 = e;"+
						"}"+
						"try {"+
						"clone.addElement(p0);"+
						"actual_result = true;"+
						"} catch (Exception e) {"+
						"e2 = e;"+
						"}"+
						"if (e1 == null ^ e2 == null || Distance.distance(expected_result, actual_result) > 0.0d || Distance.distance(instance, clone) > 0.0d)"+
						"System.out.println(\"Executed\");"+
						"}"+
						"}";
		assertASTEquals(actual, expected);
	}
	
}
