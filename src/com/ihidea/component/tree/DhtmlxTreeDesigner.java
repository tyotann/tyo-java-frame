package com.ihidea.component.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.ihidea.core.util.JSONUtilsEx;

/**
 * @author TYOTANN
 */
public class DhtmlxTreeDesigner {

	public static <T> String createZTree(String parentId, List<T> list, ITreeDesigner<T> designer) {

		Map<String, List<TreeEntity>> treeMap = new HashMap<String, List<TreeEntity>>();

		// 树对象
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();

		for (Iterator<T> i = list.iterator(); i.hasNext();) {

			TreeEntity node = designer.getTreeEntity(i.next());

			List<TreeEntity> nodeList = null;

			if (treeMap.containsKey(node.getParentId())) {
				nodeList = treeMap.get(node.getParentId());
			} else {
				nodeList = new ArrayList<TreeEntity>();
				treeMap.put(node.getParentId(), nodeList);
			}
			nodeList.add(node);
		}
		result.addAll(getChildList(parentId, treeMap));

		return JSONUtilsEx.serialize(result);
	}

	private static List<Map<String, String>> getChildList(String id, Map<String, List<TreeEntity>> tree) {

		List<Map<String, String>> result = new ArrayList<Map<String, String>>();

		if (tree.containsKey(id)) {

			List<TreeEntity> entityList = tree.get(id);

			Collections.sort(entityList, new Comparator<TreeEntity>() {
				public int compare(TreeEntity o1, TreeEntity o2) {
					return (int) (o1.getSort() - o2.getSort());
				}
			});

			for (int i = 0; i < entityList.size(); i++) {

				TreeEntity node = entityList.get(i);

				Map<String, String> nodeMap = new HashMap<String, String>();

				nodeMap.put("id", node.getId());
				nodeMap.put("pId", node.getParentId());
				nodeMap.put("name", node.getName());

				if (node.isHasChild()) {
					nodeMap.put("isParent", "true");
				}

				// add data
				for (String dataName : node.getData().keySet()) {
					nodeMap.put(dataName, node.getData().get(dataName));
				}

				result.add(nodeMap);

				result.addAll(getChildList(node.getId(), tree));
			}
		}

		return result;

	}

	public static <T> String createTree(String parentId, List<T> list, ITreeDesigner<T> designer) {

		// 做成树对象
		Map<String, List<TreeEntity>> treeMap = new HashMap<String, List<TreeEntity>>();

		for (Iterator<T> i = list.iterator(); i.hasNext();) {

			TreeEntity node = designer.getTreeEntity(i.next());

			List<TreeEntity> nodeList = null;

			if (treeMap.containsKey(node.getParentId())) {
				nodeList = treeMap.get(node.getParentId());
			} else {
				nodeList = new ArrayList<TreeEntity>();
				treeMap.put(node.getParentId(), nodeList);
			}
			nodeList.add(node);
		}

		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<tree id=\"" + parentId + "\"  >");
		sb.append(getChildLevelXml(parentId, treeMap));
		sb.append("</tree>");

		return sb.toString();
	}

	private static String getChildLevelXml(String id, Map<String, List<TreeEntity>> tree) {

		StringBuffer sb = new StringBuffer();

		if (tree.containsKey(id)) {
			List<TreeEntity> entityList = tree.get(id);

			Collections.sort(entityList, new Comparator<TreeEntity>() {
				public int compare(TreeEntity o1, TreeEntity o2) {
					return (int) (o1.getSort() - o2.getSort());
				}
			});

			for (int i = 0; i < entityList.size(); i++) {

				TreeEntity node = entityList.get(i);

				sb.append("<item text=\"" + node.getName() + "\" id=\"" + node.getId() + "\" ");

				// add has child
				node.getAttributes().put("child", node.isHasChild() ? "1" : "0");

				// add Attribute
				for (String attribute : node.getAttributes().keySet()) {
					sb.append(" " + attribute + "=\"" + node.getAttributes().get(attribute) + "\" ");
				}

				sb.append(">");

				// add data
				for (String dataName : node.getData().keySet()) {
					sb.append("<userdata name=\"" + dataName + "\" ><![CDATA[").append(node.getData().get(dataName))
							.append("]]></userdata>");
				}

				sb.append(getChildLevelXml(node.getId(), tree));
				sb.append("</item>");
			}
		}

		return sb.toString();

	}

	/**
	 * <pre>
	 * 根据TreeNode创建相应的树的xml
	 * 
	 * <pre>
	 * @param tree  节点结果集 ，若延迟加载则该结果集是点击节点下的子节点的集合；若全部加载则该结果集是整棵树的节点集合
	 * @param rootNode  树根节点:若没有自定义根节点，nodeId直接绑定页面根节点的id，一般指定数据库根节点id。若有自定义根节点，parentId绑定页面根节点id，nodeId一般绑定数据库根节点id，如果没有数据库根节点id则可以自己任意赋值。nodeName用来判断是否有自定义根节点
	 * @param lazy   true为默认值即延迟加载，点击子节点后ajax到后台加载取子节点 ，此时不需要给parentId赋值 ;   false为全部加载，全部加载时由于要构造整棵树，所以TreeNode中parentId必须赋值
	 * @return
	 */
	public static String getTreeXML(List<TreeNode> tree, TreeNode rootNode, boolean lazy) {

		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");

		// 添加root node
		Element root = document.addElement("tree");

		// 创建树根节点，有自定义根节点则使用rootNode的parentId，无自定义根节点则使用rootNode的nodeId
		if (StringUtils.isNotBlank(rootNode.getNodeName()))
			root.addAttribute("id", rootNode.getParentId());
		else
			root.addAttribute("id", rootNode.getNodeId());
		Element definedElement = null;

		// 创建自定义根节点
		if (StringUtils.isNotBlank(rootNode.getNodeName())) {
			definedElement = DhtmlxTreeDesigner.createItem(root, rootNode);
		}

		Element baseElement = null;
		if (definedElement != null)
			baseElement = definedElement;
		else
			baseElement = root;

		if (!lazy) {
			// 递归遍历生成整棵树
			if (tree != null && tree.size() > 0) {
				Map<String, List<TreeNode>> validateMap = getValidateMap(tree);
				DhtmlxTreeDesigner.createItems(baseElement, validateMap, rootNode.getNodeId());
			}
		} else {
			// 遍历生成子节点
			if (tree != null) {
				for (TreeNode node : tree) {
					DhtmlxTreeDesigner.createItem(baseElement, node);
				}
			}
		}

		return document.asXML();
	}

	/**
	 * <pre>
	 * 对树进行缓存处理的map,异步加载时可根据传回的parentId从map中取得子节点对象集合(配合缓存方式使用)
	 * 
	 * <pre>
	 * @param tree
	 * @return
	 */
	public static Map<String, List<TreeNode>> getValidateMap(List<TreeNode> tree) {

		// TODO

		Map<String, List<TreeNode>> validateMap = new HashMap<String, List<TreeNode>>();
		if (tree != null) {
			for (TreeNode node : tree) {
				List<TreeNode> list = validateMap.get(node.getParentId());
				if (list == null) {
					list = new ArrayList<TreeNode>();
				}
				list.add(node);
				validateMap.put(node.getParentId(), list);
			}
		}
		return validateMap;
	}

	/**
	 * <pre>
	 * 递归遍历生成整棵树
	 * 
	 * <pre>
	 * @param baseRoot
	 * @param validateMap
	 * @param parentId
	 */
	private static void createItems(Element baseRoot, Map<String, List<TreeNode>> validateMap, String parentId) {
		List<TreeNode> node = validateMap.get(parentId);
		if (node != null && node.size() > 0) {
			for (TreeNode leaf : node) {
				Element element = DhtmlxTreeDesigner.createItem(baseRoot, leaf);
				createItems(element, validateMap, leaf.getNodeId());
			}
		}
	}

	/**
	 * <pre>
	 * 组装子元素到节点
	 * 
	 * <pre>
	 * @param root
	 * @param node
	 * @return
	 */
	private static Element createItem(Element root, TreeNode node) {
		// 添加item node
		Element item = root.addElement("item");
		item.addAttribute("id", node.getNodeId());
		if (node.isHasChild())
			item.addAttribute("child", "1");
		else
			item.addAttribute("child", "0");
		if (node.isOpen())
			item.addAttribute("open", "1");
		item.addAttribute("text", node.getNodeName());

		// 添加item node可选参数配置
		if (node.getAttributes().size() > 0) {
			for (Iterator<String> it = node.getAttributes().keySet().iterator(); it.hasNext();) {
				String name = it.next();
				String value = node.getAttributes().get(name) == null ? "" : node.getAttributes().get(name);
				item.addAttribute(name, value);
			}
		}

		// 添加userData node自定义参数配置
		Element userData = null;
		if (node.getParams().size() > 0) {
			for (Iterator<String> it = node.getParams().keySet().iterator(); it.hasNext();) {
				String name = it.next();
				String value = node.getParams().get(name) == null ? "" : node.getParams().get(name);
				userData = item.addElement("userdata");
				userData.addAttribute("name", name);
				userData.addText(value);
			}
		}
		return item;
	}

}
