package com.dgut.main.manager.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dgut.common.hibernate3.Updater;
import com.dgut.main.dao.RoleDao;
import com.dgut.main.entity.Role;
import com.dgut.main.manager.RoleMng;

@Service
@Transactional
public class RoleMngImpl implements RoleMng {
	@Transactional(readOnly = true)
	public List<Role> getList() {
		return dao.getList();
	}

	@Transactional(readOnly = true)
	public Role findById(Integer id) {
		Role entity = dao.findById(id);
		return entity;
	}

	public Role save(Role bean, Set<String> perms) {
		bean.setPerms(perms);
		dao.save(bean);
		return bean;
	}

	public Role update(Role bean, Set<String> perms) {
		Updater<Role> updater = new Updater<Role>(bean);
		bean = dao.updateByUpdater(updater);
		bean.setPerms(perms);
		return bean;
	}

	public Role deleteById(Integer id) {
		Role bean = dao.deleteById(id);
		return bean;
	}

	public Role[] deleteByIds(Integer[] ids) {
		Role[] beans = new Role[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = deleteById(ids[i]);
		}
		return beans;
	}

	private RoleDao dao;

	@Autowired
	public void setDao(RoleDao dao) {
		this.dao = dao;
	}
}