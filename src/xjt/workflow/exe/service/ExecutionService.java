package xjt.workflow.exe.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import xjt.workflow.def.Action;
import xjt.workflow.def.Node;
import xjt.workflow.def.Transition;
import xjt.workflow.def.WorkFlow;
import xjt.workflow.def.WorkItem;
import xjt.workflow.exe.ExecutionContext;
import xjt.workflow.exe.Token;
import xjt.workflow.exe.WorkFlowInstance;
import xjt.workflow.exe.WorkItemInstance;
import xjt.workflow.page.Pager;


/**
 * 执行流程的服务接口
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public interface ExecutionService {
	/**
	 * 创建一个新的工作实例，启动工作流程
	 * @param flowName 流程名
	 * @parma globalVars 流程的全局变量
	 * @return workProcess 流程实例
	 * @since Aug 31, 2010
	 * @author xjt
	 * @version 1.00 Aug 31,2010
	 * */
	public WorkFlowInstance createWorkFlowInstance(String flowName,Map globalVars)throws Exception;
	/**
	 * 获取第一个工作项实例
	 * @param itemName 工作项名称
	 * @param workFlowInstance  工作流实例
	 * @return workItemInstance 工作项实例
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public WorkItemInstance getFirstWorkItemInstance(String itemName,WorkFlowInstance workFlowInstance);
	/**
	 * 跳转下一个工作项
	 * @param workItemInstance 当前工作项实例
	 * @param transition 路径名
	 * @return 
	 * @since Aug 18, 2010
	 * @author xjt
	 * @version 1.00 Aug 18,2010
	 * */
	public void createNextWorkItemInstance(WorkItemInstance workItemInstance,String transition)throws Exception;
	/**
	 * 分配工作项拥有者
	 * @param workFlowInstance 工作流实例
	 * @param workItem 工作项
	 * @param workItemInstance 工作项实例
	 * @return
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public void createWorkItemOwner(WorkFlowInstance workFlowInstance,WorkItem workItem,WorkItemInstance workItemInstance)throws Exception;
	/**
	 * 保存全局流程变量
	 * @param globalVars 全局变量Map
	 * @param workFlowInstance 流程实例
	 * @return 
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public void saveVariable(Map globalVars,WorkFlowInstance workFlowInstance)throws Exception;
	/**
	 * 更新流程变量,更新前确定已有此流程变量
	 * @param  name  变量名
	 * @param  value  更新的变量值
	 * @return 
	 * @since Sep 7, 2010
	 * @author xjt
	 * @version 1.00 Sep 7,2010
	 * */
	public void updateVariable(String name,String value,WorkItemInstance workItemInstance);
	/**
	 * 保存全局流程变量
	 * @param name  变量名
	 * @param value  变量值
	 * @param workFlowInstance 流程实例
	 * @return 
	 * @since Sep 7, 2010
	 * @author xjt
	 * @version 1.00 Sep 7, 2010
	 * */
	public void saveVariable(String name,String value,WorkFlowInstance workFlowInstance)throws Exception;
	/**
	 * 保存局部流程变量
	 * @param name  变量名
	 * @param value 变量值
	 * @param workItemInstance  工作项实例
	 * @return 
	 * @since Sep 21, 2010
	 * @author xjt
	 * @version 1.00 Sep 21, 2010
	 * */
	public void saveVariable(String name,String value,WorkItemInstance workItemInstance)throws Exception;
	/**
	 * 获取流程变量
	 * @param  name  变量名
	 * @param  workItemInstance 工作项实例
	 * @return  value  变量值
	 * @since Sep 7, 2010
	 * @author xjt
	 * @version 1.00 Sep 7, 2010
	 * */
	public String getVariable(String name,WorkItemInstance workItemInstance);
	/**
	 * 获取局部流程变量
	 * @param  name  变量名
	 * @param  workItemInstance 工作项实例
	 * @return  value  变量值
	 * @since Oct 8, 2010
	 * @author xjt
	 * @version 1.00 Oct 8, 2010
	 * */
	public String getVariableByWorkItemInstance(String name,WorkItemInstance workItemInstance);
	/**
	 * 通过工作项实例ID获取工作项实例
	 * @param itemId   工作项实例ID
	 * @return workItem 工作项
	 * @since Sep 7, 2010
	 * @author xjt
	 * @version 1.00 Sep 7, 2010
	 */
	public WorkItemInstance getWorkItemInstanceById(Long itemId);
	/**
	 * 获取定义的开始节点
	 * @param  workFlow 流程定义
	 * @return startNode 开始节点
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public Node getStartNode(WorkFlow workFlow);
	/**
	 * 获取节点的输出路径
	 * @param node 节点
	 * @return nextList 输出路径列表
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public List getTransitionByNode(Node node);
	/**
	 * 获取节点指定名字的输出路径
	 * @paran node 节点
	 * @param transitionName 路径名
	 * @return transition 路径
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public Transition getTransitionByNodeAndName(Node node,String transitionName);
	/**
	 * 获取工作项
	 * @param node 工作项所在节点
	 * @return workItem 节点所在的工作项
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public WorkItem getWorkItemByNode(Node node);
	/**
	 * 通过工作项实例查找包含此工作项定义的节点
	 * @param workItemInstance 工作项所在节点
	 * @return workItem 节点所在的工作项
	 * @since Sep 1, 2010
	 * @author xjt
	 * @version 1.00 Sep 1,2010
	 * */
	public Node getNodeByWorkItemInstance(WorkItemInstance workItemInstance);
	/**
	 * 获取兄弟令牌
	 * @param token 令牌
	 * @return brotherTokens 兄弟令牌
	 * @since Sep 2,2010
	 * @author xjt
	 * @version 1.00  Sep 2, 2010
	 * */
	public List getBrotherTokens(Token token);
	/**
	 * 获取节点包含的动作
	 * @param actionNode 节点
	 * @return action 动作
	 * @since Sep 2,2010
	 * @author xjt
	 * @version 1.00  Sep 2, 2010
	 * */
	public Action getActionByNode(Node actionNode);
	/**
	 * 执行动作
	 * @param className 类名
	 * @param executionContext 执行上下文
	 * @return trnasiotnName 路径名
	 * @since Sep 2,2010
	 * @author xjt
	 * @version 1.00  Sep 2, 2010
	 * */
	public String executeDecisionAction(ExecutionContext executionContext,String className);
	/**
	 * 获取判断节点动作执行后的路径
	 * @param decisionNode 决定节点
	 * @param transitionName 路径名
	 * @return transition 执行路径
	 * @since Sep 2,2010
	 * @author xjt
	 * @version 1.00  Sep 2, 2010
	 * */
	public Transition getDecisionTransition(Node decisionNode,String transitionName);
	/**
	 * 执行到分支节点的动作
	 * @param workFlowInstance 工作流实例
	 * @param forkNode 分支节点
	 * @return 
	 * @since Sep 3,2010
	 * @author xjt
	 * @version 1.00  Sep 3, 2010
	 * */
	public void executeToFork(WorkItemInstance workItemInstance,Node forkNode)throws Exception;
	/**
	 * 执行到判断节点的动作
	 * @param workFlowInstance 工作流实例
	 * @param decisionNode 判断节点
	 * @return 
	 * @since Sep 3,2010
	 * @author xjt
	 * @version 1.00  Sep 3, 2010
	 * */
	public void executeToDecision(WorkFlowInstance workFlowInstance,Node decisionNode)throws Exception;
	/**
	 * 执行到汇合节点的动作
	 * @param
	 * @return 
	 * @since Sep 3,2010
	 * @author xjt
	 * @version 1.00  Sep 3, 2010
	 * */
	public void executeToJoin(WorkFlowInstance workFlowInstance,WorkItemInstance workItemInstance,Node joinNode)throws Exception;
	/**
	 * 执行到任务节点的动作
	 * @param
	 * @return 
	 * @since Sep 3,2010
	 * @author xjt
	 * @version 1.00  Sep 3, 2010
	 * */
	public void executeToTask(WorkFlowInstance workFlowInstance,Node taskNode)throws Exception;
	/**
	 * 其他所有的兄弟令牌是否处于等待状态
	 * @param Token currentToken 当前工作项实例持有的令牌
	 * @return boolean isAllWaited 
	 * @since Sep 3,2010
	 * @author xjt
	 * @version 1.00  Sep 3, 2010
	 * */
	public boolean isAllBrotherTokenWaited(Token currentToken);
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
	public void executeActionHandler(ExecutionContext executionContext,Node taskNode);
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
			int pageSize, int startIndex);
	/**
	 * 获取工作项实例列表
	 * @param itemName  工作项名
	 * @param owner   工作项的所有者
	 * @return lst 工作项实例列表
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 */
	public List getWorkItemInstanceList(String itemName, String owner);
	/**
	 * 签收工作项实例
	 * @param itemInstanceId   工作项实例ID
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 */
	public void executeWorkItemInstance(Long itemInstanceId);
	/**
	 * 签收工作项实例
	 * @param workItemInstance   工作项实例
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 */
	public void executeWorkItemInstance(WorkItemInstance workItemInstance);
	/**
	 * 更新工作项实例的拥有者，即谁处理了这个工作项实例
	 * @param workItemInstance    工作项
	 * @param owner   工作项的拥有者
	 * @return
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 */
	public void updateItemOwner(WorkItemInstance workItemInstance, String owner);
	/**
	 * 获取工作项实例的拥有者，即谁处理了这个工作项实例
	 * @param  workItemInstance    工作项
	 * @return  owner   工作项的拥有者
	 * @return
	 * @since Sep 7, 2010
	 * @author xjt
	 * @version 1.00, Sep 7, 2010
	 */
	public String getWorkItemInstanceOwner(WorkItemInstance workItemInstance);
	/**
	 * 创建被回退的工作项实例
	 * @param workItemInstance  发起回退的工作项实例
	 * @param transition  回退的路径
	 * @return
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 * */
	public void createCallBackWorkItemInstance(WorkItemInstance workItemInstance,String transition)throws Exception;
	/**
	 * 返回被回退的工作项实例列表
	 * @param itemName  工作项名
	 * @param owner   工作项的所有者
	 * @return list 被回退的工作项实例列表
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 * */
	public List getCallBackWorkItemInstanceList(String itemName,String owner);
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
			int pageSize, int startIndex);
	/**
	 * 结束工作流程实例
	 * @param workItemInstance 当前工作项实例
	 * @return 
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 * */
	public void executeEndWorkFlowInstance(WorkItemInstance workItemInstance);
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
			String[] itemNames);
	/**
	 * 获取流程的当前工作项实例，即查看流程的进度
	 * @param workFlowInstance 流程实例
	 * @return currentWorkItemInstance  当前工作项实例
	 * @since Sep 6, 2010
	 * @author xjt
	 * @version 1.00, Sep 6, 2010
	 * */
	public WorkItemInstance getCurrentWorkItemInstance(WorkFlowInstance workFlowInstance);
	/**
	 * 将兄弟分支的工作项设置为悬挂状态
	 * @param forkWorkItemInstance 分支工作项实例
	 * @return 
	 * @since Sep 9, 2010
	 * @author xjt
	 * @version 1.00, Sep 9, 2010
	 * */
	public void executeSuspendBrotherWorkItemInstance(WorkItemInstance forkWorkItemInstance);
	/**
	 * 创建有期限的工作项实例
	 * @param workItemInstance 当前工作项实例
	 * @param transition 转移的路径路
	 * @param deadLine 最后期限
	 * @param actionName 超期时所要执行的动作
	 * @return workItemInstance 有期限的工作项实例
	 * @since Sep 27, 2010
	 * @author xjt
	 * @version 1.00, Sep 27, 2010
	 * */
	public WorkItemInstance createNextWorkItemInstanceWithDeadLine(WorkItemInstance workItemInstance,String transition,Date deadLine,String actionName)throws Exception;
	/**
	 * 创建下一个工作项实例
	 * @param workItemInstance 当前工作项实例
	 * @param transition 转移的路径路
	 * @return workItemInstance 下一个工作项实例
	 * @since Sep 29, 2010
	 * @author xjt
	 * @version 1.00, Sep 29, 2010
	 * */
	public WorkItemInstance createNextTaskWorkItemInstance(WorkItemInstance workItemInstance,String transition)throws Exception;
	/**
	 * 查询指定任务名的未完成的工作项实例
	 * @param itemName 任务名
	 * @param flowId 工作流程实例ID
	 * @return workItemInstance 符合要求的工作项实例
	 * @since Oct 9, 2010
	 * @author xjt
	 * @version 1.00, Oct 9, 2010
	 * */
	public WorkItemInstance getUndoWorkItemInstance(String itemName,Long flowId);
	/**
	 * 定时查询工作项实例是否超期,并做相应处理
	 * @since Oct 13, 2010
	 * @author xjt
	 * @version 1.00, Oct 13, 2010
	 * */
	public void executeOverTimeHandler();
	/**
	 * 由工作项名和拥有者ID获取已完成的工作项实例
	 * @param itemName 工作项名
	 * @param ownerId 拥有者ID
	 * @return list 结果列表
	 * @since Oct 15, 2010
	 * @author xjt
	 * @version 1.00, Oct 15, 2010
	 * */
	public List getHistoryWorkItemInstanceList(String itemName,Long ownerId);
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
	public Pager getHistoryWorkItemInstancePage(String itemName,Long ownerId,int pageSize, int startIndex);
}
