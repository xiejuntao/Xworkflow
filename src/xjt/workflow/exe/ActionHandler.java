package xjt.workflow.exe;

/**
 * 动作接口，任务节点最多可以包含一个动作。
 * @since Sep 6, 2010
 * @author xjt
 * @version 1.00  Sep 6, 2010
 * */
public interface ActionHandler {
	/**
	 * 进入节点，执行此动作。
	 * @param  executionContext 执行上下文，包含当前流程的信息
	 * @return
	 * @since Sep 2,2010
	 * @author xjt
	 * @version 1.00  Sep 2, 2010
	 * */
	public void execute(ExecutionContext executionContext);
}
