package sbes.stub.generator.first;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import sbes.logging.Level;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.scenario.TestScenarioRepository;
import sbes.scenario.TestScenarioWithGenerics;
import sbes.scenario.generalizer.TestScenarioGeneralizer;
import sbes.stub.Stub;
import sbes.testcase.Compilation;
import sbes.testcase.CompilationContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FirstStageStubGeneratorTest {
	
	private List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
	
	private void setUp(String classesPath, String methodSignature, String stubName) {
		Options.I().setClassesPath(classesPath);
		Options.I().setTargetMethod(methodSignature);
		Options.I().setLogLevel(Level.ERROR);
	}
	
	protected void assertASTEquals(String actual, String expected) {
		assertEquals(expected.replaceAll("\\s|\t|\n", ""), actual.replaceAll("\\s|\t|\n", ""));
	}
	
	protected void assertThatCompiles(String packagename, String filename, String classesPath) {
		CompilationContext compilationContext = new CompilationContext(
				"./test/resources/compilation/" + packagename, 
				filename + ".java", 
				"./test/resources/compilation", 
				classesPath);
		
		boolean compilationSucceeded = Compilation.compile(compilationContext);
		assertTrue(compilationSucceeded);
	}
	
	@After
	public void tearDown() {
		TestScenarioRepository.reset();
	}
	
	@AfterClass
	public static void tearDownClass() {
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
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.put(Object,Object)", "ArrayListMultimap_Stub");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.List"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.Collections"), false, false));
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap<Integer, String> arrayListMultimap0 = ArrayListMultimap.create();"+
				"Integer integer0 = new Integer(234);"+
				"List<String> list0 = Collections.emptyList();"+
				"list0.add(\"pippo\");"+
				"boolean boolean0 = arrayListMultimap0.putAll(integer0, list0);"+
				"Integer integer3 = new Integer(-1698);"+
				"String string0 = \"pluto\";"+
				"boolean boolean3 = arrayListMultimap0.put(integer3, string0);"+
				"}");
		
		CarvingResult carvedScenario = new CarvingResult(body, imports);
		
		List<TestScenario> scenarios = new ArrayList<>();
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(carvedScenario);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		assertEquals(2, ts.getInputAsFields().size());
		
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(2, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("String"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		assertEquals("String", values.get(1));
		
		scenarios.add(tswg);
		
		// preconditions ok
		
		FirstStageGeneratorStubWithGenerics fssg = new FirstStageGeneratorStubWithGenerics(scenarios);
		Stub first = fssg.generateStub();
		first.dumpStub("./test/resources/compilation");
		assertThatCompiles("com/google/common/collect", first.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
	@Test
	public void test02() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.create()", "ArrayListMultimap_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				 "ArrayListMultimap<Integer, String> arrayListMultimap0 = ArrayListMultimap.create();" +
				"}");
		
		CarvingResult carvedScenario = new CarvingResult(body, imports);
		
		List<TestScenario> scenarios = new ArrayList<>();
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(carvedScenario);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		assertEquals(0, ts.getInputAsFields().size());
		
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(2, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("String"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		assertEquals("String", values.get(1));
		
		scenarios.add(tswg);
		
		// preconditions ok
		
		FirstStageGeneratorStubWithGenerics fssg = new FirstStageGeneratorStubWithGenerics(scenarios);
		Stub first = fssg.generateStub();
		first.dumpStub("./test/resources/compilation");
		assertThatCompiles("com/google/common/collect", first.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
	@Test
	public void test03() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.replaceValues(Object,Iterable)", "ArrayListMultimap_Stub");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.List"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.ArrayList"), false, false));
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap<Integer, String> arrayListMultimap0 = ArrayListMultimap.create();"+
				"boolean boolean1 = arrayListMultimap0.put(-1698, \"pluto\");"+
				"boolean boolean2 = arrayListMultimap0.put(123, \"asd\");"+
				"boolean boolean3 = arrayListMultimap0.put(18, \"ginger\");"+
				"List<String> list1 = new ArrayList<String>();"+
				"list1.add(\"adhs8haf8shf8\");"+
				"List<String> list0 = arrayListMultimap0.replaceValues(123, list1);"+
				"}");
		
		CarvingResult carvedScenario = new CarvingResult(body, imports);
		
		List<TestScenario> scenarios = new ArrayList<>();
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(carvedScenario);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		assertEquals(2, ts.getInputAsFields().size());
		
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(2, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("String"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		assertEquals("String", values.get(1));
		
		scenarios.add(tswg);
		
		// preconditions ok
		
		FirstStageGeneratorStubWithGenerics fssg = new FirstStageGeneratorStubWithGenerics(scenarios);
		Stub first = fssg.generateStub();
		first.dumpStub("./test/resources/compilation");
		assertThatCompiles("com/google/common/collect", first.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
	@Test
	public void test04() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.TreeMultiset.create()", "TreeMultiset_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"TreeMultiset<Integer> treeMultiset0 = TreeMultiset.create();"+
				"}");
		
		CarvingResult carvedScenario = new CarvingResult(body, imports);
		
		List<TestScenario> scenarios = new ArrayList<>();
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(carvedScenario);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		assertEquals(0, ts.getInputAsFields().size());
		
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(1, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		
		scenarios.add(tswg);
		
		// preconditions ok
		
		FirstStageGeneratorStubWithGenerics fssg = new FirstStageGeneratorStubWithGenerics(scenarios);
		Stub first = fssg.generateStub();
		first.dumpStub("./test/resources/compilation");
		assertThatCompiles("com/google/common/collect", first.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
	@Test
	public void test05() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ConcurrentHashMultiset.containsAll(Collection)", "ConcurrentHashMultiset_Stub");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.List"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.ArrayList"), false, false));
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ConcurrentHashMultiset<Integer> hashMultiset0 = ConcurrentHashMultiset.create();"+
				"Integer integer0 = new Integer(-18247);"+
				"Integer integer1 = new Integer(34);"+
				"Integer integer2 = new Integer(0);"+
				"boolean boolean0 = hashMultiset0.add(integer0);"+
				"boolean boolean1 = hashMultiset0.add(integer1);"+
				"boolean boolean2 = hashMultiset0.add(integer2);"+
				"List<Integer> arrayList0 = new ArrayList();"+
				"arrayList0.add(0);"+
				"arrayList0.add(34);"+
				"boolean boolean3 = hashMultiset0.containsAll(arrayList0);"+
				"}");
		
		CarvingResult carvedScenario = new CarvingResult(body, imports);
		
		List<TestScenario> scenarios = new ArrayList<>();
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(carvedScenario);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		assertEquals(1, ts.getInputAsFields().size());
		
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(1, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		
		scenarios.add(tswg);
		
		// preconditions ok
		
		FirstStageGeneratorStubWithGenerics fssg = new FirstStageGeneratorStubWithGenerics(scenarios);
		Stub first = fssg.generateStub();
		first.dumpStub("./test/resources/compilation");
		assertThatCompiles("com/google/common/collect", first.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
	@Test
	public void test06() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.HashBasedTable.clear()", "HashBasedTable_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"HashBasedTable<Integer, String, Character> hashBasedTable0 = HashBasedTable.create();"+
				"hashBasedTable0.put(0, \"0\", 'a');"+
				"hashBasedTable0.put(1, \"0\", 'b');"+
				"hashBasedTable0.put(0, \"1\", 'c');"+
				"hashBasedTable0.put(2, \"2\", 'd');"+
				"hashBasedTable0.put(2, \"1\", 'e');"+
				"hashBasedTable0.clear();"+
				"}");
		
		CarvingResult carvedScenario = new CarvingResult(body, imports);
		
		List<TestScenario> scenarios = new ArrayList<>();
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(carvedScenario);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		assertEquals(0, ts.getInputAsFields().size());
		
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(3, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("String"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Character"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		assertEquals("String", values.get(1));
		assertEquals("Character", values.get(2));
		
		scenarios.add(tswg);
		
		// preconditions ok
		
		FirstStageGeneratorStubWithGenerics fssg = new FirstStageGeneratorStubWithGenerics(scenarios);
		Stub first = fssg.generateStub();
		first.dumpStub("./test/resources/compilation");
		assertThatCompiles("com/google/common/collect", first.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
	@Test
	public void test07() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.TreeBasedTable.clear()", "TreeBasedTable_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"TreeBasedTable<Integer, String, Character> hashBasedTable0 = TreeBasedTable.create();"+
				"hashBasedTable0.put(0, \"0\", 'a');"+
				"hashBasedTable0.put(1, \"0\", 'b');"+
				"hashBasedTable0.put(0, \"1\", 'c');"+
				"hashBasedTable0.put(2, \"2\", 'd');"+
				"hashBasedTable0.put(2, \"1\", 'e');"+
				"hashBasedTable0.clear();"+
				"}");
		
		CarvingResult carvedScenario = new CarvingResult(body, imports);
		
		List<TestScenario> scenarios = new ArrayList<>();
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(carvedScenario);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		assertEquals(0, ts.getInputAsFields().size());
		
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(3, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("String"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Character"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		assertEquals("String", values.get(1));
		assertEquals("Character", values.get(2));
		
		scenarios.add(tswg);
		
		// preconditions ok
		
		FirstStageGeneratorStubWithGenerics fssg = new FirstStageGeneratorStubWithGenerics(scenarios);
		Stub first = fssg.generateStub();
		first.dumpStub("./test/resources/compilation");
		assertThatCompiles("com/google/common/collect", first.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
	@Test
	public void test08() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.Sets.newHashSet(Iterable)", "Sets_Stub");

		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.List"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.ArrayList"), false, false));
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"List<Integer> other = new ArrayList<Integer>();"+
				"other.add(0);"+
				"other.add(13123);"+
				"other.add(-14);"+
				"HashSet<Integer> set = Sets.newHashSet(other);"+
				"}");
		
		CarvingResult carvedScenario = new CarvingResult(body, imports);
		
		List<TestScenario> scenarios = new ArrayList<>();
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(carvedScenario);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		assertEquals(1, ts.getInputAsFields().size());
		
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(1, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		
		scenarios.add(tswg);
		
		// preconditions ok
		
		FirstStageGeneratorStubWithGenerics fssg = new FirstStageGeneratorStubWithGenerics(scenarios);
		Stub first = fssg.generateStub();
		first.dumpStub("./test/resources/compilation");
		assertThatCompiles("com/google/common/collect", first.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
	@Test
	public void test09() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.TreeBasedTable.put(Object,Object,Object)", "TreeBasedTable_Stub");

		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.Collection"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.Map"), false, false));
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"Integer i = 0;"+
				"String s = \"0\";"+
				"Character c = 'a';"+
				"TreeBasedTable<Integer, String, Character> hashBasedTable0 = TreeBasedTable.create();"+
				"hashBasedTable0.put(i, s, c);"+
				"}");
		
		CarvingResult carvedScenario = new CarvingResult(body, imports);
		
		List<TestScenario> scenarios = new ArrayList<>();
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(carvedScenario);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		assertEquals(3, ts.getInputAsFields().size());
		
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(3, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("String"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Character"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		assertEquals("String", values.get(1));
		assertEquals("Character", values.get(2));
		
		scenarios.add(tswg);
		
		// preconditions ok
		
		FirstStageGeneratorStubWithGenerics fssg = new FirstStageGeneratorStubWithGenerics(scenarios);
		Stub first = fssg.generateStub();
		first.dumpStub("./test/resources/compilation");
		assertThatCompiles("com/google/common/collect", first.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
}
