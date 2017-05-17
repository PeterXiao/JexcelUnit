package jexcelunit.utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/*
 *  
 * TODO ���� �ɼǵ��� �� ��������� => Ŭ���� �ð�ȭ ����.
 * + Eclipse Custom UI.
 * + UML-lic Testing.
 * */
@SuppressWarnings("rawtypes")
public class ClassExtractor {


	private List<String> getAllFiles(File dir, String extension) { 
		ArrayList<String> fileList = new ArrayList<>(); 
		getAllFiles(dir, extension, fileList); 
		return fileList; 
	} 

	private void getAllFiles(File dir, String extension, List<String> fileList) { 
		for (File f : dir.listFiles()) { 
			if (f.getName().endsWith(extension)) { 
				String classpath = "";
				File parent=f.getParentFile();
				classpath=f.getName().substring(0,f.getName().indexOf("."));
				//				System.out.println(classpath);	

				while(!parent.getName().equals("bin")){
					classpath= parent.getName()+"."+classpath;
					parent=parent.getParentFile();
				}
				fileList.add(classpath); 
			} 
			if (f.isDirectory()) { 
				getAllFiles(f, extension, fileList);
			} 
		} 
	} 


	public ArrayList<Class> getClasses(IProject project,String encoding) throws Exception 	{
		String rootPath= project.getLocation().toString();
		String binPath= rootPath+"/bin";
		ArrayList<Class> classList= new ArrayList<Class>();

		//�����, �Ķ���� �̸� �����ؼ� �������ϵ��� IJavaProject ����.
		IJavaProject test= JavaCore.create(project);
		Map<String, String> ops = test.getOptions(true);
		ops.replace("org.eclipse.jdt.core.compiler.codegen.methodParameters", JavaCore.GENERATE);
		test.setOptions(ops);
		project = test.getProject();
		project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());

		//Ŭ���� �ε��� ���� ��Ű���� ����
		File dir =  new File(binPath);
		List<String> classFiles= getAllFiles(dir, ".class");

		List<URL> urls = new ArrayList<URL>();
		URLStreamHandler streamHandler = null;
		urls.add(new URL(null, "file:"+dir.getCanonicalPath()+File.separator, streamHandler));

		//Ŭ���� �ε�
		@SuppressWarnings("resource")
		URLClassLoader loader= new URLClassLoader(urls.toArray(new URL[urls.size()]));

		for (String string : classFiles) {
			try{
				classList.add(loader.loadClass(string));
			}
			catch(NoClassDefFoundError e){
				if (e.getMessage().contains("TestInvoker"));
				else throw(e);
			}
		}
		loader.close();

		return classList;
	}

}
