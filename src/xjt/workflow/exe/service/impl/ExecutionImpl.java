package xjt.workflow.exe.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.SQLQuery;

import xjt.workflow.dao.hibernate.HibernateDAO;
import xjt.workflow.def.Action;
import xjt.workflow.def.Node;
import xjt.workflow.def.Transition;
import xjt.workflow.def.WorkFlow;
import xjt.workflow.def.WorkItem;
import xjt.workflow.def.service.DefinitionService;
import xjt.workflow.exe.ActionHandler;
import xjt.workflow.exe.DecisionHandler;
import xjt.workflow.exe.ExecutionContext;
import xjt.workflow.exe.Job;
import xjt.workflow.exe.OverTimeHandler;
import xjt.workflow.exe.Token;
import xjt.workflow.exe.Variable;
import xjt.workflow.exe.WorkFlowInstance;
import xjt.workflow.exe.WorkItemInstance;
import xjt.workflow.exe.WorkItemOwner;
import xjt.workflow.exe.service.ExecutionService;
import xjt.workflow.page.Pager;
import xjt.workflow.util.Tools;

/**
 * 执行流程的服务实现类
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class  ExecutionImpl extends HibernateDAO implements ExecutionService {
	/**
	 * 流程定义服务
	 * */
	public DefinitionService definitionService;
	/**
	 * 流程定义服务，由Spring注入
	 * @parma definitionService 流程定义服务
	 * @return
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}
	/**
	 * 创建一个最新的流程定义的工作实例，启动工作流程
	 * @param flowName 流程名
	 * @parma globalVars 流程的全局变量
	 * @return workProcess 流程实例
	 * @since Aug 18, 2010
	 * @author xjt
	 * @version 1.00 Aug 18,2010
	 * @throws Exception 
	 * */
	public WorkFlowInstance createWorkFlowInstance(String flowName,Map globalVars) throws Exception{
		//查找最新流程定义
		WorkFlow workFlow = this.definitionService.findLastWorkFlow(flowName);
		//创建一个新的工作流实例
		WorkFlowInstance workFlowInstance = new WorkFlowInstance();
		//工作流实例开始时间
		workFlowInstance.setStartTime(new Date());
		//所属流程定义
		workFlowInstance.setWorkFlow(workFlow);
		super.insert(workFlowInstance);
		//保存流程的全局变量到变量表中
		this.saveVariable(globalVars, workFlowInstance);
		//查找开始节点
		Node startNode = this.getStartNode(workFlow);
		//查找开始节点的路径
		List nextList = this.getTransitionByNode(startNode);
		//开始节点之后只有一条路径
		if(nextList.size()>0){
			Transition transition = (Transition)nextList.get(0);
			//路径的目的节点
			Node endNode = transition.getEndNode();
			//目的节点为task类型，这是最简单的情况
			this.executeToTask(workFlowInstance, endNode);
		}
		return workFlowInstance;
	}
	/**
	 * 获取第一个工作项实例
	 * @param itemName 工作项名称
	 * @param workFlowInstance  工作流实例
	 * @return workItemInstance 工作项实例
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public WorkItemInstance getFirstWorkItemInstance(String itemName,WorkFlowInstance workFlowInstance){
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("FROM  com.pujin.workflow.exe.WorkItemInstance  workItemInstance  WHERE ")
				.append(" workItemInstance.name='").append(itemName).append(
						"' AND  workItemInstance.endTime = null  AND  workItemInstance.workFlowInstance.id= ").append(workFlowInstance.getId());
		//第一个工作项实例
		WorkItemInstance workItemInstance = (WorkItemInstance)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		return workItemInstance;
	}
	/**
	 * 跳转下一个工作项
	 * @param workItemInstance 当前工作项实例
	 * @param transition 路径名
	 * @return 
	 * @since Aug 18, 2010
	 * @author xjt
	 * @version 1.00 Aug 18,2010
	 * @throws CreateException 
	 * */
	public void createNextWorkItemInstance(WorkItemInstance workItemInstance,String transition) throws Exception{
		//结束当前的工作项实例
		workItemInstance.setEndTime(new Date());
		super.getSession().saveOrUpdate(workItemInstance);
		//当前工作项实例所在的节点
		Node node = this.getNodeByWorkItemInstance(workItemInstance);
		//当前流程实例
		WorkFlowInstance workFlowInstance = workItemInstance.getWorkFlowInstance();
		//下一流程路径
		Transition tran = this.getTransitionByNodeAndName(node, transition);
		//下一节点
		Node endNode = tran.getEndNode();
		//下一节点类型
		String nodeType = endNode.getType();
		//下一节点为一任务节点
		if("task".equalsIgnoreCase(nodeType)){
			this.executeToTask(workFlowInstance, endNode);
		}
		//下一节点为分支节点
		else if("fork".equalsIgnoreCase(nodeType)){
			this.executeToFork(workItemInstance, endNode);
		}
		//下一节点为汇合节点
		else if("join".equalsIgnoreCase(nodeType)){
			this.executeToJoin(workFlowInstance, workItemInstance, endNode);
		//下一节点为判断节点
		}else if("decision".equalsIgnoreCase(nodeType)){
			this.executeToDecision(workFlowInstance, endNode);
		//下一节点为结束节点
		}else if("end".equalsIgnoreCase(nodeType)){
			this.executeEndWorkFlowInstance(workItemInstance);
		}
	}
	/**
	 * 分配工作项拥有者
	 * @param workFlowInstance 工作流实例
	 * @param workItem 工作项
	 * @param workItemInstance 工作项实例
	 * @return
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * @throws CreateException 
	 * */
	public void createWorkItemOwner(WorkFlowInstance workFlowInstance,WorkItem workItem,WorkItemInstance workItemInstance) throws Exception{
		//从流程变量表看是否能查找到工作项拥有者的信息
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("FROM com.pujin.workflow.exe.Variable variable WHERE variable.workFlowInstance.id = ")
			   .append(workFlowInstance.getId()).append("  AND variable.varName = '").append(workItem.getOwner()).append("'");
		List ownerList = super.getSession().createQuery(strBuf.toString()).list();
		if(ownerList.size()>0){
			Variable variable = (Variable)ownerList.get(0);
			//获取第一个工作项实例的拥有者，其形式如"2,4,8"
			String value = variable.getVarValue();
			String [] ids = value.split(",");
			for(int f = 0; f < ids.length; f++){
				Long id = new Long(ids[f]);
				//工作项实例与拥有者关系表
				WorkItemOwner workItemOwner = new WorkItemOwner();
				workItemOwner.setOwnerId(id);
				workItemOwner.setItemInstanceId(workItemInstance.getId());
				//工作项分配给此拥有者，但还未被执行
				workItemOwner.setDone(Byte.valueOf("0"));
				super.insert(workItemOwner);
			}
		}
	}
	/**
	 * 保存全局流程变量
	 * @param globalVars 全局变量Map
	 * @param workFlowInstance 流程实例
	 * @return 
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * @throws CreateException 
	 * */
	public void saveVariable(Map globalVars,WorkFlowInstance workFlowInstance) throws Exception{
		if(globalVars!=null){
			//变量键集合
			Set keySet = globalVars.keySet();
			Iterator it = keySet.iterator();
			while (it.hasNext()) {
				//变量键
				String key = (String) it.next();
				//键值
				String value = (String) globalVars.get(key);
				//流程全局变量实例
				Variable variable = new Variable();
				//所属工作流程实例
				variable.setWorkFlowInstance(workFlowInstance);
				variable.setVarName(key);
				variable.setVarValue(value);
				//保存变量
				super.insert(variable);
			}
		}
	}
	/**
	 * 保存全局流程变量
	 * @param name  变量名
	 * @param value  变量值
	 * @param workFlowInstance 流程实例
	 * @return 
	 * @since Sep 7, 2010
	 * @author xjt
	 * @version 1.00 Sep 7, 2010
	 * @throws CreateException 
	 * */
	public void saveVariable(String name,String value,WorkFlowInstance workFlowInstance) throws Exception{
		//流程全局变量实例
		Variable variable = new Variable();
		variable.setWorkFlowInstance(workFlowInstance);
		variable.setVarName(name);
		variable.setVarValue(value);
		//保存变量实例
		super.insert(variable);
	}
	/**
	 * 保存局部流程变量
	 * @param name  变量名
	 * @param value 变量值
	 * @param workItemInstance  工作项实例
	 * @return 
	 * @since Sep 21, 2010
	 * @author xjt
	 * @version 1.00 Sep 21, 2010
	 * @throws CreateException 
	 * */
	public void saveVariable(String name,String value,WorkItemInstance workItemInstance) throws Exception{
		//流程局部变量实例
		Variable variable = new Variable();
		variable.setVarName(name);
		variable.setVarValue(value);
		variable.setWorkItemInstance(workItemInstance);
		variable.setWorkFlowInstance(workItemInstance.getWorkFlowInstance());
		super.insert(variable);
	}
	/**
	 * 更新流程变量,更新前确定已有此流程变量
	 * @param  name  变量名
	 * @param  value  更新的变量值
	 * @return 
	 * @since Sep 7, 2010
	 * @author xjt
	 * @version 1.00 Sep 7,2010
	 * */
	public void updateVariable(String name,String value,WorkItemInstance workItemInstance){
		//工作流程实例
		WorkFlowInstance workFlowInstance = workItemInstance.getWorkFlowInstance();
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("FROM  com.pujin.workflow.exe.Variable  variable  WHERE  variable.workFlowInstance.id = ")
				.append(workFlowInstance.getId()).append("  AND variable.varName = '").append(name).append("'");
		Variable variable = (Variable)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		variable.setVarValue(value);
		super.getSession().saveOrUpdate(variable);
	}
	/**
	 * 获取流程变量
	 * @param  name  变量名
	 * @param  workItemInstance 工作项实例
	 * @return  value  变量值
	 * @since Sep 7, 2010
	 * @author xjt
	 * @version 1.00 Sep 7, 2010
	 * */
	public String getVariable(String name,WorkItemInstance workItemInstance){
		WorkFlowInstance workFlowInstance = workItemInstance.getWorkFlowInstance();
		StringBuffer strBuf = new StringBuffer();
		//获取流程的全局变量
		strBuf.append("FROM  com.pujin.workflow.exe.Variable  variable  WHERE  variable.workFlowInstance.id = ")
				.append(workFlowInstance.getId()).append("  AND variable.varName = '").append(name).append("'");
		Variable variable = (Variable)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		return variable.getVarValue();
	}
	/**
	 * 获取局部流程变量
	 * @param  name  变量名
	 * @param  workItemInstance 工作项实例
	 * @return  value  变量值
	 * @since Oct 8, 2010
	 * @author xjt
	 * @version 1.00 Oct 8, 2010
	 * */
	public String getVariableByWorkItemInstance(String name,WorkItemInstance workItemInstance){
		WorkFlowInstance workFlowInstance = workItemInstance.getWorkFlowInstance();
		StringBuffer strBuf = new StringBuffer();
		//获取流程的局部变量
		strBuf.append("FROM  com.pujin.workflow.exe.Variable  variable  WHERE  variable.workFlowInstance.id = ")
				.append(workFlowInstance.getId())
				.append("  AND variable.workItemInstance.id = ")
				.append(workItemInstance.getId())
				.append("  AND variable.varName = '").append(name).append("'");
		Variable variable = (Variable)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		return variable.getVarValue();
	}
	/**
	 * 通过工作项实例ID获取工作项实例
	 * @param itemId   工作项实例ID
	 * @return workItem 工作项
	 * @since Sep 7, 2010
	 * @author xjt
	 * @version 1.00 Sep 7, 2010
	 */
	public WorkItemInstance getWorkItemInstanceById(Long itemId){
		WorkItemInstance workItemInstance = (WorkItemInstance)super
							.getObject(xjt.workflow.exe.WorkItemInstance.class, itemId);
		return workItemInstance;
	}
	/**
	 * 获取定义的开始节点
	 * @param  workFlow 流程定义
	 * @return startNode 开始节点
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public Node getStartNode(WorkFlow workFlow){
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("FROM com.pujin.workflow.def.Node node WHERE  node.workFlow.id = ").append(workFlow.getId())
				.append(" AND node.type = '").append("start").append("'");
		//开始节点
		Node startNode = (Node)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		return startNode;
	}
	/**
	 * 获取节点的输出路径
	 * @param node 节点
	 * @return nextList 输出路径列表
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public List getTransitionByNode(Node node){
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("FROM com.pujin.workflow.def.Transition transition  WHERE  transition.node.id = ")
				.append(node.getId());
		//节点的输出路径列表
		List nextList = super.getSession().createQuery(strBuf.toString()).list();
		return nextList;
	}
	/**
	 * 获取节点指定名字的输出路径
	 * @paran node 节点
	 * @param transitionName 路径名
	 * @return transition 路径
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public Transition getTransitionByNodeAndName(Node node,String transitionName){
		StringBuffer strBuf = new StringBuffer();
		//通过节点定义和路径名查找路径
		strBuf.append("FROM com.pujin.workflow.def.Transition  transition WHERE transition.node.id = ")
				.append(node.getId()).append("  AND transition.name = '").append(transitionName).append("'");
		Transition transition = (Transition)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		return transition;
	}
	/**
	 * 获取工作项
	 * @param node 工作项所在节点
	 * @return workItem 节点所在的工作项
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public WorkItem getWorkItemByNode(Node node){
		StringBuffer strBuf = new StringBuffer();
		//查找任务节点拥有的工作项，且只多只能拥有一个工作项
		strBuf.append("FROM com.pujin.workflow.def.WorkItem workItem  WHERE workItem.node.id = ").append(node.getId());
		//目的节点对的工作项
		WorkItem workItem = (WorkItem)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		return workItem;
	}
	/**
	 * 通过工作项实例查找包含此工作项定义的节点
	 * @param workItemInstance 工作项所在节点
	 * @return workItem 节点所在的工作项
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public Node getNodeByWorkItemInstance(WorkItemInstance workItemInstance){
		//工作项定义
		WorkItem workItem = (WorkItem)super.getObject(xjt.workflow.def.WorkItem.class,
														workItemInstance.getWorkItem().getId());
		//包含此工作项定义的节点
		Node node = (Node)super.getObject(xjt.workflow.def.Node.class, workItem.getNode().getId());
		return node;
	}
	/**
	 * 获取兄弟令牌
	 * @param token 令牌
	 * @return brotherTokens 兄弟令牌
	 * @since Sep 2,2010
	 * @author xjt
	 * @version 1.00  Sep 2, 2010
	 * */
	public List getBrotherTokens(Token token){
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("FROM com.pujin.workflow.exe.Token token WHERE token.parentToken.id = ")
				.append(token.getParentToken().getId()).append("  AND token.id != ").append(token.getId());
		//兄弟令牌列表
		List brotherTokens = super.getSession().createQuery(strBuf.toString()).list();
		return brotherTokens;
	}
	/**
	 * 获取节点包含的动作
	 * @param actionNode 节点
	 * @return action 动作
	 * @since Sep 2,2010
	 * @author xjt
	 * @version 1.00  Sep 2, 2010
	 * */
	public Action getActionByNode(Node actionNode){
		StringBuffer strBuf = new StringBuffer();
		//通过节点查找此节点包含的动作定义
		strBuf.append("FROM  com.pujin.workflow.def.Action action  WHERE action.node.id = ")
				.append(actionNode.getId());
		//最多只有一个动作节点
		Action action = (Action)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		return action;
	}
	/**
	 * 执行动作
	 * @param className 类名
	 * @param executionContext 执行上下文
	 * @return trnasiotnName 路径名
	 * @since Sep 2,2010
	 * @author xjt
	 * @version 1.00  Sep 2, 2010
	 * */
	public String executeDecisionAction(ExecutionContext executionContext,String className){
		String transition = null;
		try {
			//判断节点的动作
			DecisionHandler decisionHandler = (DecisionHandler)Class.forName(className).newInstance();
			//执行动作，返回下一执行路径
			transition = decisionHandler.execute(executionContext);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return transition;
	}
	/**
	 * 获取判断节点动作执行后的路径
	 * @param decisionNode 决定节点
	 * @param transitionName 路径名
	 * @return transition 执行路径
	 * @since Sep 2,2010
	 * @author xjt
	 * @version 1.00  Sep 2, 2010
	 * */
	public Transition getDecisionTransition(Node decisionNode,String transitionName){
		StringBuffer strBuf = new StringBuffer();
		//通过判断节点和路径名，查找路径
		strBuf.append("FROM  com.pujin.workflow.def.Transition  transition  WHERE  transition.name = '")
				.append(transitionName).append("' AND transition.node.id = ").append(decisionNode.getId());
		Transition transition = (Transition)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		return transition;
	}
	/**
	 * 执行到分支节点的动作
	 * @param workFlowInstance 工作流实例
	 * @param forkNode 分支节点
	 * @return 
	 * @since Sep 3,2010
	 * @author xjt
	 * @version 1.00  Sep 3, 2010
	 * @throws CreateException 
	 * */
	public void executeToFork(WorkItemInstance workItemInstance,Node forkNode) throws Exception {
		//执行上下文
		ExecutionContext executionContext = new ExecutionContext(workItemInstance.getWorkFlowInstance(),super.getSession());
		//分支路径
		List tranList = this.getTransitionByNode(forkNode);
		Iterator tranIt = tranList.iterator();
		//上一个工作项实例持有的令牌
		Token forkToken = workItemInstance.getToken();
		//工作流实例
		WorkFlowInstance workFlowInstance = workItemInstance.getWorkFlowInstance();
		while(tranIt.hasNext()){
			Transition  forkTransition = (Transition)tranIt.next();
			//分支只能接任务节点
			Node forkNextNode = forkTransition.getEndNode();
			//任务节点对应的工作项
			WorkItem forkNextWorkItem = this.getWorkItemByNode(forkNextNode);
			//如果此任务节点包含自定义的动作，则执行此动作。
			this.executeActionHandler(executionContext,forkNextNode);
			//创建工作项实例
			WorkItemInstance forkNextWorkItemInstance = new WorkItemInstance();
			//工作项实例名称与其对应的工作项定义名称相同
			forkNextWorkItemInstance.setName(forkNextWorkItem.getName());
			//创建在分支工作项实例上的令牌
			Token forkNextToken = new Token();
			//设置分支子令牌的父母令牌
			forkNextToken.setParentToken(forkToken);
			super.insert(forkNextToken);
			//设置分支工作项实例的令牌
			forkNextWorkItemInstance.setToken(forkNextToken);
			forkNextWorkItemInstance.setCreateTime(new Date());
			//分支工作项实例对应的工作项定义
			forkNextWorkItemInstance.setWorkItem(forkNextWorkItem);
			//分支工作项实例所属的工作流程实例
			forkNextWorkItemInstance.setWorkFlowInstance(workFlowInstance);
			super.insert(forkNextWorkItemInstance);
			//分支令牌持有的工作项实例
			forkNextToken.setWorkItemInstance(forkNextWorkItemInstance);
			super.getSession().saveOrUpdate(forkNextToken);
			//从流程变量表看是否能查找到工作项拥有者的信息，有则建立工作项实例与拥有者的关系
			this.createWorkItemOwner(workFlowInstance, forkNextWorkItem, forkNextWorkItemInstance);
		}
		super.getSession().saveOrUpdate(workFlowInstance);
	}
	/**
	 * 执行到判断节点的动作
	 * @param workFlowInstance 工作流实例
	 * @param decisionNode 判断节点
	 * @return 
	 * @since Sep 3, 2010
	 * @author xjt
	 * @version 1.00  Sep 3, 2010
	 * @throws CreateException 
	 * */
	public void executeToDecision(WorkFlowInstance workFlowInstance,Node decisionNode) throws Exception{
		//执行上下文
		ExecutionContext executionContext = new ExecutionContext(workFlowInstance,super.getSession());
		//判断节点的动作
		Action action = this.getActionByNode(decisionNode);
		//实现动作接口的类名称
		String className = action.getClassName();
		String transitionName = this.executeDecisionAction(executionContext,className);
		//路径实例
		Transition decisionTransition = this.getDecisionTransition(decisionNode,transitionName);
		//目的节点
		Node decisionEndNode = decisionTransition.getEndNode();
		if("task".equals(decisionEndNode.getType())){
			//创建一个位于任务节点的令牌
			Token decisionEndToken = new Token();
			super.insert(decisionEndToken);
			//判断节点之后任务节点的工作项定义
			WorkItem decisionEndWorkItem = this.getWorkItemByNode(decisionEndNode);
			//如果此任务节点包含自定义的动作，则执行此动作。
			this.executeActionHandler(executionContext,decisionEndNode);
			//创建相应的工作项实例
			WorkItemInstance decisionEndWorkItemInstance = new WorkItemInstance();
			decisionEndWorkItemInstance.setWorkItem(decisionEndWorkItem);
			decisionEndWorkItemInstance.setWorkFlowInstance(workFlowInstance);
			decisionEndWorkItemInstance.setName(decisionEndWorkItem.getName());
			decisionEndWorkItemInstance.setCreateTime(new Date());
			decisionEndWorkItemInstance.setToken(decisionEndToken);
			super.insert(decisionEndWorkItemInstance);
			//设置令牌指向的工作项实例
			decisionEndToken.setWorkItemInstance(decisionEndWorkItemInstance);
			super.getSession().saveOrUpdate(decisionEndWorkItemInstance);
			//更新工作流实例的根令牌
			workFlowInstance.setRootToken(decisionEndToken);
			super.getSession().saveOrUpdate(workFlowInstance);
			//从流程变量表看是否能查找到工作项拥有者的信息，有则建立工作项实例与拥有者的关系
			this.createWorkItemOwner(workFlowInstance, decisionEndWorkItem, decisionEndWorkItemInstance);
		}else if("end".equals(decisionEndNode.getType())){
			//结束当前工作流实例
			workFlowInstance.setEndTime(new Date());
			super.getSession().saveOrUpdate(workFlowInstance);
		}
	}
	/**
	 * 执行到汇合节点的动作
	 * @param
	 * @return 
	 * @since Sep 3,2010
	 * @author xjt
	 * @version 1.00  Sep 3, 2010
	 * @throws CreateException 
	 * */
	public void executeToJoin(WorkFlowInstance workFlowInstance,WorkItemInstance workItemInstance,Node joinNode) throws Exception{
		//执行上下文
		ExecutionContext executionContext = new ExecutionContext(workFlowInstance,super.getSession());
		//当前工作项实例持有的令牌
		Token currentToken = workItemInstance.getToken();
		Token nextToken = null;
		if(currentToken.getParentToken()!=null){
			//当前工作项实例持有令牌的父母令牌
			nextToken = currentToken.getParentToken();
		}
		//判断所有的兄弟节点是否处于等待状态
		boolean isAllWaited = this.isAllBrotherTokenWaited(currentToken);
		//所有的兄弟节点都处于等待状态,表明流程可以继续往下走
		if(isAllWaited){
			//汇合节点后的路径
			Transition joinTransition = (Transition)this.getTransitionByNode(joinNode).get(0);
			//汇合节点的下一个节点
			Node joinEndNode = joinTransition.getEndNode();
			//对应的工作项定义
			WorkItem joinEndWorkItem = this.getWorkItemByNode(joinEndNode);
			//如果此任务节点包含自定义的动作，则执行此动作。
			this.executeActionHandler(executionContext,joinEndNode);
			//新建令牌,标示流程走向
			Token joinEndToken = new Token();
			super.insert(joinEndToken);
			//工作项定义对应的工作项实例
			WorkItemInstance joinEndWorkItemInstance = new WorkItemInstance();
			joinEndWorkItemInstance.setName(joinEndWorkItem.getName());
			joinEndWorkItemInstance.setCreateTime(new Date());
			joinEndWorkItemInstance.setWorkItem(joinEndWorkItem);
			joinEndWorkItemInstance.setWorkFlowInstance(workFlowInstance);
			//设置工作项实例持有的令牌
			if(nextToken!=null){
				//将当前工作项实例持有令牌的父母令牌转交给汇合节点的下一个工作项实例
				joinEndWorkItemInstance.setToken(nextToken);
			}else{
				joinEndWorkItemInstance.setToken(joinEndToken);
			}
			super.insert(joinEndWorkItemInstance);
			joinEndToken.setWorkItemInstance(joinEndWorkItemInstance);
			super.getSession().saveOrUpdate(joinEndToken);
			//将工作流程先前的根令牌置为不可用
			workFlowInstance.getRootToken().setIsDisable(Byte.valueOf("1"));
			workFlowInstance.setRootToken(joinEndToken);
			//更新工作流程实例的令牌位置
			super.getSession().saveOrUpdate(workFlowInstance);
			//从流程变量表看是否能查找到工作项拥有者的信息，有则建立工作项实例与拥有者的关系
			this.createWorkItemOwner(workFlowInstance, joinEndWorkItem, joinEndWorkItemInstance);
		}else{
			//非所有的兄弟令牌都处于等待状态，则将自身设为等待状态
			currentToken.setIsWaited(Byte.valueOf("1"));
			super.getSession().saveOrUpdate(currentToken);
		}
		
	}
	/**
	 * 执行到任务节点的动作
	 * @param
	 * @return 
	 * @since Sep 3,2010
	 * @author xjt
	 * @version 1.00  Sep 3, 2010
	 * @throws CreateException 
	 * */
	public void executeToTask(WorkFlowInstance workFlowInstance,Node taskNode) throws Exception{
		//执行上下文
		ExecutionContext executionContext = new ExecutionContext(workFlowInstance,super.getSession());
		WorkItem nextWorkItem = this.getWorkItemByNode(taskNode);
		//如果此任务节点包含自定义的动作，则执行此动作。
		this.executeActionHandler(executionContext,taskNode);
		//此工作项对应的实例
		WorkItemInstance nextWorkItemInstance = new WorkItemInstance();
		//创建一个指向下一个工作项实例的令牌
		Token nextToken = new Token();
		super.insert(nextToken);
		//此工作项所属工作流程实例
		nextWorkItemInstance.setWorkFlowInstance(workFlowInstance);
		//对应的工作项定义
		nextWorkItemInstance.setWorkItem(nextWorkItem);
		//创建时间
		nextWorkItemInstance.setCreateTime(new Date());
		//其名与对应的工作项名称相同
		nextWorkItemInstance.setName(nextWorkItem.getName());
		//设置其拥有的令牌
		nextWorkItemInstance.setToken(nextToken);
		super.insert(nextWorkItemInstance);
		//更新令牌指向的工作项实例
		nextToken.setWorkItemInstance(nextWorkItemInstance);
		super.getSession().saveOrUpdate(nextToken);
		//更新当前流程实例的根令牌
		workFlowInstance.setRootToken(nextToken);
		super.getSession().saveOrUpdate(workFlowInstance);
		//从流程变量表看是否能查找到工作项拥有者的信息，有则建立工作项实例与拥有者的关系
		this.createWorkItemOwner(workFlowInstance, nextWorkItem, nextWorkItemInstance);
	}
	/**
	 * 其他所有的兄弟令牌是否处于等待状态
	 * @param Token currentToken 当前工作项实例持有的令牌
	 * @return boolean isAllWaited 
	 * @since Sep 3,2010
	 * @author xjt
	 * @version 1.00  Sep 3, 2010
	 * */
	public boolean isAllBrotherTokenWaited(Token currentToken){
		//兄弟令牌列表
		List brotherTokens = this.getBrotherTokens(currentToken);
		Iterator tokenIt = brotherTokens.iterator();
		//其他兄弟令牌是否处于等待状态，用于分支汇合时流程走向的判断
		boolean isAllWaited = true;
		while(tokenIt.hasNext()){
			Token brotherToken = (Token)tokenIt.next();
			if(brotherToken.getIsWaited().intValue()==0){
				isAllWaited = false;
				break;
			}
		}
		return isAllWaited;
	}
	/**
	 * 执行任务节点包含的动作，一个任务节点最多一个动作，且
	 * 此动作在工作项的创建前执行
	 * @param  executionContext 执行上下文，包含当前流程的信息
	 * @param taskNode 任务节点
	 * @return 
	 * @since Sep 6,2010
	 * @author xjt
	 * @version 1.00  Sep 6, 2010
	 * */
	public void executeActionHandler(ExecutionContext executionContext,Node taskNode){
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("FROM  com.pujin.workflow.def.Action  action WHERE action.node.id = ")
				.append(taskNode.getId());
		List lst = super.getSession().createQuery(strBuf.toString()).list();
		if(lst.size()>0){
			//任务节点的动作,此动作实现特定接口
			Action action = (Action)lst.get(0);
			try {
				ActionHandler actionHandler = (ActionHandler)Class.forName(action.getName()).newInstance();
				actionHandler.execute(executionContext);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 分页获取工作项实例
	 * @param itemName   工作项名
	 * @param owner   工作项实例的所有者
	 * @param pageSize   每页数
	 * @param startIndex    开始索引值
	 * @return finder 工作项分页器
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 */
	public Pager getPageWorkItemInstances(String itemName, String owner,
			int pageSize, int startIndex){
		StringBuffer hql = new StringBuffer();
		// 查询未悬置、还未处理、属于指定人的任务实例
		hql.append("SELECT wi.id FROM PJ_WorkItemInstance wi LEFT JOIN PJ_WorkItem_Owner wo ON wi.id = wo.itemInstanceId ")
				.append(" WHERE wo.done = 0 AND wi.isSuspend = 0 AND wi.isCallBack = 0 AND wi.endTime is null AND wi.name = '")
				.append(itemName)
				.append("' AND wo.ownerId = ").append(new Long(owner));
		// 分页查询
		SQLQuery query = super.getSession().createSQLQuery(hql.toString());
		int totalCount = query.list().size();
		query.setFirstResult(startIndex);
		query.setMaxResults(pageSize);
		List<?> list = query.list();
		List items = new ArrayList();
		Iterator it = list.iterator();
		while(it.hasNext()){
			Object obj =  it.next();
			WorkItemInstance workItemInstance = this.getWorkItemInstanceById(new Long(obj.toString()));
			items.add(workItemInstance);
		}
		Pager pager = new Pager(items, totalCount, pageSize,
					startIndex);
		return pager;
	}
	/**
	 * 获取工作项实例列表
	 * @param itemName  工作项名
	 * @param owner   工作项的所有者
	 * @return lst 工作项实例列表
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 */
	public List getWorkItemInstanceList(String itemName, String owner){
		StringBuffer hql = new StringBuffer();
		// 查询未悬置、还未处理、属于指定人的任务实例
		hql.append("SELECT wi.id FROM PJ_WorkItemInstance wi LEFT JOIN PJ_WorkItem_Owner wo ON wi.id = wo.itemInstanceId ")
			.append(" WHERE wo.done = 0 AND wi.isSuspend = 0 AND wi.isCallBack = 0 AND wi.endTime is null AND wi.name = '")
			.append(itemName)
			.append("' AND wo.ownerId = ").append(new Long(owner));
		Query query = super.getSession().createSQLQuery(hql.toString());
		List list = query.list();
		List items = new ArrayList();
		Iterator it = list.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			WorkItemInstance workItemInstance = this.getWorkItemInstanceById(new Long(obj.toString()));
			items.add(workItemInstance);
		}
		return items;
	}
	/**
	 * 签收工作项实例
	 * @param itemInstanceId   工作项实例ID
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 */
	public void executeWorkItemInstance(Long itemInstanceId){
		WorkItemInstance workItemInstance = (WorkItemInstance)super.getObject
												(xjt.workflow.exe.WorkItemInstance.class, itemInstanceId);
		//签收工作项实例，设置其开始时间
		workItemInstance.setStartTime(new Date());
		super.getSession().saveOrUpdate(workItemInstance);
	}
	/**
	 * 签收工作项实例
	 * @param workItemInstance   工作项实例
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 */
	public void executeWorkItemInstance(WorkItemInstance workItemInstance){
		//签收工作项实例，设置其开始时间
		workItemInstance.setStartTime(new Date());
		super.getSession().saveOrUpdate(workItemInstance);
	}
	/**
	 * 更新工作项实例的拥有者，即谁处理了这个工作项实例
	 * @param workItemInstance    工作项
	 * @param owner   工作项的拥有者
	 * @return
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 */
	public void updateItemOwner(WorkItemInstance workItemInstance, String owner){
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("FROM  com.pujin.workflow.exe.WorkItemOwner wo  WHERE wo.itemInstanceId = ")
				.append(workItemInstance.getId()).append("  AND wo.ownerId=").append(new Long(owner));
		WorkItemOwner workItemOwner = (WorkItemOwner)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		//设置已执行指定的工作项实例
		workItemOwner.setDone(Byte.valueOf("1"));
		super.getSession().saveOrUpdate(workItemOwner);
	}
	/**
	 * 获取工作项实例的拥有者，即谁处理了这个工作项实例
	 * @param  workItemInstance    工作项
	 * @return  owner   工作项的拥有者
	 * @return
	 * @since Sep 7, 2010
	 * @author xjt
	 * @version 1.00, Sep 7, 2010
	 */
	public String getWorkItemInstanceOwner(WorkItemInstance workItemInstance){
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("FROM  com.pujin.workflow.exe.WorkItemOwner wo  WHERE wo.itemInstanceId = ")
				.append(workItemInstance.getId()).append("  AND wo.done=1");
		WorkItemOwner workItemOwner = (WorkItemOwner)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		return workItemOwner.getOwnerId().toString();
	}
	/**
	 * 创建被回退的工作项实例
	 * @param workItemInstance  发起回退的工作项实例
	 * @param transition  回退的路径
	 * @return
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 * @throws CreateException 
	 * */
	public void createCallBackWorkItemInstance(WorkItemInstance workItemInstance,String transition) throws Exception{
		//执行上下文
		ExecutionContext executionContext = new ExecutionContext(workItemInstance.getWorkFlowInstance(),super.getSession());
		//结束当前的工作项实例
		workItemInstance.setEndTime(new Date());
		super.getSession().saveOrUpdate(workItemInstance);
		//当前工作项实例所在的节点
		Node node = this.getNodeByWorkItemInstance(workItemInstance);
		//当前流程实例
		WorkFlowInstance workFlowInstance = workItemInstance.getWorkFlowInstance();
		Transition tran = this.getTransitionByNodeAndName(node, transition);
		Node endNode = tran.getEndNode();
		WorkItem callBackWorkItem = this.getWorkItemByNode(endNode);
		//如果此任务节点包含自定义的动作，则执行此动作。
		this.executeActionHandler(executionContext,endNode);
		//此工作项对应的实例
		WorkItemInstance callBackWorkItemInstance = new WorkItemInstance();
		//创建一个指向下一个工作项实例的令牌
		Token nextToken = new Token();
		super.insert(nextToken);
		//此工作项所属工作流程实例
		callBackWorkItemInstance.setWorkFlowInstance(workFlowInstance);
		//对应的工作项定义
		callBackWorkItemInstance.setWorkItem(callBackWorkItem);
		//创建时间
		callBackWorkItemInstance.setCreateTime(new Date());
		//其名与对应的工作项名称相同
		callBackWorkItemInstance.setName(callBackWorkItem.getName());
		//设置其拥有的令牌
		callBackWorkItemInstance.setToken(nextToken);
		//设置此工作项实例是回退生成的
		callBackWorkItemInstance.setIsCallBack(Byte.valueOf("1"));
		super.insert(callBackWorkItemInstance);
		//更新令牌指向的工作项实例
		nextToken.setWorkItemInstance(callBackWorkItemInstance);
		super.getSession().saveOrUpdate(nextToken);
		//更新当前流程实例的根令牌
		workFlowInstance.setRootToken(nextToken);
		super.getSession().saveOrUpdate(workFlowInstance);
		//查找先前已处理的同名的工作项实例，并查找出谁执行了此工作项实例
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("FROM  com.pujin.workflow.exe.WorkItemInstance  workItemInstance  WHERE  workItemInstance.workFlowInstance.id = ")
				.append(workFlowInstance.getId()).append("  AND  workItemInstance.name = '").append(callBackWorkItem.getName()).append("'")
				.append("  AND workItemInstance.endTime != null");
		WorkItemInstance doneWorkItemInstance = (WorkItemInstance)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		strBuf.setLength(0);
		//查找先前已处理的同名的工作项实例的拥有者
		strBuf.append("FROM  com.pujin.workflow.exe.WorkItemOwner  workItemOwner  WHERE  workItemOwner.itemInstanceId = ")
				.append(doneWorkItemInstance.getId()).append("  AND  workItemOwner.done = 1");
		WorkItemOwner doneWorkItemOwner = (WorkItemOwner)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		WorkItemOwner callBackWorkItemOwner = new WorkItemOwner();
		callBackWorkItemOwner.setItemInstanceId(callBackWorkItemInstance.getId());
		//被回退任务的拥有者是先前处理该任务项的拥有者
		callBackWorkItemOwner.setOwnerId(doneWorkItemOwner.getOwnerId());
		callBackWorkItemOwner.setDone(Byte.valueOf("0"));
		super.insert(callBackWorkItemOwner);
	}
	/**
	 * 返回被回退的工作项实例列表
	 * @param itemName  工作项名
	 * @param owner   工作项的所有者
	 * @return list 被回退的工作项实例列表
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 * */
	public List getCallBackWorkItemInstanceList(String itemName,String owner){
		StringBuffer hql = new StringBuffer();
		// 查询被标置为被回退、未悬置、还未处理、属于指定人的任务实例
		hql.append("SELECT wi.id FROM PJ_WorkItemInstance wi LEFT JOIN PJ_WorkItem_Owner wo ON wi.id = wo.itemInstanceId ")
			.append(" WHERE wi.isCallBack = 1  AND wi.isSuspend = 0 AND wi.endTime is null AND wo.done = 0 AND wi.name = '")
			.append(itemName)
			.append("' AND wo.ownerId = ").append(new Long(owner));
		SQLQuery query = super.getSession().createSQLQuery(hql.toString());
		List list = query.list();
		List items = new ArrayList();
		Iterator it = list.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			WorkItemInstance workItemInstance = this.getWorkItemInstanceById(new Long(obj.toString()));
			items.add(workItemInstance);
		}
		return items;
	}
	/**
	 * 分页返回被回退的工作项实例列表
	 * @param itemName   工作项名
	 * @param owner   工作项实例的所有者
	 * @param pageSize   每页数
	 * @param startIndex    开始索引值
	 * @return finder 工作项分页器
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 */
	public Pager getPageCallBackWorkItemInstances(String itemName, String owner,
			int pageSize, int startIndex){
		StringBuffer hql = new StringBuffer();
		// 查询被标置为被回退、未悬置、还未处理、属于指定人的任务实例
		hql.append("SELECT wi.id FROM PJ_WorkItemInstance wi LEFT JOIN PJ_WorkItem_Owner wo ON wi.id = wo.itemInstanceId ")
			.append(" WHERE wi.isCallBack = 1  AND wi.isSuspend = 0 AND wi.endTime is null AND wo.done = 0 AND wi.name = '")
			.append(itemName)
			.append("' AND wo.ownerId = ").append(new Long(owner));
		SQLQuery query = super.getSession().createSQLQuery(hql.toString());
		int totalCount = query.list().size();
		query.setFirstResult(startIndex);
		query.setMaxResults(pageSize);
		List list = query.list();
		List items = new ArrayList();
		Iterator it = list.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			WorkItemInstance workItemInstance = this.getWorkItemInstanceById(new Long(obj.toString()));
			items.add(workItemInstance);
		}
		Pager pager = new Pager(items, totalCount, pageSize,
					startIndex);
		return pager;
	}
	/**
	 * 结束工作流程实例
	 * @param workItemInstance 当前工作项实例
	 * @return 
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 * */
	public void executeEndWorkFlowInstance(WorkItemInstance workItemInstance){
		//结束当前工作项实例
		workItemInstance.setEndTime(new Date());
		super.getSession().saveOrUpdate(workItemInstance);
		WorkFlowInstance workFlowInstance = workItemInstance.getWorkFlowInstance();
		//结束当前工作流实例
		workFlowInstance.setEndTime(new Date());
		super.getSession().saveOrUpdate(workFlowInstance);
	}
	/**
	 * 查询上一个工作项实例，即回退给当前工作项的工作项实例
	 * @param callBackWorkItemInstance  被回退的当前工作项实例
	 * @param itemNames  可能发起回退的上一工作项名称
	 * @return startCallBackWorkItemInstance 发起回退的工作项实例
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 */
	public WorkItemInstance getStartCallBackWorkItemInstance(WorkItemInstance callBackWorkItemInstance,
			String[] itemNames){
		StringBuffer thql = new StringBuffer();
		// 查询上一个任务实例，即回退给当前工作项实例的工作项实例
		//按其结束时间排序，取出最近结束的工作项实例
		if (itemNames.length == 1) {
			thql.append("(SELECT * FROM PJ_WorkItemInstance WHERE PJ_WorkItemInstance.name='")
				.append(itemNames[0])
				.append("' AND PJ_WorkItemInstance.flowInstanceId=")
				.append(callBackWorkItemInstance.getWorkFlowInstance().getId())
				.append(" AND PJ_WorkItemInstance.isSuspend=0")
				.append(" AND PJ_WorkItemInstance.endTime is not null")
				.append(") ORDER BY PJ_WorkItemInstance.endTime DESC");
		}
		if (itemNames.length > 1) {
			for (int i = 1; i < itemNames.length + 1; i++) {
				if (i == 1) {
					thql.append("(SELECT * FROM PJ_WorkItemInstance WHERE PJ_WorkItemInstance.name='")
						.append(itemNames[i - 1])
						.append("' AND PJ_WorkItemInstance.flowInstanceId=")
						.append(callBackWorkItemInstance.getWorkFlowInstance().getId())
						.append(" AND PJ_WorkItemInstance.isSuspend=0")
						.append(" AND PJ_WorkItemInstance.endTime is not null")
						.append(" UNION ALL ");
				} else if (i != itemNames.length) {
					thql.append(" SELECT * FROM PJ_WorkItemInstance WHERE PJ_WorkItemInstance.name='")
						.append(itemNames[i - 1])
						.append("' AND PJ_WorkItemInstance.flowInstanceId=")
						.append(callBackWorkItemInstance.getWorkFlowInstance().getId())
						.append(" AND PJ_WorkItemInstance.isSuspend=0")
						.append(" AND PJ_WorkItemInstance.endTime is not null")
						.append(" UNION ALL ");
				} else if (i == itemNames.length) {
					thql.append(" SELECT * FROM PJ_WorkItemInstance WHERE PJ_WorkItemInstance.name='")
						.append(itemNames[i - 1])
						.append("' AND PJ_WorkItemInstance.flowInstanceId=")
						.append(callBackWorkItemInstance.getWorkFlowInstance().getId())
						.append(" AND PJ_WorkItemInstance.isSuspend=0")
						.append(" AND PJ_WorkItemInstance.endTime is not null")
						.append(") ORDER BY PJ_WorkItemInstance.endTime DESC");
				}
			}
		}
		List list = super.getSession().createSQLQuery(thql.toString()).list();
		WorkItemInstance startCallBackWorkItemInstance = null;
		if (list.size() != 0) {
			Object [] objs = (Object[])list.get(0);
			startCallBackWorkItemInstance = (WorkItemInstance)this.getWorkItemInstanceById(new Long(objs[0].toString()));
		}
		return startCallBackWorkItemInstance;
	}
	
	/**
	 * 获取流程的当前工作项实例，即查看流程的进度
	 * @param workFlowInstance 流程实例
	 * @return currentWorkItemInstance  当前工作项实例
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 * */
	public WorkItemInstance getCurrentWorkItemInstance(WorkFlowInstance workFlowInstance){
		Token token = workFlowInstance.getRootToken();
		WorkItemInstance currentWorkItemInstance = token.getWorkItemInstance();
		return currentWorkItemInstance;
	}
	/**
	 * 将兄弟分支的工作项设置为悬挂状态
	 * @param forkWorkItemInstance 分支工作项实例
	 * @return 
	 * @since Sep 9, 2010
	 * @author xjt
	 * @version 1.00, Sep 9, 2010
	 * */
	public void executeSuspendBrotherWorkItemInstance(WorkItemInstance forkWorkItemInstance){
		//某一分支工作项持有的令牌
		Token token = forkWorkItemInstance.getToken();
		//兄弟令牌列表
		List brotherTokens = this.getBrotherTokens(token);
		Iterator it = brotherTokens.iterator();
		while(it.hasNext()){
			Token brotherToken = (Token)it.next();
			//兄弟令牌持有的工作项实例
			WorkItemInstance brotherWorkItemInstance = brotherToken.getWorkItemInstance();
			if(brotherWorkItemInstance!=null){
				brotherWorkItemInstance.setIsSuspend(Byte.valueOf("1"));
				super.updateObject(brotherWorkItemInstance);
			}
		}
	}
	/**
	 * 创建有期限的工作项实例
	 * @param workItemInstance 当前工作项实例
	 * @param transition 转移的路径路
	 * @param deadLine 最后期限
	 * @param actionName 超期时所要执行的动作
	 * @since Sep 27, 2010
	 * @author xjt
	 * @version 1.00, Sep 27, 2010
	 * @throws CreateException 
	 * */
	public WorkItemInstance createNextWorkItemInstanceWithDeadLine(WorkItemInstance workItemInstance,String transition,Date deadLine,String actionName) throws Exception{
		//结束当前的工作项实例
		workItemInstance.setEndTime(new Date());
		super.getSession().saveOrUpdate(workItemInstance);
		//当前工作项实例所在的节点
		Node node = this.getNodeByWorkItemInstance(workItemInstance);
		//当前流程实例
		WorkFlowInstance workFlowInstance = workItemInstance.getWorkFlowInstance();
		//下一流程路径
		Transition tran = this.getTransitionByNodeAndName(node, transition);
		//下一节点
		Node endNode = tran.getEndNode();
		//执行上下文
		ExecutionContext executionContext = new ExecutionContext(workFlowInstance,super.getSession());
		WorkItem nextWorkItem = this.getWorkItemByNode(endNode);
		//如果此任务节点包含自定义的动作，则执行此动作。
		this.executeActionHandler(executionContext,endNode);
		//此工作项对应的实例
		WorkItemInstance nextWorkItemInstance = new WorkItemInstance();
		//创建一个指向下一个工作项实例的令牌
		Token nextToken = new Token();
		super.insert(nextToken);
		//此工作项所属工作流程实例
		nextWorkItemInstance.setWorkFlowInstance(workFlowInstance);
		//对应的工作项定义
		nextWorkItemInstance.setWorkItem(nextWorkItem);
		//创建时间
		nextWorkItemInstance.setCreateTime(new Date());
		//其名与对应的工作项名称相同
		nextWorkItemInstance.setName(nextWorkItem.getName());
		//设置其拥有的令牌
		nextWorkItemInstance.setToken(nextToken);
		super.insert(nextWorkItemInstance);
		//更新令牌指向的工作项实例
		nextToken.setWorkItemInstance(nextWorkItemInstance);
		super.getSession().saveOrUpdate(nextToken);
		//更新当前流程实例的根令牌
		workFlowInstance.setRootToken(nextToken);
		super.getSession().saveOrUpdate(workFlowInstance);
		//从流程变量表看是否能查找到工作项拥有者的信息，有则建立工作项实例与拥有者的关系
		this.createWorkItemOwner(workFlowInstance, nextWorkItem, nextWorkItemInstance);
		//TODO  保存期限，job表信息
		Job job = new Job();
		job.setWorkItemInstance(nextWorkItemInstance);
		job.setDeadline(deadLine);
		job.setActionName(actionName);
		super.getSession().save(job);
		//TODO  启动quartz线程


		return nextWorkItemInstance;
	}
	/**
	 * 创建下一个工作项实例
	 * @param workItemInstance 当前工作项实例
	 * @param transition 转移的路径路
	 * @return workItemInstance 下一个工作项实例
	 * @since Sep 29, 2010
	 * @author xjt
	 * @version 1.00, Sep 29, 2010
	 * @throws CreateException 
	 * */
	public WorkItemInstance createNextTaskWorkItemInstance(WorkItemInstance workItemInstance,String transition) throws Exception{
//		结束当前的工作项实例
		workItemInstance.setEndTime(new Date());
		super.getSession().saveOrUpdate(workItemInstance);
		//当前工作项实例所在的节点
		Node node = this.getNodeByWorkItemInstance(workItemInstance);
		//当前流程实例
		WorkFlowInstance workFlowInstance = workItemInstance.getWorkFlowInstance();
		//下一流程路径
		Transition tran = this.getTransitionByNodeAndName(node, transition);
		//下一节点
		Node endNode = tran.getEndNode();
		//执行上下文
		ExecutionContext executionContext = new ExecutionContext(workFlowInstance,super.getSession());
		WorkItem nextWorkItem = this.getWorkItemByNode(endNode);
		//如果此任务节点包含自定义的动作，则执行此动作。
		this.executeActionHandler(executionContext,endNode);
		//此工作项对应的实例
		WorkItemInstance nextWorkItemInstance = new WorkItemInstance();
		//创建一个指向下一个工作项实例的令牌
		Token nextToken = new Token();
		super.insert(nextToken);
		//此工作项所属工作流程实例
		nextWorkItemInstance.setWorkFlowInstance(workFlowInstance);
		//对应的工作项定义
		nextWorkItemInstance.setWorkItem(nextWorkItem);
		//创建时间
		nextWorkItemInstance.setCreateTime(new Date());
		//其名与对应的工作项名称相同
		nextWorkItemInstance.setName(nextWorkItem.getName());
		//设置其拥有的令牌
		nextWorkItemInstance.setToken(nextToken);
		super.insert(nextWorkItemInstance);
		//更新令牌指向的工作项实例
		nextToken.setWorkItemInstance(nextWorkItemInstance);
		super.getSession().saveOrUpdate(nextToken);
		//更新当前流程实例的根令牌
		workFlowInstance.setRootToken(nextToken);
		super.getSession().saveOrUpdate(workFlowInstance);
		//从流程变量表看是否能查找到工作项拥有者的信息，有则建立工作项实例与拥有者的关系
		this.createWorkItemOwner(workFlowInstance, nextWorkItem, nextWorkItemInstance);
		return nextWorkItemInstance;
	}
	/**
	 * 查询指定任务名的未完成的工作项实例
	 * @param itemName 任务名
	 * @param flowId 工作流程实例ID
	 * @return workItemInstance 符合要求的工作项实例
	 * @since Oct 9, 2010
	 * @author xjt
	 * @version 1.00, Oct 9, 2010
	 * */
	public WorkItemInstance getUndoWorkItemInstance(String itemName,Long flowId){
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("FROM  com.pujin.workflow.exe.WorkItemInstance  workItemInstance  WHERE ")
				.append(" workItemInstance.name='").append(itemName)
				.append("' AND  workItemInstance.startTime = null AND workItemInstance.endTime = null")
				.append("  AND  workItemInstance.workFlowInstance.id= ").append(flowId);
		//第一个工作项实例
		WorkItemInstance workItemInstance = (WorkItemInstance)super.getSession().createQuery(strBuf.toString()).uniqueResult();
		return workItemInstance;
	}
	/**
	 * 定时查询工作项实例是否超期,并做相应处理
	 * @since Oct 13, 2010
	 * @author xjt
	 * @version 1.00, Oct 13, 2010
	 * */
	public void executeOverTimeHandler(){
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("SELECT Pj_Job.id FROM Pj_Job LEFT JOIN PJ_WorkItemInstance ON Pj_Job.itemInstanceId = PJ_WorkItemInstance.id  WHERE Pj_Job.deadline<= '")
				.append(Tools.getDate(super.getSessionFactory())).append("'")
				.append("  AND PJ_WorkItemInstance.endTime is null");
		List list = super.getSession().createSQLQuery(strBuf.toString()).list();
		Iterator it = list.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			Job job = (Job)super.getObject(xjt.workflow.exe.Job.class, new Long(obj.toString()));
			String actionName = job.getActionName();
			try {
				if(!"".equals(actionName)&&actionName!=null){
					OverTimeHandler overTimeHandler = (OverTimeHandler)Class.forName(actionName).newInstance();
					overTimeHandler.executeOverTimeHandler(job);
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 由工作项名和拥有者ID获取已完成的工作项实例
	 * @param itemName 工作项名
	 * @param ownerId 拥有者ID
	 * @return list 结果列表
	 * @since Oct 15, 2010
	 * @author xjt
	 * @version 1.00, Oct 15, 2010
	 * */
	public List getHistoryWorkItemInstanceList(String itemName,Long ownerId){
		StringBuffer hql = new StringBuffer();
		// 查询被未标置为被回退、未被悬置、已处理、属于指定人的任务实例
		hql.append("SELECT wi.id FROM PJ_WorkItemInstance wi LEFT JOIN PJ_WorkItem_Owner wo ON wi.id = wo.itemInstanceId ")
			.append(" WHERE wi.isCallBack = 0  AND wi.isSuspend = 0 AND wi.endTime is not null AND wo.done = 1 AND wi.name = '")
			.append(itemName)
			.append("' AND wo.ownerId = ").append(ownerId);
		SQLQuery query = super.getSession().createSQLQuery(hql.toString());
		List items = new ArrayList();
		Iterator it = query.list().iterator();
		while(it.hasNext()){
			Object obj = it.next();
			WorkItemInstance workItemInstance = this.getWorkItemInstanceById(new Long(obj.toString()));
			items.add(workItemInstance);
		}
		return items;
	}
	/**
	 * 由工作项名和拥有者ID获取已完成的工作项实例
	 * @param itemName 工作项名
	 * @param ownerId 拥有者ID
	 * @param pageSize   每页数
	 * @param startIndex    开始索引值
	 * @return finder 符合条件的工作项分页器
	 * @since Oct 15, 2010
	 * @author xjt
	 * @version 1.00, Oct 15, 2010
	 * */
	public Pager getHistoryWorkItemInstancePage(String itemName,Long ownerId,int pageSize, int startIndex){
		StringBuffer hql = new StringBuffer();
		// 查询被未标置为被回退、未被悬置、已处理、属于指定人的任务实例
		hql.append("SELECT wi.id FROM PJ_WorkItemInstance wi LEFT JOIN PJ_WorkItem_Owner wo ON wi.id = wo.itemInstanceId ")
			.append(" WHERE wi.isCallBack = 0  AND wi.isSuspend = 0 AND wi.endTime is not null AND wo.done = 1 AND wi.name = '")
			.append(itemName)
			.append("' AND wo.ownerId = ").append(ownerId)
			.append(" ORDER BY wi.endTime DESC");
		SQLQuery query = super.getSession().createSQLQuery(hql.toString());
		int totalCount = query.list().size();
		query.setFirstResult(startIndex);
		query.setMaxResults(pageSize);
		List list = query.list();
		List items = new ArrayList();
		Iterator it = list.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			WorkItemInstance workItemInstance = this.getWorkItemInstanceById(new Long(obj.toString()));
			items.add(workItemInstance);
		}
		Pager pager = new Pager(items, totalCount, pageSize,
				startIndex);
		return pager;
	}
}
