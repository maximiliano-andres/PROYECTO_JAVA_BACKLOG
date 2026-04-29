/**
 * Database Explorer App Logic
 * Enterprise Level - Professional & Secure
 */

const state = {
    selectedDb: null,
    selectedTable: null,
    databases: [],
    tables: []
};

// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    loadDatabases();
});

// --- Fetching Logic ---

async function loadDatabases() {
    const dbListContainer = document.getElementById('dbList');
    try {
        const response = await fetch('/api/v1/explorer/databases');
        if (!response.ok) throw new Error('Error al cargar bases de datos');
        
        state.databases = await response.json();
        renderDatabases();
    } catch (error) {
        dbListContainer.innerHTML = `<div class="error" style="color: #f87171; font-size: 0.8rem;">${error.message}</div>`;
    }
}

async function loadTables(dbName) {
    state.selectedDb = dbName;
    state.selectedTable = null;
    
    // UI Update
    document.getElementById('currentDb').innerText = dbName;
    document.getElementById('currentTable').innerText = "Listado de tablas disponibles";
    renderDatabases(); // Update active state in sidebar
    
    const viewArea = document.getElementById('viewArea');
    viewArea.innerHTML = '<div class="loader">Cargando tablas...</div>';
    
    try {
        const response = await fetch(`/api/v1/explorer/tables?database=${dbName}`);
        if (!response.ok) throw new Error('Error al cargar tablas');
        
        state.tables = await response.json();
        renderTables();
    } catch (error) {
        viewArea.innerHTML = `<div class="error">${error.message}</div>`;
    }
}

async function loadTableData(tableName, schema) {
    state.selectedTable = tableName;
    document.getElementById('currentTable').innerText = `${schema}.${tableName}`;
    
    const viewArea = document.getElementById('viewArea');
    viewArea.innerHTML = '<div class="loader">Cargando registros...</div>';
    
    try {
        // Fetch columns and data in parallel
        const [colsRes, dataRes] = await Promise.all([
            fetch(`/api/v1/explorer/columns?database=${state.selectedDb}&schema=${schema}&table=${tableName}`),
            fetch(`/api/v1/explorer/data?database=${state.selectedDb}&schema=${schema}&table=${tableName}&limit=50`)
        ]);

        if (!colsRes.ok || !dataRes.ok) throw new Error('Error al cargar información de la tabla');

        const columns = await colsRes.json();
        const data = await dataRes.json();

        renderDataGrid(columns, data);
    } catch (error) {
        viewArea.innerHTML = `<div class="error">${error.message}</div>`;
    }
}

// --- Rendering Logic ---

function renderDatabases() {
    const container = document.getElementById('dbList');
    container.innerHTML = '';
    
    state.databases.forEach(db => {
        const item = document.createElement('div');
        item.className = `db-item ${state.selectedDb === db.name ? 'active' : ''}`;
        item.innerHTML = `
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21c-4.97 0-9-1.79-9-4s4.03-4 9-4 9 1.79 9 4-4.03 4-9 4Z"/><path d="M3 7c0 2.21 4.03 4 9 4s9-1.79 9-4-4.03-4-9-4-9 1.79-9 4Z"/><path d="M21 7v10"/><path d="M3 7v10"/><path d="M12 11v10"/></svg>
            ${db.name}
        `;
        item.onclick = () => loadTables(db.name);
        container.appendChild(item);
    });
}

function renderTables() {
    const container = document.getElementById('viewArea');
    container.innerHTML = '<div class="grid" id="tablesGrid"></div>';
    
    const grid = document.getElementById('tablesGrid');
    state.tables.forEach(table => {
        const card = document.createElement('div');
        card.className = 'table-card';
        card.innerHTML = `
            <div style="font-size: 0.8rem; color: var(--text-dim); margin-bottom: 5px;">${table.schema}</div>
            <div style="font-weight: 600;">${table.name}</div>
            <div style="font-size: 0.7rem; color: var(--primary); margin-top: 10px;">${table.type}</div>
        `;
        card.onclick = () => loadTableData(table.name, table.schema);
        grid.appendChild(card);
    });
}

function renderDataGrid(columns, data) {
    const container = document.getElementById('viewArea');
    
    let html = `
        <div style="margin-bottom: 20px;">
            <button class="btn btn-primary" onclick="loadTables('${state.selectedDb}')">← Volver a tablas</button>
        </div>
        <div class="data-container">
            <table>
                <thead>
                    <tr>
                        ${columns.map(col => `
                            <th title="${col.type} (Size: ${col.size})">
                                ${col.name}
                                <div style="font-size: 0.6rem; font-weight: normal; color: var(--text-dim);">${col.type}</div>
                            </th>
                        `).join('')}
                    </tr>
                </thead>
                <tbody>
                    ${data.length > 0 ? data.map(row => `
                        <tr>
                            ${columns.map(col => `<td>${row[col.name.toLowerCase()] ?? ''}</td>`).join('')}
                        </tr>
                    `).join('') : '<tr><td colspan="100%" style="text-align:center; padding: 40px;">No hay datos para mostrar</td></tr>'}
                </tbody>
            </table>
        </div>
    `;
    
    container.innerHTML = html;
}