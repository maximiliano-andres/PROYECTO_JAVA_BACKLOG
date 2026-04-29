package com.LlaveMaestra.ExtractorInfoDB.adapter;

import java.util.Map;

public interface SchemaAdapter<T> {

    T adapt(Map<String, Object> row);

}
