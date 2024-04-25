package com.assignment.common;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class TreeNode {

    @Getter
    private final List<TreeNode> children = new ArrayList<TreeNode>();
    private TreeNode parent = null;
    @Getter
    @Setter
    private String data = null;

    public TreeNode(String data) {
        this.data = data;
    }

    public TreeNode(String data, TreeNode parent) {
        this.data = data;
        this.parent = parent;
    }

    public void addChild(String data) {
        TreeNode child = new TreeNode(data);
        child.parent = this;
        this.children.add(child);
    }


    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    public boolean containsChildWithData(String data) {
        return this.children.stream().anyMatch(child -> data.equals(child.getData()));
    }

    public void insertByDataSequence(String[] dataSequence) {
        insertByDataSequenceRec(dataSequence, 0, this, new StringBuilder());
    }

    private void insertByDataSequenceRec(String[] dataSequence, int index, TreeNode current, StringBuilder sb) {
        if (current == null || dataSequence.length == index) {
            return;
        }


        // MAY REMOVE THE IF
        if (!current.containsChildWithData(sb.toString() + dataSequence[index] + "\\")) {
            sb.append(dataSequence[index]);
            sb.append("\\");
            current.addChild(sb.toString());
        }

        TreeNode child =
                current.getChildren().stream()
                        .filter(listItem -> listItem.getData().contentEquals(sb))
                        .findFirst()
                        .orElse(null);

        insertByDataSequenceRec(dataSequence, index + 1, child, sb);
    }

    public TreeNode findByDataSequence(String[] dataSequence) {
        return findByDataSequenceRec(dataSequence, 0, this);
    }

    private TreeNode findByDataSequenceRec(String[] dataSequence, int index, TreeNode current) {
        if (current == null) {
            return null;
        }
        if (data == current.data && dataSequence.length - 1 == index) {
            return current;
        }

        if (dataSequence[index] == current.data) {
            for (var child : current.getChildren()) {
                return findByDataSequenceRec(dataSequence, index + 1, child);
            }
        }
        return null;
    }
}
