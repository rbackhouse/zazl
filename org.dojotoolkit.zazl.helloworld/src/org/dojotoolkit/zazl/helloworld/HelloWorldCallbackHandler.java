package org.dojotoolkit.zazl.helloworld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dojotoolkit.zazl.optimizer.OptimizerCallbackHandler;
import org.dojotoolkit.zazl.util.JSONUtils;

public class HelloWorldCallbackHandler extends OptimizerCallbackHandler {
	private static final String[] myCallbackNames = {
	     "helloWorld"
	};

	public HelloWorldCallbackHandler(HttpServletRequest request) {
		super(request);
	}

	public HelloWorldCallbackHandler() {
		super();
	}

	protected void collectCallbackNames(List<String> callbackNameList) {
		super.collectCallbackNames(callbackNameList);
		for (String callbackName : myCallbackNames) {
			callbackNameList.add(callbackName);
		}
	}
	
	public String helloWorld(String json) {
		Map<String, String> result = new HashMap<String, String>();
		result.put("helloWorldMsg", "Hello, World");
		return JSONUtils.toJson(result);
	}
}
