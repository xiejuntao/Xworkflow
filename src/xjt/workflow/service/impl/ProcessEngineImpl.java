package xjt.workflow.service.impl;

import xjt.workflow.def.service.DefinitionService;
import xjt.workflow.exe.service.ExecutionService;
import xjt.workflow.service.ProcessEngineService;

/**
 * 流程引擎服务实现类
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class ProcessEngineImpl implements ProcessEngineService {
	/**
	 * 解析流程定义的服务
	 * */
	public DefinitionService definitionService;
	/**
	 * 执行流程的服务
	 * */
	public ExecutionService executionService;
	/**
	 * 设置解析流程定义的服务，由spring注入
	 * @param definitionService
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 * */
	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}
	/**
	 * 设置执行流程的服务，由spring注入
	 * @param executionService
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 * */
	public void setExecutionService(ExecutionService executionService) {
		this.executionService = executionService;
	}
	/**
	 * 实现ProcessEngineService接口，获取解析流程定义的服务
	 * @param
	 * @return definitionService 解析流程定义的服务
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 * */
	public DefinitionService getDefinitionService() {
		return this.definitionService;
	}
	/**
	 * 实现ProcessEngineService接口，获取执行流程的服务
	 * @param
	 * @return executionService 执行流程的服务
	 * @since Aug 30, 2010
	 * @author xjt
	 * @version 1.00  Aug 30, 2010
	 * */
	public ExecutionService getExecutionService() {
		return this.executionService;
	}

}
