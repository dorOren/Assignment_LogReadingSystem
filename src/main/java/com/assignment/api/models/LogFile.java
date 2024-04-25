package com.assignment.api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@Builder
public class LogFile {

    @Id
    private String fileName;
    private long fileSize;
    private String patternsFound;
}
