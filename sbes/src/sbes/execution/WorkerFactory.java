package sbes.execution;

import sbes.execution.evosuite.Evosuite;
import sbes.execution.evosuite.EvosuiteWorker;
import sbes.execution.jbse.JBSE;
import sbes.execution.jbse.JBSEWorker;

public class WorkerFactory {

	private WorkerFactory() { } // Do not instantiate me!

	public static Worker getWorker(Tool tool) {
		if (tool instanceof Evosuite) {
			return new EvosuiteWorker((Evosuite) tool);
		} else if (tool instanceof JBSE) {
			return new JBSEWorker((JBSE) tool);
		}
		return null; // should never happen
	}

}
