package com.bbyiya.pic.web;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.baseUtils.GenUtils;
import com.bbyiya.enums.PayOrderTypeEnum;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.model.OOrderproducts;
import com.bbyiya.model.PPostmodel;
import com.bbyiya.pic.vo.order.SubmitOrderProductParam;
import com.bbyiya.service.pic.IBaseOrderMgtService;
import com.bbyiya.service.pic.IBasePostMgtService;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.order.UserOrderSubmitParam;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.web.base.SSOController;

@Controller
@RequestMapping(value = "/order")
public class SubmitOrderMgtController extends SSOController {
	@Resource(name = "baseOrderMgtServiceImpl")
	private IBaseOrderMgtService orderMgtService;
	/**
	 * 运费
	 */
	@Resource(name = "basePostMgtServiceImpl")
	private IBasePostMgtService postMgtService;

	
	
	@ResponseBody
	@RequestMapping(value = "/findpostlist")
	public String findpostlist(String area,String addressId)throws Exception{
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user = super.getLoginUser();
		if (user != null) {
			long addrid=ObjectUtil.parseLong(addressId);
			if(addrid>0){
				rq=postMgtService.find_postagelist(addrid);
			}else {
				List<PPostmodel> postlist= postMgtService.find_postlist(ObjectUtil.parseInt(area));
				if(postlist!=null){
					rq.setBasemodle(postlist);
					rq.setStatu(ReturnStatus.Success);
				}
			}
		} else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期");
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	
	/**
	 * 提交订单
	 * 
	 * @param addrId
	 * @param orderType
	 * @param remark
	 * @param productJsonStr
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/submitOrderNew")
	public String submitOrder(@RequestParam(required = false, defaultValue = "0") long addrId, String orderType, String remark, String productJsonStr) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user = super.getLoginUser();
		if (user != null) {
			SubmitOrderProductParam productParam = (SubmitOrderProductParam) JsonUtil.jsonStrToObject(productJsonStr, SubmitOrderProductParam.class);
			if (productParam != null) {
				OOrderproducts product = new OOrderproducts();
				product.setProductid(productParam.getProductId());
				product.setStyleid(productParam.getStyleId());
				product.setCount(productParam.getCount());

				// 下单参数
				UserOrderSubmitParam param = new UserOrderSubmitParam();
				param.setUserId(user.getUserId());
				param.setRemark(remark);
				if (addrId > 0) {
					param.setAddrId(addrId);
				}
				if (productParam.getCartId() != null && productParam.getCartId() > 0) {
					param.setCartId(productParam.getCartId());
				}
				int type = ObjectUtil.parseInt(orderType);
				if (type > 0) {
					param.setOrderType(type);
				}
				param.setOrderproducts(product);
				rq = orderMgtService.submitOrder_new(param);
			} else {
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("参数有误");
			}

		} else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期");
		}
		return JsonUtil.objectToJsonStr(rq);
	}

	/**
	 * 获取充值订单号
	 * @param amount
	 * @param type（2：货款充值）
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/recharge")
	public String recharge(@RequestParam(required = false, defaultValue = "0") double amount,@RequestParam(required = false, defaultValue = "2") int type) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user = super.getLoginUser();
		if(user!=null){
			String orderId=GenUtils.getOrderNo(user.getUserId());
			boolean isok=false;
			if(type==Integer.parseInt(PayOrderTypeEnum.chongzhi.toString())){
				isok=orderMgtService.addPayOrder(user.getUserId(), orderId, "", Integer.parseInt(PayOrderTypeEnum.chongzhi.toString()), amount);
			}else if (type==Integer.parseInt(PayOrderTypeEnum.postage.toString())) {
				isok=orderMgtService.addPayOrder(user.getUserId(), orderId, "", Integer.parseInt(PayOrderTypeEnum.postage.toString()), amount);
			} 
			if(isok){
				rq.setStatu(ReturnStatus.Success);
				rq.setBasemodle(orderId); 
			}else {
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("参数有误");
			}
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期");
		}
		return JsonUtil.objectToJsonStr(rq);
	}
}
