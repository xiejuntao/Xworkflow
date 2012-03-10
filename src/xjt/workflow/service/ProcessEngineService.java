package xjt.workflow.service;

import xjt.workflow.def.service.DefinitionService;
import xjt.workflow.exe.service.ExecutionService;

/**
 * 流程引擎服务接口
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public interface ProcessEngineService {
	/**
	 * 获取解析流程定义的服务
	 * @param
	 * @return definitionService 解析流程定义的服务
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 * */
	public  DefinitionService getDefinitionService();
	/**
	 * 获取执行流程的服务
	 * @param
	 * @return executionService 执行流程的服务
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 * */
	public  ExecutionService getExecutionService(); 
}
