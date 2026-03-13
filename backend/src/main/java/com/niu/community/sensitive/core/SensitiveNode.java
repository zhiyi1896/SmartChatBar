package com.niu.community.sensitive.core;

import java.util.HashMap;
import java.util.Map;

public class SensitiveNode {
    private final Map<Character, SensitiveNode> children = new HashMap<>();
    private boolean end;

    public Map<Character, SensitiveNode> getChildren() {
        return children;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }
}
