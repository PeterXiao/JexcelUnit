package jexcelunit.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import jexcelunit.utils.ClassInfo;


/*
 * Created : 2017.02.23
 * Vendor  : Taehoon Seo
 * Description : create excel file including classes, methods and constructor informations.
 * */
public class ExcelCreator implements CommonData{
	private final int CONSTRUCTOR = 0;
	private final int METHOD = 1;
	public final String[] TESTDATASET = {"TestName" ,"TestClass","Constructor Param", "TestMethod", "Method Param", "Expected", "Result", "Success"};
	
	//create xlsx for Testcases.
	public void createXlsx(String projectName ,String rootpath , HashMap<String, ClassInfo> classinfos) throws IOException{
		/*
		 * �׽�Ʈ �̸�, Ŭ����, �������Ķ����, �޼ҵ�, �޼ҵ��Ķ����, ����, ���
		 * 1. ���� �Ķ���͸� ��� ? �Ķ���� �����b �м��ؼ� Į�� �� �����Ұ�
		 * 2. ��ũ��ü�� �̿����� ���? ��ũ��ü �̸����� �����ϵ����� �⺻. ��ũ��ü ������ ������ ���� ����?
		 * 3. �׽�Ʈ��� : �ó����� or ����.
		 * 4. �������� ���̹�.
		 * 5. �ߺ����� ó��.
		 * */


		//Check .xlxs file is exists
		boolean will_create=true;
		File root = new File(rootpath);
		File[] filelist=  root.listFiles();
		for(File f: filelist){
			//			System.out.println(f.getName());
			if(f.getName().equals(projectName+".xlsx")){
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				will_create= MessageDialog.openQuestion(
						window.getShell(),
						"Do you want to create new .xlxs or overwrite?",
						"Warnning : If you overwrite, you can lose your data");
				if(will_create)f.delete();
				break;
			}
		}

		//Create
		if(will_create){
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet xssfSheet = workbook.createSheet("TestSuite 1");	
			XSSFRow row =null;
			XSSFCell cell =null;

			//make hidden sheet
			hiddensheet(workbook,classinfos);

			row=xssfSheet.createRow(0);//������ info

			//Į���� ������ ������ Ȥ�� �޼ҵ� �Ķ���Ϳ� ���� ���������� ���Ұ�.
			//Į���� Data Validation�� ����.
			int consCount=getMaxParamCount(CONSTRUCTOR,classinfos);
			int metsCount=getMaxParamCount(METHOD,classinfos);
			int cellvalindex=0;
			int totalCellCount= TESTDATASET.length + consCount + metsCount-2;
			for(int i =0; i<totalCellCount; i++){
				String val=TESTDATASET[cellvalindex];


				if(val.equals("Constructor Param")){					
					//Set Param Validation Type
					/*
					 * �̽� ���� : 
					 * 1. Ŭ�������� �ٸ� Ÿ�� ��ȿ��
					 * 2. Ŭ�������� �ٸ� �޼ҵ� ��ȿ��
					 * = >�ذ� : ���� ��Ʈ�� ��Ӵٿ� ����Ʈ�� ���� ���� �ۼ��Ұ�.
					 * ���Ŀ� �� ���� ���� ���λ����� �ƴҰ��, �����Ʈ ����. ��������ȿ�� ������ �ϴ� �������� ������..
					 * 3. �׽�Ʈ �α׸� ��� ������ ���ΰ�..
					 * 4. �ᱹ ������ �׽�Ʈ �α׸� ���ų� �Ҹ��� �÷������� �ʿ��ϴٴ°ǵ�...
					 * */
					for(int k=0; k<consCount; k++){
						cell=row.createCell(i+k);
						xssfSheet.setColumnWidth(cellvalindex, 4500);
						cell.setCellValue(val+(k+1));
					}
					i+=consCount-1;
					cellvalindex++;
				}
				else if(val.equals("TestMethod")){
					xssfSheet.setColumnWidth(cellvalindex, 3000);
					setValidation("INDIRECT($B2)", xssfSheet, cellvalindex);
					cell=row.createCell(i);
					cell.setCellValue(val);
					cellvalindex++;
				}

				else if(val.equals("Method Param")){

					for(int k=0; k<metsCount; k++){
						cell=row.createCell(i+k);
						xssfSheet.setColumnWidth(cellvalindex, 4000);
						cell.setCellValue(val+(k+1));
					}
					i+=metsCount-1;
					cellvalindex++;
				}
				else{
					xssfSheet.setColumnWidth(cellvalindex, 3000);
					cell=row.createCell(i);
					cell.setCellValue(val);
					cellvalindex++;
				}

			}

			setValidation("Class", xssfSheet, 1);
			//save xlsx
			FileOutputStream fileoutputstream=new FileOutputStream(rootpath+"/"+ projectName+".xlsx");
			//������ ����
			workbook.write(fileoutputstream);
			if(workbook!=null) workbook.close();
			//�ʼ��� �ݾ��־����
			if( fileoutputstream!=null)
				fileoutputstream.close();

			System.out.println("Created");
		}

	}

	// Create Hidden sheet for DataValidation List.
	private void hiddensheet(XSSFWorkbook workbook, HashMap<String,ClassInfo> classinfos){
		XSSFSheet hidden = workbook.createSheet("hidden");
		Set<String> keys = classinfos.keySet();
		ClassInfo info =null;
		XSSFRow firstrow=hidden.createRow(0);

		int col_index=0;
		for(String key : keys){
			info =classinfos.get(key);
			/*
			 * 1. Ŭ���� -������ �Ķ����Ÿ�� ��ȿ������ 	�Ķ���� ������ ������, Ÿ���� �ٸ���� ? ������ �ɱ⿡ ��ȣ��. �����ڴ� �̸��� �����ϱ�..	
			 * 2. Ŭ���� - �޼ҵ帮��Ʈ 				ok
			 * 3. �޼ҵ� - �Ķ���� Ÿ��				1�� ���� �̽�.
			 * 4. Ŭ���� - ����Ÿ��					�Ķ���Ͱ� ������ ������ �ٸ��� ����. Ÿ�������� �ɱ⿣ ���� ������ �ְڴµ�?
			 * => �ᱹ ���డ���Ѱ� Ŭ���� �̸�, Ŭ������ ���� �޼ҵ帮��Ʈ ����
			 * */

			firstrow.createCell(col_index).setCellValue(key);
			Set<Method> mets = info.getMethods();

			Iterator<Method> mit =mets.iterator();
			if(mets.size() >0){

				for(int i =1; i <= mets.size() && mit.hasNext(); i ++){
					Method met= mit.next();
					XSSFRow row= hidden.getRow(i);
					if(row == null) row= hidden.createRow(i);
					row.createCell(col_index).setCellValue(met.getName());
				}
				//Set Class-Method Data ReferenceList
				XSSFName namedcell =workbook.createName();
				namedcell.setNameName(info.getClz().getSimpleName()); //Nameing�� �߿�.
				char currentCol=(char) ('A'+col_index);
				String formula= "hidden!$"+currentCol+"$2:$"+currentCol+"$" + (mets.size()+1);
				System.out.println("Create : " + formula);
				namedcell.setRefersToFormula(formula);

			}
			col_index++;
		}

		//Set Class Data ReferenceList.
		XSSFName namedcell =workbook.createName();
		namedcell.setNameName("Class"); //Nameing�� �߿�.
		char cell=(char) ('A'+col_index-1);
		String formula= "hidden!$A$1:$"+cell+"$1";
		namedcell.setRefersToFormula(formula);


		//Set hidden Sheet if true=  hidden.
		workbook.setSheetHidden(1, false);

	}

	//Init DataValidation for Update Cell
	private void initValidation(){


	}

	//Ŭ���� �̸� ����
	private void setValidation(String namedcell, XSSFSheet xssfSheet ,int col){


		DataValidation dataValidation = null;
		DataValidationConstraint constraint = null;
		DataValidationHelper validationHelper = null;
		validationHelper = new XSSFDataValidationHelper(xssfSheet);
		constraint= validationHelper.createFormulaListConstraint(namedcell);
		CellRangeAddressList addresslist =null;
		if(constraint !=null){
			addresslist = new CellRangeAddressList(1,500,col,col);
			System.out.println(constraint.getFormula1());
			dataValidation= validationHelper.createValidation(constraint, addresslist);


			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.createErrorBox("Wrong Input", "You must input Right Type.");
			xssfSheet.addValidationData(dataValidation);

		}		
	}


	private int getMaxParamCount(int option,HashMap<String, ClassInfo> classinfos){
		int max= 0;
		Set<String> keys= classinfos.keySet();
		for(String key : keys){
			ClassInfo ci = classinfos.get(key);

			if(option == CONSTRUCTOR){

				for(Constructor con : ci.getConstructors()){
					if( max < con.getParameterCount())
						max = con.getParameterCount();
				}	
			}
			else if(option == METHOD){
				for(Method met : ci.getMethods()){
					if( max < met.getParameterCount())
						max = met.getParameterCount();
				}
			}

		}
		return max;
	}



}
