package com.nowcoder.community.config;

import com.nowcoder.community.quartz.AlphaJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置 -> 数据库 -> 调用
@Configuration
public class QuartzConfig {

    //BeanFactory: 容器的顶层接口
    //FactoryBean：可简化Bean的实例化过程：
    // 1. 通过FactoryBean封装了Bean的实例化过程
    // 2. 将FactoryBean装配到Spring容器里
    // 3. 将FactoryBean注入给其他的Bean
    // 4. 该Bean得到的是FactoryBean所管理的对象实例

    // 配置JobDetail
//    @Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setName("alphaJob"); //任务名字
        factoryBean.setGroup("alphaJobGroup"); //多个任务可以同属于一组
        factoryBean.setDurability(true); //声明任务是否可以持久保存
        factoryBean.setRequestsRecovery(true); //任务是否可以恢复

        return factoryBean;
    }

    // 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
//    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail); //trigger是对哪个job的触发器
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setRepeatInterval(3000); //多长时间执行一次
        factoryBean.setJobDataMap(new JobDataMap()); //trigger底层需要存储job状态
        return factoryBean;
    }
}
