package sbes.stub.generator;

import japa.parser.ASTHelper;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class SecondPhaseStrategy extends Generator {

	@Override
	protected List<ImportDeclaration> getImports() {
		List<ImportDeclaration> imports = new ArrayList<>();
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("sbes.distance.Distance"), false, false));
		return imports;
	}
	
	@Override
	protected TypeDeclaration getClassDeclaration(String className) {
		stubName = className + STUB_EXTENSION + "_2";
		
		// extends base class
		ClassOrInterfaceType extendClassDecl = new ClassOrInterfaceType(className);
		List<ClassOrInterfaceType> extendClasses = new ArrayList<ClassOrInterfaceType>();
		extendClasses.add(extendClassDecl);
		
		ClassOrInterfaceDeclaration classDecl = new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, stubName);
		classDecl.setExtends(extendClasses);
		
		return classDecl;
	}

	@Override
	protected List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c) {
		return new ArrayList<BodyDeclaration>();
	}
	
	@Override
	protected List<BodyDeclaration> getAdditionalMethods(Method[] methods) {
		return new ArrayList<BodyDeclaration>();
	}

	@Override
	protected MethodDeclaration getMethodUnderTest() {
		return null;
	}

	@Override
	protected MethodDeclaration getSetResultsMethod(Method targetMethod) {
		return null;
	}

}
