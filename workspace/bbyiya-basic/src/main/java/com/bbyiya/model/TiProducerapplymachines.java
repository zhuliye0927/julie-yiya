package com.bbyiya.model;

import java.util.List;

import com.bbyiya.vo.calendar.TiMachineproductVo;

public class TiProducerapplymachines {
    private Integer id;

    private Long produceruserid;

    private Long machineid;

    private String machinename;

    /**************************vo*****************************/ 
    private List<TiMachineproductVo> machineproduct;
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getProduceruserid() {
        return produceruserid;
    }

    public void setProduceruserid(Long produceruserid) {
        this.produceruserid = produceruserid;
    }

    public Long getMachineid() {
        return machineid;
    }

    public void setMachineid(Long machineid) {
        this.machineid = machineid;
    }

    public String getMachinename() {
        return machinename;
    }

    public void setMachinename(String machinename) {
        this.machinename = machinename == null ? null : machinename.trim();
    }

	public List<TiMachineproductVo> getMachineproduct() {
		return machineproduct;
	}

	public void setMachineproduct(List<TiMachineproductVo> machineproduct) {
		this.machineproduct = machineproduct;
	}

	
    
}