package xjt.workflow.def;
/**
 * 流程定义类
 * @since Aug 30, 2010
 * @author xjt
 * @version 1.00  Aug 30, 2010
 * */
public class WorkFlow  implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 标识ID
	 * */ 
	private Long id;
	/**
	 * 流程名
	 * */
    private String name;
    /**
     * 流程版本
     * */
    private Long version;

    // Constructors
    /** default constructor */
    public WorkFlow() {
    }

    /** full constructor */
    public WorkFlow(String name, Long version) {
        this.name = name;
        this.version = version;
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

    public Long getVersion() {
        return this.version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
}