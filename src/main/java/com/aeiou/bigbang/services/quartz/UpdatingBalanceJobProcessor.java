package com.aeiou.bigbang.services.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component("updatingBalanceJobProcessor")
public class UpdatingBalanceJobProcessor {

	private final Log log = LogFactory.getLog(getClass());

	// this will be activated by the Spring Quarz
	public synchronized void updateBalance() {
		if(log.isDebugEnabled()){
			log.debug("Quartz excuting.......................................");
		}
        System.out.println("Quartz excuting.......................................");
	}
}
