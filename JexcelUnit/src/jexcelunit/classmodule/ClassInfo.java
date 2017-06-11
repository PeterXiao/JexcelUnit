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


	public ClassInfo(Class clz){
		this.clz=clz;
		if(!clz.isPrimitive())
			name= clz.getSimpleName();
		else name= clz.getName();
		ClassInfoMap.INSTANCE.getInstance().put(name, this);
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
		fields= clz.getDeclaredFields();
		methods = clz.getDeclaredMethods();

		if(!PrimitiveChecker.isPrimitive(clz) && !clz.isSynthetic() && !clz.isAnonymousClass()){
			//Field Info Create
			for(int i=0; i<fields.length; i++){
				addChildren(new ParameterInfo(fields[i]));
			}
			//Constructor Info Create
			for (int i =0; i<constructors.length; i++) {
				addChildren(new ConstructorInfo(constructors[i]));
			}
			//Method Info Create
			for(int i=0; i<methods.length; i++){
				addChildren(new MethodInfo(methods[i]));
			}
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

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.name+ " : Class";
	}

}