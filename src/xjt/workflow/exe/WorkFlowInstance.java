package xjt.workflow.exe;

import java.util.Date;

import xjt.workflow.def.WorkFlow;

/**
 * 工作流程实例
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class WorkFlowInstance  implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	 /**
	  * 标识ID
	  * */
	private Long id;
	 /**
	  * 工作流
	  * */
    private WorkFlow workFlow;
     /**
      * 根令牌，标志流程走向
      * */
    private Token rootToken;
     /**
      * 开始时间
      * */
    private Date startTime;
     /**
      * 结束时间
      * */
    private Date endTime;


    // Constructors

    /** default constructor */
    public WorkFlowInstance() {
    }

	/** minimal constructor */
    public WorkFlowInstance(WorkFlow workFlow) {
        this.workFlow = workFlow;
    }
    
    /** full constructor */
    public WorkFlowInstance(WorkFlow workFlow,Token rootToken, Date startTime, Date endTime) {
        this.workFlow = workFlow;
        this.rootToken = rootToken;
        this.startTime = startTime;
        this.endTime = endTime;
    }

   
    // Property accessors

    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }


    public Token getRootToken() {
		return rootToken;
	}

	public void setRootToken(Token rootToken) {
		this.rootToken = rootToken;
	}

	public WorkFlow getWorkFlow() {
		return workFlow;
	}

	public void setWorkFlow(WorkFlow workFlow) {
		this.workFlow = workFlow;
	}

	public Date getStartTime() {
        return this.startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return this.endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}