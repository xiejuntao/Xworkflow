package xjt.workflow.def.service;

import java.io.InputStream;

import xjt.workflow.def.WorkFlow;



/**
 * 解析流程定义的服务接口
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public interface DefinitionService {
	/**
	 * 创建一个流程定义
	 * @param inputStream  流程定义文件的输入流
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 * @throws Exception 
	 * */
	public void createWorkFlow(InputStream InputStream)throws Exception;
	/**
	 * 查询最新版本的流程定义
	 * @param name  流程定义名
	 * @return workFlow 流程实例
	 * @since Aug 31, 2010
	 * @author xjt
	 * @version 1.00  Aug 31, 2010
	 * */
	public WorkFlow findLastWorkFlow(String name) throws Exception;
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
	public void createProcessDefinition(String xmlStr,String flowName,String ownerStr,String actionStr)throws Exception;
	/**
	 * 获取最新的流程定义并生成定义的字符串
	 * @param name  流程定义的名称
	 * @since Oct 26, 2010
	 * @author xjt
	 * @version 1.00  Oct 26, 2010
	 * */
	public String getWorkFlowDefinition(String name)throws Exception;
}
