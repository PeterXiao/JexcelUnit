package jexcelunit.testinvoker;


import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Ŭ���� ���� : Reflection�� ���� ���� �׽��� �ڵ�.
 * �� Ŭ������ import�ϰԵ� �κ��� ���ԵǸ� �� ���ֵ�, CoffeeMaker�� �˰� �����ʴ�. ��,Ư�� ������Ʈ�� ������ ����.
 * �� Ŭ������ ��ӹ޾� �׽�Ʈ�ϰ��� �ϴ� ������Ʈ�� �°� ����ϸ� �ȴ�.
 * Date: 2016/03/18
 * Student Num : 2010112469
 * Major : ��ǻ�� ���� 
 * Name : ������ 
 * (���÷����� ����ϸ� �׽�Ʈ�޼ҵ�� ����� �� ���ϴ� �׽�Ʈ ��ü�� ������ �и��� ����)
 * 
 * 
 * 
 * 2017-03-01
 * Interface �̽� .
 * Testing ��ư�� ��������. 
 * JUnit �׽�Ʈ�� �ڵ����� �����ٰǰ�...
 *  1. TestInvoker �� ��ӹ���  Suite Ŭ������ �ϳ� �������ش�. Mock ��ü.
 *  2. JUnit�� ���� ��Ÿ�� ȯ���� �ϳ� ���� �������.. �̰��� �����ɸ�����. JUnit�� �������ϴϱ�;
 *  3. 
 **/
@RunWith(Parameterized.class) //�׽�Ʈ ���̽��� �̿��Ұ��̴�.
public class TestInvoker {
	private static Map<Class, Object> classmap= new HashMap<Class, Object>(); //�ؽ������� �׽�Ʈ�� �ʿ��� ��ü���� �ϳ����� �����Ѵ�.
	private static ArrayList<Class> exceptionlist=new ArrayList<Class>();//����� ���� ���� Ŭ�������� ��Ƶδ� ��.
	private static Method[] methods; //�׽�Ʈ�� ��ü�� �޼ҵ带 �޴ºκ�
	private static int testnumber=0; //�׽�Ʈ run �ѹ�

	//�׽�Ʈ ���̽����� Ȯ���� method_params
	private Object[] constructor_params=null;
	private String testname=null, methodname=null;
	private Object[] method_params=null;
	private Object expectedResult=null;
	private Class targetclz=null;

	//�׽�Ʈ�̸�, �׽�Ʈ�� Ŭ����, ������, �׽�Ʈ�� �޼ҵ��̸�, �Ķ���͵�. �׽�Ʈ���̽��� JUnit�� �о�� �����Ű�� �κ��̴�.
	public TestInvoker(String testname,Class targetclz,Object[] constructor_params,Object expectedResult,String methodname,Object[] param1){
		this.testname= (String)testname;
		this.targetclz=targetclz;
		this.constructor_params=constructor_params;
		this.expectedResult=expectedResult;
		this.methodname=(String) methodname;
		this.method_params=param1;
	}

	//����ڰ� �����ϴ� �ͼ����� �ִ°�� �� �Լ��� ���� �����ش�
	public static void addException(Class e){
		exceptionlist.add(e);
	}

	private void handleException(Exception e){
		//e.printStackTrace();
		StackTraceElement[] exceptionClass=e.getCause().getStackTrace();
		if(exceptionClass!=null)
			for (StackTraceElement s: exceptionClass){ //���÷��� �ͼ����� ������ stacktrace ���
				System.out.println(s);
				if(s.getClassName().equals(Method.class.getName()))break;
			}
		if(!exceptionlist.isEmpty())
			for(Class ex :exceptionlist){
				if(e.getCause().getClass().equals(ex)){
					System.out.println(e.getCause()); //���� ���� ���
					break;
				}
			}
	}
	private Class unBoxing(Class wrapper){
		switch(wrapper.getTypeName().charAt(10)){
		case 'S': return wrapper.getTypeName().contains("Short")?short.class:String.class;							
		case 'B': return wrapper.getTypeName().contains("Byte")?Byte.class:Boolean.class;
		case 'C':return char.class;
		case 'I':return int.class;
		case 'L':return long.class;
		case 'D':return double.class;
		case 'F':return float.class;
		case 'V':return void.class;
		default : return null;
		}
	}
	
	private Constructor findConstructor(ArrayList<Class> paramclzlist){
		Constructor con=null;
		Class[] paramclz=null, temp=null;
		try{
			if(paramclzlist.size() > 1){//need unboxing
				paramclz=new Class[paramclzlist.size()]; int index=0;
				for(Class c: paramclzlist){
					if(isNeedUnBoxing(c)){
						paramclz[index++]=unBoxing(c);
					}else paramclz[index++]=c;			
				}
				//for(Class c : paramclz) System.out.println(c);
				con =targetclz.getConstructor(paramclz);
			}
			else{ 
				paramclz=new Class[]{(Class)paramclzlist.get(0)};
				con =targetclz.getConstructor(paramclz);
			}


		}catch (Exception e){
			handleException(e);
		}
		return con;
	}

	@SuppressWarnings("unused")
	@Before
	public void setObj(){
		if(!classmap.containsKey(targetclz)&& methodname !=null){ //������ ��ü�� ���°��
			//System.out.println(classmap.containsKey(targetclz)+"���λ���");
			try{
				ArrayList<Class> paramclzlist=new ArrayList<Class>();
				Constructor con=null;
				if( constructor_params!=null){
					for(int i=0; i< constructor_params.length;i++){
						//System.out.println(i + " : " + constructor_params[i]);
						paramclzlist.add(constructor_params[i].getClass());
					}
					con=findConstructor(paramclzlist);	
				}
				if(con !=null){
					con.setAccessible(true);
					classmap.put(targetclz, con.newInstance(constructor_params));
				}
				else{
					con=targetclz.getDeclaredConstructor();
					con.setAccessible(true);
					classmap.put(targetclz, con.newInstance());
				}
			}catch(Exception e){handleException(e);}
		}
	}

	private void constructor_test(){
		System.out.println( "\n"+(testnumber++) + " : "+testname +"\n �׽�Ʈ Ŭ���� : " +targetclz.getSimpleName());//�׽�Ʈ ��ȣ�� ���ü�κ���  �׽�Ʈ�� �̷�����������
		try{
			ArrayList<Class> paramclzlist=new ArrayList<Class>();
			Constructor con=null;
			if( constructor_params!=null){
				//System.out.println("constructor_params !=null");
				for(int i=0; i< constructor_params.length;i++){
					paramclzlist.add(constructor_params[i].getClass());
				}
				//System.out.println("add params");
				con=findConstructor(paramclzlist);
				//System.out.println("get constructor");
				if(con !=null){
					//System.out.println("con !=null");
					con.setAccessible(true);
					assertNotNull(con.newInstance(constructor_params));
				}
				else{
					fail();
				}
			}
			else{
				con=targetclz.getDeclaredConstructor();
				con.setAccessible(true);
				assertNotNull(con.newInstance());
			}
		}catch(Exception e){handleException(e);}
	}
	private boolean isNeedUnBoxing(Class clz){
		if(clz.isPrimitive() || (clz.getSuperclass()==Number.class)||
				(clz==String.class) ||(clz==Character.class)
				||(clz==Boolean.class)){ //���ð� �׽�Ʈ
			return true;
		}
		else
			return false;
	}

	/**********************************************************************
	 * �̸�         : auto_Assert
	 * �Ķ����   : 
	 * 			Object testresult	: �׽�Ʈ �޼ҵ带 ������ �� ���� ���� ��ü. ����� ���ǰ�ü�̰ų�,  
	 * 			Field  f			: testresult�� �ɹ� ������ �̸�
	 * 			Class  memeberclz	: testresult�� �ɹ� ������ Ÿ�� 
	 * ����         : �������� �����ش�. PrimitiveType�� �ƴѰ�� ��ü ������ �ʵ尪�� ������ ������ �����ش�.
	 ********************************************************************** */
	private void auto_Assert(Object testresult, Field f,Class memeberclz ) throws Exception{
		if(isNeedUnBoxing(memeberclz) ){
			System.out.println( "Assert ���  (����/�׽�Ʈ���): "+f.get(expectedResult)+ " "+f.get(testresult));
			assertThat(f.get(testresult),is(f.get(expectedResult)));
		}
		else if(memeberclz.isArray()){//�迭���� ��
			if(Array.getLength(f.get(testresult)) == Array.getLength(f.get(expectedResult))){
				for(int i= 0; i<Array.getLength(f.get(testresult)); i++){
					if(Array.get(f.get(testresult), i)!=null &&Array.get(f.get(expectedResult), i)!=null){
						System.out.println( "Assert ���  (����/�׽�Ʈ���): "+Array.get(f.get(expectedResult), i)+ " "+Array.get(f.get(testresult), i));
						assertThat(Array.get(f.get(testresult), i), is(Array.get(f.get(expectedResult), i)));
					}
				}
			}
		}
	}	

	private Method get_TargetMethod() throws Exception{
		Method target=null;
		Class[] types=null;
//		if(method_params !=null){
//			types = new Class[method_params.length];
//			int index=0;
//			for(Object p: method_params){
//				System.out.println(p.getClass());
//				types[index++] = unBoxing(p.getClass());
//			}
//		}
//		target= (types !=null)?targetclz.getMethod(methodname, types):targetclz.getDeclaredMethod(methodname);
		Method[] methods = targetclz.getMethods();
		for(Method m : methods)
			if(m.getName().equals(methodname))
				target=m;
		return target;
	}

	@Test
	public void testMethod() {
		//setObj();
		if(methodname==null){ //������ �׽�Ʈ�� ���.
			constructor_test();
			return;
		}

		Method targetmethod=null;
		Object testresult=null;	
		System.out.println( "\n"+(testnumber++) + " : "+testname +"\n �׽�Ʈ Ŭ���� : " +targetclz.getSimpleName());//�׽�Ʈ ��ȣ�� ���ü�κ���  �׽�Ʈ�� �̷�����������
		try {
			
			targetmethod=get_TargetMethod();
			if(targetmethod!=null)
				targetmethod.setAccessible(true);//private �޼ҵ带 �׽�Ʈ�ϱ� ����
			
			System.out.println("�׽�Ʈ �޼ҵ� : "+targetmethod.getName()); //�޼ҵ� �̸����
			testresult=targetmethod.invoke(classmap.get(targetclz), method_params); 				

			if(expectedResult !=null){
				if(isNeedUnBoxing(expectedResult.getClass())){ //���ð� �׽�Ʈ
					System.out.println( "Assert ���  (����/�׽�Ʈ���): " +expectedResult +" " +testresult); //�������� ������� ���
					//toString �������̵��� ���� ��ü ���¸� �ϴ� ������ �����ٸ�, �̰��� ��ǲ ��ü�� ���¸� ��°����ϴ�.
					assertThat(testresult,is(expectedResult)); //�׽��� ����� Ȯ��.
				}
				else{//����� ���ð�ü�� �ƴ� ���� ��ü�ΰ��
					Class type =expectedResult.getClass();
					Field[] flz =type.getDeclaredFields();
					for(Field f: flz){
						if (!f.isSynthetic()){
							f.setAccessible(true);
							Class memeberclz=f.getType();
							System.out.println(memeberclz.getSimpleName()+ " "+f.getName());
							auto_Assert(testresult, f, memeberclz);
						}
					}
				}

			}
		} catch (Exception e){	
			//���÷��� ��ü�� java.lang.reflect.InvocationTargetException �ͼ����� ������ �ȴ�.
			//���� StackTrace���� caused �� ������ ã�Ƽ� ĳġ�ؾ��Ѵ�. �Ʒ��� caused�� �ͼ��� Ŭ������ �о�´�.
			handleException(e);
		}

		if(targetmethod !=null)//�޼ҵ尡 �������Ǿ��ٸ�,
			System.out.println("�׽�Ʈ �Ϸ�");
		else {System.out.println("�ش� �޼ҵ尡 �������� �ʽ��ϴ�."); fail();}//�޼ҵ带 Ž�������� ���°��.
	}
}