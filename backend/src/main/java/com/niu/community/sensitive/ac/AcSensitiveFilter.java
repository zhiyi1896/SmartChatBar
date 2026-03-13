package com.niu.community.sensitive.ac;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

public class AcSensitiveFilter {

    private final AcNode root = new AcNode();

    public void init(Set<String> words) {
        root.getChildren().clear();
        for (String word : words) {
            if (word == null || word.isBlank()) {
                continue;
            }
            addWord(normalize(word));
        }
        buildFailPointers();
    }

    public boolean contains(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = normalize(text);
        AcNode current = root;
        for (char c : normalized.toCharArray()) {
            while (current != root && !current.getChildren().containsKey(c)) {
                current = current.getFail();
            }
            current = current.getChildren().getOrDefault(c, root);
            AcNode temp = current;
            while (temp != null && temp != root) {
                if (temp.isEnd()) {
                    return true;
                }
                temp = temp.getFail();
            }
        }
        return false;
    }

    private void addWord(String word) {
        AcNode node = root;
        for (char c : word.toCharArray()) {
            node = node.getChildren().computeIfAbsent(c, key -> new AcNode());
        }
        node.setEnd(true);
    }

    private void buildFailPointers() {
        Queue<AcNode> queue = new ArrayDeque<>();
        for (AcNode child : root.getChildren().values()) {
            child.setFail(root);
            queue.offer(child);
        }
        while (!queue.isEmpty()) {
            AcNode current = queue.poll();
            current.getChildren().forEach((ch, child) -> {
                AcNode fail = current.getFail();
                while (fail != null && fail != root && !fail.getChildren().containsKey(ch)) {
                    fail = fail.getFail();
                }
                if (fail == null) {
                    child.setFail(root);
                } else {
                    child.setFail(fail.getChildren().getOrDefault(ch, root));
                }
                queue.offer(child);
            });
        }
    }

    private String normalize(String text) {
        StringBuilder builder = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetterOrDigit(c) || Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                builder.append(Character.toLowerCase(c));
            }
        }
        return builder.toString();
    }
}
