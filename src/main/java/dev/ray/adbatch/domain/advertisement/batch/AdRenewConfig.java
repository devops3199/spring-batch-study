package dev.ray.adbatch.domain.advertisement.batch;

import dev.ray.adbatch.domain.advertisement.listener.AdRenewJobListener;
import dev.ray.adbatch.domain.advertisement.model.Advertisement;
import dev.ray.adbatch.domain.advertisement.processor.AdOffProcessor;
import dev.ray.adbatch.domain.advertisement.processor.AdOnProcessor;
import dev.ray.adbatch.domain.advertisement.reader.AdItemReader;
import dev.ray.adbatch.domain.advertisement.writer.AdOffWriter;
import dev.ray.adbatch.domain.advertisement.writer.AdOnWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class AdRenewConfig {

    public static final String JOB_NAME = "AD_RENEW_JOB";
    public final int CHUNK_SIZE = 10;

    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;
    private final AdItemReader adItemReader;

    public AdRenewConfig(
            @Qualifier("appDataSource") DataSource dataSource,
            @Qualifier("appTransactionManager") PlatformTransactionManager transactionManager,
            AdItemReader adItemReader) {
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
        this.adItemReader = adItemReader;
    }

    @Bean
    public AdOffProcessor adOffProcessor() {
        return new AdOffProcessor();
    }

    @Bean
    public AdOnProcessor adOnProcessor() {
        return new AdOnProcessor();
    }

    @Bean
    public AdOffWriter adOffWriter() {
        return new AdOffWriter(new JdbcTemplate(dataSource));
    }

    @Bean
    public AdOnWriter adOnWriter() {
        return new AdOnWriter(new JdbcTemplate(dataSource));
    }

    @Bean
    @JobScope
    public Step adOffStep(JobRepository jobRepository) {
        return new StepBuilder("adOffStep", jobRepository)
                .<Advertisement, Advertisement>chunk(CHUNK_SIZE, transactionManager)
                .reader(adItemReader.adOffReader(null))
                .processor(adOffProcessor())
                .writer(adOffWriter())
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    @JobScope
    public Step adOnStep(JobRepository jobRepository) {
        return new StepBuilder("adOnStep", jobRepository)
                .<Advertisement, Advertisement>chunk(CHUNK_SIZE, transactionManager)
                .reader(adItemReader.adOnReader(null))
                .processor(adOnProcessor())
                .writer(adOnWriter())
                .build();
    }

    @Bean
    public Job adRenewJob(JobRepository jobRepository, Step adOffStep, Step adOnStep, AdRenewJobListener listener) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(adOffStep)
                .next(adOnStep)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .build();
    }
}
