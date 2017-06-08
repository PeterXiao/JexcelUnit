package jexcelunit.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import jexcelunit.classmodule.ClassInfo;


/*
 * Created : 2017.02.23
 * Vendor  : Taehoon Seo
 * Description : create excel file including classes, methods and constructor informations.
 * 
 * '17 /04/ 21 TODO
 *  1. ��ũ��ü = >�ᱹ �ڵ��ؾ� �ϴµ� ���� ���ұ�   ��ũ���� ��Ʈ�� �����?!
 *   ��ũ ��ü�� �ʿ��� �͵� => ��ũ �̸�. ��ũ Ŭ����. ������ �Ķ����, ȣ�� �� ������ �Լ� , �Ķ���͵��� �ʿ�..
 *  2. �������ó�� �ð�ȭ -> �м��� ���� �Ķ���� Ÿ�Կ� ���� ��������� �����ϵ��� �ؾ���.
 *  UML �׽�Ʈ �� ��� ??!!! Star UMLó�� �м��Ͽ� �ð�ȭ �ϰ�, �Ķ���� �� �׽�Ʈ�� �巡�׾� ��ӽ����� �׽�Ʈ �����ϰ�?! + �������� ������ �ۼ� �� ����?!!!
 *  3. �ó����� �׽�Ʈ/ �����׽�Ʈ ���� �ʵ�.ù�ٿ� ���� �׽�Ʈ ��带 ������ ��. - DONE
 * */
@SuppressWarnings("rawtypes")
public class ExcelCreator{
	private final int CONSTRUCTOR = 0;
	private final int METHOD = 1;
	public final String[] TESTDATASET = {"TestName" ,"TestClass","ConsParam", "TestMethod", "MetParam", "Expected", "Result", "Success"};


	private int consCount=0;
	private int metsCount=0;
	private FileOutputStream fileoutputstream= null;
	private XSSFWorkbook workbook = null;
	private String fileName= null, containerName=null;
	private HashMap<String, ClassInfo> classinfos=null;
	private File existingExcel= null;



	public ExcelCreator(String fileName,String containerName ,HashMap<String, ClassInfo> classinfos){
		this.fileName=fileName;
		this.containerName= containerName;
		this.classinfos= classinfos;

		consCount=getMaxParamCount(CONSTRUCTOR,classinfos);
		metsCount=getMaxParamCount(METHOD,classinfos);
	}

	public void initWorkSheets(XSSFWorkbook workbook){
		//Init All names. 
		List<XSSFName> names= workbook.getAllNames();
		String[] rmvName= new String[names.size()];
		int rm_Nameindex= 0;
		for(int i =0; i<names.size(); i++){
			rmvName[rm_Nameindex++]=  names.get(i).getNameName();
		}
		for(int j=0; j< rm_Nameindex; j++){
			if(rmvName[j] !=null){
				Name name=workbook.getName(rmvName[j]);
				workbook.removeName(name);
			}else break;
		}

		String[] rmvSheet= new String[workbook.getNumberOfSheets()];
		int rm_Sheetindex=0;

		for(int sheet_index=0;sheet_index<workbook.getNumberOfSheets();sheet_index++){
			if(workbook.isSheetHidden(sheet_index)||workbook.isSheetVeryHidden(sheet_index)){//Init hidden Sheets
				rmvSheet[rm_Sheetindex++]= workbook.getSheetName(sheet_index); //Save Delete Sheet's name
			}else {
				//				workbook.getSheetName(sheet_index).contains("Mock Sheet");
				XSSFSheet sheet = workbook.getSheetAt(sheet_index);
				XSSFRow firstRow = sheet.getRow(1);

				//init DataValidations.
				unsetValidation(sheet);

				if(firstRow != null){
					//counting Constructor param Number and Method param Number.
					int old_conNum=0;
					int old_metNum=0;
					for(int cell_index =0; cell_index< firstRow.getPhysicalNumberOfCells(); cell_index++){
						XSSFCell cell=null;
						cell=firstRow.getCell(cell_index);
						if(cell.getStringCellValue().contains("ConsParam")){
							old_conNum++;
						}
						if(cell.getStringCellValue().contains("MetParam")){
							old_metNum++;
						}
					}

					//Į���� ����.
					int con_change= consCount-old_conNum;
					int met_change = metsCount- old_metNum;
					if(con_change>0){//���̸�ŭ Į�� �߰�.
						for(int i=0;i< con_change; i++){
							shiftColumn(sheet, 1+old_conNum, con_change);
						}
					}else if(con_change<0){//���̸�ŭ ����
						for(int i=0;i> con_change; i--){
							removeColumn(sheet, 1+old_conNum, -con_change);
						}
					}

					if(met_change>0){//���̸�ŭ Į�� �߰�.
						for(int i=0;i< met_change; i++){
							shiftColumn(sheet, 2+consCount, met_change);
						}
					}else if(met_change<0){//���̸�ŭ ����
						for(int i=0;i> met_change; i--){
							removeColumn(sheet, 2+consCount, -met_change);
						}
					}
				}
				sheet.removeRow(sheet.getRow(1));//remove ù��° ���� �����Ұ�.
				sheet.removeRow(sheet.getRow(0));
			}
		}

		//remove Hidden Sheet.
		for(int i=0; i<rm_Sheetindex; i++){
			if(rmvSheet[i] !=null){
				int where = workbook.getSheetIndex(rmvSheet[i]);
				workbook.removeSheetAt(where);
			}else break;
		}

	}

	private void shiftColumn(XSSFSheet sheet, int from, int offset){
		XSSFRow currentRow =null;
		CellCopyPolicy ccp = new CellCopyPolicy();

		if(sheet !=null){
			for(int i=0; i<sheet.getPhysicalNumberOfRows(); i++){
				currentRow =sheet.getRow(i);
				XSSFCell currentCell =null;
				for(int j=currentRow.getPhysicalNumberOfCells()-1; j>=from;j--){
					currentCell = currentRow.getCell(j);
					if(currentCell !=null)
						currentRow.createCell(j+offset).copyCellFrom(currentCell, ccp);
				}
				for(int k=from; k< from+offset ; k++){
					currentCell = currentRow.getCell(k);
					if(currentCell !=null){
						if(currentCell.getCellComment() !=null)
							currentCell.removeCellComment();
						currentRow.removeCell(currentCell);
					}
				}
			}
		}
	}

	//���ε����� ��������.
	private void removeColumn(XSSFSheet sheet, int from, int offset){
		XSSFRow currentRow =null;
		CellCopyPolicy ccp = new CellCopyPolicy();
		if(sheet !=null){
			int colNum =sheet.getRow(0).getPhysicalNumberOfCells();
			for(int i=0; i<sheet.getPhysicalNumberOfRows(); i++){ 
				currentRow =sheet.getRow(i);
				XSSFCell currentCell =null;

				for(int j=from+offset-1; j<colNum;j++){
					currentCell = currentRow.getCell(j);
					if(currentCell !=null)
						currentRow.getCell(j-offset).copyCellFrom(currentCell, ccp);
				}
				for(int k=colNum-offset; k< colNum ; k++){
					currentCell = currentRow.getCell(k);
					if(currentCell !=null){
						if(currentCell.getCellComment() !=null)
							currentCell.removeCellComment();
						currentRow.removeCell(currentCell);
					}
				}

			}
		}
	}


	private File getExistingExcel(){
		boolean will_create=true;
		File root = new File(containerName);
		File xlsx= null;
		File[] filelist=  root.listFiles();
		for(File f: filelist){
			//			System.out.println(f.getName());
			if(f.getName().equals(fileName+".xlsx")){
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				will_create= MessageDialog.openQuestion(
						window.getShell(),
						"Do you want to Overwrite it? Yes :Overwrite No : Delete and Create it",
						"WARNING : If you overwrite it, you can lose its data");
				if(will_create)xlsx=f;
				else f.delete();
				break;
			}
		}
		return xlsx;
	}

	/*
	 * 1. Scenario mode. 
	 * 2. Unit Tests
	 * */
	private void makeSetTestModeRow(XSSFSheet sheet, int rowIndex){
		XSSFRow row= sheet.getRow(rowIndex);
		if(row == null) row= sheet.createRow(rowIndex);

		XSSFCellStyle cs=workbook.createCellStyle();
		cs.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
		cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cs.setAlignment(HorizontalAlignment.CENTER);
		cs.setBorderTop(BorderStyle.MEDIUM);
		cs.setBorderBottom(BorderStyle.MEDIUM);
		cs.setBorderLeft(BorderStyle.MEDIUM);
		cs.setBorderRight(BorderStyle.MEDIUM);

		//Field Name;
		XSSFCell cell = row.createCell(0);
		cell.setCellValue("Test MODE");
		cell.setCellStyle(cs);

		//validation Name
		cell= row.createCell(1);
		DataValidationHelper dvh= new XSSFDataValidationHelper(sheet);
		CellRangeAddressList addr= new CellRangeAddressList(row.getRowNum(),row.getRowNum(), 1, 1);
		DataValidationConstraint dvConstraint=dvh.createExplicitListConstraint(new String[]{"Scenario", "Units"});
		DataValidation dv= dvh.createValidation(dvConstraint, addr);
		sheet.addValidationData(dv);
		cell.setCellStyle(cs);
	}

	//create xlsx for Testcases.
	/*
	 * �׽�Ʈ �̸�, Ŭ����, �������Ķ����, �޼ҵ�, �޼ҵ��Ķ����, ����, ���
	 * 1. ���� �Ķ���͸� ��� ? �Ķ���� �����b �м��ؼ� Į�� �� �����Ұ� DONE
	 * 2. ��ũ��ü�� �̿����� ���? ��ũ��ü �̸����� �����ϵ����� �⺻. ��ũ��ü ������ ������ ���� ����? DONE
	 * 3. �׽�Ʈ��� : �ó����� or ����. => ��Ʈ�� ��Ʈ������ �Ͽ� DONE.
	 * 4. �������� ���̹�. => 9. DONE
	 * 5. �ߺ����� ó��. => DONE
	 * 	5-1. UPDATE ������� ��ȿ��,���� �ʱ�ȭ �� �缳�� DONE. Į�� ���� DONE.
	 * 6. SUCCESS ó��=> DONE.  �÷� ���� DONE.
	 * 7. �α�ó��=>TODO Stack ListView�� ����. �α����� ����.
	 * 8. src ������ ��� => 9. DONE
	 * 9. Extension Point  ���� : ��ư + new. => �� ���� Wizard ������� ��Ʈ Ŭ������ xlsx ���� ��θ� �����ϰ� ������. DONE
	 *  9-1. src, targetproject ��� �����忡�� ���ù޵���. => 4, 8 �����ذ�  DONE
	 * */
	public boolean createXlsx() throws IOException{
		//Check ".xlxs" file is exist.
		existingExcel=getExistingExcel();

		//Create or Update
		XSSFSheet xssfSheet = null;
		XSSFRow row =null;
		XSSFCell cell =null;
		FileInputStream inputstream=null;
		try{
			if(existingExcel!=null){
				inputstream = new FileInputStream(existingExcel.getCanonicalPath());
				workbook= new XSSFWorkbook(inputstream);
				initWorkSheets(workbook);
				//Update.
			}else{
				workbook=new XSSFWorkbook();
				xssfSheet= workbook.createSheet("TestSuite 1");	
			}
			//make hidden sheet
			hiddensheet(workbook,classinfos);

			XSSFCellStyle cs=workbook.createCellStyle();
			cs.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			cs.setAlignment(HorizontalAlignment.CENTER);
			cs.setBorderTop(BorderStyle.MEDIUM);
			cs.setBorderBottom(BorderStyle.MEDIUM);
			cs.setBorderLeft(BorderStyle.MEDIUM);
			cs.setBorderRight(BorderStyle.MEDIUM);


			for(int sheet_index=0; sheet_index<workbook.getNumberOfSheets(); sheet_index++){
				if( !workbook.isSheetHidden(sheet_index) && !workbook.isSheetVeryHidden(sheet_index)){
					//				if( workbook.getSheetAt(sheet_index).getSheetName().equals("TestSuite 1")){
					xssfSheet=workbook.getSheetAt(sheet_index);
					makeSetTestModeRow(xssfSheet, 0); //Selection Test Mode Row
					row=xssfSheet.getRow(1);//info
					if(row==null)row = xssfSheet.createRow(1);

					int cellvalindex=0;
					int totalCellCount= TESTDATASET.length + consCount + metsCount-2;


					for(int i =0; i<totalCellCount; i++){
						String val=TESTDATASET[cellvalindex]; //Column List.
						if(val.equals("ConsParam")){					
							//Set Param Validation Type
							for(int k=0; k<consCount; k++){
								cell=row.createCell(i+k);
								xssfSheet.setColumnWidth(i+k, 2700);
								cell.setCellValue(val+(k+1));
								cell.setCellStyle(cs);
							}
							i+=consCount-1;
							cellvalindex++;
						}
						else if(val.equals("TestMethod")){
							xssfSheet.setColumnWidth(i, 3000);
							setValidation("INDIRECT(LEFT($B3,FIND(\"(\",$B3)-1))", xssfSheet, i);
							cell=row.createCell(i);
							cell.setCellValue(val);
							cellvalindex++;
						}
						else if(val.equals("MetParam")){

							for(int k=0; k<metsCount; k++){
								cell=row.createCell(i+k);
								xssfSheet.setColumnWidth(i+k, 2700);
								cell.setCellValue(val+(k+1));
								cell.setCellStyle(cs);
							}
							i+=metsCount-1;
							cellvalindex++;
						}
						else{
							xssfSheet.setColumnWidth(i, 3000);
							cell=row.createCell(i);
							cell.setCellValue(val);
							cellvalindex++;
						}
						//set Cell Style.
						if(cell !=null){
							cell.setCellStyle(cs);
						}

					}
					setValidation("Class", xssfSheet, 1);
					char colIndex=(char)('A'+totalCellCount-1);

					XSSFSheetConditionalFormatting cf=xssfSheet.getSheetConditionalFormatting();
					XSSFConditionalFormattingRule rule1 =cf.createConditionalFormattingRule("INDIRECT(ADDRESS(ROW(),"+totalCellCount+"))=\"SUCCESS\"");
					XSSFConditionalFormattingRule rule2 =cf.createConditionalFormattingRule("INDIRECT(ADDRESS(ROW(),"+totalCellCount+"))=\"FAIL\"");
					rule1.createPatternFormatting().setFillBackgroundColor(HSSFColor.LIGHT_GREEN.index);
					rule2.createPatternFormatting().setFillBackgroundColor(HSSFColor.ROSE.index);

					XSSFConditionalFormattingRule[] rules ={ rule1, rule2};
					CellRangeAddress[] range = { CellRangeAddress.valueOf("A3:"+colIndex+"500")};
					cf.addConditionalFormatting(range, rules);
				}
			}
			//save xlsx
			fileoutputstream=new FileOutputStream(containerName+"/"+ fileName+".xlsx");
			workbook.write(fileoutputstream);

			System.out.println("Created");
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			//�ʼ��� �ݾ��־����
			if(workbook!=null) workbook.close();
			if( fileoutputstream!=null)
				fileoutputstream.close();
			if(workbook!=null) workbook.close();
			if( fileoutputstream!=null)
				fileoutputstream.close();
			if(inputstream !=null) inputstream.close();
		}
		return false;
	}


	// Create Hidden sheet for DataValidation List.
	private void hiddensheet(XSSFWorkbook workbook, HashMap<String,ClassInfo> classinfos){
		//�����ڸ���Ʈ �� ������ �Ķ���� ��ȿ�� -�Ϸ�
		XSSFSheet cons_param_sheet = workbook.createSheet("ConstructorParamhidden");
		//�޼ҵ�-�Ķ���� ��ȿ��
		XSSFSheet method_param_sheet = workbook.createSheet("MethodParamhidden");
		//Ŭ��������Ʈ �� �޼ҵ� �̸� ��ȿ��-�Ϸ�
		XSSFSheet class_method_sheet = workbook.createSheet("ClassMethodhidden");

		Set<String> keys = classinfos.keySet();
		ClassInfo info =null;
		XSSFRow clz_met_firstrow=class_method_sheet.createRow(0);
		XSSFRow met_par_firstrow =method_param_sheet.createRow(0);
		XSSFRow cons_par_firstrow = cons_param_sheet.createRow(0);
		//		Drawing drawing = class_method_sheet.createDrawingPatriarch();//to Create Cell Comment
		//		CreationHelper factory =workbook.getCreationHelper();
		int clz_met_col_index=0;
		int cons_total=0, mets_total=0;
		//Class Loop Start
		for(String key : keys){
			info =classinfos.get(key);
			//Ŭ����
			Class clz= info.getClz();

			//Ŭ���� -�޼ҵ� ����
			XSSFCell infocell=clz_met_firstrow.createCell(clz_met_col_index);
			infocell.setCellValue(info.getClz().getName());

			//Method loop 
			Method[] mets = info.getMethods();
			if(mets.length >0){
				XSSFCell clz_met_cell= null;
				XSSFCell met_par_cell=null;
				int i =1;
				for(Method met : mets){
					
					//Ŭ���� -�޼ҵ� �κ�
					XSSFRow clz_met_row= class_method_sheet.getRow(i);

					if(clz_met_row == null) clz_met_row= class_method_sheet.createRow(i);
					Parameter[] params= met.getParameters();

					//Search method Params
					String methodStr =met.getReturnType().getSimpleName()+" "+met.getName() + "(";
					String methodNamedStr="MET"+clz.getSimpleName()+met.getReturnType().getSimpleName()+met.getName();
					Parameter param= null;
					for(int param_index=0; param_index<params.length; param_index++){

						param= params[param_index];
						String paramType= param.getType().getSimpleName();
						String paramStr = paramType+ " " + param.getName();
						methodStr+=paramStr;
						if(param_index!=params.length-1)
							methodStr+=',';

						//METHOD-PARAM SET
						if(param.getType().isArray())
							methodNamedStr+=paramType.substring(0, paramType.indexOf('['))+"Array";
						else if(paramType.contains("<"))
							methodNamedStr+=paramType.substring(0, paramType.indexOf('<'))+"T";
						else
							methodNamedStr+= paramType;

						XSSFRow met_par_row= method_param_sheet.getRow(param_index+1); //2��° �ٿ������� �����Ұ�.
						if(met_par_row == null) met_par_row= method_param_sheet.createRow(param_index+1);
						XSSFCell paramTypeCell = met_par_row.createCell(mets_total); //�Ķ���� Ÿ�� ����Ʈ ����.
						paramTypeCell.setCellValue(paramType);

					}
					methodStr+=')';
					System.out.println(methodStr);

					//ClassMethod sheet
					clz_met_cell= clz_met_row.createCell(clz_met_col_index);
					clz_met_cell.setCellValue(methodStr);

					//MethodParam Sheet
					met_par_cell = met_par_firstrow.createCell(mets_total);
					met_par_cell.setCellValue(methodStr);

					if(params.length>0){
						//Method Parameter List Name.
						XSSFName namedCell= workbook.createName();
						namedCell.setNameName(methodNamedStr);
						String cell=cellIndex(mets_total);
						String formula= "MethodParamhidden!$"+cell+"$2:$"+cell+"$" + (params.length+1);
						namedCell.setRefersToFormula(formula);
					}
					i++; 
					mets_total++;
				}//Method loop End

				//Set Class-Method Data ReferenceList
				String currentCol=cellIndex(clz_met_col_index+1);
				String formula= "ClassMethodhidden!$"+currentCol+"$2:$"+currentCol+"$" + (mets.length+1);
				setNamedName(workbook, info.getName(), formula);
			}

			//������ ����Ʈ �� ������ �Ķ����
			XSSFRow con_par_row=null;
			Constructor[] conset= info.getConstructors();
			Constructor con =null;
			for(int con_index=0; con_index< conset.length; con_index++){ 
				con= conset[con_index];

				//�ٸ��̸�������, ���� ��ȿ���� ����Ű�� �̸� ����. INDIRECT�� ���ؼ� ������..
				String consName=info.getClz().getSimpleName()+"(";
				String consParamNamed="CON"+info.getClz().getSimpleName();
				Parameter[] params= con.getParameters();

				for(int param_index =0; param_index< params.length; param_index++ ){
					//������ Ǯ��Ʈ�� ����
					Parameter param = params[param_index];
					consName+=param.getType().getSimpleName()+" "+param.getName();
					if(param_index != params.length-1) consName+=',';

					String paramType=param.getType().getSimpleName();
					//������+�Ķ���� Ÿ������  ���ֽ̹�Ʈ�� ����
					if(param.getType().isArray())
						consParamNamed+=paramType.substring(0, paramType.indexOf('['))+"Array";
					else if(paramType.contains("<"))
						consParamNamed+=paramType.substring(0, paramType.indexOf('<'))+"T";
					else 
						consParamNamed+=paramType;

					//�Ķ���� Ÿ�� ������
					con_par_row=cons_param_sheet.getRow(param_index+1);
					if(con_par_row ==null) con_par_row=cons_param_sheet.createRow(param_index+1);
					XSSFCell paramcell=con_par_row.createCell(cons_total);
					paramcell.setCellValue(param.getType().getSimpleName());
				}
				consName+=')';
				System.out.println(consName);

				//������ �� ����. .
				XSSFCell consCell = cons_par_firstrow.createCell(cons_total);
				consCell.setCellValue(consName);

				if(params.length>0){
					//������ ���ӻ���
					String cell=cellIndex(cons_total);
					String formula="ConstructorParamhidden!$"+cell+"$2:$"+cell+"$"+(params.length+1);
					setNamedName(workbook, consParamNamed, formula);
				}
				cons_total++;
			}//Constructor Loop End

			clz_met_col_index++;
		}//Class loop End.

		//Set Class Data ReferenceList.
		String cell=cons_total>0?cellIndex(cons_total):"A";
		String formula= "ConstructorParamhidden!$A$1:$"+cell+"$1";
		setNamedName(workbook, "Class", formula);
		System.out.println("total : " + cons_total);

		//Set hidden Sheet if true=  hidden.
		int consParam=workbook.getSheetIndex("ConstructorParamhidden");
		int metParam= workbook.getSheetIndex("MethodParamhidden");
		int clzMet=workbook.getSheetIndex("ClassMethodhidden");

		if(!workbook.isSheetHidden(consParam)) workbook.setSheetHidden(consParam, true);
		if(!workbook.isSheetHidden(metParam)) workbook.setSheetHidden(metParam, true);
		if(!workbook.isSheetHidden(clzMet)) workbook.setSheetHidden(clzMet, true);
	}

	private void setNamedName(XSSFWorkbook workbook, String name, String formula){
		XSSFName namedcell =workbook.createName();
		namedcell.setNameName(name); //Nameing�� �߿�.
		namedcell.setRefersToFormula(formula);
	}

	private void unsetValidation(XSSFSheet xssfSheet){
		CTWorksheet ctsheet = xssfSheet.getCTWorksheet();
		if(ctsheet.isSetDataValidations()){
			ctsheet.unsetDataValidations();
			ctsheet.setDataValidations(null);
		}
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
			addresslist = new CellRangeAddressList(2,500,col,col);
			dataValidation= validationHelper.createValidation(constraint, addresslist);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.createErrorBox("Wrong Input", "You must input Right Type.");
			xssfSheet.addValidationData(dataValidation);
		}		
	}

	private static String cellIndex(int offset){
		char a= 'A'-1;
		int q=Math.floorDiv(offset, 26),r=offset%26;
		if(q==0) {
			if(r==0)return new Character((char) (a+r+1)).toString();
			return new Character((char) (a+r)).toString();
		}
		if(r==0)return new Character((char) (a+26)).toString();
		return cellIndex(q)+(char)(a+r);
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
