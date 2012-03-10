package xjt.workflow.exe;
/**
 * 判断动作接口,decision类型的节点有且可有一个动作，
 * 且执行该动作的类实现此接口。
 * @since Sep 2, 2010
 * @author xjt
 * @version 1.00  Sep 2, 2010
 * */
public interface DecisionHandler {
	/**
	 * 执行动作,返回流程接下来的路径
	 * @param executionContext 执行上下文，包含当前流程的信息
	 * @return  transition 接下来的路径
	 * @since Sep 2,2010
	 * @author xjt
	 * @version 1.00  Sep 2, 2010
	 * */
	public String execute(ExecutionContext executionContext);
}
