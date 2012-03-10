package xjt.workflow.dao.hibernate;

import java.io.Serializable;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;

import org.hibernate.type.Type;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


public class HibernateDAO extends HibernateDaoSupport {
	protected final Log log = LogFactory.getLog(getClass());    

    /**
     * 插入数据
     */
    public Object insert(Object po) throws Exception {

        try {
            getHibernateTemplate().save(po);
            return po;
        } catch (Exception ex) {
        	ex.printStackTrace();
            throw new RuntimeException(
                    "UnChecked Exception occur when creating record: " +
                    ex.getMessage());
        } 
    }
    /**
     * 删除一条数据
     */
    public void delete(Class businessClass, java.io.Serializable id) throws
           Exception {

        try {
        	
            Object obj = getHibernateTemplate().get(businessClass,id);
            if(obj!=null){
            	getHibernateTemplate().delete(obj);
            }
        } catch (HibernateException se) {
            throw new Exception(se.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(
                    "UnChecked Exception occur when removing a record: " +
                    e.getMessage());
        }
    }

    
    /**
     * 根据SQL语句删除数据
     *
     * @param hsql String
     * @param values Object[]  值
     * @param types Type[]     参数类型
     * @throws RemoveException
     */
    public void delete(String hsql, Object[] values, Type[] types) throws
            Exception {

        try {
        	     
            if (values == null || types == null) { //记录不存在时,delete也不会出错
            	List list = search(hsql);
            	getHibernateTemplate().deleteAll(list);
            } else {
            	List list = search(hsql,values,types);
            	getHibernateTemplate().deleteAll(list);
            }
                        
            getHibernateTemplate().flush();
            
            
        }catch (HibernateException se) {
            throw new Exception(se.getMessage());
        }catch (Exception ex) {
            throw new RuntimeException(
                    "UnChecked Exception occur when removing a record: " +
                    ex.getMessage());
        } 
    }

    /**
     * 查询一组符合条件的记录
     *
     * @param hsql String   HSQL语句
     * @param value Object  参数值
     * @param type Type     参数Hibernate类型
     * @return List         一组对象
     * @throws Exception
     */
    public List search(String hsql, Object value, Type type) throws
            Exception {
        Object[] values = {value};
        Type[] types = {type};

        return search(hsql, values, types);
    }

    /**
     * 查询一组符合条件的记录
     *
     * @param hsql String      HSQL语句
     * @param values Object[]  参数值数组
     * @param types Type[]     参数Hibernate类型数组
     * @return List             一组结果对象
     * @throws Exception
     */
    public List search(String hsql, Object[] values, Type[] types) throws
            Exception {

        try {
            if (values == null || types == null) {
                return getHibernateTemplate().find(hsql);
            }
            
            return getHibernateTemplate().find(hsql,values);
//            
//            return getHibernateTemplate().find(hsql, values, types);
//            return null;
        } catch (Exception ex) {
            throw ex;
        } 
    }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
    /**
     * 根据example查询
     * @param hsql 查询语句
     * @param o example
     * @return
     * @throws Exception
     */
    public List searchByExample(String hsql, Object o) throws Exception {

		try {
		    if (o == null) {
		        return getHibernateTemplate().find(hsql);
		    }
		    
		    return getHibernateTemplate().findByExample(o);
		} catch (Exception ex) {
		    throw ex;
		} 
    }
    
    
    /**
     * 查询一组符合条件的记录
     * @param hsql HSQL语句
     * @param parameterNames 参数名
     * @param values 参数值数组
     * @return 一组结果对象
     * @throws Exception
     */
    public List search(String hsql, String[] parameterNames, Object[] values) throws
            Exception {

        try {
            if (values == null  || parameterNames == null) {
                return getHibernateTemplate().find(hsql);
            }
            
            return getHibernateTemplate().findByNamedParam(hsql,parameterNames,values);

        } catch (Exception ex) {
            throw ex;
        } 
    }
    
    /**
     * 查询一组符合条件的记录(hibernate3)
     *
     * @param hsql String   HSQL语句
     * @param value Object  参数值
     * @return List         一组对象
     * @throws Exception
     */
    public List search(String hsql, Object value) throws
            Exception {
        Object[] values = {value};

        return search(hsql, values);
    }

    /**
     * 查询一组符合条件的记录(hibernate3)
     *
     * @param hsql String      HSQL语句
     * @param values Object[]  参数值数组
     * @return List             一组结果对象
     * @throws Exception
     */
    public List search(String hsql, Object[] values) throws
            Exception {

        try {
            if (values == null ) {
                return getHibernateTemplate().find(hsql);
            }
            
            return getHibernateTemplate().find(hsql,values);

        } catch (Exception ex) {
            throw ex;
        } 
    }

    /**
     * 查询一组记录
     *
     * @param hsql String      HSQL语句
     * @param values Object[]  参数值数组
     * @param types Type[]     参数Hibernate类型数组
     * @return List             一组结果对象
     * @throws Exception
     */
    public List search(String hsql) throws
            Exception {
       
        try {
            return getHibernateTemplate().find(hsql);

        } catch (Exception ex) {
            throw ex;
        } 
    }

    /**
     * 查询一条特定的记录
     * @param businessClass Class 业务对象的class
     * @param id SerialObject 主键,串行化对象
     * @throws PrimaryKeyNotFoundException 根据提供的主键找不到数据时抛出的异常
     * @return Object 业务对象
     */
    public Object search(Class businessClass, java.io.Serializable id) throws
    		Exception {

        try {
    
            return super.getHibernateTemplate().load(businessClass, id);
            //return session.load(businessClass, id); //记录不存在时,会抛出ObjectNotFoundException
        }catch (Exception ex) {
            throw new RuntimeException(
                    "UnChecked Exception occur when finding a record: " +
                    ex.getMessage());
        } 
    }

    /**
     * 记录返回所有记录
     * @param businessClass Class 业务对象的class
     * @return List 记录集
     */
    public List search(Class businessClass) {
        return getObjects(businessClass);
    }
    /**
     * @see org.appfuse.dao.DAO#saveObject(java.lang.Object)
     */
    public void saveObject(Object o) {
        getHibernateTemplate().saveOrUpdate(o);
    }
    
    /**
     * Generic method to update an object.
     * @param o the object to update
     */
    public void refresh(Object o,LockMode lockMode)
    {

        try {
        	
        	getHibernateTemplate().flush();
        	getHibernateTemplate().refresh(o,lockMode);            
            
        } catch (Exception ex) {            
        	throw new RuntimeException(
                    "UnChecked Exception occur when creating record: " +
                    ex.getMessage());
        } 
    }
    
    /**
     * @see org.appfuse.dao.DAO#updateObject(java.lang.Object)
     */
    public void updateObject(Object o) {
    
        try {
        	
            getHibernateTemplate().update(o);            
            getHibernateTemplate().flush();
            
        } catch (Exception ex) {
            throw new RuntimeException(
                    "UnChecked Exception occur when creating record: " +
                    ex.getMessage());
        } 
    }

    /**
     * @see org.appfuse.dao.DAO#getObject(java.lang.Class, java.io.Serializable)
     */
    public Object getObject(Class clazz, Serializable id) {
        Object o = getHibernateTemplate().get(clazz, id);
        
        if (o == null) {
            throw new ObjectRetrievalFailureException(clazz, id);
        }

        return o;
    }

    /**
     * @see org.appfuse.dao.DAO#getObjects(java.lang.Class)
     */
    public List getObjects(Class clazz) {
        return getHibernateTemplate().loadAll(clazz);
    }

    /**
     * @see org.appfuse.dao.DAO#removeObject(java.lang.Class, java.io.Serializable)
     */
    public void removeObject(Class clazz, Serializable id) {
        getHibernateTemplate().delete(getObject(clazz, id));
    }
}
