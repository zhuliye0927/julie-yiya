package com.bbyiya.pic.web;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.service.pic.IBaseProductService;
import com.bbyiya.utils.ConfigUtil;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.RedisUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.web.base.SSOController;

@Controller
@RequestMapping(value = "/product")
public class ProductController extends SSOController {
	@Resource(name="baseProductServiceImpl")
	private IBaseProductService productService;
	/**
	 * P01 �����б�
	 * @param type
	 * @param code
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/scenelist")
	public String area(@RequestParam(required = false, defaultValue = "0") int type, String code) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			rq.setStatu(ReturnStatus.Success);
			rq.setBasemodle(ConfigUtil.getMaplist("scenelist")); 
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	
	/**
	 * P02 ����б�
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/productlist")
	public String product(@RequestParam(required = false, defaultValue = "0") long uid) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			rq.setStatu(ReturnStatus.Success);
			rq.setBasemodle(productService.findProductList(uid));   
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	/**
	 * P03 ��Ʒ����/�������
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/pro")
	public String productlist(@RequestParam(required = false, defaultValue = "0") long pid) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			rq.setStatu(ReturnStatus.Success);
			rq.setBasemodle(productService.getProductResult(pid));   
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	/**
	 * ��ȡ����Ψһ��
	 * @param pid
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getworkId")
	public String getworkId(@RequestParam(required = false, defaultValue = "0") long pid) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null&&user.getUserId()!=null&&user.getUserId()>0){
			rq.setStatu(ReturnStatus.Success);
			rq.setBasemodle(getIndex(user.getUserId(),pid));   
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	
	/**
	 * ��ȡ����Ψһ��
	 * @param userId
	 * @param productId
	 * @return
	 */
	public String getIndex(Long userId,Long productId){
		int temp=1000;
		long index=userId%temp;
		String key="user_work_index_"+index;
		Map<Long, Map<Long, Integer>> map= (Map<Long, Map<Long, Integer>>)RedisUtil.getObject(key);
		int val=1;
		if(map!=null){
			if(map.containsKey(userId)){
			 	Map<Long, Integer> userMap= map.get(userId);
			 	if(userMap.containsKey(productId)){
			 		val=userMap.get(productId)+1;
			 		userMap.put(productId, val);
			 	}else {
					userMap.put(productId, val);
				}
			 	map.put(userId, userMap);
			}else {
				Map<Long, Integer> userMap= new HashMap<Long, Integer>();
				userMap.put(productId, val);
				map.put(userId, userMap);
			}
		}else {
			map=new HashMap<Long, Map<Long,Integer>>();
			Map<Long, Integer> userMap= new HashMap<Long, Integer>();
			userMap.put(productId, val);
			map.put(userId, userMap);
		}
		RedisUtil.setObject(key, map); 
		return userId+"-"+productId+"-"+val;
	}
}