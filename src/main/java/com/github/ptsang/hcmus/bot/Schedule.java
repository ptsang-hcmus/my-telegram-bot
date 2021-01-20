package com.github.ptsang.hcmus.bot;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class Schedule {
	private static final Logger logger = LoggerFactory.getLogger(Bot.class);

//	@Scheduled(fixedDelay = 10000)
//	public void run() {
//		logger.info("Current time is :: " + Calendar.getInstance().getTime());
//	}
}
