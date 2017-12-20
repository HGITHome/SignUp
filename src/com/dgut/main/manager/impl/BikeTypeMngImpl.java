package com.dgut.main.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dgut.common.hibernate3.Updater;
import com.dgut.main.dao.BikeTypeDao;
import com.dgut.main.entity.BikeType;
import com.dgut.main.manager.BikeTypeMng;

@Service
@Transactional
public class BikeTypeMngImpl implements BikeTypeMng {
	@Transactional(readOnly = true)
	public List<BikeType> getList() {
		return dao.getList();
	}

	@Transactional(readOnly = true)
	public BikeType findById(Integer id) {
		BikeType entity = dao.findById(id);
		return entity;
	}

	public BikeType save(BikeType bean) {
		dao.save(bean);
		return bean;
	}

	public BikeType update(BikeType bean) {
		Updater<BikeType> updater = new Updater<BikeType>(bean);
		bean = dao.updateByUpdater(updater);
		return bean;
	}

	public BikeType deleteById(Integer id) {
		BikeType bean = dao.deleteById(id);
		return bean;
	}

	public BikeType[] deleteByIds(Integer[] ids) {
		BikeType[] beans = new BikeType[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = deleteById(ids[i]);
		}
		return beans;
	}

	private BikeTypeDao dao;

	@Autowired
	public void setDao(BikeTypeDao dao) {
		this.dao = dao;
	}
}