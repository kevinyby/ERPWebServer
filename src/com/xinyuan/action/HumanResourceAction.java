package com.xinyuan.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opensymphony.xwork2.Action;
import com.xinyuan.dao.BaseDAO;
import com.xinyuan.dao.impl.HumanResourceDAOIMP;
import com.xinyuan.message.MessageConstants;
import com.xinyuan.message.ResponseMessage;


public class HumanResourceAction extends BaseAction {
	
	@Override
	protected BaseDAO getDao() {
		return new HumanResourceDAOIMP();
	}
	
	public String create() throws Exception {
		super.create();
		if (message.status.equals(ResponseMessage.STATUS_SUCCESS)) {
			
			String json = request.getParameter(MessageConstants.JSON);
			JsonObject jsonObject = (JsonObject)(new JsonParser()).parse(json);
			JsonElement objectElement = jsonObject.get(MessageConstants.USERS);
			String objectString = new Gson().toJson(objectElement);
			
			Class<?> modelClass = Class.forName(MessageConstants.MODELPACKAGE + ".User");
			Object object = new GsonBuilder().setDateFormat(MessageConstants.DATE_FORMAT).create().fromJson(objectString, modelClass);

			// TODO, check the username == employeeNO
			dao.create(object);
		}
		
		return Action.NONE;
	}
}
