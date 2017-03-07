package com.bbyiya.pic.web.version_one;

import java.util.Date;







import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.dao.EErrorsMapper;
import com.bbyiya.dao.UBranchinfotempMapper;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.enums.user.UserStatusEnum;
import com.bbyiya.model.UAgentapply;
import com.bbyiya.model.UBranches;
import com.bbyiya.model.UBranchinfotemp;
import com.bbyiya.pic.service.IPic_BranchMgtService;
import com.bbyiya.pic.utils.Json2Objects;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.web.base.SSOController;


@Controller
@RequestMapping(value = "/ibs/branch")
public class BranchMgtController extends SSOController {
	@Resource(name = "pic_BranchMgtService")
	private IPic_BranchMgtService branchService;
	
	@Autowired
	private UBranchinfotempMapper tempMapper;
	@Autowired
	private EErrorsMapper logger;
	/**
	 * B01 招商报名
	 * @param companyJson
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/companySubmit")
	public String companySubmit( String companyJson) throws Exception {
		ReturnModel rq = new ReturnModel();
		try {
			
			UBranchinfotemp model= Json2Objects.getParam_UBranchinfotemp(companyJson);// (UBranchinfotemp)JsonUtil.jsonStrToObject(companyJson, UBranchinfotemp.class);
			if(model!=null){
				if(ObjectUtil.isEmpty(model.getPhone())){
					rq.setStatu(ReturnStatus.ParamError);
					rq.setStatusreson("手机号不能为空");
					return JsonUtil.objectToJsonStr(rq);
				}
				if(ObjectUtil.isEmpty(model.getContactname())){
					rq.setStatu(ReturnStatus.ParamError);
					rq.setStatusreson("联系人不能为空");
					return JsonUtil.objectToJsonStr(rq);
				}
				if(ObjectUtil.isEmpty(model.getCompanyname())){
					rq.setStatu(ReturnStatus.ParamError);
					rq.setStatusreson("公司信息不能为空");
					return JsonUtil.objectToJsonStr(rq);
				}
				if(!(ObjectUtil.validSqlStr(model.getPhone())&&ObjectUtil.validSqlStr(model.getCompanyname())&&ObjectUtil.validSqlStr(model.getContactname()))){
					rq.setStatu(ReturnStatus.ParamError);
					rq.setStatusreson("存在非法字符");
					return JsonUtil.objectToJsonStr(rq);
				}
			}else {
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("参数错误");
				return JsonUtil.objectToJsonStr(rq);
			}
			model.setCreatetime(new Date()); 
			tempMapper.insertSelective(model);
			rq.setStatu(ReturnStatus.Success);
			rq.setStatusreson("提交成功！");
		} catch (Exception e) {
			rq.setStatu(ReturnStatus.ParamError);
			rq.setStatusreson("参数错误001");
			return JsonUtil.objectToJsonStr(rq);
		} 
		return JsonUtil.objectToJsonStr(rq);
	}
	
	/**
	 * 根据地区获取代理金额
	 * @param province
	 * @param city
	 * @param district
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getBranchAreaPrice")
	public String getBranchAreaPrice(String province,String city,String district) throws Exception {
		ReturnModel rq = branchService.getBranchAreaPrice(ObjectUtil.parseInt(province) , ObjectUtil.parseInt(city), ObjectUtil.parseInt(district));
		return JsonUtil.objectToJsonStr(rq);
	}
	
	
	/**
	 *  代理商申请
	 * @param agentJson
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/agentApply")
	public String agentApply(String agentJson) throws Exception {
		ReturnModel rq=new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			if(user.getStatus()!=null&&user.getStatus().intValue()==Integer.parseInt(UserStatusEnum.ok.toString())){
				try {
					UAgentapply applyInfo=(UAgentapply)JsonUtil.jsonStrToObject(agentJson, UAgentapply.class);
					rq =branchService.applyAgent(user.getUserId(), applyInfo);
				} catch (Exception e) {
					// TODO: handle exception
					rq.setStatu(ReturnStatus.ParamError);
					rq.setStatusreson("参数有误");
					return JsonUtil.objectToJsonStr(rq);
				}
			}else {
				rq.setStatu(ReturnStatus.LoginError_2);
				rq.setStatusreson("未完成注册");
				return JsonUtil.objectToJsonStr(rq);
			}
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期");
			return JsonUtil.objectToJsonStr(rq);
		}
		
		return JsonUtil.objectToJsonStr(rq);
	}
	
	/**
	 *  分店申请
	 * @param agentJson
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/branchApply")
	public String branchApply(String branchJson) throws Exception {
		ReturnModel rq=new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			if(user.getStatus()!=null&&user.getStatus().intValue()==Integer.parseInt(UserStatusEnum.ok.toString())){
				try {
					UBranches applyInfo=(UBranches)JsonUtil.jsonStrToObject(branchJson, UBranches.class);
					rq =branchService.applyBranch(user.getUserId(), applyInfo);
				} catch (Exception e) {
					// TODO: handle exception
					rq.setStatu(ReturnStatus.ParamError);
					rq.setStatusreson("参数有误");
					return JsonUtil.objectToJsonStr(rq);
				}
			}else {
				rq.setStatu(ReturnStatus.LoginError_2);
				rq.setStatusreson("未完成注册");
				return JsonUtil.objectToJsonStr(rq);
			}
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期");
			return JsonUtil.objectToJsonStr(rq);
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	
	/**
	 * 获取代理单元（根据县区判断代理单元）
	 * @param areaCode
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getAgentAreas")
	public String getAgentAreas(Integer areaCode) throws Exception {
		ReturnModel rq=new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			rq=branchService.getAgentArea(areaCode);
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期");
			return JsonUtil.objectToJsonStr(rq);
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	/**
	 * 判断用户代理商申请状态
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getApplyStatus")
	public String getApplyStatus(Integer type) throws Exception {
		ReturnModel rq=new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			if(type!=null&&type==1){
				rq=branchService.getAgentApplyStatusModel(user.getUserId());
			}else {
				rq=branchService.getBranchApplyStatusModel(user.getUserId());
			}
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期");
			return JsonUtil.objectToJsonStr(rq);
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	
	
}
