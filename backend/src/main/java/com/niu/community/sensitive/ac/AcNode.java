package com.niu.community.sensitive.ac;

import java.util.HashMap;
import java.util.Map;

public class AcNode {
    private final Map<Character, AcNode> children = new HashMap<>();
    private AcNode fail;
    private boolean end;

    public Map<Character, AcNode> getChildren() {
        return children;
    }

    public AcNode getFail() {
        return fail;
    }

    public void setFail(AcNode fail) {
        this.fail = fail;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }
}
