package com.backbase.sample.segmentation.recommendation.upload;

import static java.util.Objects.requireNonNull;

import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.sample.segmentation.recommendation.RecommendationEntry;
import com.backbase.sample.segmentation.recommendation.RecommendationReport;
import com.backbase.sample.segmentation.recommendation.RecommendationRepository;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RecommendationReportStorageService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationReportStorageService.class);

    private final CsvParserProperties csvParserProperties;

    private final RecommendationRepository recommendationRepository;

    public RecommendationReportStorageService(
        CsvParserProperties csvParserProperties,
        RecommendationRepository recommendationRepository) {
        this.csvParserProperties = csvParserProperties;
        this.recommendationRepository = recommendationRepository;
    }

    public void store(MultipartFile file) {
        log.info("Storing file {}", file.getOriginalFilename());
        try (Reader reader = new InputStreamReader(asBOMInputStream(file))) {
            try (CSVParser csvParser = getCsvParser(reader)) {
                RecommendationReport recommendationReport = new RecommendationReport();
                recommendationReport.setCreatedAt(LocalDateTime.now());
                for (CSVRecord csvRecord : csvParser) {
                    RecommendationEntry entry = new RecommendationEntry();
                    entry.setUserExternalId(readColumn(csvRecord, csvParserProperties.getHeaders().getUserId()));
                    entry.setCategory(readColumn(csvRecord, csvParserProperties.getHeaders().getCategory()));
                    entry.setSubCategory(readColumn(csvRecord, csvParserProperties.getHeaders().getSubCategory()));
                    recommendationReport.addPatch(entry);
                }
                recommendationRepository.save(recommendationReport);
            }
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not store recommendation report", e);
        }
    }

    private BOMInputStream asBOMInputStream(MultipartFile file) throws IOException {
        return BOMInputStream.builder()
            .setInputStream(file.getInputStream())
            .setByteOrderMarks(ByteOrderMark.UTF_8)
            .setInclude(false)
            .get();
    }

    private CSVParser getCsvParser(Reader reader) throws IOException {
        Builder csvFormtBuilder = CSVFormat.Builder.create();
        csvFormtBuilder.setDelimiter(csvParserProperties.getDelimiter());
        csvFormtBuilder.setHeader();
        return csvFormtBuilder.build().parse(reader);
    }

    private static String readColumn(CSVRecord csvRecord, String columnName) {
        return requireNonNull(csvRecord.get(columnName)).strip();
    }


}
