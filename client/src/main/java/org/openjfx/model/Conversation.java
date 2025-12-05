package org.openjfx.model;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    public String id;
    public String name;
    public boolean isGroup;

    public Conversation(String id, String name, boolean isGroup) {
        this.id = id;
        this.name = name;
        this.isGroup = isGroup;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isGroup() { return isGroup; }
    public void setGroup(boolean group) { isGroup = group; }
}
