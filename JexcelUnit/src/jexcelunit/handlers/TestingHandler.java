package jexcelunit.handlers;

import java.io.File;
import java.io.FileWriter;

import java.io.PrintWriter;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;



public class TestingHandler extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkspaceRoot root=  ResourcesPlugin.getWorkspace().getRoot();
		ListSelectionDialog dlg = new ListSelectionDialog(window.getShell(), 
				root, 
				new BaseWorkbenchContentProvider(), new WorkbenchLabelProvider(), "Select the Project:");
		dlg.setTitle("Project Selection");
		dlg.open();
		Object[] results = (Object[]) dlg.getResult();
		IProject targetproject =null;
		//		ArrayList<Class> classlist=null;

		if(results.length>0)
			for(Object result : results){

				targetproject =root.getProject(result.toString().substring(2));
				System.out.println(targetproject.getName());
				String rootpath= targetproject.getLocation().toString();

				
				//Make Suite Class.
				PrintWriter pw = null; 
				File suiteclass= new File( rootpath+"/src/TestSuite.java" );
				if(!suiteclass.exists()){
					try {
						pw =  new PrintWriter(new FileWriter(suiteclass));

						String importinvoker= "import jexcelunit.testinvoker.TestInvoker;\n";
						String[] classcode={
								"public class TestSuite extends TestInvoker{",
								"\tpublic TestSuite(String testname, Class targetclz, Object[] constructor_params, Object expectedResult,",
								"\tString methodname, Object[] param1) {\n",
								"\t\tsuper(testname, targetclz, constructor_params, methodname, param1,expectedResult);",
								"\t}",
								"\tprivate void setup() {",
								"\t\t/* Make Your Mock Objects  using mockObject.put(\"mock name\", mock object);",
								"\t\t* Make Your Custom Exceptions using  addException(your Exception e);*/",
								"\t}\n}"
						};

						pw.println(importinvoker);
						for(String code : classcode){
							pw.println(code);
						}
						pw.close();
					}catch (Exception e) {
						e.printStackTrace();
					}
	
					
				}
				

				//creator must Create Suite Class extended TestInvoker
				//JUnitCore jc= new JUnitCore(); 			

				/*
				 * 1. �¾��ڵ尡 �ִ���(��ũ����Ұǰ�) ���θ� Ȯ��.
				 * 1-1. �ִٸ� Suite Ŭ������ �������ش�. TestInvoker ����� ����� Ŭ������ �������ش�/.
				 * 1-1-1. �̹� SuiteŬ������ �ִ����� Ȯ���Ұ�. 
				 * 
				 * 1-2. �ƴϸ� �ٷ� Excel�� �о ����.
				 * 2.  �̽� ** �α� ������ ��� �Ұǰ� ? 
				 * 
				 * 	TARGET PROJECT�� META-INF�� �����Ѵ�.
				 * */

			}

		return null;
	}

}
