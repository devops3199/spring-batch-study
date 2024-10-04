package dev.ray.adbatch.domain.advertisement.processor;

import dev.ray.adbatch.domain.advertisement.model.Advertisement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class AdRenewProcessor implements ItemProcessor<Advertisement, Advertisement> {

    @Override
    public Advertisement process(Advertisement advertisement) {
        log.info(">>> Advertisement process: {}", advertisement.getName());
        return advertisement;
    }
}
