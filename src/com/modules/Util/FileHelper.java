package com.modules.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileHelper {
	
	
	public static String FILE_SEPARATOR = System.getProperty("file.separator");
	
	public static String JAVA_PACKAGE_CONNECTOR = ".";
	
	public static final String CLASS_FILE_SUFFIX = ".class";
	
	/**
	 * 
	 * @param classesPath	your complied classes path
	 * @param javaPackagePath	your package name , ie "com.xinyuan.model"
	 * @return
	 */
	public static List<String> getClassesNames(String classesPath, String javaPackagePath) {
		String fullPath = connectClassesPackagePath(classesPath, javaPackagePath);
		
		List<String> fileNameList = listFilesForFolder(fullPath);
		
		
		List<String> classesNamesList = new ArrayList<String>();
		for (Iterator<String> iterator = fileNameList.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();				// "Approval.Approvals.class"
			String className = string.replaceAll(CLASS_FILE_SUFFIX, "");
			String wholeClassName =javaPackagePath + JAVA_PACKAGE_CONNECTOR + className;			// MODELPACKAGE + "Approval.Approvals"
			classesNamesList.add(wholeClassName);
		}
		
		return classesNamesList;
	}
	
	public static String connectClassesPackagePath(String classesPath, String javaPackagePath) {
		if (!classesPath.endsWith(FILE_SEPARATOR)) {
			classesPath = classesPath + FILE_SEPARATOR;
		}
		String packagePath = javaPackagePath.replaceAll("\\.", FILE_SEPARATOR);
		String fullPath = classesPath + packagePath;
		return fullPath;
	}
	
	
	public static List<String> listFilesForFolder(String fullPath) {
		File folder = new File(fullPath);
		List<String> fileList = new ArrayList<String>();
		FileHelper.listFilesForSubFolder(folder, fileList);
		return fileList;
	}
	
	public static void listFilesForFolder(File folder, List<String> list) {
	    for (File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry, list);
	        } else {
//	            System.out.println(fileEntry.getName());
	        	list.add(fileEntry.getParentFile().getName() + "." + fileEntry.getName());		// use "parentFolder.fileName" format . i.e. "Approval/Approvals.class"-> "Approval.Approvals.class"
	        }
	    }
	}
	
	public static void listFilesForSubFolder(File folder, List<String> list) {
	    for (File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry, list);
	        }
	    }
	}

}
