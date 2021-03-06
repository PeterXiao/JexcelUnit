package jexcelunit.classmodule;

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;



/*
 * Tree Type 1. Class 2.Method 3. Constructor 4. Field 5. Parameter
 *  1. Class 는 2,3,4를 갖는다
 *  2. 5를 갖는다. 
 *  3. 5를 갖는다.
 *  4. 1과 같다. field Name을 가진다.
 *  5. 1과 같다. Parameter Name을 가진다.
 * */

public abstract class Info implements IAdaptable {
	protected String name;
	protected Info parent;
	protected ArrayList<Info> children = new ArrayList<>();
	
	
	@Override
	public <T> T getAdapter(Class<T> arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Info getParent() {
		return parent;
	}

	public void setParent(Info parent) {
		this.parent = parent;
	}

	public ArrayList<Info> getChildren(){
		return children;
	}
	public void addChildren(Info child){
		if(this.children !=null)
			this.children.add(child);
	}
	
	public boolean hasChildren(){
		if((children!=null))
			return children.size()>0?true: false;
		return false;
	}
	@SuppressWarnings("rawtypes")
	public static ClassInfo checkClassInfos(Class clz){
		ClassInfo result= null;
		result= ClassInfoMap.INSTANCE.getInfos().get(clz.getSimpleName());
		if(result ==null)
			result = ClassInfoMap.INSTANCE.getInfos().get(clz.getName());
		return result;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}
	
}
