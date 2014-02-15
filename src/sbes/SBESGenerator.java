package sbes;

import java.util.List;

import sbes.scenario.TestScenario;
import sbes.scenario.TestScenarioGenerator;
import sbes.statistics.Statistics;
import sbes.stub.Stub;
import sbes.stub.generator.FirstPhaseStubStrategy;
import sbes.stub.generator.StubGenerator;
import sbes.util.ClasspathHandler;
import sbes.util.DirectoryUtils;

public class SBESGenerator {

	private Statistics statistics;
	
	public SBESGenerator() {
		this.statistics = new Statistics();
	}
	
	public void generateES() throws SBESException {
		statistics.synthesisStarted();
		
		// =================================== INIT =================================== 
		DirectoryUtils directory = DirectoryUtils.I();
		ClasspathHandler.checkClasspath();

		// ===================== INITIAL TEST SCENARIO GENERATION =====================
		TestScenarioGenerator scenarioGenerator = TestScenarioGenerator.getInstance();
		scenarioGenerator.generateTestScenarios();
		List<TestScenario> initialScenarios = scenarioGenerator.getScenarios();

		// ======================= FIRST PHASE STUB GENERATION ========================
		StubGenerator firstPhaseGenerator = new FirstPhaseStubStrategy(initialScenarios);
		Stub firstPhaseStub = firstPhaseGenerator.generateStub();
		directory.createFirstStubDir();
		firstPhaseStub.dumpStub(directory.getFirstStubDir());

		boolean terminated = false;
		while (terminated) {
		// ========================== FIRST PHASE SYNTHESIS ===========================
			// compile stub
			// run evosuite
			// analyze test case
			// carve result
			
		// ======================= SECOND PHASE STUB GENERATION =======================
			// generate second stub from carved test case
			
		// ========================== SECOND PHASE SYNTHESIS ==========================
			// compile second stub
			// run evosuite
			// analyze test case
			// carve result
			
			// if solution is not found: add test scenario to stub
			
			// determine exit condition: solutionFound || time expired
			terminated = true;
		}
		
		statistics.synthesisFinished();
		
		statistics.print();
	}

}
