package com.tests;

import static org.junit.jupiter.api.Assertions.*;

import com.assignment.api.models.LogFile;
import com.assignment.api.repos.LogFileRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;

@SpringBootTest
public class LogFileRepositoryTests {

    private final LogFileRepo repo;

    @Autowired
    public LogFileRepositoryTests(LogFileRepo repo) {
        this.repo = repo;
    }

    @Test
    public void testInsertLogFile() {
        String fileName = "sarah_the_test_master.txt";
        LogFile logFile = LogFile.builder().fileName(fileName).fileSize(1000).patternsFound("sarahPattern").build();
        try {
            LogFile saved = repo.insert(logFile);
            assertTrue(repo.findById(fileName).isPresent(), "if saved- should be present");
        } catch (DuplicateKeyException dke) {
            // ok
        } catch (Exception e) {
            fail("Error");
        }
    }

    @Test
    public void testFindLogFile() {
        // Assume a log file is already saved
        try {
            assertTrue(repo.findById("sarah_the_test_master.txt").isPresent(), "if present- shouldn't be null");
        } catch (Exception e) {
            fail("Error");
        }
    }

    @Test
    public void testDeleteLogFile() {
        // Assume a log file is already saved
        try {
            repo.deleteById("sarah_the_test_master.txt");
            assertFalse(repo.findById("sarah_the_test_master.txt").isPresent(), "if deleted- should be null");
        } catch (Exception e) {
            fail("Error");
        }

    }
}
