package com.ihidea.core.util;

import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

public class VelocityUtils {

	public static String asString(String name, String path, Map<String, Object> param) throws Exception {

		StringWriter writer = null;

		// 获取模板引擎
		VelocityEngine ve = new VelocityEngine();

		// 设置参数
		ve.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, path);

		// 处理中文问题
		ve.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
		ve.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
		ve.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");

		try {

			ve.init();

			// Velocity模板
			Template template = ve.getTemplate(name);

			// 设置写入的文件编码,解决中文问题
			writer = new StringWriter();

			VelocityContext context = new VelocityContext();

			if (param != null) {
				for (String key : param.keySet()) {
					context.put(key, param.get(key));
				}
			}

			template.merge(context, writer);

			return writer.toString();
		} finally {

			if (writer != null) {
				writer.flush();
				writer.close();
			}

		}
	}

}
