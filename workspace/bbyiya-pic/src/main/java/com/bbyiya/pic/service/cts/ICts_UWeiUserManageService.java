package com.bbyiya.pic.service.cts;

import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.UWeiUserSearchParam;

public interface ICts_UWeiUserManageService {

	/**
	 * ��ȡӰ¥�Ƽ��˷�չ���û������б�
	 * @param branchuserId Ӱ¥ID
	 * @param startTimeStr ��ʼʱ��
	 * @param endTimeStr	����ʱ��
	 * @param status	״̬
	 * @param index
	 * @param size
	 * @return
	 */
	ReturnModel find_payorderExtByBranchUpUserid(Long branchuserId,String startTimeStr,String endTimeStr,Integer status, int index,int size);
	/**
	 * ��ȡ΢���б�
	 * @param param
	 * @param index
	 * @param size
	 * @return
	 */
	ReturnModel findWeiUserVoList(UWeiUserSearchParam param, int index, int size);
}