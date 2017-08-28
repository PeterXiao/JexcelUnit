package jexcelunit.testinvoker;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jexcelunit.classmodule.PrimitiveChecker;
import jexcelunit.excel.ExcelReader;
import jexcelunit.excel.ExcelResultSaver;
import jexcelunit.excel.MockVO;
import jexcelunit.excel.TestcaseVO;

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
 **/
@SuppressWarnings("rawtypes")
@RunWith(Parameterized.class) //�׽�Ʈ ���̽��� �̿��Ұ��̴�.
public class TestInvoker {
	private static Map<Class, Object> classmap=new HashMap<Class, Object>(); //�ؽ������� �׽�Ʈ�� �ʿ��� ��ü���� �ϳ����� �����Ѵ�.
	private static ArrayList<Class> exceptionlist=new ArrayList<Class>();//����� ���� ���� Ŭ�������� ��Ƶδ� ��.
	private static Map<String,String> sheets=new HashMap<String,String>();
	protected static HashMap<String,Object> mock=new HashMap<String,Object>();//��ũ��ü ����
	private static int sheetNum= -1,rowIndex=0,testnumber=0;
	private static boolean[][] success=null;
	private static String[][] result=null;
	private static File file=null;
	private static int[] rowSize=null;
	private static String currentSheet=null;

	//�׽�Ʈ ���̽����� Ȯ���� method_params
	private String sheet=null;
	private String testname=null;
	private Class targetclz=null;
	private Constructor constructor = null;
	private Object[] constructor_params=null;
	private Method targetmethod=null;
	private Object[] method_params=null;
	private Object expectedResult=null;

	//�׽�Ʈ�̸�, �׽�Ʈ�� Ŭ����, �׽�Ʈ�Ķ����,  �׽�Ʈ�� �޼ҵ��̸�, �Ķ���͵�,�������� JUnit�� �о�� �����Ű�� �κ��̴�.
	public TestInvoker(String sheet,String testname,Class targetclz,Constructor constructor,Object[] constructor_params,Method targetmethod,Object[] param1,Object expectedResult){
		this.sheet=sheet;
		this.testname= (String)testname;
		this.targetclz=targetclz;
		this.constructor=constructor;
		this.constructor_params=constructor_params;
		this.expectedResult=expectedResult;
		this.targetmethod=targetmethod;
		this.method_params=param1;
	}


	/*
	 * 1. ������̼����� ���� path�� �о��.
	 * 2. �޾ƿ�  path��  �ٽ� �����Ҷ� ���.
	 * 3. suite�� ���� row index�� �� �����ؾ��ϴ°� .
	 * 4. �׷��ٿ� suiteInfo ��� �ɹ�Ŭ������ �ּ� �����ϴ°� ��������.
	 * */
	public static Collection parmeterizingExcel(String filePath) throws InstantiationException{
		
		//��Ÿ�����͸� ������ �� �ۿ�����.
		//�ڵ鷯 �������� Ÿ�� ������Ʈ ������ �����Ұ�.
		file = new File(filePath);
		ArrayList<ArrayList<TestcaseVO>> testcases=null;
		Object[][] parameterized= null;

		if(file.exists()){
			try {
				ExcelReader reader = new ExcelReader(filePath);
				testcases = reader.readExcel();

				if(testcases.size()>0){

					int total_row_index=0, maxRow=0,suiteNum=0;
					rowSize=new int[testcases.size()]; //suite��  rowSize�� �����Ұ�.
					for(ArrayList<TestcaseVO> testcase : testcases){
						int size= testcase.size();
						rowSize[suiteNum++]=size;
						total_row_index+=size;
						if(size>maxRow) maxRow=size;
					}
					parameterized = new Object[total_row_index][8];
					//init success
					success= new boolean[testcases.size()][maxRow];//�������������Ұ�
					for(int i=0; i<testcases.size(); i++)
						Arrays.fill(success[i], true);

					result= new String[testcases.size()][maxRow];//����� ����.


					//Setting SheetNames and TestMode.
					ArrayList<String> sheetModes=reader.getTestSheetMode();
					if( sheetModes.size() != testcases.size()) throw new InstantiationException("Check Sheet Info");

					for(int i=0; i<sheetModes.size(); i++){
						ArrayList<TestcaseVO> testcase = testcases.get(i);
						if(testcase.size()>0)
							sheets.put(testcase.get(0).getSheetName(), sheetModes.get(i));
						else throw new InstantiationException("There's no Test Case in the Sheet : "+testcase.get(0).getSheetName());
					}

					//Set @Parameters.
					int row_index=0;
					for(ArrayList<TestcaseVO> testcase : testcases){
						if(row_index < total_row_index){
							for(TestcaseVO currentCase: testcase){
								parameterized[row_index][0]=currentCase.getSheetName();
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

				//setUp Mock Object
				ArrayList<MockVO> mockList= reader.readMocks();
				for(MockVO mock : mockList){
					//make Mock and Put.
					System.out.println(mock.getMockName());
					System.out.println(mock.getConstructor());
					ArrayList<Object> consParams= mock.getConsParams();
					if(consParams!=null)
					for(Object obj :consParams) {
						System.out.println(obj);
					}
					Map<Field,Object> fieldSet = mock.getFieldSet();
					if(fieldSet !=null) {
						for(Field f : fieldSet.keySet()) {
							System.out.println(f.getName() + " : " + fieldSet.get(f));
						}
					}
					
				}
			} catch (Exception e) {
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



	@Before
	public void setObj(){
		if(currentSheet !=sheet){ 
			if(sheets.get(sheet).equals("Scenario")) //���ο� �ó����� �׽�Ʈ
				classmap.clear();
			rowIndex=0; //�� �ʱ�ȭ
			sheetNum++;
		}

		if(sheets.get(sheet).equals("Scenario")){
			if(!classmap.containsKey(targetclz)&& targetmethod !=null){ //�ó����� �׽�Ʈ���� ������ ��ü�� ���°��
				makeTestInstance();
			}	
		}
		else if(sheets.get(sheet).equals("Units")){
			if(classmap.containsKey(targetclz))
				classmap.remove(targetclz);
			makeTestInstance();
		}

	}
	private void makeTestInstance(){
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

	private Object[] getMock(Class[] types, Object[] params){
		for(int i= 0; i<types.length; i++){
			Class paramClass=params[i].getClass();
			if(PrimitiveChecker.isPrimitiveOrWrapper(paramClass))
				paramClass= PrimitiveChecker.unWrapping(paramClass);

			if(!types[i].equals(paramClass)){ // ���� ó�� �Ŀ��� Ÿ���� ���� ���� ���. 1. primitive Ÿ�԰� wrapper Ÿ���� ����.	
				Object mockObject=mock.get(params[i]);
				if( types[i].isInstance(mockObject) && mockObject!=null){
					params[i]=mockObject;
				}else
					throw new AssertionError("Wrong Parameter Types");
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
		}catch(AssertionError e){
			success[sheetNum][rowIndex-1]=false;
			throw(e);
		}
		catch(Exception e){handleException(e);}
	}


	private void assertion(Object testResult, Object expectedResult, Class resultType) throws IllegalArgumentException, IllegalAccessException, AssertionError{
		if(PrimitiveChecker.isPrimitiveOrWrapper(testResult.getClass())){ //���ð� �׽�Ʈ
			System.out.println( "Assert ���  (����/�׽�Ʈ���): " +expectedResult+" " +testResult); //�������� ������� ���
			//toString �������̵��� ���� ��ü ���¸� �ϴ� ������ �����ٸ�, �̰��� ��ǲ ��ü�� ���¸� ��°����ϴ�.
			switch(PrimitiveChecker.getFloatingType(testResult.getClass())){
			case 1:
				assertThat(new Double(Float.toString((float)testResult)),
						is(closeTo(new Double(Float.toString((float)expectedResult)), 0.00001)));
				break;
			case 0:
				assertThat((double)testResult,is(closeTo((double)expectedResult, 0.00001)));
				break;
			default:
				assertThat(testResult,is(expectedResult));
			}


		}
		else{//����� ���ð�ü�� �ƴ� ���� ��ü�ΰ��
			Field[] flz =resultType.getDeclaredFields();

			for(Field f: flz){
				if (!f.isSynthetic()){
					f.setAccessible(true);
					Class memberclz=f.getType();
					System.out.println(memberclz.getSimpleName()+ " "+f.getName());
					try {
						auto_Assert(testResult, f, memberclz);
					} catch (IllegalArgumentException | IllegalAccessException e) {handleException(e);}
				}
			}
		}
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
	private void auto_Assert(Object testresult, Field f,Class memberclz ) throws IllegalArgumentException, IllegalAccessException, AssertionError{

		if(PrimitiveChecker.isPrimitiveOrWrapper(memberclz) ){ //�Ϲ� ���Һ�
			System.out.println( "Assert ���  (����/�׽�Ʈ���): "+f.get(expectedResult)+ " "+f.get(testresult));
			switch(PrimitiveChecker.getFloatingType(f.get(testresult).getClass())){
			case 1: //Float
				assertThat(new Double(Float.toString((float)f.get(testresult))),
						is(closeTo(new Double(Float.toString((float)f.get(expectedResult))), 0.00001)));
				break;
			case 0://Double
				assertThat((double)f.get(testresult),is(closeTo((double)f.get(expectedResult), 0.00001)));
				break;
			default:
				assertThat(f.get(testresult),is(equalTo(f.get(expectedResult))));
			}
		}
		else if(memberclz.isArray()){//�迭���� ��
			if(Array.getLength(f.get(testresult)) == Array.getLength(f.get(expectedResult))){
				for(int i= 0; i<Array.getLength(f.get(testresult)); i++){
					if(Array.get(f.get(testresult), i)!=null &&Array.get(f.get(expectedResult), i)!=null){
						assertion(Array.get(f.get(testresult), i),Array.get(f.get(expectedResult), i),f.getType());
					}
				}
			}else fail("Array Length�� ��ġ���� �ʽ��ϴ�.");
		}else if(Collection.class.isInstance(f.get(expectedResult))){//�÷��� ���� ��
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
		}else fail("There's Custom Object Field.");
		//��� �ʿ�.
	}	

	/* �̽� 
	 *  ��ũ��ü�ε� ��ũ��ü�� primitive Ÿ���ΰ�� ? isMock �÷��׸� �δ°� �����Ű�����
	 * */
	@Test
	public void testMethod() throws Throwable {
		currentSheet=sheet;
		rowIndex++;
		//setObj();
		if(targetmethod==null){ //������ �׽�Ʈ�� ���.
			constructor_test();
			return;
		}

		Object testResult=null;
		System.out.println( "\n"+(testnumber++) + " : "+testname +"\n �׽�Ʈ Ŭ���� : " +targetclz.getSimpleName());//�׽�Ʈ ��ȣ�� ���ü�κ���  �׽�Ʈ�� �̷�����������

		if(targetmethod!=null)
			targetmethod.setAccessible(true);//private �޼ҵ带 �׽�Ʈ�ϱ� ����

		System.out.println("�׽�Ʈ �޼ҵ� : "+targetmethod.getName()); //�޼ҵ� �̸����
		//Method param ��ũ��ü ����.
		Class[] paramsTypes= targetmethod.getParameterTypes();
		Object[] params= getMock(paramsTypes, method_params);
		try {			

			testResult=targetmethod.invoke(classmap.get(targetclz), params);

			if(targetmethod.getReturnType()==null ||targetmethod.getReturnType().equals(void.class));
			else{
				Class[] type=null;Object[] returnObj=null;
				if(testResult !=null){
					//��ũ �¾�
					type= new Class[1];
					result[sheetNum][rowIndex-1]=testResult.toString();
					type[0]= testResult.getClass();//���� ����Ÿ��
				}				

				if(expectedResult !=null){
					returnObj=new Object[1];
					returnObj[0]=expectedResult;
					if(type!=null)
						if(!type[0].equals(expectedResult.getClass())){//������ mock��ü�ΰ��.
							returnObj=getMock(type,returnObj);
							expectedResult=returnObj[0];
						}
				}
				//����
				assertion(testResult,expectedResult,type[0]);
			}

		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			success[sheetNum][rowIndex-1]=false;
			if(exceptionlist.contains(e.getClass())){
				result[sheetNum][rowIndex-1]="Method Exception Occurred";
				StackTraceElement[] elem =new StackTraceElement[1];			
				elem[0]=new StackTraceElement(targetclz.getName(), targetmethod.getName(), targetclz.getCanonicalName(),1);
				e.setStackTrace(elem);
				throw(e);
			}
			else {result[sheetNum][rowIndex-1]="InitError : Check Cell's data or Custom Exception";
			// TODO Auto-generated catch block
			Throwable fillstack=e.fillInStackTrace();
			Throwable cause=null;
			if(fillstack !=null){
				cause= fillstack.getCause(); 
				if(cause!=null) cause.printStackTrace();
				fail();
				throw(cause);
			}//Method Exception.
			}
		}catch(AssertionError e){
			success[sheetNum][rowIndex-1]=false;
			//��Ȯ�� ���� ã�� �̽�..
			StackTraceElement[] elem =new StackTraceElement[1];			
			elem[0]=new StackTraceElement(targetclz.getName(), targetmethod.getName(), targetclz.getCanonicalName(),1);
			e.setStackTrace(elem);
			throw(e);
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	//�������� �� ��� ����.
	@AfterClass
	public static void log(){
		try {
			ExcelResultSaver save=new ExcelResultSaver(file.getCanonicalPath());
			Set<String> sheetNames=sheets.keySet();
			Iterator sit= sheetNames.iterator();
			for(int i=0; i<=sheetNum&&sit.hasNext(); i++){
				String sheetname=(String)sit.next();
				//				System.out.println(sheetname);
				save.writeResults(sheetname, rowSize[i], result[i], success[i]);
			}
			save.write();
			save.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}