package com.niu.community.sensitive.core;

import java.util.Set;

public class DfaSensitiveFilter {

    private final SensitiveNode root = new SensitiveNode();

    public void init(Set<String> words) {
        root.getChildren().clear();
        for (String word : words) {
            if (word == null || word.isBlank()) {
                continue;
            }
            addWord(word.trim());
        }
    }

    public boolean contains(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            SensitiveNode node = root;
            for (int j = i; j < chars.length; j++) {
                node = node.getChildren().get(chars[j]);
                if (node == null) {
                    break;
                }
                if (node.isEnd()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addWord(String word) {
        SensitiveNode node = root;
        for (char c : word.toCharArray()) {
            node = node.getChildren().computeIfAbsent(c, key -> new SensitiveNode());
        }
        node.setEnd(true);
    }
}
