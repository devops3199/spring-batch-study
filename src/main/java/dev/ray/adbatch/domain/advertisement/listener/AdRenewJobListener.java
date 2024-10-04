package dev.ray.adbatch.domain.advertisement.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdRenewJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(">>> Ad Renew Job is about to start: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info(">>> Ad Renew Job finished successfully: {}", jobExecution.getJobInstance().getJobName());
        } else {
            log.info(">>> Ad Renew Job failed with status: {}", jobExecution.getStatus());
        }
    }
}