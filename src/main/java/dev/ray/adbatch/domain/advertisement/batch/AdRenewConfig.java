package dev.ray.adbatch.domain.advertisement.batch;

import dev.ray.adbatch.domain.advertisement.listener.AdRenewJobListener;
import dev.ray.adbatch.domain.advertisement.model.Advertisement;
import dev.ray.adbatch.domain.advertisement.processor.AdRenewProcessor;
import dev.ray.adbatch.domain.advertisement.writer.AdRenewWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class AdRenewConfig {

    public static final String JOB_NAME = "AD_RENEW_JOB";

    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;

    public AdRenewConfig(
            @Qualifier("appDataSource") DataSource dataSource,
            @Qualifier("appTransactionManager") PlatformTransactionManager transactionManager) {
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<Advertisement> reader(@Value("#{jobParameters[now]}") String nowStr) {
        LocalDateTime now = LocalDateTime.parse(nowStr);
        return new JdbcCursorItemReaderBuilder<Advertisement>()
                .name("advertisementReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM advertisement a WHERE a.status = 'ON' AND a.ended_at < ?")
                .preparedStatementSetter(ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(now));
                })
                .rowMapper(new BeanPropertyRowMapper<>(Advertisement.class))
                .build();
    }

    @Bean
    public AdRenewProcessor processor() {
        return new AdRenewProcessor();
    }

    @Bean
    public AdRenewWriter writer() {
        return new AdRenewWriter(new JdbcTemplate(dataSource));
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
    @JobScope
    public Step adRenewStep(JobRepository jobRepository) {
        return new StepBuilder("adRenewStep", jobRepository)
                .<Advertisement, Advertisement>chunk(10, transactionManager)
                .reader(reader(null))
                .processor(processor())
                .writer(writer())
                .allowStartIfComplete(true)
                .build();
    }
}
