package com.xinyuan.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.Global.SessionManager;
import com.modules.HttpWriter.ResponseWriter;
import com.modules.Introspector.IntrospectHelper;
import com.modules.Introspector.ModelIntrospector;
import com.modules.Util.DLog;
import com.modules.Util.FileHelper;
import com.modules.Util.SecurityCode;
import com.modules.Util.VerifyCode;
import com.opensymphony.xwork2.Action;
import com.xinyuan.Util.ApnsHelper;
import com.xinyuan.Util.ParametersHelper;
import com.xinyuan.Util.SettingHelper;
import com.xinyuan.dao.BusinessDAO;
import com.xinyuan.dao.HumanResourceDAO;
import com.xinyuan.dao.SuperDAO;
import com.xinyuan.dao.impl.BusinessDAOIMP;
import com.xinyuan.dao.impl.HumanResourceDAOIMP;
import com.xinyuan.dao.impl.SuperDAOIMP;
import com.xinyuan.message.ConfigConstants;
import com.xinyuan.message.ConfigJSON;
import com.xinyuan.model.Setting.APPSettings;

/**
 * 
 * This Action is No need to check permission, but need check signed or not .
 * 		So put the no permission checked user interface here .
 * 
 * With get* prefix , the not signed user can get
 * 
 * With read* prefix , it needs to signed 
 * 
 *
 */
public class SettingAction extends ActionBase {
	
	private static final long serialVersionUID = 1L;
	
	public static boolean isInitilized = false;
	
	@Override
	protected SuperDAO getDao() { return null; }
	
	
	
	/**
	 * connect the server , get the cookie, no db operation here now
	 * @return
	 * @throws Exception
	 */
	public String getConnection() throws Exception {
		if (!isInitilized) {
			Boolean isUserTableEmpty = SettingHelper.isUserTableEmpty();
			if (isUserTableEmpty) {
				responseMessage.descriptions = ConfigConstants.SystemNeedInitialed;
			} else {
				isInitilized = true;
			}
		} 
		
		this.sendVerifyCode();
		Map<String, Map<String, List<String>>> map = this.sendModelsStructures();
		responseMessage.results = map;
		
		responseMessage.status = ConfigConstants.STATUS_POSITIVE;
		return Action.NONE;
	}
	
	private void sendVerifyCode() throws Exception {
		String VERIFYCODE_TYPE = ParametersHelper.getParameter(requestMessage, ConfigJSON.VERIFYCODE_TYPE);
		String VERIFYCODE_COUNT = ParametersHelper.getParameter(requestMessage, ConfigJSON.VERIFYCODE_COUNT);
		
		int count = 4;		// default
		boolean randomBool = new Random().nextBoolean() ;		// default
		try {
			if (VERIFYCODE_COUNT != null) count = Integer.valueOf(VERIFYCODE_COUNT) ;
			if (VERIFYCODE_TYPE != null) randomBool = Boolean.valueOf(VERIFYCODE_TYPE);
		} catch (NumberFormatException e) {
			// 
		}
		String verifyCode = randomBool ? VerifyCode.generateCode(count) : SecurityCode.getSecurityCode(count);
		byte[] imageBytes = randomBool ? VerifyCode.generateImageBytes(verifyCode) : SecurityCode.getImageAsBytes(verifyCode);
		
		// header
		HttpServletResponse response = ResponseWriter.getResponse();
		response.addIntHeader(ConfigJSON.BINARY_LENGHT, imageBytes.length);		// before write data

		// send binary data
		SessionManager.put(ConfigJSON.VERIFYCODE, verifyCode);
		ResponseWriter.write(imageBytes);
		
		DLog.log(ConfigJSON.VERIFYCODE + " : " + verifyCode + " . " + imageBytes.length );
	}
	
	private Map<String, Map<String, List<String>>> sendModelsStructures() {
		// For the first time connect , send the structures
		HttpServletRequest request = ServletActionContext.getRequest();
		String cookie = request.getHeader("cookie");
		if (cookie != null)
			return null;
	
		// get the file name list
		File folder = new File(ConfigConstants.Models_Class_Files_Path);
		List<String> modelsList = new ArrayList<String>();
		FileHelper.listFilesForSubFolder(folder, modelsList);
		
		// get the model package path class name list
		List<String> classesNamesList = new ArrayList<String>();
		for (Iterator<String> iterator = modelsList.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();				// "Approval.Approvals.class" , ".class" not ".java" 
			
			if (string.contains(ConfigConstants.CATEGORIE_USER) || string.contains(ConfigConstants.CATEGORIE_SETTING)) continue;		// exclude user and approval, extensions
			
			String className = string.replaceAll(ConfigConstants.SUFFIX_CLASS, ConfigConstants.EMPTY_STRING);
			String wholeClassName = ConfigConstants.MODELPACKAGE + ConfigConstants.PACKAGE_CONNECTOR + className;			// MODELPACKAGE + "Approval.Approvals"
			classesNamesList.add(wholeClassName);
		}
		
		// translate classes properties name to map
		Map<String, Map<String, List<String>>> categoriesModelsMap = IntrospectHelper.translateToPropertiesMap(classesNamesList);
		categoriesModelsMap.get(ConfigConstants.CATEGORIE_APPROVAL).clear();
		
		return categoriesModelsMap;
	}
	
	
	/**
	 * When client singined
	 * @return
	 */
	public String readSignedIndData() {			// need to refresh 
		
		HumanResourceDAO humanResourceDAO = new HumanResourceDAOIMP();
		List<Object> hrList = humanResourceDAO.getUsersNOPairs();
		
		BusinessDAO businessDAO = new BusinessDAOIMP();
		List<Object> bsList = businessDAO.getClientsNOPairs();
		
		SuperDAOIMP dao = new SuperDAOIMP();
		List<Object> settingList = dao.getObjects("select settings.settings from APPSettings settings where settings.type = '" + ConfigConstants.APPSETTINGS_APPROVALS +"'");
		
		List<Object> results = new ArrayList<Object>();
		results.add(hrList);
		results.add(bsList);
		results.add(settingList);
		
		responseMessage.status = ConfigConstants.STATUS_POSITIVE;
		responseMessage.results = results;
		
		return Action.NONE;
	}
	
	
	/**
	 * Push notifications
	 * @return
	 */
	public String inform() {
		try {
			boolean isAllSuccess = ApnsHelper.inform(requestMessage.getAPNS_FORWARDS(), requestMessage.getAPNS_CONTENTS());
			if (isAllSuccess) responseMessage.status = ConfigConstants.STATUS_POSITIVE;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Action.NONE;
	}
	
	
	public String modifyType() throws Exception {
		
		SuperDAO superDao = new SuperDAOIMP();
		
		List<Map<String, String>> identities = requestMessage.getIDENTITYS();
		
		APPSettings appSettingVO = (APPSettings) models.get(0);
			
		// get PO
		Map<String, String> idenfier = identities.get(0);
		ModelIntrospector.setProperty(appSettingVO, idenfier);
		APPSettings appSettingPO =  superDao.readUnique(appSettingVO, idenfier.keySet());
			
		if (appSettingPO == null) {
			appSettingPO = appSettingVO;
			superDao.create(appSettingPO);
		} else {
			ModelIntrospector.copyVoToPo(appSettingVO, appSettingPO, modelsKeys.get(0));
			superDao.modify(appSettingPO);
		}
		
		return Action.NONE;
	}
	
	
	public String readType() throws Exception{
		
		SuperDAO superDao = new SuperDAOIMP();
		
		List<Map<String, String>> identities = requestMessage.getIDENTITYS();
		
		APPSettings appSettingVO = (APPSettings) models.get(0);
			
		// get PO
		Map<String, String> idenfier = identities.get(0);
		ModelIntrospector.setProperty(appSettingVO, idenfier);
		APPSettings appSettingPO =  superDao.readUnique(appSettingVO, idenfier.keySet());
		
		responseMessage.status = ConfigConstants.STATUS_POSITIVE;
		responseMessage.results = appSettingPO;
		
		return Action.NONE;
	}
	
	
	/**
	 * Product Category Modify
	 * @return
	 * @throws Exception
	 */
	public String modifyProductCategory() throws Exception {
        if (models.size() != 1) return Action.NONE;
		
		SuperDAO superDao = new SuperDAOIMP();
		
		List<Map<String, String>> identities = requestMessage.getIDENTITYS();
		
		APPSettings appSettingVO = (APPSettings) models.get(0);
			
		// get PO
		Map<String, String> idenfier = identities.get(0);
		ModelIntrospector.setProperty(appSettingVO, idenfier);
		APPSettings appSettingPO =  superDao.readUnique(appSettingVO, idenfier.keySet());
			
		if (appSettingPO == null) {
			appSettingPO = appSettingVO;
			superDao.create(appSettingPO);
		} else {
			ModelIntrospector.copyVoToPo(appSettingVO, appSettingPO, modelsKeys.get(0));
			superDao.modify(appSettingPO);
		}
		
		responseMessage.status = ConfigConstants.STATUS_POSITIVE;
		
		return Action.NONE;
	}
	
	/** 
	 * Product Category Read
	 * @return
	 * @throws Exception
	 */
	public String readProductCategory() throws Exception{
		
		SuperDAOIMP productDAO = new SuperDAOIMP();
		List<Object> productCategoryList = productDAO.getObjects("select settings.settings from APPSettings settings where settings.type = '" + ConfigConstants.APPSETTINGS_PRODUCTCATEGORY +"'");
		
		List<Object> results = new ArrayList<Object>();
		results.add(productCategoryList);
		
		responseMessage.status = ConfigConstants.STATUS_POSITIVE;
		responseMessage.results = results;
		
		return Action.NONE;
	}
	
	
}

