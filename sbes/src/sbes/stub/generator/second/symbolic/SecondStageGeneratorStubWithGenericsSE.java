package sbes.stub.generator.second.symbolic;

import japa.parser.ASTHelper;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclaratorId;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.Stub;
import sbes.util.ReflectionUtils;

public class SecondStageGeneratorStubWithGenericsSE extends SecondStageGeneratorStubSE {

	private Map<TypeVariable<?>, String> genericToConcreteClasses;

	public SecondStageGeneratorStubWithGenericsSE(
			final List<TestScenario> scenarios, 
			final Stub stub,
			final CarvingResult candidateES,
			final Map<TypeVariable<?>, String> genericToConcreteClasses) {
		super(scenarios, stub, candidateES);
		this.genericToConcreteClasses = genericToConcreteClasses;
		if (genericToConcreteClasses.containsValue("Integer")) {
			for (TypeVariable<?> var : genericToConcreteClasses.keySet()) {
				if (genericToConcreteClasses.get(var).equals("Integer")) {
					genericToConcreteClasses.put(var, "IntegerMock");
					break;
				}
			}
		}
	}
	
	@Override
	protected List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c) {
		List<BodyDeclaration> fields = new ArrayList<>();
		// return fields
		if (!targetMethod.getReturnType().equals(void.class)) {
			String resultType = getActualResultType(targetMethod);
			fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType(resultType, 0), EXP_RES));
			fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType(resultType, 0), ACT_RES));
		}
		// exception fields
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType("Exception", 0), "e1"));
		fields.add(ASTHelper.createFieldDeclaration(0, ASTHelper.createReferenceType("Exception", 0), "e2"));
		Type[] genericParams = targetMethod.getGenericParameterTypes();
		Class<?>[] concreteParams = targetMethod.getParameterTypes();
		List<Parameter> param = getGenericParameterType(targetMethod, genericParams, concreteParams);
		for (int i = 0; i < param.size(); i++) {
			Parameter p = param.get(i);
			fields.add(ASTHelper.createFieldDeclaration(0, p.getType(), p.getId().getName()));
		}
		return fields;
	}
	
	@Override
	protected String getActualResultType(Method targetMethod) {
		String className = targetMethod.getGenericReturnType().toString();
		Set<TypeVariable<?>> types = genericToConcreteClasses.keySet();
		for (TypeVariable<?> typeVariable : types) {
			if (className.contains(typeVariable.toString())) {
				className = className.replaceAll(typeVariable.toString(), genericToConcreteClasses.get(typeVariable));
			}
		}

		return className;
	}
	
	protected List<Parameter> getGenericParameterType(Method targetMethod, Type[] genericParams, Class<?>[] concreteParams) {
		List<Parameter> toReturn = new ArrayList<Parameter>();
		for (int i = 0; i < genericParams.length; i++) {
			Type type = genericParams[i];
			VariableDeclaratorId id = new VariableDeclaratorId("p" + i);
			String typeClass;
			if (type instanceof TypeVariable<?>) {
				typeClass = type.toString();
				Set<TypeVariable<?>> types = genericToConcreteClasses.keySet();
				for (TypeVariable<?> typeVariable : types) {
					if (typeClass.contains(typeVariable.toString())) {
						typeClass = typeClass.replaceAll(typeVariable.toString(), genericToConcreteClasses.get(typeVariable));
					}
				}
			} 
			else if (type instanceof ParameterizedType) {
				typeClass = type.toString();
				Set<TypeVariable<?>> types = genericToConcreteClasses.keySet();
				for (TypeVariable<?> typeVariable : types) {
					if (typeClass.contains(typeVariable.toString())) {
						typeClass = typeClass.replaceAll(typeVariable.toString(), genericToConcreteClasses.get(typeVariable));
					}
				}
			}
			else {
				typeClass = concreteParams[i].getCanonicalName();
				typeClass = typeClass.indexOf(" ") >= 0 ? typeClass.split(" ")[1] : typeClass;
			}
			
			int dimensions = ReflectionUtils.getArrayDimensionCount(concreteParams[i]);
			Parameter p = new Parameter(ASTHelper.createReferenceType(typeClass, dimensions), id);
			toReturn.add(p);
		}

		return toReturn;
	}
	
}
