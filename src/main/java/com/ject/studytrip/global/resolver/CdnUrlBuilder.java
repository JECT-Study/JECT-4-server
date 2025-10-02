package com.ject.studytrip.global.resolver;

import com.ject.studytrip.global.config.properties.CdnProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CdnUrlBuilder {
    private final CdnProperties props;

    public String build(String key) {
        return props.domain() + "/" + key;
    }
}
