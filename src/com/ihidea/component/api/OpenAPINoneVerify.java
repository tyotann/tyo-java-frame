package com.ihidea.component.api;

import java.util.Map;

public class OpenAPINoneVerify implements IAPIVerify {

	@Override
	public boolean verify(Map<String, Object> params) {
		return true;
	}

}
