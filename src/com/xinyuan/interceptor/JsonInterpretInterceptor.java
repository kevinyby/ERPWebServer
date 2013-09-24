package com.xinyuan.interceptor;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.modules.util.DLog;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.xinyuan.action.ActionBase;
import com.xinyuan.action.SuperAction;
import com.xinyuan.message.ConfigConstants;
import com.xinyuan.model.BaseOrderModel;
import com.xinyuan.util.JsonHelper;

public class JsonInterpretInterceptor extends AbstractInterceptor {

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		
		DLog.log(" Ready");
		
		SuperAction action = (SuperAction)invocation.getAction();
		HttpServletRequest request = ServletActionContext.getRequest();
		
		String json = request.getParameter(ConfigConstants.JSON);
		JsonObject jsonObject = (JsonObject)(new JsonParser()).parse(json);
		DLog.log(json);
		
		
		List<Object> models = new ArrayList<Object>();
		List<JsonElement> ojects = new ArrayList<JsonElement>();
		
		JsonArray modelsArray = (JsonArray) jsonObject.get(ConfigConstants.MODELS);
		JsonArray objectsArray = (JsonArray) jsonObject.get(ConfigConstants.OBJECTS);
		
		if(modelsArray.size() != objectsArray.size()) return Action.NONE;
		
		for (int i = 0; i < modelsArray.size(); i++) {
			JsonElement modelElement = modelsArray.get(i);
			JsonElement objectElement = objectsArray.get(i);
			
			String modelString = modelElement.getAsString();
			String objectString = JsonHelper.getGson().toJson(objectElement);
			
			Class<?> modelClass = Class.forName(ConfigConstants.MODELPACKAGE + getContextAction() + modelString);
			Object model = JsonHelper.getGson().fromJson(objectString, modelClass);
			
			models.add(model);
			ojects.add(objectElement);
		}
		
		
		action.setModels(models);
		action.setObjects(ojects);
		action.setAllJsonObject(jsonObject);
		
		return invocation.invoke();
	}
	
	public static String getContextName() {
		return ActionContext.getContext().getName();
	}
	public static String getContextAction() {
		String action = getContextName();
		return action.split("__")[0];
	}
	public static String getContextMethod() {
		String action = getContextName();
		return action.split("__")[1];
	}

}
