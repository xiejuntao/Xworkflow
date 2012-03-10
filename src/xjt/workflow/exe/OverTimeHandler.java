package xjt.workflow.exe;

import gov.gdgs.common.database.hibernate.AbstractHibernateDAO;

import org.hibernate.SessionFactory;

import xjt.workflow.util.WebConstants;



/**
 * 工作项实例超时完成时所执行的动作。
 * @since Oct 13, 2010
 * @author xjt
 * @version 1.00  Oct 13, 2010
 * */
public abstract class OverTimeHandler extends AbstractHibernateDAO{
	/**
	 * 构造函数,设置数据库会话工厂。
	 * @since Oct 13, 2010
	 * @author xjt
	 * @version 1.00  Oct 13, 2010
	 * */
	public OverTimeHandler(){
		setSessionFactory((SessionFactory)WebConstants.WEB_APP_CONTEXT.getBean("mySessionFactory"));
	}
	/**
	 * 超时处理
	 * @param job 到期未完成的工作
	 * @return
	 * @since Oct 13, 2010
	 * @author xjt
	 * @version 1.00  Oct 13, 2010
	 * */
	public abstract void executeOverTimeHandler(Job job)throws Exception;
}
