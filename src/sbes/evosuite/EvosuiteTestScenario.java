package sbes.evosuite;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sbes.SBESException;
import sbes.option.Options;
import sbes.util.ClassUtils;
import sbes.util.DirectoryUtils;
import sbes.util.IOUtils;

public class EvosuiteTestScenario extends Evosuite {

	public EvosuiteTestScenario(String classSignature, String methodSignature) {
		super(classSignature, methodSignature);
		this.outputDir =  DirectoryUtils.I().getTestScenarioDir();
	}
	
	@Override
	public String[] getCommand() {
		List<String> evo = new ArrayList<String>();
		evo.addAll(Arrays.asList(javaCommand));
		evo.add("-jar");
		if (!Options.I().getJavaPath().equals("")) {
			evo.add(IOUtils.concatPath(Options.I().getJavaPath(), jarName));
		}
		else {
			evo.add(jarName);
		}
		evo.add("-DCP=" + Options.I().getClassesPath());
		evo.add("-class");
		evo.add(classSignature);
		evo.add("-Dtarget_method=" + getTargetMethodSignature());
		evo.add("-Dsearch_budget=" + Options.I().getTestSearchBudget());
		evo.add("-Dtest_dir=" + outputDir);
		evo.add("-Dassertions=false");
		evo.add("-Dhtml=false");
		evo.add("-generateSuite");
		evo.add("-Dshow_progress=false");
		evo.add("-Dcriterion=branch");
		evo.add("-Dtest_factory=RANDOM");
		command = evo.toString();
		return evo.toArray(new String[0]);
	}

	@Override
	protected String getTargetMethodSignature() {
		return getBytecodeSignature(classSignature, methodSignature);
	}
	
	private String getBytecodeSignature(final String classname, final String methodname) {
		String toReturn = null;
		Class<?> c;
		try {
			c = Class.forName(classname, false, classLoader);
			String method = methodname.split("\\[")[0];
			String args[] = methodname.split("\\[")[1].replaceAll("\\]", "").split(",");
			if (args.length == 1) {
				args = args[0].equals("") ? new String[0] : args;
			}
			for (Method m : c.getDeclaredMethods()) {
				if (m.getName().equals(method) && m.getParameterTypes().length == args.length) {
					int i;
					for (i = 0; i < args.length; i++) {
						if (!m.getParameterTypes()[i].getCanonicalName().contains(args[i])) {
							break;
						}
					}
					if (i == args.length) {
						toReturn = ClassUtils.getBytecodeSignature(m);
						break;
					}
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error("Unable to find class", e);
			throw new SBESException("Unable to find class");
		}
		
		if (toReturn == null) {
			throw new SBESException("Method not found: " + classname + "." + methodname);
		}
		
		return toReturn;
	}
	
}
