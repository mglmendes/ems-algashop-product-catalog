package com.algaworks.algashop.product.catalog.infrastructure.persistence.dataload;

import com.algaworks.algashop.product.catalog.infrastructure.utility.AlgaShopResourceUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonArray;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements ApplicationRunner {

    private final MongoOperations mongoOperations;

    private final DataLoadProperties dataLoadProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!dataLoadProperties.getEnabled()) {
            return;
        }
        log.info("Data Load started");
        if (dataLoadProperties.getSources() == null) {
            log.info("No sources configured");
            return;
        }

        dataLoadProperties.getSources().forEach(this::importJsonFileToCollection);
    }

    private void importJsonFileToCollection(DataLoadProperties.DataLoadSource source) {
        String rawJson = AlgaShopResourceUtils.readContent(source.getLocation());
        if (StringUtils.isBlank(rawJson)) {
            log.warn("No data found at {} ", source.getLocation());
            return;
        }

        List<Document> docs = parseRawJsonToDocuments(rawJson);
        int inserted = insertInto(docs, source.getCollection());
        log.info("{} - Imports: {}/{}", source.getLocation(), inserted, docs.size());
    }

    private List<Document> parseRawJsonToDocuments(String rawJson) {
        try {
            BsonArray bsonArray = BsonArray.parse(rawJson);
            return bsonArray.stream().map(Object::toString).map(Document::parse).collect(Collectors.toList());
        }  catch (Exception e) {
            log.error("Failed to parse raw JSON at location {}", rawJson, e);
            return Collections.emptyList();
        }
    }

    private int insertInto(List<Document> mongoDocs, @NotBlank String collectionName) {
        if (mongoDocs == null && mongoDocs.isEmpty()) {
            return 0;
        }

        try {
            if (Boolean.TRUE.equals(dataLoadProperties.getAutoDrop())) {
                mongoOperations.getCollection(collectionName).drop();
            }
            return mongoOperations.insert(mongoDocs, collectionName).size();
        } catch (Exception e) {
            log.error("Error inserting documents into {}: {}", collectionName, e.getMessage(), e);
        }
        return 0;
    }
}
