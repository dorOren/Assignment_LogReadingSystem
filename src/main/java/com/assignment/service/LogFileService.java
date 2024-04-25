package com.assignment.service;

import com.assignment.api.models.LogFile;
import com.assignment.api.repos.LogFileRepo;
import com.assignment.common.TreeNode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class LogFileService {
    private final LogFileRepo repo;
    private final DbRequestQueue dbRequestQueue = new DbRequestQueue(2);

    public List<LogFile> getAllLogFiles() throws InterruptedException, ExecutionException {
        return dbRequestQueue.submitDbOperation(repo::findAll).get();
    }

    public String deleteLogFile(String logFileName) {
        dbRequestQueue.submitDbOperation(() -> {
            repo.deleteById(logFileName);
            return logFileName;
        });
        return null;
    }

    public void CheckForPatternsAndUpdate(MultipartFile[] logFiles) throws IOException, ExecutionException, InterruptedException {
        TreeNode patternsTree = parsePatterns();
        Map<String, Future<String>> patternsFoundByFileName = matchPattensInFiles(logFiles, patternsTree);
        List<LogFile> models = new ArrayList<>();

        if (patternsFoundByFileName != null) {
            for (int i = 0; i < logFiles.length; i++) {
                var patterns = patternsFoundByFileName.get(logFiles[i].getOriginalFilename());
                models.add(LogFile.builder()
                        .fileName(logFiles[i].getOriginalFilename())
                        .fileSize(logFiles[i].getSize())
                        .patternsFound(patterns != null ? (patterns).get() : null)
                        .build());
            }
        }

        dbRequestQueue.submitDbOperation(() -> {
            repo.insert(models);
            return null;
        });
    }

    private Map<String, Future<String>> matchPattensInFiles(MultipartFile[] logFiles, TreeNode patternsTree) {
        ExecutorService executor = Executors.newFixedThreadPool(logFiles.length);
        Map<String, Future<String>> results = new HashMap<>();

        for (MultipartFile logFile : logFiles) {
            Future<String> future = executor.submit(() -> {
                try {
                    return readFile(logFile, patternsTree);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return null;
                }
            });
            results.put(logFile.getOriginalFilename(), future);
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            return results;
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    private String readFile(MultipartFile logFile, TreeNode patternsTree) throws IOException {
        InputStream inputStream = logFile.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        // first is dummyHead with data = "root"
        // we ALWAYS need to check for the roots, because they can appear line after line in the file
        Queue<TreeNode> treeRoots = new ConcurrentLinkedQueue<>(patternsTree.getChildren());

        // we iterate on all non-root nodes at the same, which helps us to continue reading and not go back
        Queue<TreeNode> treeNonRoots = new ConcurrentLinkedQueue<>();

        // if we arrive to a leaf, it means we found a pattern
        StringBuilder patternsFound = new StringBuilder();


        while ((line = reader.readLine()) != null) {
            String lastMatch;
            String[] parts;

            for (TreeNode treeNonRoot : treeNonRoots) {
                parts = treeNonRoot.getData().split("\\\\");
                lastMatch = parts[parts.length - 1];
                if (line.contains(lastMatch)) {
                    if (treeNonRoot.isLeaf()) {
                        patternsFound.append(treeNonRoot.getData()).append("\\\\");
                    }
                    treeNonRoots.remove(treeNonRoot);
                    if (!treeNonRoot.getChildren().isEmpty())
                        treeNonRoots.addAll(treeNonRoot.getChildren().stream()
                                .filter(child -> !treeNonRoots.contains(child))
                                .toList());

                } else {
                    treeNonRoots.remove(treeNonRoot);
                }
            }

            for (TreeNode treeRoot : treeRoots) {
                parts = treeRoot.getData().split("\\\\");
                lastMatch = parts[parts.length - 1];

                if (line.contains(lastMatch)) {
                    var children = treeRoot.getChildren();
                    if (children.isEmpty())
                        patternsFound.append(treeRoot.getData()).append("\\\\");
                    else
                        treeNonRoots.addAll(children.stream()
                                .filter(child -> !treeNonRoots.contains(child))
                                .toList());
                }
            }
        }

        if (patternsFound.isEmpty())
            return null;
        else
            return patternsFound.substring(0, patternsFound.length() - 2);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> addNestedMap(Map<String, Object> currentLevel, List<String> keys, int index) {
        if (index < keys.size() - 1) {
            currentLevel.putIfAbsent(keys.get(index), new HashMap<>());
            return addNestedMap((Map<String, Object>) currentLevel.get(keys.get(index)), keys, index + 1);
        } else {
            currentLevel.put(keys.get(index), new HashMap<>());
            return currentLevel;
        }
    }

    private TreeNode parsePatterns() throws IOException {
        TreeNode patternsTree = new TreeNode("root");
        ClassPathResource resource = new ClassPathResource("logfiles/patterns.py");
        InputStream is = resource.getInputStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            String patternLine;
            List<String> visitedPatternParts = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                // We start iterating over a new pattern, store previous one
                if (line.startsWith("pattern")) {
                    if (!visitedPatternParts.isEmpty())
                        patternsTree.insertByDataSequence(visitedPatternParts.toArray(String[]::new));

                    visitedPatternParts.clear();
                    patternLine = line.split("=")[1].trim().replaceAll("\"", "").replace("\\", "").trim();

                    // We wait until we finish iterate over the pattern and then store it
                } else
                    patternLine = line.trim().replaceAll("\"", "").replace("\\", "").trim();

                visitedPatternParts.add(patternLine);
            }

            if (!visitedPatternParts.isEmpty())
                patternsTree.insertByDataSequence(visitedPatternParts.toArray(String[]::new));
        }
        return patternsTree;
    }
}
