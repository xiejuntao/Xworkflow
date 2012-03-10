package xjt.workflow.exe;
/**
 * 工作项与拥有者关系类
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class WorkItemOwner  implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 标识ID
	 * */
	private Long id;
	/**
	 * 工作项实例ID
	 * */
    private Long itemInstanceId;
    /**
     * 工作项拥有者标识
     * */
    private Long ownerId;
    /**
     * 是否处理了此任务
     * */
    private Byte done;
    // Constructors

    /** default constructor */
    public WorkItemOwner() {
    }
    /** full constructor */
    public WorkItemOwner(Long itemInstanceId, Long ownerId,Byte done) {
        this.itemInstanceId = itemInstanceId;
        this.ownerId = ownerId;
        this.done = done;
    }
    // Property accessors

    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemInstanceId() {
        return this.itemInstanceId;
    }
    
    public void setItemInstanceId(Long itemInstanceId) {
        this.itemInstanceId = itemInstanceId;
    }

    public Long getOwnerId() {
        return this.ownerId;
    }
    
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
	public Byte isDone() {
		return done;
	}
	public void setDone(Byte done) {
		this.done = done;
	} 
}