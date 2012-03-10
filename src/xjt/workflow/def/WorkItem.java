package xjt.workflow.def;
/**
 * 工作项类
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class WorkItem  implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 标识ID
	 * */
	private Long id;
	/**
	 * 工作项名称
	 * */
    private String name;
    /**
     * 所在节点
     * */
    private Node node;
    /**
     * 工作项拥有者标识
     * */
    private String owner;

    // Constructors

    /** default constructor */
    public WorkItem() {
    }

	/** minimal constructor */
    public WorkItem(String name, Node node) {
        this.name = name;
        this.node = node;
    }
    
    /** full constructor */
    public WorkItem(String name, Node node, String owner) {
        this.name = name;
        this.node = node;
        this.owner = owner;
    }

   
    // Property accessors

    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public String getOwner() {
        return this.owner;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
}