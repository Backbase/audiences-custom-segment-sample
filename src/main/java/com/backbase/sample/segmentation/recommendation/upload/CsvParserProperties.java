package com.backbase.sample.segmentation.recommendation.upload;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
    prefix = CsvParserProperties.PREFIX,
    ignoreUnknownFields = false,
    ignoreInvalidFields = false
)
public class CsvParserProperties {

    public static final String PREFIX = "backbase.custom-segment-sample.csv";

    private char delimiter = ';';

    private Headers headers = new Headers();

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    public static class Headers {

        private String userId = "Customer_Num";

        private String category = "Category";

        private String subCategory = "Sub_Category";

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getSubCategory() {
            return subCategory;
        }

        public void setSubCategory(String subCategory) {
            this.subCategory = subCategory;
        }
    }

}
