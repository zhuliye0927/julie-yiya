package com.bbyiya.pic.web.version_one;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.dao.PMyproductdetailsMapper;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.model.PMyproductdetails;
import com.bbyiya.pic.service.IPic_ProductService;
import com.bbyiya.pic.vo.product.MyProductParam;
import com.bbyiya.pic.web.common.Json2Objects;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.utils.RedisUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.web.base.SSOController;

@Controller
@RequestMapping(value = "/myProduct")
public class MyProductController extends SSOController {
	@Resource(name = "pic_productService")
	private IPic_ProductService proService;
	@Autowired
	private PMyproductdetailsMapper detaiMapper;

	/**
	 * ���桢�����ҵ���Ʒ
	 * 
	 * @param myproductJson
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/saveMyproduct")
	public String saveMyproduct(String myproductJson) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user = super.getLoginUser();
		if (user != null) {
			MyProductParam param = Json2Objects.getParam_MyProductParam(myproductJson);// (MyProductParam) JsonUtil.jsonStrToObject(myproductJson, MyProductParam.class);
			if (param != null) {
				rq = proService.saveOrEdit_MyProducts(user.getUserId(), param);
			} else {
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("��������");
			}
		} else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}

	

	/**
	 * �ҵ���Ʒ�б�
	 * 
	 * @param myproductJson
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/mylist")
	public String myProlist() throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user = super.getLoginUser();
		if (user != null) {
			rq = proService.findMyProlist(user.getUserId());
		} else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}

	/**
	 * �ҵ���Ʒ����
	 * 
	 * @param cartId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/details")
	public String details(@RequestParam(required = false, defaultValue = "0") long cartId) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user = super.getLoginUser();
		if (user != null) {
			rq = proService.getMyProductInfo(user.getUserId(), cartId);
		} else {//�ǵ�¼����ҳ
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("δ��¼"); 
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	/**
	 * �ҵ���Ʒ������ҳ
	 * @param cartId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/sharedetails")
	public String sharedetails(@RequestParam(required = false, defaultValue = "0") long cartId) throws Exception {
		ReturnModel rq = new ReturnModel();
		String key = "shareurl02142-cartid-" + cartId;
		rq = (ReturnModel) RedisUtil.getObject(key);
		if (rq == null || !rq.getStatu().equals(ReturnStatus.Success)) {
			rq = proService.getMyProductInfo(cartId);
			if (ReturnStatus.Success.equals(rq.getStatu())) {
				RedisUtil.setObject(key, rq, 3600);
			}
		}

		return JsonUtil.objectToJsonStr(rq);
	}

	@ResponseBody
	@RequestMapping(value = "/dele")
	public String dele(@RequestParam(required = false, defaultValue = "0") long pdid) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user = super.getLoginUser();
		if (user != null) {
			rq = proService.del_myProductDetail(user.getUserId(), pdid);
		} else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}

}