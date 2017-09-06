package com.bbyiya.pic.service.impl.calendar;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbyiya.dao.OUserordersMapper;
import com.bbyiya.dao.TiActivityoffMapper;
import com.bbyiya.dao.TiActivitysMapper;
import com.bbyiya.dao.TiActivitysinglesMapper;
import com.bbyiya.dao.TiActivityworksMapper;
import com.bbyiya.dao.TiProductsMapper;
import com.bbyiya.dao.TiPromoteremployeesMapper;
import com.bbyiya.dao.UUseraddressMapper;
import com.bbyiya.dao.UUsersMapper;
import com.bbyiya.enums.OrderTypeEnum;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.enums.calendar.ActivityWorksStatusEnum;
import com.bbyiya.enums.calendar.TiActivityTypeEnum;
import com.bbyiya.model.OUserorders;
import com.bbyiya.model.PMyproducttempusers;
import com.bbyiya.model.TiActivityoff;
import com.bbyiya.model.TiActivitys;
import com.bbyiya.model.TiProducts;
import com.bbyiya.model.TiPromoteremployees;
import com.bbyiya.model.UUsers;
import com.bbyiya.pic.service.calendar.IIbs_CalendarActivityService;
import com.bbyiya.pic.vo.calendar.CalendarActivityAddParam;
import com.bbyiya.utils.DateUtil;
import com.bbyiya.utils.PageInfoUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.calendar.TiActivitysVo;
import com.bbyiya.vo.calendar.TiActivitysWorkVo;
import com.bbyiya.vo.calendar.TiEmployeeActOffVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;


@Service("ibs_CalendarActivityService")
@Transactional(rollbackFor = { RuntimeException.class, Exception.class })
public class Ibs_CalendarActivityServiceImpl implements IIbs_CalendarActivityService{
	
	
	@Autowired
	private TiActivitysMapper activityMapper;
	@Autowired
	private TiProductsMapper tiproductMapper;
	@Autowired
	private TiActivityworksMapper actworkMapper;
	@Autowired
	private TiActivitysinglesMapper actworksingleMapper;
	@Autowired
	private TiPromoteremployeesMapper promoteremployeeMapper;	
	@Autowired
	private TiActivityoffMapper actoffMapper;
	@Autowired
	private OUserordersMapper orderMapper;
	
	/*-------------------用户信息------------------------------------------------*/
	@Autowired
	private UUsersMapper usersMapper;
	/**
	 * 添加日历活动
	 * 
	 * */
	public ReturnModel addCalendarActivity(Long userid,CalendarActivityAddParam param){
		ReturnModel rq=new ReturnModel();
		rq.setStatu(ReturnStatus.SystemError);	
		TiActivitys ti=new TiActivitys();
		ti.setTitle(param.getTitle());
		ti.setActtype(param.getActtype());
		ti.setCreatetime(new Date());
		ti.setDescription(param.getDescription());
		ti.setExtcount(param.getExtCount());//目标分享人数
		ti.setFreecount(param.getFreecount());//目标参与总数量
		ti.setProductid(param.getProductid());
		ti.setProduceruserid(userid);//推广者Id
		ti.setStatus(1);//默认就是已开启的活动
		activityMapper.insert(ti);
		rq.setStatu(ReturnStatus.Success);
		rq.setStatusreson("添加日历活动成功！");
		return rq;
	}
	
	/**
	 * 修改日历活动
	 * 
	 * */
	public ReturnModel editCalendarActivity(CalendarActivityAddParam param){
		ReturnModel rq=new ReturnModel();
		rq.setStatu(ReturnStatus.SystemError);	
		TiActivitys ti=activityMapper.selectByPrimaryKey(param.getActivityid());
		if(ti!=null){
			ti.setTitle(param.getTitle());
			ti.setDescription(param.getDescription());
			ti.setFreecount(param.getFreecount());//目标参与总数量
			Integer freecount=(param.getFreecount()==null)?0:param.getFreecount();
			//得到总报名人数
			int applycount=(ti.getApplycount()==null?0:ti.getApplycount());
			if(freecount.intValue()!=0&&freecount.intValue()<applycount){
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("邀请总数限制不得小于总报名人数！");
				return rq;
			}
			activityMapper.updateByPrimaryKey(ti);
		}
		rq.setStatu(ReturnStatus.Success);
		rq.setStatusreson("修改日历活动成功！");
		return rq;
	}
	
	/**
	 * 活动列表
	 * @param index
	 * @param size
	 * @param keywords
	 * @param type
	 * @return
	 */
	public ReturnModel findCalendarActivityList(int index,int size,Long userid,Integer status,String keywords,Integer type){
		ReturnModel rq=new ReturnModel();
		rq.setStatu(ReturnStatus.SystemError);
		
		PageHelper.startPage(index, size);
		List<TiActivitysVo> activitylist=activityMapper.findCalenderActivityList(userid,status,keywords,type);
		PageInfo<TiActivitysVo> pageresult=new PageInfo<TiActivitysVo>(activitylist);
		for (TiActivitysVo ti : pageresult.getList()) {
			ti.setCreateTimestr(DateUtil.getTimeStr(ti.getCreatetime(), "yyyy-MM-dd"));
			TiProducts product=tiproductMapper.selectByPrimaryKey(ti.getProductid());
			ti.setProductName(product.getTitle());
			//得到未完成数量
			Integer notsubmitcount=actworkMapper.getCountByActStatus(ti.getActid(), Integer.parseInt(ActivityWorksStatusEnum.apply.toString()));
			ti.setNotsubmitcount(notsubmitcount==null?0:notsubmitcount);
			//得到图片已提交未分享数量
			Integer notsharecount=actworkMapper.getCountByActStatus(ti.getActid(), Integer.parseInt(ActivityWorksStatusEnum.imagesubmit.toString()));
			ti.setNotsharecount(notsharecount==null?0:notsharecount);
			//得到已邀请数量
			if(ti.getActtype()==Integer.parseInt(TiActivityTypeEnum.toAll.toString())){
				ti.setYaoqingcount(ti.getFreecount());
			}else{
				Integer yaoqingcount=actworksingleMapper.getYaoqingCountByActId(ti.getActid());
				ti.setYaoqingcount(yaoqingcount==null?0:yaoqingcount);
			}
		}
		rq.setBasemodle(pageresult);
		rq.setStatu(ReturnStatus.Success);
		rq.setStatusreson("获取列表成功！");
		return rq;
	}
	
	/**
	 * 活动制作进度列表
	 */
	public ReturnModel getActWorkListByActId(int index,int size,Integer actid,Integer status,String keywords){
		ReturnModel rq=new ReturnModel();
		rq.setStatu(ReturnStatus.SystemError);
		
		
		TiActivitys act=activityMapper.selectByPrimaryKey(actid);
		PageHelper.startPage(index, size);
		List<TiActivitysWorkVo> activitylist=actworkMapper.findActWorkListByActId(actid, status, keywords);
		PageInfo<TiActivitysWorkVo> pageresult=new PageInfo<TiActivitysWorkVo>(activitylist);
		if(pageresult!=null&&pageresult.getList()!=null){
			for (TiActivitysWorkVo ti : pageresult.getList()) {
				if(act!=null){
					ti.setTargetextCount(act.getExtcount());
				}
				ti.setCreateTimestr(DateUtil.getTimeStr(ti.getCreatetime(), "yyyy-MM-dd"));
				UUsers user=usersMapper.selectByPrimaryKey(ti.getUserid());
				if(user!=null){
					ti.setWeiNickName(user.getNickname());
				}		
				// 得到作品订单集合
				List<OUserorders> orderList = orderMapper.findOrderListByCartId(ti.getWorkid(),Integer.parseInt(OrderTypeEnum.ti_branchOrder.toString()));
				List<String> orderNoList = new ArrayList<String>();
				for (OUserorders order : orderList) {
					orderNoList.add(order.getUserorderid());
				}
				ti.setOrdernolist(orderNoList);
				
			}
		}
		rq.setBasemodle(pageresult);
		rq.setStatu(ReturnStatus.Success);
		rq.setStatusreson("获取列表成功！");
		return rq;
	}
	
	/**
	 * 修改活动备注
	 */
	public ReturnModel editActivityRemark(Integer actid,String remark){
		ReturnModel rq=new ReturnModel();
		TiActivitys ti=activityMapper.selectByPrimaryKey(actid);
		if(ti!=null){
			ti.setRemark(remark);
			activityMapper.updateByPrimaryKey(ti);
		}
		rq.setStatu(ReturnStatus.Success);
		rq.setStatusreson("设置成功！");
		return rq;
	}
	
	
	/**
	 * 影楼员工负责模板信息列表
	 * @return
	 */
	public ReturnModel findActivityoffList(int index,int size,Long promoterUserId,Integer actid){
		ReturnModel rq=new ReturnModel();
		rq.setStatu(ReturnStatus.Success);
		PageHelper.startPage(index, size);	
		List<TiPromoteremployees> list= promoteremployeeMapper.findEmployeelistByPromoterUserId(promoterUserId);		
		PageInfo<TiPromoteremployees> reuslt=new PageInfo<TiPromoteremployees>(list); 
		if(reuslt!=null&&reuslt.getList()!=null&&reuslt.getList().size()>0){	
			List<TiEmployeeActOffVo> usertemplist=new ArrayList<TiEmployeeActOffVo>();
			for (TiPromoteremployees buser : list) {
				TiEmployeeActOffVo usertemp=new TiEmployeeActOffVo();
				usertemp.setActid(actid);
				usertemp.setName(buser.getName());
				usertemp.setPromoteruserid(promoterUserId);
				usertemp.setStatus(1);//默认全开启权限
				usertemp.setUserid(buser.getUserid());
				UUsers user=usersMapper.selectByPrimaryKey(buser.getUserid());
				if(user!=null&&user.getMobilephone()!=null){
					usertemp.setPhone(user.getMobilephone());
				}
				TiActivityoff actoff=actoffMapper.selectByPromoterUserIdAndActId(buser.getPromoteruserid(), actid);
				if(actoff!=null){
					usertemp.setStatus(0);
				}
				usertemplist.add(usertemp);
			}
			//根据一个PageInfo初始化另一个page
			PageInfoUtil<TiEmployeeActOffVo> reusltPage=new PageInfoUtil<TiEmployeeActOffVo>(reuslt, usertemplist);
			rq.setBasemodle(reusltPage);
			rq.setStatu(ReturnStatus.Success);
			rq.setStatusreson("获取列表成功！");
		}
		return rq;
	}
	
	
	/**
	 * 设置员工活动负责权限
	 * @return
	 */
	public ReturnModel setUserActPromotePermission(Long userId,Integer actid,Integer status){
		ReturnModel rq=new ReturnModel();
		if(actid==null){
			rq.setStatu(ReturnStatus.ParamError);
			rq.setStatusreson("参数错误：actid为空！");
			return rq;
		}
		if(userId==null){
			rq.setStatu(ReturnStatus.ParamError);
			rq.setStatusreson("参数错误：userId为空！");
			return rq;
		}
		if(status==null){
			rq.setStatu(ReturnStatus.ParamError);
			rq.setStatusreson("参数错误：status为空！");
			return rq;
		}
		TiActivityoff actoff=actoffMapper.selectByPromoterUserIdAndActId(userId, actid);
		if(status==1){
			if(actoff!=null){
				actoffMapper.deleteByPrimaryKey(actoff.getId());
			}
		}else{
			if(actoff==null){
				actoff=new TiActivityoff();
				actoff.setActid(actid);
				actoff.setPromoteruserid(userId);
				actoffMapper.insert(actoff);
			}
		}		
		rq.setStatu(ReturnStatus.Success);
		rq.setStatusreson("设置成功！");
		return rq;
	}
	
	
}