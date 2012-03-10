package xjt.workflow.def;
/**
 * 节点类
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class Node  implements java.io.Serializable {
 
	private static final long serialVersionUID = 1L;
	/**
	 * 标识ID
	 * */
	private Long id;
	/**
	 * 流程定义
	 * */
    private WorkFlow workFlow;
    /**
     * 节点名
     * */
    private String name;
    /**
     * 节点类型名，有start、task、decision、fork、join、end.
     * */
    private String type;

    // Constructors
    /** default constructor */
    public Node() {
    }

    /** full constructor */
    public Node(WorkFlow workFlow, String name, String type) {
        this.workFlow = workFlow;
        this.name = name;
        this.type = type;
    }

    // Property accessors

    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public WorkFlow getWorkFlow() {
		return workFlow;
	}

	public void setWorkFlow(WorkFlow workFlow) {
		this.workFlow = workFlow;
	}

	public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}