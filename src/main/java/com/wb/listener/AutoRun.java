package com.wb.listener;

import java.text.ParseException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.wb.demo.V2exFree;

public class AutoRun implements ServletContextListener{

    public void contextDestroyed(ServletContextEvent arg0) {
        
    }

    public void contextInitialized(ServletContextEvent arg0) {
        //执行定时任务
        JobDetail detail = new JobDetail("job1", "group1", V2exFree.class);
        CronTrigger cronTrigger = new CronTrigger("job1", "group1");
        try {
            CronExpression cronExpression = new CronExpression("0 1/3 8-21 * * ? *");
            cronTrigger.setCronExpression(cronExpression);
            SchedulerFactory factory = new StdSchedulerFactory();
            Scheduler scheduler;
            try {
                scheduler = factory.getScheduler();
                try {
                    scheduler.scheduleJob(detail, cronTrigger);
                    scheduler.start();
                    System.out.println("============定时任务已启动================");
                } catch (SchedulerException e) {
                    e.printStackTrace();
                }
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        AutoRun autoRun = new AutoRun();
        autoRun.contextInitialized(null);
    }

}
