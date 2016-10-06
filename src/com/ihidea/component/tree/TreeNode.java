package com.ihidea.component.tree;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <per> 树的节点实体对象 </per>
 * @author XUYI
 */
public class TreeNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5132267135631042333L;

	// 叶子ID
	private String nodeId = "";

	// 叶子显示名称
	private String nodeName = "";

	// 叶子父节点id
	private String parentId = "";

	// 该节点是否默认打开
	private boolean open = false;

	// 该节点是否为目录(true：目录 ;false：文件)
	private boolean hasChild = true;

	// dhtmlxtree中节点可选参数配置(例如:
	// select:是否选中；im0:没有儿子的节点图片；im1:打开有儿子节点时的图片；im2:关闭有儿子节点的图片；tooltip:节点的提示等)
	private Map<String, String> attributes = new HashMap<String, String>();

	// 所需userdata自定义数据集
	private Map<String, String> params = new HashMap<String, String>();

	public TreeNode() {

	}

	public TreeNode(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public boolean isHasChild() {
		return hasChild;
	}

	public void setHasChild(boolean hasChild) {
		this.hasChild = hasChild;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + (hasChild ? 1231 : 1237);
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
		result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
		result = prime * result + (open ? 1231 : 1237);
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TreeNode other = (TreeNode) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (hasChild != other.hasChild)
			return false;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		if (nodeName == null) {
			if (other.nodeName != null)
				return false;
		} else if (!nodeName.equals(other.nodeName))
			return false;
		if (open != other.open)
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (parentId == null) {
			if (other.parentId != null)
				return false;
		} else if (!parentId.equals(other.parentId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("TreeNode [nodeId=" + nodeId + ", nodeName=" + nodeName + ", hasChild=" + hasChild + ", open="
				+ open + ", parentId=" + parentId);
		if (params.size() > 0) {
			for (Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				String value = params.get(key);
				sb.append(", ").append(key).append("=").append(value);
			}
		}
		if (attributes.size() > 0) {
			for (Iterator<String> it = attributes.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				String value = attributes.get(key);
				sb.append(", ").append(key).append("=").append(value);
			}
		}
		return sb.append("]").toString();
	}

}
