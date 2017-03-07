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
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import jexcelunit.excel.ExcelReader;
import jexcelunit.excel.TestcaseVO;

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
	//	private static Method[] methods; //�׽�Ʈ�� ��ü�� �޼ҵ带 �޴ºκ�
	private static HashMap<String,Object> mock=new HashMap<String,Object>();//��ũ��ü ����
	
	private static int testnumber=0; //�׽�Ʈ run �ѹ�

	//�׽�Ʈ ���̽����� Ȯ���� method_params
	private String testname=null;
	private Class targetclz=null;
	private Constructor constructor = null;
	private Object[] constructor_params=null;
	private Method targetmethod=null;
	private Object[] method_params=null;
	private Object expectedResult=null;


	//�׽�Ʈ�̸�, �׽�Ʈ�� Ŭ����, �׽�Ʈ�Ķ����,  �׽�Ʈ�� �޼ҵ��̸�, �Ķ���͵�,�������� JUnit�� �о�� �����Ű�� �κ��̴�.
	public TestInvoker(String testname,Class targetclz,Constructor constructor,Object[] constructor_params,Method targetmethod,Object[] param1,Object expectedResult){
		this.testname= (String)testname;
		this.targetclz=targetclz;
		this.constructor=constructor;
		this.constructor_params=constructor_params;
		this.expectedResult=expectedResult;
		this.targetmethod=targetmethod;
		this.method_params=param1;
	}

	public static Collection parmeterizingExcel(){
		ExcelReader reader = new ExcelReader();
		//��Ÿ�����͸� ������ �� �ۿ�����.
		//�ڵ鷯 �������� Ÿ�� ������Ʈ ������ �����Ұ�.
		File file = new File(".");
		ArrayList<TestcaseVO> testcases=null;
		Object[][] parameterized= null;

		if(file.exists()){
			try {
				File realPath= new File(file.getCanonicalPath());

				testcases = reader.readExcel(realPath.getName(), file.getCanonicalPath());
				TestcaseVO currentCase =null;

				if(!testcases.isEmpty())
				{
					for(TestcaseVO c : testcases){
						System.out.println(c.getTestname());
					}

				}
				parameterized = new Object[testcases.size()][7];
				for(int row_index = 0; row_index < testcases.size(); row_index++){
					currentCase = testcases.get(row_index);
					parameterized[row_index][0]=currentCase.getTestname();
					parameterized[row_index][1]=currentCase.getTestclass();
					parameterized[row_index][2]=currentCase.getConstructor();
					parameterized[row_index][3]=currentCase.getConstructorParams().toArray();
					parameterized[row_index][4]=currentCase.getMet();
					parameterized[row_index][5]=currentCase.getMethodParams().toArray();
					parameterized[row_index][6]=currentCase.getResult();	

					//To do list : 1. Testinvoker �޼ҵ� ���� (con, method�� �ٷ� �����ϵ���..
					//2. Custom Parameter Converting ��ũ��ü. Date Format �̽� ó��.
					//3. �α�
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//�о���� ����Ʈ�� String, Class, Object[] Object, String Object�� �ٱ����.
		return Arrays.asList(parameterized);
	} 

	//������.
	public static void setUp() {
		// TODO Auto-generated method stub

	}

	@Parameters( name = "{index}: {0}")
	public static Collection<Object[][]> parameterized(){
		setUp();
		return parmeterizingExcel();
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

	//	private Class unBoxing(Class wrapper){
	//		switch(wrapper.getTypeName().charAt(10)){
	//		case 'S': return wrapper.getTypeName().contains("Short")?short.class:String.class;							
	//		case 'B': return wrapper.getTypeName().contains("Byte")?Byte.class:Boolean.class;
	//		case 'C':return char.class;
	//		case 'I':return int.class;
	//		case 'L':return long.class;
	//		case 'D':return double.class;
	//		case 'F':return float.class;
	//		case 'V':return void.class;
	//		default : return null;
	//		}
	//	}

	@SuppressWarnings("unused")
	@Before
	public void setObj(){
		if(!classmap.containsKey(targetclz)&& targetmethod !=null){ //������ ��ü�� ���°��
			//System.out.println(classmap.containsKey(targetclz)+"���λ���");
			constructor.setAccessible(true);
			try {
				if(constructor_params.length==0)
					classmap.put(targetclz, constructor.newInstance());
				//Ÿ���� �ȸ����� mock ��ü �����ð�.
				else
					classmap.put(targetclz, constructor.newInstance(constructor_params));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				handleException(e);
			}
		}
	}

	private void constructor_test(){
		System.out.println( "\n"+(testnumber++) + " : "+testname +"\n �׽�Ʈ Ŭ���� : " +targetclz.getSimpleName());//�׽�Ʈ ��ȣ�� ���ü�κ���  �׽�Ʈ�� �̷�����������
		try{
			//
			constructor.setAccessible(true);
			
			if(constructor_params.length==0)
				assertNotNull(constructor.newInstance());
			//Ÿ���� �ȸ����� mock ��ü �����ð�.
			else
				assertNotNull(constructor.newInstance(constructor_params));

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

	//	private Method get_TargetMethod() throws Exception{
	//		Method target=null;
	//		Class[] types=null;
	//		Method[] methods = targetclz.getMethods();
	//		for(Method m : methods)
	//			if(m.getName().equals(methodname))
	//				target=m;
	//		return target;
	//	}

	@Test
	public void testMethod() {
		//setObj();
		if(targetmethod==null){ //������ �׽�Ʈ�� ���.
			constructor_test();
			return;
		}

		Object testresult=null;
		System.out.println( "\n"+(testnumber++) + " : "+testname +"\n �׽�Ʈ Ŭ���� : " +targetclz.getSimpleName());//�׽�Ʈ ��ȣ�� ���ü�κ���  �׽�Ʈ�� �̷�����������
		try {

			if(targetmethod!=null)
				targetmethod.setAccessible(true);//private �޼ҵ带 �׽�Ʈ�ϱ� ����

			System.out.println("�׽�Ʈ �޼ҵ� : "+targetmethod.getName()); //�޼ҵ� �̸����

			//Method param ����.

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