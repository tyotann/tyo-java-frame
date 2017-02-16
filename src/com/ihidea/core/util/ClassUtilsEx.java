package com.ihidea.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.exception.ServiceException;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;

public class ClassUtilsEx {

	private final static Logger logger = LoggerFactory.getLogger(ClassUtilsEx.class);

	private static Map<String, Object> frameCache = new HashMap<String, Object>();

	/**
	 * <pre>
	 * 根据传入参数,动态反射调用方法
	 * </pre>
	 * 
	 * @param className
	 *            类名
	 * @param methodName
	 *            方法名
	 * @param params
	 *            参数(Map格式)
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object invokeMethod(String className, String methodName, Map<String, Object> params) throws Exception {

		Object service = SpringContextLoader.getBean(className);

		Class cls = service.getClass();

		Object result = null;

		boolean hasMethod = false;

		for (Method method : cls.getDeclaredMethods()) {

			if (method.getName().equals(methodName)) {

				hasMethod = true;

				if (method.getParameterTypes().length == 0) {

					result = method.invoke(service);

				} else if (method.getParameterTypes().length > 0) {

					List<Object[]> methodParamList = getMethodParams(method);

					// 一个参数
					if (methodParamList.size() == 1) {

						Object param = null;

						String methodParamName = (String) methodParamList.get(0)[0];

						Class methodParamType = (Class) methodParamList.get(0)[1];

						// 如果函数参数不是简单类型或者是容器(也就是参数是POJO的时候),或者属性在map中存在,则做转化,否则,在map中取出值后再做转化
						if (!ClassUtilsEx.isSimpleClz(methodParamType) && !Collection.class.isAssignableFrom(methodParamType)
								&& !params.containsKey(methodParamName)) {
							param = BeanUtilsEx.convert(params, methodParamType);
						} else {
							param = BeanUtilsEx.convert(params.get(methodParamName), methodParamType);
						}

						result = method.invoke(service, param);
					} else {

						// 多个参数
						Object[] param = new Object[methodParamList.size()];

						for (int i = 0; i < methodParamList.size(); i++) {

							String methodParamName = (String) methodParamList.get(i)[0];

							Class methodParamType = (Class) methodParamList.get(i)[1];

							if (!params.containsKey(methodParamName)) {
								param[i] = null;
							} else {
								param[i] = BeanUtilsEx.convert(params.get(methodParamName), methodParamType);
							}
						}
						result = method.invoke(service, param);
					}
				}
				break;
			}
		}

		if (!hasMethod) {
			throw new ServiceException("类:" + className + "中没有找到方法:" + methodName);
		}

		return result;
	}

	private static Map<String, List<Object[]>> methodParamsList = new ConcurrentHashMap<String, List<Object[]>>();

	/**
	 * 得到方法参数名与参数类型
	 * 
	 * @param method
	 * @return 按参数顺序放入Object[0]:参数名 Object[1]:参数类型
	 * @throws Exception
	 */
	public static List<Object[]> getMethodParams(Method method) throws Exception {

		String clzName = filterCGLIB(method.getDeclaringClass().getName());
		String methodName = method.getName();

		if (!methodParamsList.containsKey(clzName + "." + methodName)) {
			initMethodParams(clzName, methodName, method);
		}

		return methodParamsList.get(clzName + "." + methodName);
	}

	// ClassPool.insertClassPath会有内存泄露,所以自己创建维护map
	private synchronized static void initMethodParams(String clzName, String methodName, Method method) throws Exception {

		if (!methodParamsList.containsKey(clzName + "." + methodName)) {

			// ClassPool pool = ClassPool.getDefault();
			ClassPool pool = new ClassPool(null);

			// 如果使用ClassPool.getDefault();发现多次调用pool.insertClassPath后有内存泄露，泄露对象:ClassClassPath,ClassPathList
			// 所以改为 new ClassPool(true);每次都重复创建ClassPool,使用完后销毁
			pool.insertClassPath(new ClassClassPath(method.getClass()));

			CtMethod cm = pool.getMethod(clzName, methodName);

			CodeAttribute codeAttribute = cm.getMethodInfo().getCodeAttribute();

			LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

			List<Object[]> paramList = new ArrayList<Object[]>(cm.getParameterTypes().length);

			int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;

			for (int i = 0; i < cm.getParameterTypes().length; i++) {
				Object[] param = new Object[2];
				param[0] = attr.variableName(i + pos);
				param[1] = method.getParameterTypes()[i];
				paramList.add(param);
			}

			methodParamsList.put(clzName + "." + methodName, paramList);
		}
	}

	/**
	 * 过滤掉被代理的类名
	 * 
	 * @param className
	 * @return
	 */
	private static String filterCGLIB(String className) {

		int cglibIdx = StringUtils.indexOf(className, "$$EnhancerBy");
		if (cglibIdx > -1) {
			className = StringUtils.substring(className, 0, cglibIdx);
		}

		return className;
	}

	/**
	 * <pre>
	 * 得到传入Class所有的方法
	 * </pre>
	 * 
	 * @param clz
	 *            class类
	 * @return <方法名,方法>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, List<Method>> getClassMethodMap(Class clz) {

		// Map<String, List<Method>> result = (Map<String, List<Method>>)
		// CacheSupport.get("frameCache", "ClassUtilsEx.getClassMethodMap"
		// + clz.getName(), Map.class);

		Map<String, List<Method>> result = (Map<String, List<Method>>) frameCache.get("ClassUtilsEx.getClassMethodMap" + clz.getName());

		if (result == null) {

			result = new HashMap<String, List<Method>>();

			// 如果有基类，递归取属性
			if (clz.getSuperclass() != null) {

				Map<String, List<Method>> superClassMethodMap = getClassMethodMap(clz.getSuperclass());

				for (String methodName : superClassMethodMap.keySet()) {

					if (result.containsKey(methodName)) {

						// TODO 重复问题
						result.get(methodName).addAll(superClassMethodMap.get(methodName));
					} else {
						result.put(methodName, superClassMethodMap.get(methodName));
					}
				}
				result.putAll(getClassMethodMap(clz.getSuperclass()));
			}

			for (Method method : clz.getDeclaredMethods()) {

				if (!result.containsKey(method.getName())) {
					List<Method> methodList = new ArrayList<Method>();
					result.put(method.getName(), methodList);
				}
				result.get(method.getName()).add(method);
			}

			frameCache.put("ClassUtilsEx.getClassMethodMap" + clz.getName(), result);
		}

		return result;
	}

	/**
	 * 得到Class中包含有传入Annotation类型的方法
	 * 
	 * @param clz
	 *            Class类型
	 * @param annoClz
	 *            Annotation类型
	 * @return 传入Annotation类型标记的方法
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<Method> getClassMethodByAnnotation(Class clz, Class annoClz) throws Exception {

		List<Method> result = (List<Method>) frameCache.get("ClassUtilsEx.getClassMethodByAnnotation" + clz.getName() + annoClz.getName());

		if (result == null) {

			// 如果是以jar包加载,可能使用了不同的classLoader
			clz = Class.forName(filterCGLIB(clz.getName()), true, clz.getClassLoader());

			result = new ArrayList<Method>();

			for (Method method : clz.getMethods()) {

				if (method.getAnnotation(annoClz) != null) {
					result.add(clz.getMethod(method.getName(), method.getParameterTypes()));
				}
			}

			frameCache.put("ClassUtilsEx.getClassMethodByAnnotation" + clz.getName() + annoClz.getName(), result);
		}

		return result;
	}

	/**
	 * 得到类中属性的类型，主要是POJO类
	 * 
	 * @param clz
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, Class> getClassPropertyType(Class clz) {

		Map<String, Class> result = (Map<String, Class>) frameCache.get("ClassUtilsEx.getClassPropertyType" + clz.getName());

		if (result == null) {
			result = new HashMap<String, Class>();

			// 如果有基类，递归取属性
			if (clz.getSuperclass() != null) {
				result.putAll(getClassPropertyType(clz.getSuperclass()));
			}

			for (Field field : clz.getDeclaredFields()) {
				result.put(field.getName(), field.getType());
			}

			frameCache.put("ClassUtilsEx.getClassPropertyType" + clz.getName(), result);
		}

		return result;
	}

	/**
	 * 是否是简单类型的类
	 * 
	 * @param clz
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isSimpleClz(Class clz) {
		return clz.equals(String.class) || clz.equals(Long.class) || clz.equals(BigDecimal.class) || clz.equals(Integer.class)
				|| clz.equals(Double.class) || clz.equals(double.class) || clz.equals(int.class) || clz.equals(long.class)
				|| clz.equals(Float.class) || clz.equals(float.class);
	}

	/**
	 * 表名转化为类的驼峰命名
	 * 
	 * @param tableName
	 * @return
	 */
	public static String tableName2ClassName(String tableName) {

		String result = StringUtils.EMPTY;

		if (!StringUtils.isBlank(tableName)) {
			String[] tableNameArray = tableName.toLowerCase().split("_");

			for (int i = 0; i < tableNameArray.length; i++) {

				result += tableNameArray[i].substring(0, 1).toUpperCase() + tableNameArray[i].substring(1);
			}
		}

		return result;
	}
}
