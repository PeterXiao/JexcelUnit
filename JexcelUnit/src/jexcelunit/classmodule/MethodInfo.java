package jexcelunit.classmodule;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class MethodInfo extends Info{

	private Method method;
	private ClassInfo returnClass;
	
	public MethodInfo(Method method){
		//Method ��  Name ����
		this.method= method;
		name= method.getName();
		
		//Method Return Type ����
		Map<String, ClassInfo> classInfoMap = ClassInfoMap.INSTANCE.getInstance();
		Class<?> returnType= method.getReturnType();
		 if(classInfoMap.containsKey(returnType.getSimpleName())){
			 returnClass= classInfoMap.get(returnType.getSimpleName());
		 }
		 else {
			 returnClass= new ClassInfo(returnType);
			 classInfoMap.put(returnClass.getName(), returnClass);
		 }
		 
		//Parameter ����
		Parameter[] params= method.getParameters();
		for(int i=0; i< params.length; i++)
			addChildren(new ParameterInfo(params[i]));
		
		//not implemented yet. about sequences;
	}

	
	
	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public ClassInfo getReturnClass() {
		return returnClass;
	}
	public void setReturnClass(ClassInfo returnClass) {
		this.returnClass = returnClass;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return returnClass.getName() +' '+this.name+ " : Method ";
	}
	
}
