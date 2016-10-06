package com.ihidea.component.sms;

import java.math.BigDecimal;
import java.util.List;

public interface ISms {

	public void send(String[] mobileId, String msg);

	public BigDecimal balance();

	public List<SmsReportEntity> report();

}
