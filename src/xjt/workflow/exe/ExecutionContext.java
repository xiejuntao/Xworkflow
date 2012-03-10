package xjt.workflow.exe;

import org.hibernate.Session;

public class ExecutionContext {
	protected WorkFlowInstance workFlowInstance;
	protected Session hibernateSession;

	public ExecutionContext() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ExecutionContext(WorkFlowInstance workFlowInstance,Session hibernateSession) {
		super();
		this.workFlowInstance = workFlowInstance;
		this.hibernateSession = hibernateSession;
	}
	/**
	 * 获取当前流程的全局变量
	 * @param varName 变量名
	 * @return varValue 变量值
	 * @since Sep 8, 2010
	 * @author xjt
	 * @version 1.00, Sep 8, 2010
	 * */
	public String getVariable(String varName){
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("FROM  com.pujin.workflow.exe.Variable  variable  WHERE  variable.workFlowInstance.id = ")
				.append(workFlowInstance.getId()).append("  AND variable.varName = '").append(varName).append("'");
		Variable variable = (Variable)hibernateSession.createQuery(strBuf.toString()).uniqueResult();
		return variable.getVarValue();
	}
	/**
	 * 获取hibernate会话
	 * @return session
	 * @since Sep 28, 2010
	 * @author xjt
	 * @version 1.00, Sep 28, 2010
	 * */
	public Session getHibernateSession(){
		return this.hibernateSession;
	}
}
