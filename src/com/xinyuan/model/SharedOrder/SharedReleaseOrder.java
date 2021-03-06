package com.xinyuan.model.SharedOrder;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.xinyuan.model.App2;

@Entity
@Table
public class SharedReleaseOrder extends App2 {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	private Date releaseDate ; 			// 放行日期
	
	private String exitSecEmployeeNO;	// 出厂放行 (保安NO)
	
	
	private Set<SharedReleaseBill> bills;

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getExitSecEmployeeNO() {
		return exitSecEmployeeNO;
	}

	public void setExitSecEmployeeNO(String exitSecEmployeeNO) {
		this.exitSecEmployeeNO = exitSecEmployeeNO;
	}

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn(name="SharedReleaseOrder_id")
	public Set<SharedReleaseBill> getBills() {
		return bills;
	}

	public void setBills(Set<SharedReleaseBill> bills) {
		this.bills = bills;
	}
	
}
