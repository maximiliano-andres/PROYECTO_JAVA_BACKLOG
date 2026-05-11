package com.LlaveMaestra.ExtractorInfoDB.controller;

import com.LlaveMaestra.ExtractorInfoDB.dto.ColumnInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.TableInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.PageData;
import com.LlaveMaestra.ExtractorInfoDB.service.metadata.MetadataService;
import com.LlaveMaestra.ExtractorInfoDB.service.query.QueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.LlaveMaestra.ExtractorInfoDB.util.Wrapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpSession;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/explorer")
@Slf4j
@RequiredArgsConstructor
public class DatabaseExplorerController {

    private final MetadataService metadataService;
    private final QueryService queryService;

    @GetMapping("/databases")
    public ResponseEntity<Wrapper<List<DatabaseInfo>>> listDatabases(HttpSession session) throws SQLException {

        List<DatabaseInfo> dbs = metadataService.getDatabases(session.getId());

        log.info("Databases found ({} en Total): {}", dbs.size(), dbs.stream().map(DatabaseInfo::getName).toList());

        return ResponseEntity.ok(Wrapper.success("Bases de datos listadas exitosamente", dbs));
    }

    @GetMapping("/tables")
    public ResponseEntity<Wrapper<List<TableInfo>>> listTables(
            @RequestParam(required = false) String database,
            @RequestParam(required = false) String schema,
            HttpSession session) throws SQLException {

        List<TableInfo> tablesDb = metadataService.getTables(database, schema, session.getId());

        log.info("TABLAS ENCONTRADAS TOTALES {}", tablesDb.size());

        return ResponseEntity.ok(Wrapper.success("Tablas listadas exitosamente", tablesDb));
    }

    @GetMapping("/columns")
    public ResponseEntity<Wrapper<List<ColumnInfo>>> listColumns(
            @RequestParam(required = false) String database,
            @RequestParam(required = false) String schema,
            @RequestParam String table,
            HttpSession session) throws SQLException {

        List<ColumnInfo> columns = metadataService.getColumns(database, schema, table, session.getId());

        log.info("COLUMNAS TOTALES EN: DB {}, SCHEMA {}, TABLA {}, total columnas: {}", database, schema, table,
                columns.size());

        return ResponseEntity.ok(Wrapper.success("Columnas listadas exitosamente", columns));
    }

    @GetMapping("/data")
    public ResponseEntity<Wrapper<PageData<Map<String, Object>>>> viewData(
            @RequestParam(required = false) String database,
            @RequestParam(required = false) String schema,
            @RequestParam String table,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size,
            HttpSession session) throws SQLException {

        PageData<Map<String, Object>> pagedData = queryService.getTableData(database, schema, table, page, size,
                session.getId());

        log.info("DATOS PAGINADOS EN: DB {}, SCHEMA {}, TABLA {}, página {}, total elementos: {}",
                database, schema, table, page, pagedData.getTotalElements());

        return ResponseEntity.ok(Wrapper.success("Datos recuperados exitosamente", pagedData));
    }
}
