package sbes.stub.generator;

import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class SecondPhaseStrategy extends Generator {

	@Override
	protected TypeDeclaration getClassDeclaration(String className) {
		stubName = className + STUB_EXTENSION + "_2";
		return new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, stubName);
	}

	@Override
	protected List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c) {
		return new ArrayList<BodyDeclaration>();
	}

	@Override
	protected MethodDeclaration createMethodUnderTest() {
		return null;
	}

	@Override
	protected MethodDeclaration createSetResultsMethod(Method targetMethod) {
		return null;
	}

	@Override
	protected List<BodyDeclaration> additionalMethods(Method[] methods) {
		return null;
	}

}
