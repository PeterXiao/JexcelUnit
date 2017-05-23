package jexcelunit.classmodule;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


@SuppressWarnings("rawtypes")
public class ClassInfo extends Info{
	

	Class clz=null;
	
	Constructor[] constructors;
	Method[] methods;
	Field[] fields;
	
	ParameterInfo[] fieldInfos;
	MethodInfo[] methodInfos;
	ConstructorInfo[] cosntructorInfos;
	
	public ClassInfo(Class clz){
		this.clz=clz;
		initialize();
	}
	// for Tree
/*
 * 1.  �޸� �̽� // ��������� init�� ���ں��� ������ ������ �� �ִ¤� ������ �߻�.
 *  => ClassAnalyzer�� �̱������� �����ϰ�, �޸� ������  �̰����� �ϵ��� ���� . Static HashSet Ȥ�� Map ���̿��Ͽ� ���۷��� ���踸 ������ ��. 
 * 2. ClassInfo ���� Constructor & Method �� Raw Type���� ���� ���ΰ�. Info Type���� ���� ���ΰ�. ???
 * 3. 
 * */
	
	// for infos
	private void initialize(){
		constructors= clz.getDeclaredConstructors();
		cosntructorInfos = new ConstructorInfo[constructors.length];
		for (int i =0; i<constructors.length; i++) {
			cosntructorInfos[i] = new ConstructorInfo(constructors[i]);
		}
		fields= clz.getDeclaredFields();
		fieldInfos= new ParameterInfo[fields.length];
		for(int i=0; i<fields.length; i++){
			fieldInfos[i] =new ParameterInfo(fields[i]);
		}
		
		methods = clz.getDeclaredMethods();
		methodInfos= new MethodInfo[methods.length];
		for(int i=0; i<methods.length; i++){
			methodInfos[i]=  new MethodInfo(methods[i]);
		}
		
	}
	
	public Constructor[] getConstructors() {
		return constructors;
	}
	public void setConstructors(Constructor[] constructors) {
		this.constructors = constructors;
	}
	public Method[] getMethods() {
		return methods;
	}
	public void setMethods(Method[] methods) {
		this.methods = methods;
	}
	public Field[] getFields() {
		return fields;
	}
	public void setFields(Field[] fields) {
		this.fields = fields;
	}
	
	public Class getClz() {
		return clz;
	}

	public void setClz(Class clz) {
		this.clz = clz;
	}

}