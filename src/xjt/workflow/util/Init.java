package xjt.workflow.util;

import javax.servlet.http.HttpServlet;

import org.springframework.web.context.support.WebApplicationContextUtils;


public class Init 	extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public void init() {
		//初始化系统参数。
		initWebConstants();
	}
	/**
	 * 初始化系统各参数。
	 */
	private void initWebConstants(){
		WebConstants.WEB_APP_CONTEXT = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());

	}
	
}
