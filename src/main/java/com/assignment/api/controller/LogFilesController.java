package com.assignment.api.controller;

import com.assignment.api.models.LogFile;
import com.assignment.service.LogFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
public class LogFilesController {

    private final LogFileService logFileService;

    @Autowired
    public LogFilesController(LogFileService logFileService) {
        this.logFileService = logFileService;
    }

    @PostMapping("/logFile")
    public ResponseEntity<?> CheckForPatternsAndUpdate(@RequestParam("files") MultipartFile[] logFiles) {
        try {
            logFileService.CheckForPatternsAndUpdate(logFiles);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            log.error("Error processing file upload", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }

    @GetMapping("/logFile")
    public ResponseEntity<List<LogFile>> getAllLogFiles() throws InterruptedException, ExecutionException {
        try {
            List<LogFile> res = logFileService.getAllLogFiles();
            return ResponseEntity.ok().body(res);
        } catch (Exception ex) {
            log.error("Error retrieving log files", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/logFile")
    public ResponseEntity<String> deleteLogFile(@RequestParam String logFileName) {
        try {
            String deletedDoc = logFileService.deleteLogFile(logFileName);
            return ResponseEntity.ok().body(deletedDoc);
        } catch (Exception ex) {
            log.error("Error deleting log file", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }

    @RequestMapping("/**")
    public ResponseEntity<String> handleFallback() {
        return new ResponseEntity<>("No matching endpoint found", HttpStatus.NOT_FOUND);
    }
}
