package sbes.stub.generator;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import sbes.result.CarvingResult;
import sbes.stub.Stub;

public class SecondStageGenericStubGenerator extends SecondStageStubGenerator {

	private String concreteClass;
	
	public SecondStageGenericStubGenerator(Stub stub, CarvingResult candidateES, String concreteClass) {
		super(stub, candidateES);
		this.concreteClass = concreteClass;
	}
	
	@Override
	protected TypeDeclaration getClassDeclaration(String className) {
		stubName = className + STUB_EXTENSION + "_2";
		String genericDecl = "<E extends " + concreteClass + ">";
		
		// extends base class
		ClassOrInterfaceType extendClassDecl = new ClassOrInterfaceType(className + "<E>");
		List<ClassOrInterfaceType> extendClasses = new ArrayList<ClassOrInterfaceType>();
		extendClasses.add(extendClassDecl);
		
		ClassOrInterfaceDeclaration classDecl = new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, stubName + genericDecl);
		classDecl.setExtends(extendClasses);
		
		return classDecl;
	}

}
