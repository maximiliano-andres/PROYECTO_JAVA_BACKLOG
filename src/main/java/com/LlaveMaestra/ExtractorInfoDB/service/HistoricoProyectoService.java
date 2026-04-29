package com.LlaveMaestra.ExtractorInfoDB.service;

import com.LlaveMaestra.ExtractorInfoDB.adapter.HistoricoProyecto;
import com.LlaveMaestra.ExtractorInfoDB.adapter.HistoricoProyectoAdapter;
import com.LlaveMaestra.ExtractorInfoDB.database.DynamicQueryExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoricoProyectoService {

    private final DynamicQueryExecutor executor;
    private final HistoricoProyectoAdapter adapter;

    public HistoricoProyectoService(DynamicQueryExecutor executor,
            HistoricoProyectoAdapter adapter) {
        this.executor = executor;
        this.adapter = adapter;
    }

    public List<HistoricoProyecto> obtener(String sql) {

        return executor.execute(sql)
                .stream()
                .map(adapter::adapt)
                .collect(Collectors.toList());
    }
}