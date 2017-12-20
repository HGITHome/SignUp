package com.dgut.main.action.main;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dgut.main.entity.BikeType;
import com.dgut.main.manager.AdminLogMng;
import com.dgut.main.manager.BikeTypeMng;
import com.dgut.main.web.WebErrors;

@Controller
public class BikeTypeAct {
	private static final Logger log = LoggerFactory.getLogger(BikeTypeAct.class);

	@RequestMapping("/bikeType/v_list.do")
	public String list(HttpServletRequest request, ModelMap model) {
		List<BikeType> list = manager.getList();
		model.addAttribute("list", list);
		return "bikeType/list";
	}

	@RequestMapping("/bikeType/v_add.do")
	public String add(ModelMap model) {
		return "bikeType/add";
	}

	@RequestMapping("/bikeType/v_edit.do")
	public String edit(Integer id, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		model.addAttribute("bikeType", manager.findById(id));
		return "bikeType/edit";
	}

	@RequestMapping("/bikeType/o_save.do")
	public String save(BikeType bean,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateSave(bean, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.save(bean);
		log.info("save BikeType id={}", bean.getId());
		logMng.operating(request, "车类型保存", "id=" + bean.getId()
				+ ";name=" + bean.getName());
		return "redirect:v_list.do";
	}

	@RequestMapping("/bikeType/o_update.do")
	public String update(BikeType bean,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateUpdate(bean.getId(), request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.update(bean);
		log.info("update BikeType id={}.", bean.getId());
		logMng.operating(request, "车类型更新", "id=" + bean.getId()
				+ ";name=" + bean.getName());
		return list(request, model);
	}

	@RequestMapping("/bikeType/o_delete.do")
	public String delete(Integer[] ids, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		BikeType[] beans = manager.deleteByIds(ids);
		for (BikeType bean : beans) {
			log.info("delete BikeType id={}", bean.getId());
			logMng.operating(request, "删除车类型", "id="
					+ bean.getId() + ";name=" + bean.getName());
		}
		return list(request, model);
	}

	private WebErrors validateSave(BikeType bean, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		return errors;
	}

	private WebErrors validateEdit(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (vldExist(id, errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateUpdate(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (vldExist(id, errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateDelete(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (errors.ifEmpty(ids, "ids")) {
			return errors;
		}
		for (Integer id : ids) {
			vldExist(id, errors);
		}
		return errors;
	}

	private boolean vldExist(Integer id, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		BikeType entity = manager.findById(id);
		if (errors.ifNotExist(entity, BikeType.class, id)) {
			return true;
		}
		return false;
	}

	@Autowired
	private AdminLogMng logMng;
	@Autowired
	private BikeTypeMng manager;
}