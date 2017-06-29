package com.bbyiya.pic.web.ibs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.baseUtils.ExportExcel;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.pic.service.ibs.IIbs_ActivityCodeService;
import com.bbyiya.pic.vo.product.ActivityCodeProductVO;
import com.bbyiya.pic.vo.product.MyProductTempAddParam;
import com.bbyiya.utils.FileUtils;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.web.base.SSOController;
import com.github.pagehelper.PageInfo;
import com.sdicons.json.mapper.MapperException;

@Controller
@RequestMapping(value = "/ibs/temp")
public class ActivityCodeController extends SSOController {
	
	@Resource(name = "ibs_ActivityCodeService")
	private IIbs_ActivityCodeService activitycodeService;
	
	
	
	/**
	 * 添加模板
	 * @param title 模板标题
	 * @param remark 备注
	 * @param productid 产品款示
	 * @param needverifer 是否需要审核
	 * @param discription 活动需知
	 * @param codeurl  二维码图片
	 * @returnp
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/addActivityCode")
	public String addActivityCode(String myproductTempJson) throws Exception {
		ReturnModel rq=new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			MyProductTempAddParam param = (MyProductTempAddParam)JsonUtil.jsonStrToObject(myproductTempJson,MyProductTempAddParam.class);			
			if (param == null) {
				rq.setStatu(ReturnStatus.ParamError_1);
				rq.setStatusreson("参数不全");
				return JsonUtil.objectToJsonStr(rq);
			}
			if(ObjectUtil.isEmpty(param.getTitle())){
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("活动名称不能为空!");
				return JsonUtil.objectToJsonStr(rq);
			}
			if(ObjectUtil.isEmpty(param.getProductid())){
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("对应产品不能为空!");
				return JsonUtil.objectToJsonStr(rq);
			}
			if(ObjectUtil.isEmpty(param.getStyleId())){
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("活动奖品不能为空!");
				return JsonUtil.objectToJsonStr(rq);
			}
			if(ObjectUtil.isEmpty(param.getApplycount())){
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("生成数量不能为空!");
				return JsonUtil.objectToJsonStr(rq);
			}
			if(param.getApplycount()!=null&&param.getApplycount()<=0){
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("生成数量必须大于零!");
				return JsonUtil.objectToJsonStr(rq);
			}
			if(!ObjectUtil.isEmpty(param.getTitle())&&!ObjectUtil.validSqlStr(param.getTitle())){
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("活动名称存在危险字符!");
				return JsonUtil.objectToJsonStr(rq);
			}
			if(!ObjectUtil.isEmpty(param.getCodesm())&&!ObjectUtil.validSqlStr(param.getCodesm())){
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("二维码文字说明在危险字符!");
				return JsonUtil.objectToJsonStr(rq);
			}			
			rq=activitycodeService.addActivityCode(user.getUserId(), param);
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期");
			return JsonUtil.objectToJsonStr(rq);
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	
	/**
	 * 得到相关批次的的活动码作品列表
	 * @param index
	 * @param size
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/findMyProductslistForActivityCode")
	public String findMyProductslistForActivityCode(Integer tempid,Integer activeStatus,
			@RequestParam(required = false, defaultValue = "")String keywords,
			@RequestParam(required = false, defaultValue = "1")int index,@RequestParam(required = false, defaultValue = "20")int size) throws Exception {
		ReturnModel rq=new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			rq=activitycodeService.findMyProductslistForActivityCode(user.getUserId(), tempid,activeStatus,keywords, index, size);
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期");
			return JsonUtil.objectToJsonStr(rq);
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	
	
	
	/**
	 * 删除活动码
	 * @param tempId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteActivityCode")
	public String deleteActivityCode(String codeno) throws Exception {
		ReturnModel rq=new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			rq=activitycodeService.deleteActivityCode(codeno);
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期");
			return JsonUtil.objectToJsonStr(rq);
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	
	
	
	@ResponseBody
	@RequestMapping(value = "/getActivityCodeDetail")
	public String getActivityCodeDetail(Integer tempid) throws Exception {
		ReturnModel rq=new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){		
			rq=activitycodeService.getActivityCodeDetail(tempid);
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期");
			return JsonUtil.objectToJsonStr(rq);
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	/**
	 * 重置活动下的已报名的序号
	 * @param cartId
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/reSetTempApplySort")
	public String resetAllTempApplySort(Integer tempid) throws Exception {
		ReturnModel rq=new ReturnModel();
		rq=activitycodeService.resetAllTempApplySort(tempid);
		return JsonUtil.objectToJsonStr(rq);
		
	}
	
	/**
	 * 条形码excel导出
	 * @param request
	 * @return
	 * @throws MapperException 
	 */
	@RequestMapping(value="/activityCodeProductExportExcel")
	@ResponseBody
	public String activityCodeProductExportExcel(Integer tempid,Integer activeStatus,
			@RequestParam(required = false, defaultValue = "")String keywords) throws Exception {
		// 列头
		String[] headers =new String[11];
		headers[0]="活动码";
		headers[1]="对应产品";
		headers[2]="活动状态";
		headers[3]="客户昵称";
		headers[4]="宝宝生日";
		headers[5]="作品进度";
		headers[6]="评论数";
		headers[7]="客户手机";
		headers[8]="收货地址";
		headers[9]="开始制作时间";
		headers[10]="最近更新时间";	
		String[] fields = new String[11];
		fields[0]="code.codeno";
		fields[1]="productTitle";
		fields[2]="activeStatus";
		fields[3]="invitedName";
		fields[4]="birthdayStr";
		fields[5]="count";
		fields[6]="commentsCount";
		fields[7]="phone";
		fields[8]="address";
		fields[9]="createtimestr";
		fields[10]="updatetimestr";
		
		
		//导出格式
		String format =".xlsx";
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user = super.getLoginUser();
		if (user != null) {
			rq=activitycodeService.findMyProductslistForActivityCode(user.getUserId(), tempid,activeStatus,keywords, 0, 0);
			
			if(rq.getStatu()!=ReturnStatus.Success){
				return JsonUtil.objectToJsonStr(rq);
			}
			
			PageInfo<ActivityCodeProductVO> resultPage =(PageInfo<ActivityCodeProductVO>) rq.getBasemodle();
			
			List<ActivityCodeProductVO> list=resultPage.getList();
			Long seed = System.currentTimeMillis();// 获得系统时间，作为生成随机数的种子
			// 获取用户的当前工作主目录 
			String sep=System.getProperty("file.separator");
			String currentWorkDir = System.getProperty("user.home") +sep+ "imagedownloadtemp"+sep;
			FileUtils.isDirExists(currentWorkDir);
			ExportExcel<ActivityCodeProductVO> ex = new ExportExcel<ActivityCodeProductVO>();
			String filename =  seed + format; ;
			File file = new File(currentWorkDir + filename);	
			try { 
				OutputStream out = new FileOutputStream(file);				
				ex.exportExcel("活动码", headers, fields, list, out, "yyyy-MM-dd");				
				out.close();				
				rq.setStatu(ReturnStatus.Success);
				rq.setBasemodle(file.getPath());
				return JsonUtil.objectToJsonStr(rq);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期");
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	
	@RequestMapping(value="/download")
	public String download(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			String path = request.getParameter("path");
			// path是指欲下载的文件的路径。
			File file = new File(path);
		    // 路径为文件且不为空则进行删除  
		    if (file.isFile() && file.exists()) {
				// 取得文件名。
				String filename = file.getName();
				//FileDownloadUtils.download(path, filename);
				// 以流的形式下载文件。
				InputStream fis = new BufferedInputStream(new FileInputStream(path));
				byte[] buffer = new byte[fis.available()];
				fis.read(buffer);
				fis.close();
				// 清空response
				response.reset();
				// 设置response的Header
				response.setCharacterEncoding("utf-8");  
				response.setContentType("application/vnd.ms-excel;charset=utf-8");
				response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
				response.addHeader("Content-Length", "" + file.length());
				OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
				toClient.write(buffer);
				toClient.flush();
				toClient.close();  
				// 删除文件
		        file.delete();  
		    }
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
		
}
