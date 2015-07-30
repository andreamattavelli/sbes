package sbes.symbolic;

import japa.parser.JavaParser;
import japa.parser.ast.stmt.BlockStmt;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sbes.exceptions.GenerationException;
import sbes.exceptions.SBESException;
import sbes.execution.InternalClassloader;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.symbolic.mock.DoubleLinkedList;
import sbes.symbolic.mock.IntegerMock;
import sbes.symbolic.mock.Stack;
import sbes.util.ClassUtils;
import sbes.util.ReflectionUtils;

public class DynamicSymbolicAnalysis {

	private static final Logger logger = new Logger(DynamicSymbolicAnalysis.class);

	private static final List<BlockStmt> testCases = new ArrayList<BlockStmt>();

	private DynamicSymbolicAnalysis() { } // Do not instantiate me!

	public static void reset() {
		testCases.clear();
	}

	public static List<BlockStmt> getTestcases() {
		return testCases;
	}

	public static void generateTestCases(Object symbolicObject) {
		InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath());
		ClassLoader classloader = ic.getClassLoader();
		Class<?> c;
		try {
			c = Class.forName(ClassUtils.getCanonicalClassname(Options.I().getTargetMethod()), false, classloader);
		} catch (ClassNotFoundException e) {
			// infeasible, we already checked the classpath
			throw new SBESException("Target class not found");
		}
		
		// get class' methods
		Method[] methods = ReflectionUtils.getClassMethods(c);
		// get method signature
		String methodSignature = ClassUtils.getMethodname(Options.I().getTargetMethod());
		// get target method from the list of class' methods
		Method targetMethod = ReflectionUtils.findTargetMethod(methods, methodSignature);
		Class<?>[] parameters = targetMethod.getParameterTypes();
		
		if (symbolicObject instanceof Stack<?>) {
			try {
				String addMethod = getStackAddMethod();
				Stack<?> stack = (Stack<?>) symbolicObject;
				
				String code = "Stack<Integer> s = new Stack<Integer>();";
				Object[] thisArray = stack.toArray();
				int i;
				for (i = 0; i < thisArray.length; i++) {
					Object obj = thisArray[i];
					if (obj == null) {
						code += "s." + addMethod + "(0);";
					} else if (obj instanceof IntegerMock) {
						code += "s." + addMethod + "(" + ((IntegerMock) obj).intValue() + ");";
					}
				}
				if (stack.size() > 0 && i == 0) {
					code += "s." + addMethod + "(0);";
				}
				
				code += "s.method_under_test(";
				if (parameters != null && parameters.length > 0) {
					for (int j = 0; j < parameters.length; j++) {
						code += "ELEMENT_0_" + j;
						if (j > 0 && j < parameters.length - 1) {
							code += ", ";
						}
					}
				}
				code += ");";
				
				BlockStmt block = JavaParser.parseBlock("{" + code + "}");
				testCases.add(block);
			} catch (Throwable e) {
				logger.error("Unable to parse JBSE test case", e);
			}
		}

		else if (symbolicObject instanceof DoubleLinkedList) {
			try {
				DoubleLinkedList dll = (DoubleLinkedList) symbolicObject;
				
				String code = "DoubleLinkedList dll = new DoubleLinkedList()";
				Object[] thisArray = dll.toArray();
				int i;
				for (i = 0; i < thisArray.length; i++) {
					Object obj = thisArray[i];
					if (obj == null) {
						code += "dll.add(0);";
					} else if (obj instanceof IntegerMock) {
						code += "dll.add(" + ((IntegerMock) obj).intValue() + ");";
					}
				}
				if (dll.size() > 0 && i == 0) {
					code += "dll.add(0);";
				}
				
				code += "s.method_under_test(";
				if (parameters != null && parameters.length > 0) {
					for (int j = 0; j < parameters.length; j++) {
						code += "ELEMENT_0_" + j;
						if (j > 0 && j < parameters.length - 1) {
							code += ", ";
						}
					}
				}
				code += ");";
				
				BlockStmt block = JavaParser.parseBlock("{" + code + "}");
				testCases.add(block);
			} catch (Throwable e) {
				logger.error("Unable to parse JBSE test case", e);
			}
		}

		else {
			logger.fatal("Unable to generate test cases for JBSE");
			throw new GenerationException("Unable to generate test cases for JBSE: no dynamic analysis defined");
		}
	}

	private static String getStackAddMethod() {
		if (Options.I().getTargetMethod().contains("push")) {
			return "add";
		} else {
			return "push";
		}
	}

}
