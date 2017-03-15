package jexcelunit.testinvoker;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jexcelunit.excel.ExcelReader;
import jexcelunit.excel.TestcaseVO;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/*****
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
@SuppressWarnings("rawtypes")
@RunWith(Parameterized.class) //�׽�Ʈ ���̽��� �̿��Ұ��̴�.
public class TestInvoker {
	private static Map<Class, Object> classmap= new HashMap<Class, Object>(); //�ؽ������� �׽�Ʈ�� �ʿ��� ��ü���� �ϳ����� �����Ѵ�.
	private static ArrayList<Class> exceptionlist=new ArrayList<Class>();//����� ���� ���� Ŭ�������� ��Ƶδ� ��.
	//	private static Method[] methods; //�׽�Ʈ�� ��ü�� �޼ҵ带 �޴ºκ�
	protected static HashMap<String,Object> mock=new HashMap<String,Object>();//��ũ��ü ����
	private static int suitenumber=0;
	private static int testnumber=0; //�׽�Ʈ run �ѹ�

	//�׽�Ʈ ���̽����� Ȯ���� method_params
	private int suite;
	private String testname=null;
	private Class targetclz=null;
	private Constructor constructor = null;
	private Object[] constructor_params=null;
	private Method targetmethod=null;
	private Object[] method_params=null;
	private Object expectedResult=null;


	//�׽�Ʈ�̸�, �׽�Ʈ�� Ŭ����, �׽�Ʈ�Ķ����,  �׽�Ʈ�� �޼ҵ��̸�, �Ķ���͵�,�������� JUnit�� �о�� �����Ű�� �κ��̴�.
	public TestInvoker(int suite,String testname,Class targetclz,Constructor constructor,Object[] constructor_params,Method targetmethod,Object[] param1,Object expectedResult){
		this.suite=suite;
		this.testname= (String)testname;
		this.targetclz=targetclz;
		this.constructor=constructor;
		this.constructor_params=constructor_params;
		this.expectedResult=expectedResult;
		this.targetmethod=targetmethod;
		this.method_params=param1;
	}

	public static Collection parmeterizingExcel(String fileName){
		ExcelReader reader = new ExcelReader();
		//��Ÿ�����͸� ������ �� �ۿ�����.
		//�ڵ鷯 �������� Ÿ�� ������Ʈ ������ �����Ұ�.
		File file = new File(".");
		ArrayList<ArrayList<TestcaseVO>> testcases=null;
		Object[][] parameterized= null;

		if(file.exists()){
			try {
				testcases = reader.readExcel(fileName, file.getCanonicalPath());

				if(testcases.size()>0)
				{
					int total_row_index=0;
					for(ArrayList<TestcaseVO> testcase : testcases){
						total_row_index+=testcase.size();
					}
					parameterized = new Object[total_row_index][8];

					int row_index=0;
					for(ArrayList<TestcaseVO> testcase : testcases){
						if(row_index < total_row_index){
							for(TestcaseVO currentCase: testcase){
								parameterized[row_index][0]=currentCase.getSuiteNumber();
								parameterized[row_index][1]=currentCase.getTestname();
								parameterized[row_index][2]=currentCase.getTestclass();
								parameterized[row_index][3]=currentCase.getConstructor();
								parameterized[row_index][4]=currentCase.getConstructorParams().toArray();
								parameterized[row_index][5]=currentCase.getMet();
								parameterized[row_index][6]=currentCase.getMethodParams().toArray();
								parameterized[row_index][7]=currentCase.getExpect();	
								row_index++;
							}
						}
					}

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//�о���� ����Ʈ�� String, Class, Object[] Object, String Object�� �ٱ����.
		return Arrays.asList(parameterized);
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
		case 'B': return wrapper.getTypeName().contains("Byte")?Byte.class:boolean.class;
		case 'C':return char.class;
		case 'I':return int.class;
		case 'L':return long.class;
		case 'D':return double.class;
		case 'F':return float.class;
		case 'V':return void.class;
		default : return null;
		}
	}


	@Before
	public void setObj(){
		if(suitenumber !=suite){ //���ο� �ó����� �׽�Ʈ.
			classmap.clear();
		}
		if(!classmap.containsKey(targetclz)&& targetmethod !=null){ //������ ��ü�� ���°��
			//System.out.println(classmap.containsKey(targetclz)+"���λ���");
			constructor.setAccessible(true);
			try {
				if(constructor_params.length==0)
					classmap.put(targetclz, constructor.newInstance());

				else{
					Class[] paramTypes=constructor.getParameterTypes();
					Object[] params= getMock(paramTypes,constructor_params);
					classmap.put(targetclz, constructor.newInstance(params));
				}

			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				handleException(e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Object[] getMock(Class[] types, Object[] params){
		for(int i= 0; i<types.length; i++){
			Class paramClass=params[i].getClass();
			if(isNeedUnBoxing(paramClass))
				paramClass= unBoxing(paramClass);

			if(!types[i].equals(paramClass)){
				Object mockObject=mock.get(params[i]);
				if(mockObject.getClass().equals(types[i]) && mockObject!=null){
					params[i]=mockObject;
				}else
					throw new IllegalArgumentException("Wrong Argument Type");
			}
		}
		return params;
	} 

	private void constructor_test(){
		System.out.println( "\n"+(testnumber++) + " : "+testname +"\n �׽�Ʈ Ŭ���� : " +targetclz.getSimpleName());//�׽�Ʈ ��ȣ�� ���ü�κ���  �׽�Ʈ�� �̷�����������
		try{
			//
			constructor.setAccessible(true);

			if(constructor_params.length==0)
				assertNotNull(constructor.newInstance());

			else{
				//Ÿ���� �ȸ����� mock ��ü �����ð�.
				Class[] paramTypes=constructor.getParameterTypes();
				Object[] params= getMock(paramTypes,constructor_params);
				assertNotNull(constructor.newInstance(params));
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
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 ********************************************************************** */
	private void auto_Assert(Object testresult, Field f,Class memberclz ) throws IllegalArgumentException, IllegalAccessException{
		if(isNeedUnBoxing(memberclz) ){
			System.out.println( "Assert ���  (����/�׽�Ʈ���): "+f.get(expectedResult)+ " "+f.get(testresult));
			assertThat(f.get(testresult),is(f.get(expectedResult)));
		}
		else if(memberclz.isArray()){//�迭���� ��
			if(Array.getLength(f.get(testresult)) == Array.getLength(f.get(expectedResult))){
				for(int i= 0; i<Array.getLength(f.get(testresult)); i++){
					if(Array.get(f.get(testresult), i)!=null &&Array.get(f.get(expectedResult), i)!=null){
						System.out.println( "Assert ���  (����/�׽�Ʈ���): "+Array.get(f.get(expectedResult), i)+ " "+Array.get(f.get(testresult), i));
						assertThat(Array.get(f.get(testresult), i), is(Array.get(f.get(expectedResult), i)));
					}
				}
			}
		}else if(Collection.class.isInstance(f.get(expectedResult))){
			Collection expect=(Collection) f.get(expectedResult);
			Collection result=(Collection) f.get(testresult);
			Iterator ex_it = expect.iterator();
			Iterator re_it = result.iterator();
			while(ex_it.hasNext() && re_it.hasNext()){
				Object ex=ex_it.next();
				Object re=re_it.next();
				System.out.println( "Assert ���  (����/�׽�Ʈ���): "+ex +" "+ re);
				assertThat(re, is(ex));
			}
		}
	}	

	@Test
	public void testMethod() throws Throwable {
		//setObj();
		if(targetmethod==null){ //������ �׽�Ʈ�� ���.
			constructor_test();
			return;
		}

		Object testresult=null;
		System.out.println( "\n"+(testnumber++) + " : "+testname +"\n �׽�Ʈ Ŭ���� : " +targetclz.getSimpleName());//�׽�Ʈ ��ȣ�� ���ü�κ���  �׽�Ʈ�� �̷�����������


		if(targetmethod!=null)
			targetmethod.setAccessible(true);//private �޼ҵ带 �׽�Ʈ�ϱ� ����

		System.out.println("�׽�Ʈ �޼ҵ� : "+targetmethod.getName()); //�޼ҵ� �̸����
		//Method param ��ũ��ü ����.
		Class[] paramsTypes= targetmethod.getParameterTypes();
		Object[] params= getMock(paramsTypes, method_params);
		try {			
			testresult=targetmethod.invoke(classmap.get(targetclz), params);
				
			if(expectedResult !=null){
				if(isNeedUnBoxing(testresult.getClass())){ //���ð� �׽�Ʈ
					System.out.println( "Assert ���  (����/�׽�Ʈ���): " +expectedResult +" " +testresult); //�������� ������� ���
					//toString �������̵��� ���� ��ü ���¸� �ϴ� ������ �����ٸ�, �̰��� ��ǲ ��ü�� ���¸� ��°����ϴ�.
					assertThat(testresult,is(expectedResult)); //�׽��� ����� Ȯ��.
				}
				else{//����� ���ð�ü�� �ƴ� ���� ��ü�ΰ��
					Class[] type =new Class[1];
					type[0]=testresult.getClass();//���� ����Ÿ��
					Object[] returnObj=new Object[1];
					returnObj[0]=expectedResult;
					if(!type[0].equals(expectedResult.getClass())){//������ mock��ü�ΰ��.
						returnObj=getMock(type,returnObj);
						expectedResult=returnObj[0];
					}
					Field[] flz =type[0].getDeclaredFields();

					for(Field f: flz){
						if (!f.isSynthetic()){
							f.setAccessible(true);
							Class memberclz=f.getType();
							System.out.println(memberclz.getSimpleName()+ " "+f.getName());
							try {
								auto_Assert(testresult, f, memberclz);
							} catch (IllegalArgumentException | IllegalAccessException e) {
								// TODO Auto-generated catch block
								handleException(e);
							}
						}
					}
					
					
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			Throwable fillstack=e.fillInStackTrace();
			Throwable cause=null;
			if(fillstack !=null){
				cause= fillstack.getCause(); 
				if(cause!=null) cause.printStackTrace();
				throw(cause);
			}//Method Exception.
		}catch(AssertionError e){
			//��Ȯ�� ���� ã�� �̽�..
			StackTraceElement[] elem =new StackTraceElement[1];			
			elem[0]=new StackTraceElement(targetclz.getName(), targetmethod.getName(), targetclz.getCanonicalName(),1);
			e.setStackTrace(elem);
			throw(e);
		}

	}


	@After
	public void log(){
		//1.�αװ���. ���� Success ����.
		//2.��Ӵٿ� ������ ����Ʈ���. �α� ���ϵ� �����.. ��Ȯ�� ��� Ŭ������ ��� ������ ������
	}
}