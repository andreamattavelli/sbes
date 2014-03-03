package sbes.ast;

import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class GenericToConcreteClassVisitor extends VoidVisitorAdapter<Void> {

	private String className;
	private String concreteClass;
	
	public GenericToConcreteClassVisitor(String className) {
		this.className = className;
	}
	
	public String getConcreteClass() {
		return concreteClass;
	}
	
	@Override
	public void visit(VariableDeclarationExpr vde, Void arg1) {
		if (vde.getType() instanceof ReferenceType) {
			ReferenceType refType = (ReferenceType) vde.getType();
			if (refType.getType() instanceof ClassOrInterfaceType) {
				ClassOrInterfaceType coit = (ClassOrInterfaceType) refType.getType();
				if (coit.getName().startsWith(className)) {
					if (coit.getTypeArgs() != null) {
						concreteClass = coit.getTypeArgs().toString().replaceAll("\\[", "").replaceAll("\\]", "");
					}
				}
			}
		}
		
		super.visit(vde, arg1);
	}
	
}
