package xjt.workflow.def;
/**
 * 动作类
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class Action  implements java.io.Serializable {
 
	private static final long serialVersionUID = 1L;
    /**
     * 标识ID
     * */
	private Long id;
	/**
	 * 动作名
	 * */
    private String name;
    /**
     * 对应的类名
     * */
    private String className;
    /**
     * 所在的节点
     * */
    private Node node;
     
    // Constructors
    /** default constructor */
    public Action() {
    }
    /** full constructor */
    public Action(String name, String className, Node node) {
        this.name = name;
        this.className = className;
        this.node = node;
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

    public String getClassName() {
        return this.className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
}