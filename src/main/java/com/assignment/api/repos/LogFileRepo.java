package com.assignment.api.repos;

import com.assignment.api.models.LogFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogFileRepo extends MongoRepository<LogFile, String> {
}
