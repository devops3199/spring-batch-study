package dev.ray.adbatch.domain.advertisement.batch;

import dev.ray.adbatch.domain.advertisement.listener.AdRenewJobListener;
import dev.ray.adbatch.domain.advertisement.model.Advertisement;
import dev.ray.adbatch.domain.advertisement.processor.AdRenewProcessor;
import dev.ray.adbatch.domain.advertisement.writer.AdRenewWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
@AllArgsConstructor
public class AdRenewConfig {

    public static final String JOB_NAME = "AD_RENEW_JOB";

    private final DataSource dataSource;

    private final int chunkSize = 10;

    @Bean
    public JdbcCursorItemReader<Advertisement> reader() {
        return new JdbcCursorItemReaderBuilder<Advertisement>()
                .name("advertisementReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM advertisement")
                .rowMapper(new BeanPropertyRowMapper<>(Advertisement.class))
                .build();
    }

    @Bean
    public AdRenewProcessor processor() {
        return new AdRenewProcessor();
    }

    @Bean
    public AdRenewWriter writer() {
        return new AdRenewWriter();
    }

    @Bean
    public Job adRenewJob(JobRepository jobRepository, Step adRenewStep, AdRenewJobListener listener) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(adRenewStep)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .build();
    }

    @Bean
    public Step adRenewStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("adRenewStep", jobRepository)
                .<Advertisement, Advertisement>chunk(chunkSize, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .allowStartIfComplete(true)
                .build();
    }
}
