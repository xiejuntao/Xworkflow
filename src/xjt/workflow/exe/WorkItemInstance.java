package xjt.workflow.exe;

import java.util.Date;

import xjt.workflow.def.WorkItem;

/**
 * 工作项实例类
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class WorkItemInstance  implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 标识ID
	 * */
	private Long id;
	/**
	 * 工作流实例
	 * */
    private WorkFlowInstance workFlowInstance;
    /**
     * 工作项定义
     * */
    private WorkItem workItem;
    /**
     * 工作项实例名称
     * */
    private String name;
    /**
     * 工作项创建时间
     * */
    private Date createTime;
    /**
     * 工作项开始时间，即签收时间
     * */
    private Date startTime;
    /**
     * 工作项完成时间
     * */
    private Date endTime;
    /**
     * 所持的令牌
     * */
    private Token token;
    /**
     * 标志此工作项是否悬挂
     * */
    private Byte isSuspend;
    /**
     * 标志此工作项是否是回退所创建
     * */
    private Byte isCallBack;

    // Constructors

    /** default constructor */
    public WorkItemInstance() {
    }

	/** minimal constructor */
    public WorkItemInstance(WorkFlowInstance workFlowInstance, WorkItem workItem,String name, Date createTime) {
        this.workFlowInstance = workFlowInstance;
        this.workItem = workItem;
        this.name = name;
        this.createTime = createTime;
    }
    
    /** full constructor */
    public WorkItemInstance(WorkFlowInstance workFlowInstance, WorkItem workItem,String name, Date createTime, Date startTime, Date endTime, Token token, Byte isSuspend, Byte isCallBack) {
        this.workFlowInstance = workFlowInstance;
        this.workItem = workItem;
        this.name = name;
        this.createTime = createTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.token = token;
        this.isSuspend = isSuspend;
        this.isCallBack = isCallBack;
    }
    
    // Property accessors
    
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public WorkFlowInstance getWorkFlowInstance() {
		return workFlowInstance;
	}

	public void setWorkFlowInstance(WorkFlowInstance workFlowInstance) {
		this.workFlowInstance = workFlowInstance;
	}

	public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return this.createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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

    public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public Byte getIsSuspend() {
        return this.isSuspend;
    }
    
    public void setIsSuspend(Byte isSuspend) {
        this.isSuspend = isSuspend;
    }

    public Byte getIsCallBack() {
        return this.isCallBack;
    }
    
    public void setIsCallBack(Byte isCallBack) {
        this.isCallBack = isCallBack;
    }

	public WorkItem getWorkItem() {
		return workItem;
	}

	public void setWorkItem(WorkItem workItem) {
		this.workItem = workItem;
	}
    
}