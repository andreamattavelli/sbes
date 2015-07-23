package sbes.stub.generator.second.symbolic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
public class SecondStageStubGeneratorSETest {
	
	private List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
	private Stub stub;
	
	private void setUp(String classesPath, String methodSignature, String stubName) {
		Options.I().setClassesPath(classesPath);
		Options.I().setTargetMethod(methodSignature);
		Options.I().setLogLevel(Level.ERROR);
		Options.I().setDontResolveGenerics(false);
		Options.I().setSymbolicExecutionCounterexample(true);
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
		SecondStageGeneratorStubWithGenericsSE sssg = new SecondStageGeneratorStubWithGenericsSE(new ArrayList<TestScenario>(), stub, candidateES, genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin:./lib/jbse-0.7.jar");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import jbse.meta.Analysis;"+
				"import jbse.meta.annotations.ConservativeRepOk;"+
				"import sbes.symbolic.mock.IntegerMock;"+
				"import sbes.symbolic.mock.Stack;"+
				"public class Stack_Stub_2 {"+
				"private interface FakeVariable {"+
				"}"+
				"Stack<IntegerMock> v_Stack1;"+
				"Stack<IntegerMock> v_Stack2;"+
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
				"actual_result = p0;"+
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
				"}";

		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test02() throws ParseException {
		setUp("./bin", "stack.util.Stack.indexOf(Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{" + 
				"Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0;"+
				"int int0 = stack_Stub0.indexOf(integer0);"+
				"stack_Stub0.set_results(int0);"+
				"stack_Stub0.method_under_test();" + 
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStubWithGenericsSE sssg = new SecondStageGeneratorStubWithGenericsSE(new ArrayList<TestScenario>(), stub, candidateES, genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin:./lib/jbse-0.7.jar");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import jbse.meta.Analysis;"+
				"import jbse.meta.annotations.ConservativeRepOk;"+
				"import sbes.symbolic.mock.IntegerMock;"+
				"import sbes.symbolic.mock.Stack;"+
				"public class Stack_Stub_2 {"+
				"private interface FakeVariable {"+
				"}"+
				"Stack<IntegerMock> v_Stack1;"+
				"Stack<IntegerMock> v_Stack2;"+
				"FakeVariable forceConservativeRepOk;"+
				"FakeVariable forceConservativeRepOk2;"+
				"FakeVariable forceConservativeRepOk3;"+
				"int expected_result;"+
				"int actual_result;"+
				"Exception e1;"+
				"Exception e2;"+
				"java.lang.Object p0;"+
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
				"e1 = null;"+
				"e2 = null;"+
				"try {"+
				"expected_result = v_Stack1.indexOf(p0);"+
				"} catch (Exception e) {"+
				"e1 = e;"+
				"}"+
				"try {"+
				"int actual_result = v_Stack2.indexOf(p0);"+
				"} catch (Exception e) {"+
				"e2 = e;"+
				"}"+
				"boolean ok = mirrorFinalConservative();"+
				"FakeVariable fake = forceConservativeRepOk;"+
				"Analysis.ass3rt(ok);"+
				"if (expected_result != actual_result)"+
				"ok = false;"+
				"FakeVariable fake2 = forceConservativeRepOk2;"+
				"Analysis.ass3rt(ok);"+
				"if (e1 == null ^ e2 == null)"+
				"ok = false;"+
				"FakeVariable fake3 = forceConservativeRepOk3;"+
				"Analysis.ass3rt(ok);"+
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
						"Integer integer0 = Stack_Stub.ELEMENT_0_0;"+
						"stack_Stub0.addElement(integer0);"+
						"stack_Stub0.set_results(integer0);"+
						"stack_Stub0.method_under_test();"+
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStubWithGenericsSE sssg = new SecondStageGeneratorStubWithGenericsSE(new ArrayList<TestScenario>(), stub, candidateES, genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin:./lib/jbse-0.7.jar");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import jbse.meta.Analysis;"+
				"import jbse.meta.annotations.ConservativeRepOk;"+
				"import sbes.symbolic.mock.IntegerMock;"+
				"import sbes.symbolic.mock.Stack;"+
				"public class Stack_Stub_2 {"+
				"private interface FakeVariable {"+
				"}"+
				"Stack<IntegerMock> v_Stack1;"+
				"Stack<IntegerMock> v_Stack2;"+
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
				"actual_result = p0;"+
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
				"}";

		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test04() throws ParseException {
		setUp("./bin", "stack.util.Stack.push(Object)", "Stack_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0_0;"+
				"Integer integer1 = new Integer((int) integer0);"+
				"Integer integer2 = new Integer((int) integer1);"+
				"boolean boolean0 = stack_Stub0.add(integer2);"+
				"stack_Stub0.set_results(integer2);"+
				"stack_Stub0.method_under_test();"+
				"}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		SecondStageGeneratorStubWithGenericsSE sssg = new SecondStageGeneratorStubWithGenericsSE(new ArrayList<TestScenario>(), stub, candidateES, genericToConcrete);
		Stub second = sssg.generateStub();
		second.dumpStub("./test/resources/compilation");
		assertCompiles("stack/util", second.getStubName(), "./bin:./lib/jbse-0.7.jar");
		
		String actual = second.getAst().toString();
		String expected = "package stack.util;"+
				"import sbes.distance.Distance;"+
				"import sbes.cloning.Cloner;"+
				"import jbse.meta.Analysis;"+
				"import jbse.meta.annotations.ConservativeRepOk;"+
				"import sbes.symbolic.mock.IntegerMock;"+
				"import sbes.symbolic.mock.Stack;"+
				"public class Stack_Stub_2 {"+
				"private interface FakeVariable {"+
				"}"+
				"Stack<IntegerMock> v_Stack1;"+
				"Stack<IntegerMock> v_Stack2;"+
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
				"boolean boolean0 = v_Stack2.add(new IntegerMock(new IntegerMock(p0.intValue()).intValue()));"+
				"actual_result = new IntegerMock(new IntegerMock(p0.intValue()).intValue());"+
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
				"}"+
				"";

		assertASTEquals(actual, expected);
	}
	
}
