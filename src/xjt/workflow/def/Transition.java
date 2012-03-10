package xjt.workflow.def;
/**
 * 路径类
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class Transition  implements java.io.Serializable {
	
	 private static final long serialVersionUID = 1L;
	 /**
	  * 标识ID
	  * */
     private Long id;
     /**
      * 路径名
      * */
     private String name;
     /**
      * 开始节点，即路径所在的节点
      * */
     private Node node;
     /**
      * 结束节点
      * */
     private Node endNode;

    // Constructors
    /** default constructor */
    public Transition() {
    }

    /** full constructor */
    public Transition(String name,Node node,Node endNode) {
        this.name = name;
        this.node = node;
        this.endNode = endNode;
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

	public Node getEndNode() {
		return endNode;
	}

	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
}