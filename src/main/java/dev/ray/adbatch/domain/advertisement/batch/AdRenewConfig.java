package dev.ray.adbatch.domain.advertisement.batch;

import dev.ray.adbatch.domain.advertisement.model.Advertisement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
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
    public AdvertisementProcessor processor() {
        return new AdvertisementProcessor();
    }

    @Bean
    public AdvertisementWriter writer() {
        return new AdvertisementWriter();
    }

    @Bean
    public Job adRenewJob(JobRepository jobRepository, Step adRenewStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(adRenewStep)
                .build();
    }

    @Bean
    public Step adRenewStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("adRenewStep", jobRepository)
                .<Advertisement, Advertisement>chunk(chunkSize, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
}
