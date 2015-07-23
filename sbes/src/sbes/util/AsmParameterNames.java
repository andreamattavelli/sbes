package sbes.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import sbes.execution.InternalClassloader;
import sbes.option.Options;

public class AsmParameterNames {
	
	public static boolean isSizeParam(String param) {
		if (param.contains("index") || param.contains("size") || param.contains("capacity") ||
				param.contains("Index") || param.contains("Size") || param.contains("Capacity")) {
			return true;
		}
		return false;
	}
	
	public static String[] getParameterNames(AccessibleObject methodOrConstructor) {
		Class<?>[] types = null;
		Class<?> declaringClass = null;
		String name = null;
		if (methodOrConstructor instanceof Method) {
			Method method = (Method) methodOrConstructor;
			types = method.getParameterTypes();
			name = method.getName();
			declaringClass = method.getDeclaringClass();
		} else {
			Constructor<?> constructor = (Constructor<?>) methodOrConstructor;
			types = constructor.getParameterTypes();
			declaringClass = constructor.getDeclaringClass();
			name = "<init>";
		}

		if (types.length == 0) {
			return new String[0];
		}
		
		InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath());
		ClassLoader classLoader = ic.getClassLoader();
		if (classLoader == null) {
			classLoader = ClassLoader.getSystemClassLoader();
		}
		
		String cname = declaringClass.getName().replace('.', '/') + ".class";
		// better pre-cache all methods otherwise this content will be loaded
		// multiple times
		InputStream asStream = classLoader.getResourceAsStream(cname);
		if (asStream == null) {
			asStream = AsmParameterNames.class.getResourceAsStream(cname);
		}
		InputStream content = asStream;
		
		if (content == null) {
			return new String[0];
		}
		try {
			ClassReader reader = new ClassReader(content);
			TypeCollector visitor = new TypeCollector(null, name, types);
			reader.accept(visitor, ClassReader.SKIP_CODE & ClassReader.SKIP_FRAMES);
			
			List<String> l = visitor.getParameterNames();
			if (l == null) {
				return new String[0];
			}
			else {
				return l.toArray(new String[0]);
			}
		} catch (IOException e) {
			return new String[0];
		}
	}
}

class TypeCollector extends ClassVisitor {

	private String methodName;
	private Class<?>[] parametersType;
	private ParameterVisitor mv = null;
	private List<String> parameterNames;
	
	public TypeCollector(ClassVisitor cv, String methodName, Class<?>[] parametersType) {
		super(Opcodes.ASM5, cv);
		this.methodName = methodName;
		this.parametersType = parametersType;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (mv != null) {
			return null;
		}
		else {
			if (isMethod(name, desc) && !((access & Opcodes.ACC_NATIVE) == Opcodes.ACC_NATIVE)) {
				mv = new ParameterVisitor(super.visitMethod(access, name, desc, signature, exceptions), indexLimits(desc));
			}

			return mv;
		}
	}
	
	@Override
	public void visitEnd() {
		if (mv != null) {
			parameterNames = new ArrayList<String>(mv.getParameterNames());
		}
	}

	private boolean isMethod(String name, String desc) {
		boolean isMethod = false;
		
		// check method name
		if (name.equals(methodName)) {
			//check arguments
			Type argsType[] = Type.getArgumentTypes(desc);
			if (argsType.length == parametersType.length) {
				int i = 0;
				for (; i < argsType.length; i++) {
					if (!argsType[i].getClassName().equals(parametersType[i].getCanonicalName())) {
						break;
					}
				}
				
				if (i == argsType.length) {
					isMethod = true;
				}
			}
		}
		
		return isMethod;		
	}
	
	private int indexLimits(String desc) {
		int toReturn = 0;
		
		Type argsType[] = Type.getArgumentTypes(desc);
		for (Type type : argsType) {
			// long and double requires 64-bits
			if (type.getDescriptor().equals("J") || 
				type.getDescriptor().equals("D")) {
				toReturn++;
			}
			toReturn++;
		}
		
		return toReturn;
	}
	
	public List<String> getParameterNames() {
		return parameterNames;
	}
	
}

class ParameterVisitor extends MethodVisitor {

	private int indexLimit;
	private List<String> parameterNames;
	
	public ParameterVisitor(MethodVisitor mv, int indexLimit) {
		super(Opcodes.ASM5, mv);
		this.indexLimit = indexLimit;
		this.parameterNames = new ArrayList<String>();
	}
	
	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		if (index > 0 && index <= indexLimit) {
			parameterNames.add(name);
		}
	}
	
	public List<String> getParameterNames() {
		return parameterNames;
	}
	
}
