package com.LlaveMaestra.ExtractorInfoDB.controller;

import com.LlaveMaestra.ExtractorInfoDB.dto.ColumnInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.TableInfo;
import com.LlaveMaestra.ExtractorInfoDB.service.DatabaseExplorerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/explorer")
public class DatabaseExplorerController {

    private final DatabaseExplorerService explorerService;

    public DatabaseExplorerController(DatabaseExplorerService explorerService) {
        this.explorerService = explorerService;
    }

    @GetMapping("/databases")
    public ResponseEntity<List<DatabaseInfo>> listDatabases() throws SQLException {
        return ResponseEntity.ok(explorerService.getDatabases());
    }

    @GetMapping("/tables")
    public ResponseEntity<List<TableInfo>> listTables(
            @RequestParam(required = false) String database) throws SQLException {
        return ResponseEntity.ok(explorerService.getTables(database));
    }

    @GetMapping("/columns")
    public ResponseEntity<List<ColumnInfo>> listColumns(
            @RequestParam(required = false) String database,
            @RequestParam(required = false) String schema,
            @RequestParam String table) throws SQLException {
        return ResponseEntity.ok(explorerService.getColumns(database, schema, table));
    }

    @GetMapping("/data")
    public ResponseEntity<List<Map<String, Object>>> viewData(
            @RequestParam(required = false) String database,
            @RequestParam(required = false) String schema,
            @RequestParam String table,
            @RequestParam(defaultValue = "100") int limit) throws SQLException {
        return ResponseEntity.ok(explorerService.getTableData(database, schema, table, limit));
    }
}
