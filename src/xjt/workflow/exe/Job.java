package xjt.workflow.exe;


import java.util.Date;


/**
 * 限时工作项实例
 * @since Sep  27, 2010
 * @author xjt
 * @version 1.00  Sep  27, 2010
 * */
public class Job  implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	// Fields    
	/**
	 * 标识ID
	 * */
    private Long id;
    /**
     * 工作项实例
     * */
    private WorkItemInstance workItemInstance;
    /**
     * 最后期限
     * */
    private Date deadline;
    /**
     * 工作项实例到期未执行时所要执行的动作
     * */
    private String actionName;


    // Constructors
    /** full constructor */
    public Job(WorkItemInstance workItemInstance, Date deadline, String actionName) {
		super();
		this.workItemInstance = workItemInstance;
		this.deadline = deadline;
		this.actionName = actionName;
	}
	/** default constructor */
    public Job() {
    }   
    
    // Property accessors

    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public WorkItemInstance getWorkItemInstance() {
		return workItemInstance;
	}
	public void setWorkItemInstance(WorkItemInstance workItemInstance) {
		this.workItemInstance = workItemInstance;
	}
	
	public Date getDeadline() {
        return this.deadline;
    }
    
    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getActionName() {
        return this.actionName;
    }
    
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
   








}