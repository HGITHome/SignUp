package com.dgut.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dgut.app.pck.Encrypt;
import com.dgut.app.pck.JSONUtils;
import com.dgut.common.security.BadCredentialsException;
import com.dgut.common.security.DisabledException;
import com.dgut.common.security.UsernameNotFoundException;
import com.dgut.common.sns.spi.SmsWebApiKit;
import com.dgut.common.util.DateUtils;
import com.dgut.common.util.StrUtils;
import com.dgut.common.web.RequestUtils;
import com.dgut.main.Constants;
import com.dgut.main.entity.City;
import com.dgut.main.manager.CityMng;
import com.dgut.main.web.CmsUtils;
import com.dgut.member.entity.Member;
import com.dgut.member.entity.MemberAuthentication;
import com.dgut.member.manager.MemberAuthenticationMng;
import com.dgut.member.manager.MemberLogMng;
import com.dgut.member.manager.MemberMng;
import com.dgut.member.manager.impl.MemberAuthenticationMngImpl;

@Service
public class RequestData {

	/**
	 * 注册 opt = 1
	 * @throws IOException
	 */
	public String regist(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> parameters)
			throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();

		String mobile = parameters.get("mobile");
		String password = parameters.get("pwd");
		String code = parameters.get("code");
		if (StringUtils.isBlank(password)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "密码不能为空");
			return JSONUtils.printObject(jsonMap);
		}

		password = Encrypt.decrypt3DES(password, Constants.ENCRYPTION_KEY);
		
		Map<String, String> m = RequestData.codeVerify(mobile, code);
		if (m.get("error").equals("-3")) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", m.get("msg"));
			return JSONUtils.printObject(jsonMap);
		}
		
		boolean notExist = memberMng.usernameNotExist(mobile);
		if(notExist==false){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该手机号码已注册");
			return JSONUtils.printObject(jsonMap);
		}
		String ip = RequestUtils.getIpAddr(request);
		Member member = memberMng.saveMember(mobile, password, ip, null	, null);
		jsonMap.put("error", -1);
		jsonMap.put("msg", "注册成功");
		jsonMap.put("phone", member.getUsername());
		jsonMap.put("realname", member.getRealname());
		jsonMap.put("uid", Encrypt.encrypt3DES(member.getId() + "",
				Constants.ENCRYPTION_KEY));
		return JSONUtils.printObject(jsonMap);
	}
	/**
	 * 手机验证码验证
	 * @return
	 */
	public static Map<String, String> codeVerify(String mobile,String code)
	{
		Map<String, String> jsonMap = new HashMap<String, String>();
		if (!StrUtils.isMobileNum(mobile)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "手机号码错误");
			return jsonMap;
		}
		if (StringUtils.isBlank(code)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "验证码不能为空");
			return jsonMap;
		}
		try {
			String c = SmsWebApiKit.checkcode(mobile, "86", code);
			if (c.equals("200")) {
				jsonMap.put("error", "-1");
				jsonMap.put("msg", "验证成功");
			}else if(c.equals("468")){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "验证码错误");
			}else if(c.equals("467")){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "请求校验验证码频繁");
			}else {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "code:"+c);
			}
		} catch (Exception e) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请求失败"+e.getLocalizedMessage());
		}
		
		return jsonMap;
	}
	/**
	 * 用户登录(opt=2) 
	 * 登录成功后返回的token时长是2天，也就是2天内没操作需要重新登录
	 * 如果在2天内的时候登录的话，token时长重新计算
	 * 
	 * @param mobile
	 *            用户名
	 * @param pwd
	 *            密码
	 * @throws IOException
	 */
	public String login(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> parameters)
			throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		String mobile = parameters.get("mobile");
		String password = parameters.get("pwd");

		if (StringUtils.isBlank(mobile)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入手机号码");
			return JSONUtils.printObject(jsonMap);
		}
		if(!StrUtils.isMobileNum(mobile)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入正确的手机号码");
			return JSONUtils.printObject(jsonMap);
		}

		if (StringUtils.isBlank(password)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入密码");
			return JSONUtils.printObject(jsonMap);
		}

		password = Encrypt.decrypt3DES(password, Constants.ENCRYPTION_KEY);

		String ip = RequestUtils.getIpAddr(request);
		Member member;
		MemberAuthentication auth;
		try {
			auth = memberAuthenMng
					.login(mobile, password, ip, request, response, session);
			memberMng.updateLoginInfo(auth.getUid(), ip);
			member = memberMng.findById(auth.getUid());
			if (member.getDisabled()) {
				// 如果已经禁用，则退出登录。
				memberAuthenMng.deleteById(auth.getId());
				session.logout(request, response);
				throw new DisabledException("user disabled");
			}
			memberLogMng.loginSuccess(request, member, "登录成功");
		} catch (UsernameNotFoundException e) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "用户名不存在");
			return JSONUtils.printObject(jsonMap);
		} catch (BadCredentialsException e) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "用户名或密码错误");
			return JSONUtils.printObject(jsonMap);
		} catch (DisabledException e) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "账户被禁用，请联系管理员");
			return JSONUtils.printObject(jsonMap);
		}
		jsonMap.put("error", -1);
		jsonMap.put("msg", "登录成功");
		jsonMap.put("phone", member.getUsername());
		jsonMap.put("realname", member.getRealname());
		jsonMap.put("uid", Encrypt.encrypt3DES(member.getId() + "",
				Constants.ENCRYPTION_KEY));
		jsonMap.put("loginCount", member.getLoginCount());
		jsonMap.put("lastLoginIp", member.getLastLoginIp());
		jsonMap.put("lastLoginTime",
				DateUtils.dateToString(member.getLastLoginTime()));
		jsonMap.put("token", auth.getId());
		jsonMap.put("expires_in", MemberAuthenticationMngImpl.timeout/1000);
		return JSONUtils.printObject(jsonMap);
	}
	/**
	 * 忘记密码 opt = 3
	 */
	public String forgetPwd(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> parameters)
			throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();

		String mobile = parameters.get("mobile");
		String password = parameters.get("pwd");
		String code = parameters.get("code");
		if (StringUtils.isBlank(password)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "密码不能为空");
			return JSONUtils.printObject(jsonMap);
		}

		password = Encrypt.decrypt3DES(password, Constants.ENCRYPTION_KEY);
		
		Map<String, String> m = RequestData.codeVerify(mobile, code);
		if (m.get("error").equals("-3")) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", m.get("msg"));
			return JSONUtils.printObject(jsonMap);
		}
		
		Member member = memberMng.findByUsername(mobile);
		if(member==null){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "手机号码未注册");
			return JSONUtils.printObject(jsonMap);
		}
		member = memberMng.updateMember(member, password);
		jsonMap.put("error", -1);
		jsonMap.put("msg", "更新密码成功");
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 获取城市列表
	 */
	public String citys(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> parameters)
			throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		List<City> list = cityMng.getList();
		jsonMap.put("error", -1);
		jsonMap.put("msg", "请求成功");
		jsonMap.put("citys",list);
		System.out.println("member:"+CmsUtils.getMember(request));
		return JSONUtils.printObject(jsonMap);
	}
	
	public String test(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> parameters)
			throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("error", -1);
		jsonMap.put("msg", "请求成功");
		System.out.println("member:"+CmsUtils.getMember(request));
		return JSONUtils.printObject(jsonMap);
	}
	

	
	@Autowired
	private MemberAuthenticationMng memberAuthenMng;

	@Autowired
	private com.dgut.common.web.session.SessionProvider session;
	
	@Autowired
	private MemberMng memberMng;
	
	@Autowired
	private MemberLogMng memberLogMng;
	
	@Autowired
	private CityMng cityMng;

}
