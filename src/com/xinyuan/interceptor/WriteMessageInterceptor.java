package com.xinyuan.interceptor;


import java.io.IOException;
import java.util.Date;

import com.google.gson.Gson;
import com.modules.httpWriter.ResponseWriter;
import com.modules.util.DLog;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.xinyuan.action.ActionBase;
import com.xinyuan.message.MessageConstants;
import com.xinyuan.message.ResponseMessage;

public class WriteMessageInterceptor extends AbstractInterceptor {
	
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		ActionBase action = (ActionBase)invocation.getAction();
		ResponseMessage message = action.getMessage(invocation);
		
		DLog.log("WriteMessageInterceptor Ready");
		
		Exception exceptionInvoke = null;
		try {
			invocation.invoke();
		} catch (Exception e) {
			exceptionInvoke = e;
			message.status = ResponseMessage.STATUS_FAILED;
			message.description = MessageConstants.SERVER_ERROR;
			message.object = null;
			message.exception += (new Date()).toString() + ": \n" + e.toString() + "\n";
//			StackTraceElement[] trace = e.getStackTrace();
//          for (StackTraceElement traceElement : trace) message.exception += ("\tat " + traceElement + "\n");   //TODO: For debug mode , in Production remove it
            		
			e.printStackTrace();
		}
		
		try {
			String json = new Gson().toJson(message); 			// TODO: replace the password using regex
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
//		if (true) {
			throw new RuntimeException(exceptionInvoke); // transaction roll back 
		}
		
		return Action.NONE;
	}

}
