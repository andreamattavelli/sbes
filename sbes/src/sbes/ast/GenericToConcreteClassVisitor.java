package sbes.ast;

import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class GenericToConcreteClassVisitor extends VoidVisitorAdapter<Void> {

	private String className;
	private List<String> generics;
	
	public GenericToConcreteClassVisitor(String className) {
		this.className = className;
		this.generics = new ArrayList<String>();
	}
	
	public List<String> getConcreteClass() {
		return generics;
	}
	
	@Override
	public void visit(VariableDeclarationExpr vde, Void arg1) {
		if (vde.getType() instanceof ReferenceType) {
			ReferenceType refType = (ReferenceType) vde.getType();
			if (refType.getType() instanceof ClassOrInterfaceType) {
				ClassOrInterfaceType coit = (ClassOrInterfaceType) refType.getType();
				if (coit.getName().startsWith(className)) {
					if (coit.getTypeArgs() != null) {
						for (Type t : coit.getTypeArgs()) {
							generics.add(t.toString());
						}
					}
				}
			}
		}
		
		super.visit(vde, arg1);
	}
	
}
