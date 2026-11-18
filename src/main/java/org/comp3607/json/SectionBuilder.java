package org.comp3607.json;

import java.util.Map;

import org.comp3607.api.EventSource;

public interface SectionBuilder {

    String sectionName();
    Map<String, Object> build(EventSource source);
}
