package dev.ray.adbatch.domain.advertisement.batch;

import dev.ray.adbatch.domain.advertisement.listener.AdRenewJobListener;
import dev.ray.adbatch.domain.advertisement.model.Advertisement;
import dev.ray.adbatch.domain.advertisement.processor.AdOffProcessor;
import dev.ray.adbatch.domain.advertisement.processor.AdOnProcessor;
import dev.ray.adbatch.domain.advertisement.writer.AdOffWriter;
import dev.ray.adbatch.domain.advertisement.writer.AdOnWriter;
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
import java.time.LocalDateTime;

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
    public JdbcCursorItemReader<Advertisement> adOffReader(@Value("#{jobParameters[now]}") String nowStr) {
        LocalDateTime now = LocalDateTime.parse(nowStr);
        return new JdbcCursorItemReaderBuilder<Advertisement>()
                .name("adOffReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM advertisement a WHERE a.status = 'ON' AND a.ended_at < ?")
                .preparedStatementSetter(ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(now));
                })
                .rowMapper(new BeanPropertyRowMapper<>(Advertisement.class))
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<Advertisement> adOnReader(@Value("#{jobParameters[now]}") String nowStr) {
        LocalDateTime now = LocalDateTime.parse(nowStr);
        return new JdbcCursorItemReaderBuilder<Advertisement>()
                .name("adOnReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM advertisement a WHERE a.status = 'READY' AND a.started_at <= ?")
                .preparedStatementSetter(ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(now));
                })
                .rowMapper(new BeanPropertyRowMapper<>(Advertisement.class))
                .build();
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
                .<Advertisement, Advertisement>chunk(10, transactionManager)
                .reader(adOffReader(null))
                .processor(adOffProcessor())
                .writer(adOffWriter())
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    @JobScope
    public Step adOnStep(JobRepository jobRepository) {
        return new StepBuilder("adOnStep", jobRepository)
                .<Advertisement, Advertisement>chunk(10, transactionManager)
                .reader(adOnReader(null))
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
