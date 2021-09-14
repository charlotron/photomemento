package org.photomemento.back.repository.media;

import org.photomemento.back.domain.entity.GeoDataTotal;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.domain.entity.file.media.geodata.ZoomLevels;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
public class MediaRepositoryImpl implements IMediaRepository {

    private static final String COLLECTION = "media";

    private final MongoTemplate mongoTemplate;

    public MediaRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<GeoDataTotal> findAllLocationsTotals(Pageable pageable, ZoomLevels.ZOOM_LEVEL zoom) {
        final String field = "geoData.zoomLevels." + zoom.getField();
        final MatchOperation matchOp = match(Criteria.where(field).exists(true));
        final GroupOperation groupOp = group(field)
                .count().as("count")
                .first("latitude").as("latitude")
                .first("longitude").as("longitude");
        final SkipOperation offsetOp = skip((long) pageable.getPageNumber() * pageable.getPageSize());
        final LimitOperation limitOp = limit(pageable.getPageSize());

        //This is for return paged results
        Aggregation aggResults = newAggregation(
                matchOp,
                groupOp,
                offsetOp,
                limitOp);

        /*final CountOperation countOp = count().as("total");
        //This is for paging to count number of results
        Aggregation aggCount = newAggregation(
                matchOp,
                groupOp,
                countOp);*/

        //Execute aggregation and resolve results to be returned
        //List<GeoDataTotal> list = mongoTemplate.aggregate(aggResults, COLLECTION, GeoDataTotal.class).getMappedResults();

        return mongoTemplate.aggregate(aggResults, COLLECTION, GeoDataTotal.class).getMappedResults();

        //Prepare the paginated response
        /*return PageableExecutionUtils.getPage(
                list,
                pageable,
                () -> Optional.ofNullable(
                        mongoTemplate.aggregate(
                                aggCount,
                                COLLECTION,
                                CountTotal.class).getUniqueMappedResult()) //CountTotal is an aux object with a "total" field with total of results
                        .map(CountTotal::getTotal)
                        .orElse(0L)); //Returns a long with the results number*/
    }

    //Source: https://stackoverflow.com/questions/27296533/spring-custom-query-with-pageable
    @Override
    public Page<Media> searchAndHasMedia(Pageable pageable, String text) {
        //Prepare teh filtered query and add the score field
        final Query query = TextQuery
                .queryText(
                        TextCriteria
                                .forDefaultLanguage()
                                .matching(text))
                .sortByScore()
                .includeScore("score");
        query.addCriteria(Criteria.where("hasMedia").exists(true))
                .with(Sort.by(Sort.Direction.DESC, "shotDate")) //TODO: Not sure if taking this in account
                .with(pageable);
        query.fields().exclude("metadata", "status");

        //Return list of paged results
        List<Media> list = mongoTemplate.find(query, Media.class);
        //Compose the paginated response
        return PageableExecutionUtils.getPage(
                list,
                pageable,
                () -> mongoTemplate.count(
                        Query
                                .of(query)
                                .limit(-1)
                                .skip(-1),
                        Media.class));
    }
}
