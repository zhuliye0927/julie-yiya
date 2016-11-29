package com.bbyiya.api.web.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.dao.UUsersMapper;
import com.bbyiya.model.UUsers;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.utils.RedisUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.web.base.SSOController;

@Controller
@RequestMapping(value = "/")
public class TestController extends SSOController{
	@Autowired
	private UUsersMapper userMapper;
	
	
	@RequestMapping(value = "/login")
	public String login(Model model,String userno) throws Exception {
		String key="userid_"+userno;
		UUsers user=(UUsers)RedisUtil.getObject(key);// 
		if(user==null){
			user=userMapper.getUUsersByUserID(ObjectUtil.parseLong(userno));
			if(user!=null){
				RedisUtil.setObject(key, user,120);	
			}
		}else {
			model.addAttribute("isok", "ok");
		}
		return "login";
	}
	
	@RequestMapping(value = "/register")
	public String register(Model model,String aa) throws Exception {
		return "register";
	}
	
	
	
}