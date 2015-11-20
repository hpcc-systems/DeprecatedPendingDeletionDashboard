package org.hpccsystems.dashboard.chart.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RelevantData extends ChartData {
	
	private String claimId;
	
	private String claimImage;
	
	private String personImage;
	
	private String vehicleImage;
	
	private String policyImage;
	
	private String groupTypeId;
	
	private String groupId;

	@XmlElement
	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}
	
	@XmlElement
	public String getClaimImage() {
		return claimImage;
	}

	public void setClaimImage(String claimImage) {
		this.claimImage = claimImage;
	}
	
	@XmlElement
	public String getPersonImage() {
		return personImage;
	}

	public void setPersonImage(String personImage) {
		this.personImage = personImage;
	}
	
	@XmlElement
	public String getVehicleImage() {
		return vehicleImage;
	}

	public void setVehicleImage(String vehicleImage) {
		this.vehicleImage = vehicleImage;
	}
	
	@XmlElement
	public String getPolicyImage() {
		return policyImage;
	}

	public void setPolicyImage(String policyImage) {
		this.policyImage = policyImage;
	}
	
	@Override
	public String toString() {
		return "RelevantData [ClaimId=" + claimId
				+ ", claimImage= " + claimImage
				+ ", personImage= " + personImage
				+ ", vehicleImage= " + vehicleImage
				+ ", groupTypeId= " + groupTypeId
				+ ", groupId= " + groupId
				+ ", policyImage= " + policyImage + "]";
	}

	@XmlElement
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @XmlElement
    public String getGroupTypeId() {
        return groupTypeId;
    }

    public void setGroupTypeId(String groupTypeId) {
        this.groupTypeId = groupTypeId;
    }
	
}
