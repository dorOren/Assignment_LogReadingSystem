package com.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.assignment.api.controller.LogFilesController;
import com.assignment.api.models.LogFile;
import com.assignment.service.LogFileService;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;

import java.util.List;

@WebMvcTest(LogFilesController.class)
public class LogFileControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogFileService logFileService;

    @Test
    public void testFallbackEndpoint() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/some/undefined/route"))
                .andExpect(status().isNotFound())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        assertEquals("No matching endpoint found", response);
    }

    @Test
    public void testInvalidFileUpload() throws Exception {
        mockMvc.perform(multipart("/logFile"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInvalidLogFileDeletionRequest() throws Exception {
        mockMvc.perform(delete("/logFile"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUnmatchedRoute() throws Exception {
        mockMvc.perform(get("/undefinedRoute"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCheckForPatternsAndUpdate() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("files", "testFile.txt", "text/plain", "some content".getBytes());

        mockMvc.perform(multipart("/logFile").file(mockFile))
                .andExpect(status().isOk());

        verify(logFileService).CheckForPatternsAndUpdate(any());
    }

    @Test
    public void testGetAllLogFiles() throws Exception {
        // Assume only 1 file is in the DB
        List<LogFile> logFiles = List.of(LogFile.builder().fileName("sarah_the_queen").fileSize(1000).patternsFound("sarahPattern").build());
        given(logFileService.getAllLogFiles()).willReturn(logFiles);

        mockMvc.perform(get("/logFile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(logFileService).getAllLogFiles();
    }

    @Test
    public void testDeleteLogFile() throws Exception {
        String logFileName = "testFile.txt";
        given(logFileService.deleteLogFile(logFileName)).willReturn(logFileName);

        mockMvc.perform(delete("/logFile").param("logFileName", logFileName))
                .andExpect(status().isOk())
                .andExpect(content().string(logFileName));

        verify(logFileService).deleteLogFile(logFileName);
    }
}


