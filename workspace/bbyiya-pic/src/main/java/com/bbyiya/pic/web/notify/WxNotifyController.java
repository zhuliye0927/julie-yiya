package com.bbyiya.pic.web.notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.service.pic.IBaseOrderMgtService;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.pay.ParamUtils;
import com.bbyiya.utils.pay.WxPayAppConfig;
import com.bbyiya.utils.pay.WxPayConfig;
import com.bbyiya.utils.pay.WxPayUtils;
import com.bbyiya.utils.pay.WxUtil;
import com.bbyiya.vo.ReturnModel;
import com.sdicons.json.mapper.MapperException;

@Controller
@RequestMapping(value = "/wxpay")
public class WxNotifyController {
	@Resource(name = "baseOrderMgtServiceImpl")
	private IBaseOrderMgtService orderMgtService;

	/**
	 * ΢��֧����д��ַ
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 * @throws MapperException
	 */
	@ResponseBody
	@RequestMapping(value = "/WxPayNotify", method = { RequestMethod.POST, RequestMethod.GET })
	public String wxpayNotify(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException, MapperException {
		ReturnModel rq = new ReturnModel();
		String xmlStr = readReqStr(request);
		SortedMap<String, String> notifymap = ParamUtils.xml2Map(xmlStr);
		if (notifymap == null || notifymap.size() < 1) {
			rq.setStatu(ReturnStatus.SystemError);
			rq.setStatusreson("error");
			return JsonUtil.objectToJsonStr(rq);
		}
		if (WxPayUtils.isWXsign(notifymap, WxPayConfig.AppSecret)) {
			String result_code = notifymap.get("result_code");
			if ("SUCCESS".equals(result_code)) {
				String quReuslt = queryWxOrder(notifymap.get("transaction_id"), notifymap.get("out_trade_no"));
				if (quReuslt.equals("success")) {
					rq.setStatu(ReturnStatus.Success);
				}
			} else {
				rq.setStatu(ReturnStatus.SystemError);
				rq.setStatusreson("error");
				return JsonUtil.objectToJsonStr(rq);
			}
		}
		return JsonUtil.objectToJsonStr(rq);
	}

	private String queryWxOrder(String transaction_id, String out_trade_no) {

		List<NameValuePair> paramlist = new ArrayList<NameValuePair>();
		paramlist.add(new BasicNameValuePair("appid", WxPayConfig.APPID));
		paramlist.add(new BasicNameValuePair("mch_id", WxPayConfig.PARNER));
		paramlist.add(new BasicNameValuePair("nonce_str", WxPayUtils.genNonceStr()));
		paramlist.add(new BasicNameValuePair("out_trade_no", out_trade_no));
		paramlist.add(new BasicNameValuePair("transaction_id", transaction_id));
		String sign = WxPayUtils.genPackageSign(paramlist);
		paramlist.add(new BasicNameValuePair("sign", sign));
		String xmlstring = WxUtil.toXml(paramlist);
		String xmlResult = WxUtil.httpsRequest("https://api.mch.weixin.qq.com/pay/orderquery", xmlstring);
		SortedMap<String, String> map = ParamUtils.xml2Map(xmlResult);
		if (WxPayUtils.isWXsign(map, WxPayConfig.AppSecret)) {
			if (map != null && map.get("return_code").equals("SUCCESS") && map.get("result_code").equals("SUCCESS") && map.get("trade_state").equals("SUCCESS")) {
				// ��д����״̬ TODO ��д����״̬
				
			}
		}
		return "success";
	}

	public static String readReqStr(HttpServletRequest request) {
		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
		try {
			reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != reader) {
					reader.close();
				}
			} catch (IOException e) {

			}
		}
		return sb.toString();
	}

}
