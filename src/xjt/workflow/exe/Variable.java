package xjt.workflow.exe;
/**
 * 工作流程全局变量类
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class Variable  implements java.io.Serializable {

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
     * 工作项实例
     * */
    private WorkItemInstance workItemInstance;
    /**
     * 变量名
     * */
    private String varName;
    /**
     * 变量值
     * */
    private String varValue;


    // Constructors
    /** default constructor */
    public Variable() {
    }

	/** minimal constructor */
    public Variable(WorkFlowInstance workFlowInstance, String varName) {
        this.workFlowInstance = workFlowInstance;
        this.varName = varName;
    }
    
    /** minimal constructor */
    public Variable(WorkFlowInstance workFlowInstance, String varName, String varValue) {
        this.workFlowInstance = workFlowInstance;
        this.varName = varName;
        this.varValue = varValue;
    }
    /** full constructor */
    public Variable(WorkFlowInstance workFlowInstance, WorkItemInstance workItemInstance,String varName, String varValue) {
        this.workFlowInstance = workFlowInstance;
        this.workItemInstance = workItemInstance;
        this.varName = varName;
        this.varValue = varValue;
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

	public WorkItemInstance getWorkItemInstance() {
		return workItemInstance;
	}

	public void setWorkItemInstance(WorkItemInstance workItemInstance) {
		this.workItemInstance = workItemInstance;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getVarValue() {
		return varValue;
	}

	public void setVarValue(String varValue) {
		this.varValue = varValue;
	}
	
}