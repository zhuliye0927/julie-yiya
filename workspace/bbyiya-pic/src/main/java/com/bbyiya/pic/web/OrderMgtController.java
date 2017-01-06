package com.bbyiya.pic.web;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.model.OOrderproductdetails;
import com.bbyiya.model.OOrderproducts;
import com.bbyiya.pic.vo.order.OrderPhotoParam;
import com.bbyiya.pic.vo.order.SaveOrderPhotoParam;
import com.bbyiya.pic.vo.order.SubmitOrderProductParam;
import com.bbyiya.service.pic.IBaseOrderMgtService;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.web.base.SSOController;

@Controller
@RequestMapping(value = "/order")
public class OrderMgtController extends SSOController {

	@Resource(name = "baseOrderMgtServiceImpl")
	private IBaseOrderMgtService orderMgtService;

	/**
	 * O01 �ύ����������
	 * 
	 * @param addrId
	 * @param productJsonStr
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/submitOrder")
	public String submitOrder(@RequestParam(required = false, defaultValue = "0") long addrId, String remark, String productJsonStr) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user = super.getLoginUser();
		if (user != null) {
			if (!ObjectUtil.isEmpty(productJsonStr)) {
				SubmitOrderProductParam param = (SubmitOrderProductParam) JsonUtil.jsonStrToObject(productJsonStr, SubmitOrderProductParam.class);
				if (param != null) {
					OOrderproducts product = new OOrderproducts();
					product.setProductid(param.getProductId());
					product.setStyleid(param.getStyleId());
					product.setCount(param.getCount());
					product.setSalesuserid(0l);
					rq = orderMgtService.submitOrder_singleProduct(user.getUserId(), addrId, remark, product);
				}
			}
			rq.setStatu(ReturnStatus.Success);

		} else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}

	/**
	 * O02 ���涩����Ƭ
	 * 
	 * @param orderImagesJson
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/saveProductOrderDetail")
	public String saveProductOrderDetail(String orderImagesJson) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user = super.getLoginUser();
		if (user != null) {
			if (!ObjectUtil.isEmpty(orderImagesJson)) {
				SaveOrderPhotoParam param = (SaveOrderPhotoParam) JsonUtil.jsonStrToObject(orderImagesJson, SaveOrderPhotoParam.class);// (productImagelistJson);
				if (param == null || ObjectUtil.isEmpty(param.getOrderId())) {
					rq.setStatu(ReturnStatus.ParamError_1);
					rq.setStatusreson("������ȫ");
					return JsonUtil.objectToJsonStr(rq);
				}
				// List<OrderPhotoParam> list = param.getImageList();
				if (param.getImageList() != null && param.getImageList().size() > 0) {
					if (param.getImageList().size() < 12) {
						rq.setStatu(ReturnStatus.ParamError_1);
						rq.setStatusreson("ͼƬ����12�ţ�");
						return JsonUtil.objectToJsonStr(rq);
					}
					List<OOrderproductdetails> images = new ArrayList<OOrderproductdetails>();
					for (OrderPhotoParam pp : param.getImageList()) {
						if (ObjectUtil.isEmpty(pp.getImageUrl()) || ObjectUtil.isEmpty(pp.getPrintNo())) {
							rq.setStatu(ReturnStatus.ParamError_1);
							rq.setStatusreson("ͼƬ��Ϣ���󣬴�ӡ�ţ�" + pp.getPrintNo());
							return JsonUtil.objectToJsonStr(rq);
						}
						OOrderproductdetails item = new OOrderproductdetails();
						item.setOrderproductid(param.getOrderId());
						item.setPrintno(pp.getPrintNo());
						String printNo=pp.getPrintNo();
						item.setPosition(ObjectUtil.parseInt(printNo.substring(printNo.lastIndexOf("-")+1, printNo.length())));
						item.setImageurl(pp.getImageUrl()); 
						images.add(item);
					}
					rq = orderMgtService.saveOrderImages(param.getOrderId(), images);
				}
			} else {
				rq.setStatu(ReturnStatus.ParamError_1);
				rq.setStatusreson("������ȫ");
			}
		} else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}

	/**
	 * O04 ȥ֧�����Ӷ�������/�б�������
	 * 
	 * @param orderId
	 * @param productImagelistJson
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getPayOrder")
	public String getPayOrder(String userOrderId) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user = super.getLoginUser();
		if (user != null) {
			rq = orderMgtService.getPayOrderByOrderId(userOrderId);
		} else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}

	/**
	 * O03 �ҵĹ��򶩵�
	 * 
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/buylist")
	public String buylist() throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user = super.getLoginUser();
		if (user != null) {
			rq = orderMgtService.findOrderlist(user.getUserId());
		} else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}

}