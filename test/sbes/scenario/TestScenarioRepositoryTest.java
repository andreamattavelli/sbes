package sbes.scenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestScenarioRepositoryTest {

	@Before
	public void setUp() {
		TestScenarioRepository.reset();
	}
	
	@Test
	public void test0()  throws Throwable  {
		TestScenarioRepository testScenarioRepository0 = TestScenarioRepository.I();
		TestScenarioRepository testScenarioRepository1 = TestScenarioRepository.I();
		assertSame(testScenarioRepository1, testScenarioRepository0);
	}

	@Test
	public void test1()  throws Throwable  {
		TestScenarioRepository testScenarioRepository0 = TestScenarioRepository.I();
		List<TestScenario> list0 = testScenarioRepository0.getScenarios();
		assertEquals(true, list0.isEmpty());
	}

	@Test
	public void test2()  throws Throwable  {
		TestScenarioRepository testScenarioRepository0 = TestScenarioRepository.I();
		LinkedList<TestScenario> linkedList0 = new LinkedList<TestScenario>();
		testScenarioRepository0.addScenarios((List<TestScenario>) linkedList0);
		assertEquals(true, testScenarioRepository0.getScenarios().isEmpty());
	}

	@Test
	public void test3()  throws Throwable  {
		TestScenarioRepository testScenarioRepository0 = TestScenarioRepository.I();
		testScenarioRepository0.addScenario((TestScenario) null);
		assertEquals(1, testScenarioRepository0.getScenarios().size());
	}

	@Test
	public void test4()  throws Throwable  {
		TestScenarioRepository testScenarioRepository0 = TestScenarioRepository.I();
		assertEquals(0, testScenarioRepository0.getScenarios().size());
		testScenarioRepository0.addCounterexample(null);
		assertEquals(1, testScenarioRepository0.getScenarios().size());
		testScenarioRepository0.addScenario((TestScenario) null);
		assertEquals(2, testScenarioRepository0.getScenarios().size());
		testScenarioRepository0.resetCounterexamples();
		assertEquals(1, testScenarioRepository0.getScenarios().size());
	}

}
