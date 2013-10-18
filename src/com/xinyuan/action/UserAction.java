package com.xinyuan.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.global.SessionManager;
import com.modules.httpWriter.ResponseWriter;
import com.modules.introspector.ModelIntrospector;
import com.modules.util.DLog;
import com.modules.util.SecurityCode;
import com.modules.util.VerifyCode;
import com.opensymphony.xwork2.Action;
import com.xinyuan.dao.BaseDAO;
import com.xinyuan.dao.UserDAO;
import com.xinyuan.dao.impl.UserDAOIMP;
import com.xinyuan.interceptor.AdministratorInterceptor;
import com.xinyuan.message.ConstantsConfig;
import com.xinyuan.model.User.User;
import com.xinyuan.util.ApprovalHelper;
import com.xinyuan.util.JsonHelper;

public class UserAction extends ActionModelBase {
	@Override
	protected BaseDAO getDao() { return null; }
	
//	private HttpServletRequest request = ServletActionContext.getRequest();
	
	private UserDAO userDAO = new UserDAOIMP();
	
	
	public String connect() throws Exception {
		
//		HttpServletRequest request = ServletActionContext.getRequest();
//		Enumeration<String> enumeration = request.getHeaderNames();
//		while (enumeration.hasMoreElements()) {
//			String name = (String) enumeration.nextElement();
//			String value = request.getHeader(name);
//			System.out.println(name + " : " + value);
//		}
//		
//		HttpSession session = ServletActionContext.getRequest().getSession();
//		String sessionID = session.getId();
//		System.out.println("session id -> " + sessionID);
		
		
//		HttpServletRequest request = ServletActionContext.getRequest();
//		Enumeration<String> enumeration = request.getParameterNames();
//		while (enumeration.hasMoreElements()) {
//			String name = (String) enumeration.nextElement();
//			String value = request.getParameter(name);
//			System.out.println(name + " - " + value);
//		}

		
//		String VERIFYCODE_TYPE = request.getParameter(ConstantsConfig.VERIFYCODE_TYPE);
//		String VERIFYCODE_COUNT = request.getParameter(ConstantsConfig.VERIFYCODE_COUNT);
		
		String VERIFYCODE_TYPE = JsonHelper.getParameter(allJsonObject, ConstantsConfig.VERIFYCODE_TYPE);
		String VERIFYCODE_COUNT = JsonHelper.getParameter(allJsonObject, ConstantsConfig.VERIFYCODE_COUNT);
		
		int count = 7;
		try {
			count = VERIFYCODE_COUNT == null ? 4 : Integer.valueOf(VERIFYCODE_COUNT) ;
		} catch (NumberFormatException e) {
			//
		}
		
		boolean randomBool = (VERIFYCODE_TYPE == null) ? (new Random()).nextBoolean() : Boolean.valueOf(VERIFYCODE_TYPE);
		String verifyCode = randomBool ? VerifyCode.generateCode(count) : SecurityCode.getSecurityCode(count);
		byte[] imageBytes = randomBool ? VerifyCode.generateImageBytes(verifyCode) : SecurityCode.getImageAsBytes(verifyCode);

		SessionManager.put(ConstantsConfig.VERIFYCODE, verifyCode);
		ResponseWriter.write(imageBytes);
		message.status = ConstantsConfig.STATUS_SUCCESS;
		DLog.log(ConstantsConfig.VERIFYCODE + " : " + verifyCode);

		return Action.NONE;
	}

	
	
	public String signin() throws Exception {
		if (models.size() != 1) return Action.NONE;		// Forbid modified multi-
		if (this.isVerifyCodeError()) return Action.NONE;
		
//		for (int i = 0; i < models.size(); i++) {
			
		User model = (User)models.get(0);
		
		
		String username = model.getUsername();
		String password = model.getPassword();
		String apnsToken = JsonHelper.getParameter(allJsonObject,ConstantsConfig.APNSTOKEN);
		
		User user = userDAO.getUser(username);
		if (user == null) {
			message.description = ConstantsConfig.USER.UserNotExist;
		} else if (password.equals(user.getPassword())) {
			List<Object> result = new ArrayList<Object>();
			message.status = ConstantsConfig.STATUS_SUCCESS;
			message.description = ConstantsConfig.USER.UserLoginSuccess;
			
			result.add(user.getId()+"");
			result.add(user.getPermissions());
			result.add(userDAO.getAllUsers());
			message.object = result;
			
			// put the permission in session
			String perssionStr = user.getPermissions();
			SessionManager.put(ConstantsConfig.PERMISSIONS, perssionStr.split(","));
			SessionManager.put(ConstantsConfig.SIGNIN_USER, user);
			
			// update the apnsToken in db
			if (!AdministratorInterceptor.isAdmin(user)) {
				String userToken = ApprovalHelper.getAPNSToken(user.getUsername());
				if (apnsToken != null && !apnsToken.equals(userToken)) {	// TODO: when update token , logout first and clear auto login
					ApprovalHelper.setAPNSToken(username, apnsToken);
				}
			}
			
		} else {
			message.description = ConstantsConfig.USER.UserPasswordError;
		}
			
//		}
		
		

		return Action.NONE;
	}
	
	public String signout() throws Exception {
		SessionManager.remove(ConstantsConfig.PERMISSIONS);
		SessionManager.remove(ConstantsConfig.SIGNIN_USER);
		
		User user = (User) SessionManager.get(ConstantsConfig.SIGNIN_USER);
//		ApprovalHelper.deleteAPNSToken(username, apnsToken);
		ApprovalHelper.setAPNSToken(user.getUsername(), "");
		
		message.status = ConstantsConfig.STATUS_SUCCESS;
		return Action.NONE;
	}
	
	
	public String modify() throws Exception {
		
		return Action.NONE;
	}
	
	
	
	
	
	/**
	 * 
	 * Admin Begin
	 * 
	 * 
	 * 
	 */
	
	
	public String adminModify() throws Exception {
//		if (models.size() != 1) return Action.NONE;		// Forbid modified multi-
		
		List<String> identityList = JsonHelper.getListFromJson(allJsonObject, ConstantsConfig.IDENTITYS, false);
		
		for (int i = 0; i < models.size(); i++) {
			
			User model = (User)models.get(i);
			
			Set<String> keys = objectKeys.get(i);
			
			String username = identityList.get(i);
			User persistence = userDAO.getUser(username); 	// po
			
			ModelIntrospector.copyVoToPo(model, persistence, keys);
			
			userDAO.modify(persistence);
		}
		
		message.status = ConstantsConfig.STATUS_SUCCESS;
		return Action.NONE;
	}
	
	
	public String signup() throws Exception {
		return Action.NONE; // TODO: ...
	}
	
	
	
	
	/**
	 * 
	 * Admin End
	 * 
	 * 
	 * 
	 */
	
	
	
	
	
	
	
	
	/**
	 * Private Methods
	 * @return
	 */
	private boolean isVerifyCodeError() {
		
		String userVerifyCode = JsonHelper.getParameter(allJsonObject, ConstantsConfig.VERIFYCODE);
		String verifyCode = (String) SessionManager.get(ConstantsConfig.VERIFYCODE);
		SessionManager.remove(ConstantsConfig.VERIFYCODE);
		
		boolean isError = userVerifyCode == null || userVerifyCode.isEmpty() || !userVerifyCode.equals(verifyCode);
		if (isError) message.description = ConstantsConfig.USER.VerifyCodeError;
		
//		return isError;
		
		return false ; // TODO: For test
	}

	
	
}
