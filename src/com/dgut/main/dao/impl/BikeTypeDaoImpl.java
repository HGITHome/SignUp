package com.dgut.main.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dgut.common.hibernate3.HibernateBaseDao;
import com.dgut.main.dao.BikeTypeDao;
import com.dgut.main.entity.BikeType;

@Repository
public class BikeTypeDaoImpl extends HibernateBaseDao<BikeType, Integer>
		implements BikeTypeDao {
	@SuppressWarnings("unchecked")
	public List<BikeType> getList() {
		String hql = "from BikeType bean order by bean.priority asc";
		return find(hql);
	}

	public BikeType findById(Integer id) {
		BikeType entity = get(id);
		return entity;
	}

	public BikeType save(BikeType bean) {
		getSession().save(bean);
		return bean;
	}

	public BikeType deleteById(Integer id) {
		BikeType entity = super.get(id);
		if (entity != null) {
			getSession().delete(entity);
		}
		return entity;
	}

	@Override
	protected Class<BikeType> getEntityClass() {
		return BikeType.class;
	}
}