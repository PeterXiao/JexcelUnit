package jexcelunit.classmodule;

import org.eclipse.core.runtime.IAdaptable;



/*
 * Tree Type 1. Class 2.Method 3. Constructor 4. Field 5. Parameter
 *  1. Class �� 2,3,4�� ���´�
 *  2. 5�� ���´�. 
 *  3. 5�� ���´�.
 *  4. 1�� ����. field Name�� ������.
 *  5. 1�� ����. Parameter Name�� ������.
 * */

public abstract class Info implements IAdaptable {
	protected String name;
	protected Info parent;
	
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

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}
	
}
