package com.mapbox.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Upload {
    private String tileSet;

    private String url;

    private String name;
}
