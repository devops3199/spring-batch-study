package dev.ray.adbatch.domain.advertisement.batch;

import dev.ray.adbatch.domain.advertisement.model.Advertisement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
public class AdvertisementWriter implements ItemWriter<Advertisement> {

    @Override
    public void write(Chunk<? extends Advertisement> chunk) {
        for (Advertisement advertisement : chunk) {
            log.info(">>> Advertisement write each: {}", advertisement);
        }
    }
}
