package com.xinyuan.interceptor;


import java.io.IOException;

import org.apache.struts2.ServletActionContext;

import com.modules.HttpWriter.ResponseWriter;
import com.modules.Util.DLog;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.xinyuan.Util.JsonHelper;
import com.xinyuan.action.ActionBase;
import com.xinyuan.message.ConfigConstants;
import com.xinyuan.message.ResponseMessage;

public class WriteMessageInterceptor extends AbstractInterceptor {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;





	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		ActionBase action = (ActionBase)invocation.getAction();
		ResponseMessage message = action.getResponseMessage();
		String url = ServletActionContext.getRequest().getRequestURL().toString();
		message.action = url.substring(url.lastIndexOf("/") + 1);
		
		DLog.log("");
		
		Exception exceptionInvoke = null;
		try {
			invocation.invoke();
		} catch (Exception e) {
			exceptionInvoke = e;
			e.printStackTrace();
			
			message.status = ConfigConstants.STATUS_NEGATIVE;
			String description = message.description == null || message.description.isEmpty() ? getDescription(e) : message.description;
			message.description = description == null || description.isEmpty() ? ConfigConstants.REQUEST_ERROR : description;
			message.objects = null;
			message.exception = e.getClass().getName() + " : " + getDescription(e) ;
		}
		
		
		
		// Write data to client
		
		try {
			String json = JsonHelper.getGson().toJson(message);
			ResponseWriter.write(json.getBytes("UTF-8"));
			DLog.log("Response JSON : " + json);
		} catch (IOException e) {
			exceptionInvoke = e;
			e.printStackTrace();
			DLog.log("ResponseWriter.write() Error Important!!! Cause DB Will Roll Back");
		} finally {
			ResponseWriter.close();
		}
		
		
		
		
		
		if (exceptionInvoke != null) {
//		if (true) {		 // for test transaction roll back 
			throw new RuntimeException(exceptionInvoke);
		}
		
		return Action.NONE;
	}
	
	
	
	
	
	/**
	 * 
	 * @param e private method
	 * @return	return the description
	 */
	private String getDescription(Exception e) {
		String message = e.getLocalizedMessage();
		
		StringBuilder messageBuilder = new StringBuilder();
		if (message != null) messageBuilder.append(message);
		
		Throwable cause = null;
		if ((cause = e.getCause()) != null) {
			String causeMessage = cause.getLocalizedMessage();
			if (causeMessage != null) messageBuilder.append(" | " + causeMessage);
		}
		
		return messageBuilder.toString().replaceAll("\\'", "|");
	}

}
