package com.xinyuan.dao.impl;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.Global.HibernateInitializer;


public class HibernateDAO {
	/**
	 * 保存实体的对象
	 * @param obj 被保存的实体对象
	 * @return 返回保存是否成功，true代表是。
	 * */
	public Serializable saveObject(Object obj){
		return getSession().save(obj);
	}
	
	/**
	 * 修改实体的对象
	 * @param obj 被修改的实体对象
	 * @return 修改是否成功，true代表是.
	  */
	public void updateObject(Object obj){
		getSession().update(obj);
	}
	
	/**
	 * 使用HQL语句来得到实体对象的列表
	 * @param hsql 需要执行的HQL语句
	 * @return 附和条件的实体对象的列表
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getObjects(String hsql){
		return getSession().createQuery(hsql).list();
	}
	
	/**使用HQL来得到对象或者其他类型对象的实例
	 * @param hsql 需要执行的HQL语句
	 * @return HQL语句的执行结果
	 */
	public Object getObject(String hqsl) {
		return getSession().createQuery(hqsl).uniqueResult();
	}
	
	public Object getObject(Class<?> cls,	Serializable id){
		return getSession().get(cls, id);
	}
	
	/**
	 * 删除实体对象
	 * @param id 通过ID要删除的实体对象
	 */
	public void deleteObject(Object obj){
		getSession().delete(obj);
	}
	
	
	/**
	 * Get the object by the column has the unique value
	 * @param cls
	 * @param uniqueColumnName
	 * @param uniqueValue
	 * @return
	 * 
	 */
	public Object getObject(Class<?> cls,	String uniqueColumnName, Serializable uniqueValue ){
		String clsName = cls.getName();
		String alias = clsName.substring(clsName.lastIndexOf(".") + 1);
		String queryString = "FROM " + clsName + " " + alias + " WHERE " + uniqueColumnName + " = " + ":" + uniqueColumnName;
		Query query = createQuery(queryString);
		query.setParameter(uniqueColumnName, uniqueValue);
		return query.uniqueResult();
	}

	
	 public Session getSession(){
		 return HibernateInitializer.getSessionFactory().getCurrentSession();
	 }
	 
	 public Query createQuery(String queryString){
		 return getSession().createQuery(queryString);
	 }

}
