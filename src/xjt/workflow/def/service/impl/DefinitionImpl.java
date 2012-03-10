package xjt.workflow.def.service.impl;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.xml.sax.InputSource;

import xjt.workflow.dao.hibernate.HibernateDAO;
import xjt.workflow.def.Action;
import xjt.workflow.def.Node;
import xjt.workflow.def.Transition;
import xjt.workflow.def.WorkFlow;
import xjt.workflow.def.WorkItem;
import xjt.workflow.def.service.DefinitionService;
import xjt.workflow.util.ParserUtil;

/**
 * 解析流程定义的服务的实现类
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class DefinitionImpl extends HibernateDAO implements DefinitionService {
	/**
	 * 创建一个流程定义
	 * @param inputStream  流程定义文件的输入流
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 * @throws Exception 
	 * */
	public void createWorkFlow(InputStream InputStream)throws Exception{
		//流程定义的Document对象
		Document document = ParserUtil.parserXml(InputStream);
		//根节点
		Element rootElement = ParserUtil.getRootElement(document);
		//流程定义的名称
		String defName = ParserUtil.getAttributeValue(rootElement,"name");
		//此流程名的最新流程定义
		WorkFlow workFlow = this.findLastWorkFlow(defName);
		if(workFlow!=null){
			//创建一个新的工作流程定义
			WorkFlow newWorkFlow = new WorkFlow();
			newWorkFlow.setName(workFlow.getName());
			//将流程定义版本加1
			newWorkFlow.setVersion(new Long(workFlow.getVersion().longValue()+1));
			super.insert(newWorkFlow);
			workFlow = newWorkFlow;
		}else{
			workFlow = new WorkFlow();
			workFlow.setName(defName);
			//新定义流程，版本为1
			workFlow.setVersion(new Long(1));
			super.insert(workFlow);
		}
		//根元素的子素元素列表，在此为<node></node>元素列表
		Iterator it = ParserUtil.getElementList(rootElement);
		while(it.hasNext()){
			Element nodeElement = (Element)it.next();
			//node名称
			String nodeName = ParserUtil.getAttributeValue(nodeElement, "name");
			//node类型
			String nodeType = ParserUtil.getAttributeValue(nodeElement, "type");
			Node node = new Node();
			node.setName(nodeName);
			node.setType(nodeType);
			//node节点所属流程定义
			node.setWorkFlow(workFlow);
			super.insert(node);
			//node节点包含的action定义
			Element actionElement = ParserUtil.getElement(nodeElement, "action");
			String actionName = null;
			String actionClass = null;
			if(actionElement!=null){
				//action名称
				actionName = ParserUtil.getAttributeValue(actionElement, "name");
				//action的类名，，为实现特定接口的类的名称
				actionClass = ParserUtil.getAttributeValue(actionElement, "class");
				Action action = new Action();
				action.setName(actionName);
				action.setClassName(actionClass);
				action.setNode(node);
				super.insert(action);
			}
			//node节点包含的workitem定义
			Element itemElement = ParserUtil.getElement(nodeElement, "workitem");
			String itemName = null;
			String itemOwner = null;
			//task类型的节点有且只有一个workitem定义
			if(itemElement!=null){
				//workitem名称
				itemName = ParserUtil.getAttributeValue(itemElement, "name");
				//workitem的拥有者标识
				itemOwner = ParserUtil.getAttributeValue(itemElement, "owner");
				WorkItem workItem = new WorkItem();
				workItem.setName(itemName);
				workItem.setOwner(itemOwner);
				workItem.setNode(node);
				super.insert(workItem);
			}
		}
		Iterator otherIt = ParserUtil.getElementList(rootElement);
		while(otherIt.hasNext()){
			Element nodeElement = (Element)otherIt.next();
			//node的transition路径定义集合
			Iterator tranIterator = ParserUtil.getElementListByName(nodeElement,"transition");
			while(tranIterator.hasNext()){
				Element tranElement = (Element)tranIterator.next();
				StringBuffer sql = new StringBuffer();
				sql.append("FROM com.pujin.workflow.def.Node node WHERE node.name = '")
					.append(ParserUtil.getAttributeValue(nodeElement, "name"))
					.append("'").append(" AND node.workFlow.id = ").append(workFlow.getId());
				//开始节点
				Node fromNode = (Node)super.search(sql.toString()).get(0);
				String tranName = ParserUtil.getAttributeValue(tranElement, "name");
				String to = ParserUtil.getAttributeValue(tranElement, "to");
				sql.setLength(0);
				sql.append("FROM com.pujin.workflow.def.Node node WHERE node.name = '")
					.append(to).append("'").append(" AND node.workFlow.id = ").append(workFlow.getId());
				//结束节点
				Node toNode = (Node)super.search(sql.toString()).get(0);
				Transition transition = new Transition();
				transition.setName(tranName);
				//路径的开始节点
				transition.setNode(fromNode);
				transition.setEndNode(toNode);
				super.insert(transition);
			}
		}
	}
	/**
	 * 查询最新版本的流程定义
	 * @param name  流程定义名
	 * @return workFlow 流程实例
	 * @since Aug 31, 2010
	 * @author xjt
	 * @version 1.00  Aug 31, 2010
	 * @throws Exception 
	 * */
	public WorkFlow findLastWorkFlow(String name) throws Exception{
		StringBuffer strBuf = new StringBuffer();
		//查找最新版本的流程定义
		strBuf.append("FROM com.pujin.workflow.def.WorkFlow workFlow WHERE workFlow.name = '")
				.append(name).append("' ORDER BY workFlow.version DESC");
		List lst = super.search(strBuf.toString());
		WorkFlow workFlow = null;
		if(lst.size()>0){
			workFlow = (WorkFlow)lst.get(0);
		}
		return workFlow;
	}
	/**
	 * 从流程设计器发布一个流程定义
	 * @param xmlStr xml格式的字符串
	 * @since Oct 20, 2010
	 * @author xjt
	 * @version 1.00  Oct 20, 2010
	 * */
	/**
	 * 从流程设计器发布一个流程定义
	 * @param xmlStr xml格式的字符串
	 * @param flowName 工作流程名称
	 * @param ownerStr 工作项拥有者标识字符串
	 * @param actionStr  动作类标识字符串
	 * @since Oct 20, 2010
	 * @author xjt
	 * @version 1.00  Oct 20, 2010
	 * */
	public void createProcessDefinition(String xmlStr,String flowName,String ownerStr,String actionStr)throws Exception{
		WorkFlow workFlow = this.findLastWorkFlow(flowName);
		if(workFlow!=null){
			//创建一个新的工作流程定义
			WorkFlow newWorkFlow = new WorkFlow();
			newWorkFlow.setName(workFlow.getName());
			//将流程定义版本加1
			newWorkFlow.setVersion(new Long(workFlow.getVersion().longValue()+1));
			super.insert(newWorkFlow);
			workFlow = newWorkFlow;
		}else{
			workFlow = new WorkFlow();
			workFlow.setName(flowName);
			//新定义流程，版本为1
			workFlow.setVersion(new Long(1));
			super.insert(workFlow);
		}
		String [] owners = null;
		Map ownerMap = new HashMap();
		if(!"".equalsIgnoreCase(ownerStr)&&ownerStr!=null){
			owners = ownerStr.split(",");
			for(int i=0;i<owners.length;i++){
				String [] owner = owners[i].split(":");
				if(ownerMap.containsKey(owner[0])){
					ownerMap.remove(owner[0]);
					ownerMap.put(owner[0], owner[1].trim());
				}else{
					ownerMap.put(owner[0], owner[1].trim());
				}
			}
		}
		String [] actions = null;
		Map actionMap = new HashMap();
		if(!"".equalsIgnoreCase(actionStr)&&actionStr!=null){
			actions = actionStr.split(",");
			for(int i=0;i<actions.length;i++){
				String [] action = actions[i].split(":");
				if(actionMap.containsKey(action[0])){
					actionMap.remove(action[0]);
					actionMap.put(action[0], action[1].trim());
				}else{
					actionMap.put(action[0], action[1].trim());
				}
			}
		}
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		org.w3c.dom.Document document = docBuilder.parse(new InputSource(new StringReader(xmlStr)));
		DOMReader reader = new DOMReader();
		org.dom4j.Document doc = reader.read(document);
		Element root = ParserUtil.getElement(ParserUtil.getRootElement(doc),"root");
		Iterator iterator = ParserUtil.getElementList(root);
		List transitionList = new ArrayList();
		Map nodeMap = new HashMap();
		while(iterator.hasNext()){
			Element mxCell = (Element)iterator.next();
			if("start".equalsIgnoreCase(ParserUtil.getAttributeValue(mxCell, "value"))&&"shape=ellipse".equalsIgnoreCase(ParserUtil.getAttributeValue(mxCell, "style"))){
				Node node = new Node();
				node.setName("start");
				node.setType("start");
				//node节点所属流程定义
				node.setWorkFlow(workFlow);
				super.insert(node);
				nodeMap.put(ParserUtil.getAttributeValue(mxCell, "id"), node);
				continue;
			}
			if("end".equalsIgnoreCase(ParserUtil.getAttributeValue(mxCell, "value"))&&"shape=ellipse".equalsIgnoreCase(ParserUtil.getAttributeValue(mxCell, "style"))){
				Node node = new Node();
				node.setName("end");
				node.setType("end");
				//node节点所属流程定义
				node.setWorkFlow(workFlow);
				super.insert(node);
				nodeMap.put(ParserUtil.getAttributeValue(mxCell, "id"), node);
				continue;
			}
			if("fork".equalsIgnoreCase(ParserUtil.getAttributeValue(mxCell, "value"))){
				Node node = new Node();
				node.setName("fork");
				node.setType("fork");
				//node节点所属流程定义
				node.setWorkFlow(workFlow);
				super.insert(node);
				nodeMap.put(ParserUtil.getAttributeValue(mxCell, "id"), node);
				continue;
			}
			if("join".equalsIgnoreCase(ParserUtil.getAttributeValue(mxCell, "value"))){
				Node node = new Node();
				node.setName("join");
				node.setType("join");
				//node节点所属流程定义
				node.setWorkFlow(workFlow);
				super.insert(node);
				nodeMap.put(ParserUtil.getAttributeValue(mxCell, "id"), node);
				continue;
			}
			if("decision".equalsIgnoreCase(ParserUtil.getAttributeValue(mxCell, "value"))){
				Node node = new Node();
				node.setName("decision");
				node.setType("decision");
				//node节点所属流程定义
				node.setWorkFlow(workFlow);
				super.insert(node);
				String cellId = ParserUtil.getAttributeValue(mxCell, "id");
				nodeMap.put(cellId, node);
				if(actionMap.containsKey(cellId)){
					Action action = new Action();
					action.setName("decisionAction " + cellId);
					//action的类名，，为实现特定接口的类的名称
					action.setClassName((String)actionMap.get(cellId));
					action.setNode(node);
					super.insert(action);
				}
				continue;
			}
			if("shape=rounded".equalsIgnoreCase(ParserUtil.getAttributeValue(mxCell, "style"))){
				Node node = new Node();
				String value = ParserUtil.getAttributeValue(mxCell, "value");
				if(value.indexOf("{owner:")!=-1){
					value = value.substring(0, value.indexOf("{owner:"));	
				};
				node.setName(value);
				node.setType("task");
				//node节点所属流程定义
				node.setWorkFlow(workFlow);
				super.insert(node);
				String cellId = ParserUtil.getAttributeValue(mxCell, "id");
				nodeMap.put(cellId, node);
				if(actionMap.containsKey(cellId)){
					Action action = new Action();
					action.setName("itemAction " + cellId);
					//action的类名，，为实现特定接口的类的名称
					action.setClassName((String)actionMap.get(cellId));
					action.setNode(node);
					super.insert(action);
				}
				WorkItem workItem = new WorkItem();
				workItem.setName(value);
				if(ownerMap.containsKey(cellId)){
					//workitem的拥有者标识
					workItem.setOwner((String)ownerMap.get(cellId));
				}
				workItem.setNode(node);
				super.insert(workItem);
				continue;
			}
			if(ParserUtil.getAttributeValue(mxCell, "source")!=null&&ParserUtil.getAttributeValue(mxCell, "target")!=null){
				transitionList.add(mxCell);
				continue;
			}
		}
		Iterator transitionIterator = transitionList.iterator();
		while(transitionIterator.hasNext()){
			Element transitionElement =  (Element)transitionIterator.next();
			String sourceId = ParserUtil.getAttributeValue(transitionElement, "source");
			//开始节点
			Node fromNode = (Node)nodeMap.get(sourceId);
			String targetId = ParserUtil.getAttributeValue(transitionElement, "target");
			Node toNode = (Node)nodeMap.get(targetId);
			Transition transition = new Transition();
			transition.setName(ParserUtil.getAttributeValue(transitionElement, "value"));
			//路径的开始节点
			transition.setNode(fromNode);
			transition.setEndNode(toNode);
			super.insert(transition);
		}
	}
	/**
	 * 获取最新的流程定义并生成定义的字符串
	 * @param name  流程定义的名称
	 * @param result 结果字符串
	 * @since Oct 26, 2010
	 * @author xjt
	 * @version 1.00  Oct 26, 2010
	 * */
	public String getWorkFlowDefinition(String name)throws Exception{
		StringBuffer result = new StringBuffer();
		result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		result.append("\r\n");
		WorkFlow workFlow = this.findLastWorkFlow(name);
		result.append("<workflow name=\"").append(workFlow.getName()).append("\">");
		result.append("\r\n");
		//迭代node，获取node类型
		StringBuffer strBuf = new StringBuffer();
		
		strBuf.append("FROM com.pujin.workflow.def.Node node WHERE node.workFlow.id = ")
				.append(workFlow.getId());
		List nodeList = super.getSession().createQuery(strBuf.toString()).list();
		Iterator nodeIterator = nodeList.iterator();
		while(nodeIterator.hasNext()){
			Node node = (Node)nodeIterator.next();
			String nodeType = node.getType();
			if("start".equalsIgnoreCase(nodeType)){
				result.append("<node name=\"").append(node.getName()).append("\"  type=\"start\">");
				result.append("\r\n");
				strBuf.setLength(0);
				strBuf.append("FROM  com.pujin.workflow.def.Transition  transition WHERE transition.node.id = ")
						.append(node.getId());
				List tranList = super.getSession().createQuery(strBuf.toString()).list();
				Iterator tranIterator = tranList.iterator();
				while(tranIterator.hasNext()){
					Transition transition = (Transition)tranIterator.next();
					result.append("<transition  name=\"").append(transition.getName()).append("\"  to=\"")
							.append(transition.getEndNode().getName()).append("\">").append("</transition>");
					result.append("\r\n");
				}
				result.append("</node>");
				result.append("\r\n");
			}
			if("task".equalsIgnoreCase(nodeType)){
				result.append("<node name=\"").append(node.getName()).append("\"  type=\"task\">");
				result.append("\r\n");
				strBuf.setLength(0);
				strBuf.append("FROM  com.pujin.workflow.def.Action  action WHERE action.node.id=")
						.append(node.getId());
				List actionList = super.getSession().createQuery(strBuf.toString()).list();
				if(actionList.size()>0){
					Action action = (Action)actionList.get(0);
					result.append("<action name=\"").append(action.getName()).append("\"")
							.append("  class=\"").append(action.getClassName()).append("\"/>");
					result.append("\r\n");
				}
				strBuf.setLength(0);
				strBuf.append("FROM com.pujin.workflow.def.WorkItem workItem WHERE workItem.node.id = ")
						.append(node.getId());
				WorkItem workItem = (WorkItem)super.getSession().createQuery(strBuf.toString()).uniqueResult();
				result.append("<workitem name=\"").append(workItem.getName()).append("\"  owner=\"")
						.append(workItem.getOwner()).append("\">").append("</workitem>");
				result.append("\r\n");
				strBuf.setLength(0);
				strBuf.append("FROM  com.pujin.workflow.def.Transition  transition WHERE transition.node.id = ")
						.append(node.getId());
				List tranList = super.getSession().createQuery(strBuf.toString()).list();
				Iterator tranIterator = tranList.iterator();
				while(tranIterator.hasNext()){
					Transition transition = (Transition)tranIterator.next();
					result.append("<transition  name=\"").append(transition.getName()).append("\"  to=\"")
							.append(transition.getEndNode().getName()).append("\">").append("</transition>");
					result.append("\r\n");
				}
				result.append("</node>");
				result.append("\r\n");
			}
			if("fork".equalsIgnoreCase(nodeType)){
				result.append("<node name=\"").append(node.getName()).append("\"  type=\"fork\">");
				result.append("\r\n");
				strBuf.setLength(0);
				strBuf.append("FROM  com.pujin.workflow.def.Transition  transition WHERE transition.node.id = ")
						.append(node.getId());
				List tranList = super.getSession().createQuery(strBuf.toString()).list();
				Iterator tranIterator = tranList.iterator();
				while(tranIterator.hasNext()){
					Transition transition = (Transition)tranIterator.next();
					result.append("<transition  name=\"").append(transition.getName()).append("\"  to=\"")
							.append(transition.getEndNode().getName()).append("\">").append("</transition>");
					result.append("\r\n");
				}
				result.append("</node>");
				result.append("\r\n");
			}
			if("join".equalsIgnoreCase(nodeType)){
				result.append("<node name=\"").append(node.getName()).append("\"  type=\"join\">");
				result.append("\r\n");
				strBuf.setLength(0);
				strBuf.append("FROM  com.pujin.workflow.def.Transition  transition WHERE transition.node.id = ")
						.append(node.getId());
				Transition transition = (Transition)super.getSession().createQuery(strBuf.toString()).uniqueResult();
				result.append("<transition  name=\"").append(transition.getName()).append("\"  to=\"")
						.append(transition.getEndNode().getName()).append("\">").append("</transition>");
				result.append("\r\n");
				result.append("</node>");
				result.append("\r\n");
			}
			if("decision".equalsIgnoreCase(nodeType)){
				result.append("<node name=\"").append(node.getName()).append("\"  type=\"decision\">");
				result.append("\r\n");
				strBuf.setLength(0);
				strBuf.append("FROM  com.pujin.workflow.def.Action  action WHERE action.node.id=")
						.append(node.getId());
				Action action = (Action)super.getSession().createQuery(strBuf.toString()).uniqueResult();
				result.append("<action name=\"").append(action.getName()).append("\"")
						.append("  class=\"").append(action.getClassName()).append("\"/>");
				result.append("\r\n");
				strBuf.setLength(0);
				strBuf.append("FROM  com.pujin.workflow.def.Transition  transition WHERE transition.node.id = ")
						.append(node.getId());
				List tranList = super.getSession().createQuery(strBuf.toString()).list();
				Iterator tranIterator = tranList.iterator();
				while(tranIterator.hasNext()){
					Transition transition = (Transition)tranIterator.next();
					result.append("<transition  name=\"").append(transition.getName()).append("\"  to=\"")
							.append(transition.getEndNode().getName()).append("\">").append("</transition>");
					result.append("\r\n");
				}
				result.append("</node>");
				result.append("\r\n");
			}
			if("end".equalsIgnoreCase(nodeType)){
				result.append("<node name=\"end\" type=\"end\"></node>");
				result.append("\r\n");
			}
		}
		result.append("</workflow>");
		return result.toString();
	}
		
}
