package org.photomemento.back.config;

import org.photomemento.back.domain.entity.file.Media;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

@Configuration
public class MongoConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate){
        this.mongoTemplate=mongoTemplate;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .withDefaultLanguage("en")
                .named("text_search")
                //File fields
                .onField("id")
                .onField("parent")
                .onField("path")
                .onField("name")
                //Media fields
                .onField("type")
                .onField("contentType")
                //GeoData fields
                .onField("geoData.city")
                .onField("geoData.town")
                .onField("geoData.village")
                .onField("geoData.district")
                .onField("geoData.state")
                .onField("geoData.country")
                .onField("geoData.municipality")
                .onField("geoData.borough")
                .build();
        mongoTemplate.indexOps(Media.class).ensureIndex(textIndex);
    }
}
