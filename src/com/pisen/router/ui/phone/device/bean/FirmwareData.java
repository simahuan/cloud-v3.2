package com.pisen.router.ui.phone.device.bean;

import java.io.Serializable;

/**
 * 固件信息下载进度
 * @author Liuhc
 * @version 1.0 2015年7月3日09:37:14
 */
public class FirmwareData implements Serializable{

	/** TODO */
	private static final long serialVersionUID = 1L;
	/**
	 * "Total_Percentage": "100",
        "Total": "7168k",
        "Received_Percentage": "100",
        "Received": "7168k",
        "Xferd_Percentage": "0",
        "Xferd": "0",
        "AverageDload": "490k",
        "SpeedUpload": "0",
        "TimeTotal": "0:00:14",
        "TimeSpent": "0:00:14",
        "TimeLeft": "--:--:--",
        "CurrentSpeed": "472k",
        "State": "complet"
	 */
	String Total_Percentage;
	String Total;
	String Received_Percentage;
	String Received;
	String Xferd_Percentage;
	String Xferd;
	String AverageDload;
	String SpeedUpload;
	String TimeTotal;
	String TimeSpent;
	String TimeLeft;
	String CurrentSpeed;
	String State;
	
	String result;
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	
	//--------------------------------------------
	public String getTotal_Percentage() {
		return Total_Percentage;
	}
	public void setTotal_Percentage(String total_Percentage) {
		Total_Percentage = total_Percentage;
	}
	public String getTotal() {
		return Total;
	}
	public void setTotal(String total) {
		Total = total;
	}
	public String getReceived_Percentage() {
		return Received_Percentage;
	}
	public void setReceived_Percentage(String received_Percentage) {
		Received_Percentage = received_Percentage;
	}
	public String getReceived() {
		return Received;
	}
	public void setReceived(String received) {
		Received = received;
	}
	public String getXferd_Percentage() {
		return Xferd_Percentage;
	}
	public void setXferd_Percentage(String xferd_Percentage) {
		Xferd_Percentage = xferd_Percentage;
	}
	public String getXferd() {
		return Xferd;
	}
	public void setXferd(String xferd) {
		Xferd = xferd;
	}
	public String getAverageDload() {
		return AverageDload;
	}
	public void setAverageDload(String averageDload) {
		AverageDload = averageDload;
	}
	public String getSpeedUpload() {
		return SpeedUpload;
	}
	public void setSpeedUpload(String speedUpload) {
		SpeedUpload = speedUpload;
	}
	public String getTimeTotal() {
		return TimeTotal;
	}
	public void setTimeTotal(String timeTotal) {
		TimeTotal = timeTotal;
	}
	public String getTimeSpent() {
		return TimeSpent;
	}
	public void setTimeSpent(String timeSpent) {
		TimeSpent = timeSpent;
	}
	public String getTimeLeft() {
		return TimeLeft;
	}
	public void setTimeLeft(String timeLeft) {
		TimeLeft = timeLeft;
	}
	public String getCurrentSpeed() {
		return CurrentSpeed;
	}
	public void setCurrentSpeed(String currentSpeed) {
		CurrentSpeed = currentSpeed;
	}
	public String getState() {
		return State;
	}
	public void setState(String state) {
		State = state;
	}
	
}
