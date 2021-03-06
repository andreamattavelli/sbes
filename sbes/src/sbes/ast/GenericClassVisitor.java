package sbes.ast;

import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

import sbes.util.ASTUtils;

public class GenericClassVisitor extends VoidVisitorAdapter<Void> {

	private String className;
	private List<String> generics;
	
	public GenericClassVisitor(final String className) {
		this.className = className;
		this.generics = new ArrayList<String>();
	}
	
	public List<String> getConcreteClasses() {
		return generics;
	}
	
	@Override
	public void visit(final VariableDeclarationExpr vde, final Void arg1) {
		if (vde.getType() instanceof ReferenceType) {
			ReferenceType refType = (ReferenceType) vde.getType();
			if (refType.getType() instanceof ClassOrInterfaceType) {
				ClassOrInterfaceType coit = (ClassOrInterfaceType) refType.getType();
				if (coit.getName().startsWith(className)) {
					if (coit.getTypeArgs() != null) {
						for (Type t : coit.getTypeArgs()) {
							if (!generics.contains(t.toString())) {
								generics.add(t.toString());
							}
						}
					}
				}
				else if (vde.getVars().get(0).getInit() instanceof MethodCallExpr) {
					MethodCallExpr mce = (MethodCallExpr) vde.getVars().get(0).getInit();
					if (mce.getScope() instanceof NameExpr && ASTUtils.getName(mce.getScope()).equals(className)) {
						if (coit.getTypeArgs() != null) {
							for (Type t : coit.getTypeArgs()) {
								if (!generics.contains(t.toString())) {
									generics.add(t.toString());
								}
							}
						}
					}
				}
			}
		}
		
		super.visit(vde, arg1);
	}
	
}
