package com.es.statistic;

import java.util.ResourceBundle;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.Async;

import com.es.manager.DataStatisticManager;
import com.es.util.CommonUntil;

public class ScheduleTask {

	
	/*
	 * 定时数据处理--开启定时任务 1.获取es数据---全量、特定索引
	 * 
	 * 2.源数据转换-统计规则-处理后数据 3.处理后数据入库
	 */
	public static void main(String[] args) {
		System.out.println("application start....");
		ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");

	}

	// 获取es数据
	@Async("threadPoolTaskExecutor")
	public void errorInfoStatisticMinuteOf5() {
		System.out.println("statistic begin...." + Thread.currentThread().getName());
		DataStatisticManager dsm = new DataStatisticManager();
		dsm.errorInfoStatistic();
	}

	// 每小时统计
	public void errorInfoStatisticHours() {
		DataStatisticManager dsm = new DataStatisticManager();
		dsm.statisticHourError();
	}
	
	// 每天统计
		public void errorInfoStatisticDays() {
			DataStatisticManager dsm = new DataStatisticManager();
			dsm.statisticDayError();
		}
	
}
