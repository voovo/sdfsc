package com.example.domain.DTO;

import java.util.Date;

public class Trackhis {
	
	private String FLIGHTID;
	
	private Date RCVTIME;

	public String getFLIGHTID() {
		return FLIGHTID;
	}

	public void setFLIGHTID(String fLIGHTID) {
		FLIGHTID = fLIGHTID;
	}

	public Date getRCVTIME() {
		return RCVTIME;
	}

	public void setRCVTIME(Date rCVTIME) {
		RCVTIME = rCVTIME;
	}

	
}
