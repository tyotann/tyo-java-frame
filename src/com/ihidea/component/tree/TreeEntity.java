package com.ihidea.component.tree;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <per> 树的节点实体对象 </per>
 */
public class TreeEntity implements Serializable {

	// 叶子ID
	private String id = "";

	// 叶子显示名称
	private String name = "";

	// 排序
	private long sort = 0L;

	// 叶子父节点id
	private String parentId = "";

	// 该节点是否为目录(true：目录 ;false：文件)
	private boolean hasChild = true;

	// dhtmlxtree中节点可选参数配置(例如:
	// select:是否选中；im0:没有儿子的节点图片；im1:打开有儿子节点时的图片；im2:关闭有儿子节点的图片；tooltip:节点的提示等)
	private Map<String, String> attributes = new HashMap<String, String>();

	// 所需userdata自定义数据集
	private Map<String, String> data = new HashMap<String, String>();

	public TreeEntity(String id, String name, String parentId, long sort, Boolean hasChild) {
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.sort = sort;
		this.hasChild = hasChild;
		if ("0".equals(parentId)) {
			attributes.put("open", "1");
		}
	}

	public TreeEntity(String id, String name, String parentId, long sort, Boolean hasChild, Map<String, String> attributes,
			Map<String, String> data) {

		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.sort = sort;
		this.hasChild = hasChild;

		if (attributes == null) {
			attributes = new HashMap<String, String>();
		}

		if ("0".equals(parentId)) {
			attributes.put("open", "1");
		}
		this.attributes.putAll(attributes);
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long getSort() {
		return sort;
	}

	public String getParentId() {
		return parentId;
	}

	public boolean isHasChild() {
		return hasChild;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public Map<String, String> getData() {
		return data;
	}

}
