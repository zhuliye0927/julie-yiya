package com.bbyiya.pic.web;

import java.net.URLEncoder;
import java.util.Date;


import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bbyiya.baseUtils.CookieUtils;
import com.bbyiya.common.vo.wechatmsg.WxUserInfo;
import com.bbyiya.dao.EErrorsMapper;
import com.bbyiya.dao.ULoginlogsMapper;
import com.bbyiya.dao.UOtherloginMapper;
import com.bbyiya.enums.LoginTypeEnum;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.model.EErrors;
import com.bbyiya.model.ULoginlogs;
import com.bbyiya.pic.service.IPic_UserMgtService;
import com.bbyiya.pic.vo.LoginTempVo;
import com.bbyiya.service.IUserLoginService;
import com.bbyiya.utils.ConfigUtil;
import com.bbyiya.utils.HttpRequestHelper;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.utils.RedisUtil;
import com.bbyiya.utils.WechatUtils;
import com.bbyiya.utils.encrypt.UrlEncodeUtils;
import com.bbyiya.utils.pay.WxPayConfig;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.vo.user.OtherLoginParam;
import com.bbyiya.web.base.SSOController;

@Controller
@RequestMapping(value = "/login")
public class LoginController extends SSOController {
	/**
	 * 登陆、注册 service
	 */  
	@Resource(name = "userLoginService")
	private IUserLoginService loginBaseService; 
	/**
	 * 登陆、注册 service
	 */
	@Resource(name = "pic_userMgtService")
	private IPic_UserMgtService loginService;

	@Autowired
	private ULoginlogsMapper loginLogMapper;

	@Autowired
	private EErrorsMapper errorMapper;
	@Autowired
	private UOtherloginMapper otherloginMapper;
	private static String LOGIN_TEMP="LoginTemp";
	
	/**
	 * 登录 中转页
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/transfer")
	public String transferPage(String m,String uid,String redirct_url) throws Exception {
		//是否测试地址
		int loginTo=ObjectUtil.parseInt(m);
		String keyId= LOGIN_TEMP+request.getSession().getId();
		long branch_userid=ObjectUtil.parseLong(uid);
		if(branch_userid>0||!ObjectUtil.isEmpty(m)||!ObjectUtil.isEmpty(redirct_url)){  
			LoginTempVo loginTemp=new LoginTempVo();
			loginTemp.setUpUserId(branch_userid);
			loginTemp.setLoginTo(loginTo);
			if(!ObjectUtil.isEmpty(redirct_url)&&!"null".equals(redirct_url)){
				redirct_url=UrlEncodeUtils.urlDecode(redirct_url, "");
				loginTemp.setRedirect_url(redirct_url); 
				if(branch_userid<=0){
					Map<String, String> paramUrl=ObjectUtil.getUrlParam(redirct_url);
					if(paramUrl!=null&&!ObjectUtil.isEmpty(paramUrl.get("uid"))){
						loginTemp.setUpUserId(ObjectUtil.parseLong(paramUrl.get("uid")));  
					}
				}
			}
			RedisUtil.setObject(keyId, loginTemp, 30);
		}else {
			if(!ObjectUtil.isEmpty(RedisUtil.getObject(keyId))){
				RedisUtil.delete(keyId);
			}
		}
		return "redirect:https://open.weixin.qq.com/connect/oauth2/authorize?appid="+WxPayConfig.APPID+"&redirect_uri=https%3A%2F%2Fmpic.bbyiya.com%2Flogin%2FwxLogin&response_type=code&scope=snsapi_base#wechat_redirect" ;		
	}
	
	/**
	 * c端测试版本登陆入口
	 * @param headImg
	 * @param loginType
	 * @param nickName
	 * @param openId
	 * @param upUid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/wxLoginTest")
	public String wxLoginTest(String headImg, @RequestParam(required = false, defaultValue = "2") int loginType, String nickName, String openId,String upUid,String redirect_url) throws Exception {
		headImg = UrlEncodeUtils.urlDecode(headImg, "");
		nickName = UrlEncodeUtils.urlDecode(nickName, "");
		openId = UrlEncodeUtils.urlDecode(openId, "");
		if (!ObjectUtil.validSqlStr(headImg) || !ObjectUtil.validSqlStr(nickName) || !ObjectUtil.validSqlStr(openId)) {
			ReturnModel rqModel = new ReturnModel();
			rqModel.setStatu(ReturnStatus.ParamError_2);
			rqModel.setStatusreson("参数有非法字符");
			return JsonUtil.objectToJsonStr(rqModel);
		}
		OtherLoginParam param = new OtherLoginParam();
		param.setOpenId(openId);
		param.setLoginType(loginType);
		if(!ObjectUtil.isEmpty(nickName)){
			nickName=ObjectUtil.filterEmojiNew(nickName);
			param.setNickName(nickName); 
			addlog("nickname:"+nickName); 
		}
		param.setHeadImg(headImg);
		if(!ObjectUtil.isEmpty(upUid)){
			param.setUpUserId(ObjectUtil.parseLong(upUid)); 
		} 
		
		ReturnModel rqModel = loginService.otherLogin(param);
		if (ReturnStatus.Success.equals(rqModel.getStatu()) && !ObjectUtil.isEmpty(rqModel.getBasemodle())) {
			addLoginLogAndCookie(rqModel.getBasemodle(),0);
		}
		if(!ObjectUtil.isEmpty(redirect_url)&&!"null".equals(redirect_url)){
			redirect_url=UrlEncodeUtils.urlDecode(redirect_url, "");
			addlog("url:"+ConfigUtil.getSingleValue("currentDomain")+redirect_url); 
			if(redirect_url.contains("http")){
				return "redirect:" + redirect_url;
			}else {
				return "redirect:" +ConfigUtil.getSingleValue("currentDomain")+redirect_url;
			}
		}
		return "redirect:" + ConfigUtil.getSingleValue("photo-net-url") ;
	}
	
	

	/**
	 * 微信登录
	 * 
	 * @param code
	 * @return
	 * @throws Exception
	 */
//	@RequestMapping(value = "/wxLogin")
//	public String wxLogin(String code, String state) throws Exception {
//		if (ObjectUtil.isEmpty(code)) {
//			code = request.getParameter("code");
//		}
//		String urlGetAccess = "https://api.weixin.qq.com/sns/oauth2/access_token";
//		String paramAccess = "appid=" + WxPayConfig.APPID + "&secret=" + WxPayConfig.AppSecret + "&code=" + code + "&grant_type=authorization_code";
//		String resultAccessJson = HttpRequestHelper.sendPost(urlGetAccess, paramAccess);
//
//		JSONObject model = JSONObject.fromObject(resultAccessJson);
//		ReturnModel rqModel = new ReturnModel();
//		//中转信息
//		LoginTempVo logintemp= (LoginTempVo)RedisUtil.getObject(LOGIN_TEMP+request.getSession().getId());
//		if (model != null) {
//			if (!ObjectUtil.isEmpty(model.get("openid")) ) {
//				String openid = model.getString("openid");
//				/*------------获取用户信息----------------------------------------------------*/
//				String userInfoUrl = "https://api.weixin.qq.com/cgi-bin/user/info";
//				String data2 = "access_token=" + WxPublicUtils.getAccessToken() + "&openid=" + openid;
//				String userInfoJson = HttpRequestHelper.sendPost(userInfoUrl, data2);
//				JSONObject userJson = JSONObject.fromObject(userInfoJson);
//				if (userInfoJson != null&&userJson!=null) {
//					OtherLoginParam param = new OtherLoginParam();
//					param.setOpenId(openid);
//					param.setLoginType(Integer.parseInt(LoginTypeEnum.weixin.toString()));
//					String nickName=String.valueOf(userJson.get("nickname"));
//					String headimg=String.valueOf(userJson.get("headimgurl"));
////					//没有用户信息
//					if((ObjectUtil.isEmpty(nickName)||"null".equals(nickName))&&(ObjectUtil.isEmpty(headimg)||"null".equals(headimg))){
//						return "redirect:https://open.weixin.qq.com/connect/oauth2/authorize?appid="+WxPayConfig.APPID+"&redirect_uri=https%3A%2F%2Fmpic.bbyiya.com%2Flogin%2FwxLoginInfo&response_type=code&scope=snsapi_userinfo#wechat_redirect" ;		
//					}
//					if(!ObjectUtil.isEmpty(nickName)&&!"null".equals(nickName)){
//						nickName=ObjectUtil.filterEmojiNew(nickName);
//						param.setNickName(nickName);
//					}
//					if(!ObjectUtil.isEmpty(headimg)&&!"null".equals(headimg)){
//						param.setHeadImg(headimg);
//					}
//					if(logintemp!=null){
//						if(logintemp.getUpUserId()!=null&&logintemp.getUpUserId()>0){
//							param.setUpUserId(logintemp.getUpUserId()); 
//						}
//						int m=logintemp.getLoginTo()==null?0:logintemp.getLoginTo();
//						if(m==1){ //photo测试地址
//							String paramtest="?headImg="+param.getHeadImg()+"&loginType="+param.getLoginType()+"&nickName="+ URLEncoder.encode(param.getNickName(),"utf-8")+"&openId="+param.getOpenId()+"&upUid="+param.getUpUserId();
//							if(!ObjectUtil.isEmpty(logintemp.getRedirect_url())&&!"null".equals(logintemp.getRedirect_url())){
//								paramtest+="&redirect_url="+URLEncoder.encode(logintemp.getRedirect_url(), "gb2312"); 
//							}else if (!ObjectUtil.isEmpty(logintemp.getUpUserId())) {//测试店铺页
//								paramtest+="&redirect_url="+URLEncoder.encode(ConfigUtil.getSingleValue("photo-net-url")+"?uid="+logintemp.getUpUserId(), "gb2312"); 
//							}
//							//跳转mpic测试接口地址中转
//							return "redirect:" + ConfigUtil.getSingleValue("mpic-net-url")+paramtest;
//						}
//					}
//					rqModel = loginService.otherLogin(param);
//					if (ReturnStatus.Success.equals(rqModel.getStatu()) && !ObjectUtil.isEmpty(rqModel.getBasemodle())) {
//						addLoginLogAndCookie(rqModel.getBasemodle(),Integer.parseInt(LoginTypeEnum.weixin.toString())); 
//					}else {
//						addlog("LoginError:" +rqModel.getBasemodle()+"param:"+JsonUtil.objectToJsonStr(param) );
//					}
//					
//				} else {
//					rqModel.setStatu(ReturnStatus.SystemError);
//					rqModel.setStatusreson("获取用户信息失败");
//					addlog(resultAccessJson); 	
//				}
//			} else {
//				rqModel.setStatu(ReturnStatus.ParamError);
//				rqModel.setStatusreson(String.valueOf(model.get("errmsg")));
//				addlog(resultAccessJson); 
//			}
//		} else {
//			rqModel.setStatu(ReturnStatus.SystemError);
//			rqModel.setStatusreson("获取微信登录权限失败");
//			addlog(resultAccessJson); 
//		}
//		if (rqModel.getStatu().equals(ReturnStatus.Success)) {
//			//用户跳转
//			if(logintemp!=null&&!ObjectUtil.isEmpty(logintemp.getRedirect_url())){
//				if(logintemp.getRedirect_url().contains("http")){
//					return "redirect:"+logintemp.getRedirect_url(); 
//				}else {
//					return "redirect:" +ConfigUtil.getSingleValue("currentDomain")+logintemp.getRedirect_url(); 	
//				}
//			}else if (logintemp!=null&&!ObjectUtil.isEmpty(logintemp.getUpUserId())) {//店铺页
//				return "redirect:" +ConfigUtil.getSingleValue("currentDomain")+"?uid="+logintemp.getUpUserId(); 
//			} 
//			return "redirect:" + ConfigUtil.getSingleValue("loginbackurl") ;
//		} else {
//			return "redirect:" + ConfigUtil.getSingleValue("loginbackurl") ;
//		}
//	}
	
	
	/**
	 * 17-11-01
	 * max
	 * @param code
	 * @param state
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/wxLogin")
	public String wxLogin(String code, String state) throws Exception {
		if (ObjectUtil.isEmpty(code)) {
			code = request.getParameter("code");
		}
		//获取微信授权
		String urlGetAccess = "https://api.weixin.qq.com/sns/oauth2/access_token";
		String paramAccess = "appid=" + WxPayConfig.APPID + "&secret=" + WxPayConfig.AppSecret + "&code=" + code + "&grant_type=authorization_code";
		String resultAccessJson = HttpRequestHelper.sendPost(urlGetAccess, paramAccess);

		JSONObject model = JSONObject.fromObject(resultAccessJson);
		ReturnModel rqModel = new ReturnModel();
		//中转信息
		LoginTempVo logintemp= (LoginTempVo)RedisUtil.getObject(LOGIN_TEMP+request.getSession().getId());
		if (model != null) {
			if (!ObjectUtil.isEmpty(model.get("openid")) ) {
				String openid = model.getString("openid");
				OtherLoginParam loginparam = new OtherLoginParam();
				loginparam.setOpenId(openid);
				loginparam.setLoginType(Integer.parseInt(LoginTypeEnum.weixin.toString()));
				/*------------获取用户信息----------------------------------------------------*/
				//获取 用户微信信息
				WxUserInfo userInfo= WechatUtils.getUserInfo(openid);
				if(userInfo!=null&&userInfo.getSubscribe()==1){
					 loginparam.setNickName(userInfo.getNickname());
					 loginparam.setHeadImg(userInfo.getHeadimgurl()); 
				}else {
					return "redirect:https://open.weixin.qq.com/connect/oauth2/authorize?appid="+WxPayConfig.APPID+"&redirect_uri=https%3A%2F%2Fmpic.bbyiya.com%2Flogin%2FwxLoginInfo&response_type=code&scope=snsapi_userinfo#wechat_redirect" ;		
				}
				if (logintemp != null) {
					if (logintemp.getUpUserId() != null && logintemp.getUpUserId() > 0) {
						loginparam.setUpUserId(logintemp.getUpUserId());
					}
					int m = logintemp.getLoginTo() == null ? 0 : logintemp.getLoginTo();
					if (m == 1) { // photo测试地址
						String paramtest = "?headImg=" + loginparam.getHeadImg() + "&loginType=" + loginparam.getLoginType() + "&nickName=" + UrlEncodeUtils.urlEncode(loginparam.getNickName(), "") + "&openId=" + loginparam.getOpenId() + "&upUid=" + loginparam.getUpUserId();
						if (!ObjectUtil.isEmpty(logintemp.getRedirect_url()) && !"null".equals(logintemp.getRedirect_url())) {
							paramtest += "&redirect_url=" + UrlEncodeUtils.urlEncode(logintemp.getRedirect_url(), "");
						} else if (!ObjectUtil.isEmpty(logintemp.getUpUserId())) {// 测试店铺页
							paramtest += "&redirect_url=" + UrlEncodeUtils.urlEncode(ConfigUtil.getSingleValue("photo-net-url") + "?uid=" + logintemp.getUpUserId(), "");
						}
						// 跳转mpic测试接口地址中转
						return "redirect:" + ConfigUtil.getSingleValue("mpic-net-url") + paramtest;
					}
				}
				rqModel = loginService.otherLogin(loginparam);
				if (ReturnStatus.Success.equals(rqModel.getStatu()) && !ObjectUtil.isEmpty(rqModel.getBasemodle())) {
					addLoginLogAndCookie(rqModel.getBasemodle(), Integer.parseInt(LoginTypeEnum.weixin.toString()));
				} else {
					addlog("LoginError:" + rqModel.getBasemodle() + "param:" + JsonUtil.objectToJsonStr(loginparam));
				}
				
			} else {
				rqModel.setStatu(ReturnStatus.ParamError);
				rqModel.setStatusreson(String.valueOf(model.get("errmsg")));
				addlog(resultAccessJson); 
			}
		} else {
			rqModel.setStatu(ReturnStatus.SystemError);
			rqModel.setStatusreson("获取微信登录权限失败");
			addlog(resultAccessJson); 
		}
		if (rqModel.getStatu().equals(ReturnStatus.Success)) {
			//用户跳转
			if(logintemp!=null&&!ObjectUtil.isEmpty(logintemp.getRedirect_url())){
				if(logintemp.getRedirect_url().contains("http")){
					return "redirect:"+logintemp.getRedirect_url(); 
				}else {
					return "redirect:" +ConfigUtil.getSingleValue("currentDomain")+logintemp.getRedirect_url(); 	
				}
			}else if (logintemp!=null&&!ObjectUtil.isEmpty(logintemp.getUpUserId())) {//店铺页
				return "redirect:" +ConfigUtil.getSingleValue("currentDomain")+"?uid="+logintemp.getUpUserId(); 
			} 
			return "redirect:" + ConfigUtil.getSingleValue("loginbackurl") ;
		} else {
			return "redirect:" + ConfigUtil.getSingleValue("loginbackurl") ;
		}
	}
	
	
	/**
	 * 需要授权登录（未关注公众号时）
	 * @param code
	 * @param state
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/wxLoginInfo")
	public String wxLoginInfo(String code, String state) throws Exception {
		if (ObjectUtil.isEmpty(code)) {
			code = request.getParameter("code");
		}
		String urlString = "https://api.weixin.qq.com/sns/oauth2/access_token";
		String dataString = "appid=" + WxPayConfig.APPID + "&secret=" + WxPayConfig.AppSecret + "&code=" + code + "&grant_type=authorization_code";
		String result = HttpRequestHelper.sendPost(urlString, dataString);

		JSONObject model = JSONObject.fromObject(result);
		ReturnModel rqModel = new ReturnModel();
		//中转信息
		LoginTempVo logintemp= (LoginTempVo)RedisUtil.getObject(LOGIN_TEMP+request.getSession().getId());
		if (model != null&&!ObjectUtil.isEmpty(model.get("openid"))&&!ObjectUtil.isEmpty(model.get("access_token"))) {
			String openid = model.getString("openid");
			String access_token =model.getString("access_token");
			if (true) {
				String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo"; 
				String dataParam = "access_token=" + access_token + "&openid=" + openid;
				String userInfoJson = HttpRequestHelper.sendPost(userInfoUrl, dataParam);
				/*------------获取用户信息----------------------------------------------------*/
				JSONObject userJson = JSONObject.fromObject(userInfoJson);
				if (userInfoJson != null) {
					OtherLoginParam param = new OtherLoginParam();
					param.setOpenId(openid);
					param.setLoginType(Integer.parseInt(LoginTypeEnum.weixin.toString()));
					if(!ObjectUtil.isEmpty(userJson.get("nickname"))){
						String nickName=ObjectUtil.filterEmojiNew(userJson.getString("nickname"));
						param.setNickName(nickName);
					}
					if(!ObjectUtil.isEmpty(userJson.get("headimgurl"))){
						param.setHeadImg(ObjectUtil.filterEmojiNew(userJson.getString("headimgurl")));
					}
					if(logintemp!=null){
						if(logintemp.getUpUserId()!=null&&logintemp.getUpUserId()>0){
							param.setUpUserId(logintemp.getUpUserId()); 
						}
						int m=logintemp.getLoginTo()==null?0:logintemp.getLoginTo();
						if(m==1){ //photo测试地址
							String paramtest="?headImg="+param.getHeadImg()+"&loginType="+param.getLoginType()+"&nickName="+ URLEncoder.encode(param.getNickName(),"utf-8")+"&openId="+param.getOpenId()+"&upUid="+param.getUpUserId();
							if(!ObjectUtil.isEmpty(logintemp.getRedirect_url())&&!"null".equals(logintemp.getRedirect_url())){
								paramtest+="&redirect_url="+URLEncoder.encode(logintemp.getRedirect_url(), "gb2312"); 
							}
							//跳转mpic测试接口地址中转
							return "redirect:" + ConfigUtil.getSingleValue("mpic-net-url")+paramtest;
						}
					}
					rqModel = loginService.otherLogin(param);
					if (ReturnStatus.Success.equals(rqModel.getStatu()) && !ObjectUtil.isEmpty(rqModel.getBasemodle())) {
						addLoginLogAndCookie(rqModel.getBasemodle(),Integer.parseInt(LoginTypeEnum.weixin.toString())); 
					}else {
						addlog("LoginError:" +rqModel.getBasemodle()+"param:"+JsonUtil.objectToJsonStr(param) ); 
					}
				} else {
					rqModel.setStatu(ReturnStatus.SystemError);
					rqModel.setStatusreson("获取用户信息失败");
				}
			} 
		} else {
			rqModel.setStatu(ReturnStatus.SystemError);
			rqModel.setStatusreson("获取微信登录权限失败");
		}
		if (rqModel.getStatu().equals(ReturnStatus.Success)) {
			//用户跳转
			if(logintemp!=null&&!ObjectUtil.isEmpty(logintemp.getRedirect_url())){
				if(logintemp.getRedirect_url().contains("http")){
					return "redirect:" + logintemp.getRedirect_url();
				}else {
					return "redirect:" +ConfigUtil.getSingleValue("currentDomain")+logintemp.getRedirect_url();
				}
			}else if (logintemp!=null&&!ObjectUtil.isEmpty(logintemp.getUpUserId())) {//店铺页
				return "redirect:" +ConfigUtil.getSingleValue("currentDomain")+"?uid="+logintemp.getUpUserId(); 
			} 
			return "redirect:" + ConfigUtil.getSingleValue("loginbackurl") ;
		} else {
			return "redirect:" + ConfigUtil.getSingleValue("loginbackurl") ;
		}
	}
	



	/**
	 * 登录日志
	 * @param obj
	 * @param type
	 */
	private void addLoginLogAndCookie(Object obj,int type) {
		try {
			LoginSuccessResult user = (LoginSuccessResult) obj;
			if (user != null) {
				ULoginlogs loginLogs = new ULoginlogs();
				loginLogs.setUserid(user.getUserId());
				loginLogs.setLogintime(new Date());
				loginLogs.setLogintype(type);
				loginLogs.setIpstr(super.getIpStr());
				loginLogs.setNickname(user.getNickName()); 
				loginLogs.setSourcetype(1);
				loginLogMapper.insert(loginLogs);
//				CookieUtils.addCookieBySessionId(request, response,user.getTicket(),86400); 
				CookieUtils.addCookie_web(request, response, user.getTicket(),86400);
			}
		} catch (Exception e) {
			// TODO: handle exception
			addlog("e3:"+e.getMessage());
		}
	}
	
	/**
	 * 插入错误Log
	 * @param msg
	 */
	public void addlog(String msg) {
		try {
			EErrors errors = new EErrors();
			errors.setClassname(this.getClass().getName());
			errors.setMsg(msg);
			errors.setCreatetime(new Date()); 
			errorMapper.insert(errors);
		} catch (Exception e) {
		}
	}
}
