package xjt.workflow.exe;

/**
 * 令牌类，标志流程进展
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class Token  implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 标识ID
	 * */
	private Long id;
	/**
	 * 当前所在的工作项实例
	 * */
    private WorkItemInstance workItemInstance;
    /**
     * 是否处于等待状态，用于分支、汇合的判断
     * */
    private Byte isWaited;
    /**
     * 是否废弃，当子令牌继承父母令牌时，将父母令牌设为不可用
     * */
    private Byte isDisable;
    /**
     * 父母令牌
     * */
    private Token parentToken;

    // Constructors
    /** default constructor */
    public Token() {
    }
    
    /** full constructor */
    public Token(WorkItemInstance workItemInstance, Byte isWaited, Byte isDisable,Token parentToken) {
        this.workItemInstance = workItemInstance;
        this.isWaited = isWaited;
        this.isDisable = isDisable;
        this.parentToken = parentToken;
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

	public Byte getIsWaited() {
        return this.isWaited;
    }
    
    public void setIsWaited(Byte isWaited) {
        this.isWaited = isWaited;
    }

    public Byte getIsDisable() {
        return this.isDisable;
    }
    
    public void setIsDisable(Byte isDisable) {
        this.isDisable = isDisable;
    }
    
    public Token getParentToken() {
		return parentToken;
	}

	public void setParentToken(Token parentToken) {
		this.parentToken = parentToken;
	}
}