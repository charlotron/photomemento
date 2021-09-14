package org.photomemento.back.config;

import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.service.SystemService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Slf4j
@Component
public class ApplicationStartEndsHook implements ApplicationListener<ContextRefreshedEvent> {

    private final SystemService systemService;

    public ApplicationStartEndsHook(SystemService systemService){
        this.systemService=systemService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info(String.format("##### Application started ##### start time: %s", this.systemService.getStartTime()));
    }

    @PreDestroy
    public void onApplicationEnds() {
        log.info("##### Application finishing ##### ");
    }
}

