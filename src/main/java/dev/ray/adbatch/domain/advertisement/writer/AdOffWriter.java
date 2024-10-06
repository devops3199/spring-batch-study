package dev.ray.adbatch.domain.advertisement.writer;

import dev.ray.adbatch.domain.advertisement.model.Advertisement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
public class AdOffWriter implements ItemWriter<Advertisement> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void write(Chunk<? extends Advertisement> chunk) {
        String sql = "UPDATE advertisement SET status = 'OFF' WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Advertisement advertisement = chunk.getItems().get(i);
                ps.setLong(1, advertisement.getId());
            }

            public int getBatchSize() {
                return chunk.size();
            }
        });
    }
}
