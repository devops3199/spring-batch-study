package dev.ray.adbatch.domain.advertisement.reader;

import dev.ray.adbatch.domain.advertisement.model.Advertisement;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Configuration
public class AdItemReader {

    private final DataSource dataSource;

    public AdItemReader(@Qualifier("appDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
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
}
