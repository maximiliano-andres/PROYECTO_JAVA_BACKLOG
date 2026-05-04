const state = {
    selectedDb: null,
    selectedTable: null,
    selectedSchema: null,
    databases: [],
    tables: [],
    currentPage: 0,
    pageSize: 500,
    totalPages: 0,
    totalElements: 0,
    currentColumns: []
};

document.addEventListener('DOMContentLoaded', () => {
    loadDatabases();

    document.getElementById('menuToggle').onclick = () => {
        document.getElementById('sidebar').classList.toggle('open');
    };
});

/* ===== Utils ===== */

function escapeHTML(str) {
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
}

function renderError(msg) {
    return `<div class="empty-state"><p style="color:red;">${msg}</p></div>`;
}

/* ===== Fetch ===== */

async function loadDatabases() {
    const container = document.getElementById('dbList');
    container.innerHTML = '<div class="loader"><div class="spinner"></div></div>';

    try {
        const res = await fetch('/api/v1/explorer/databases');
        const wrapper = await res.json();
        
        if (!wrapper.ok) {
            container.innerHTML = renderError(wrapper.descripcion);
            return;
        }
        
        state.databases = wrapper.data || [];
        renderDatabases();
    } catch (e) {
        container.innerHTML = renderError(e.message);
    }
}

async function loadTables(db) {
    state.selectedDb = db;
    document.getElementById('currentDb').innerText = db;

    const view = document.getElementById('viewArea');
    view.innerHTML = '<div class="loader"><div class="spinner"></div></div>';

    try {
        const res = await fetch(`/api/v1/explorer/tables?database=${db}`);
        const wrapper = await res.json();
        
        if (!wrapper.ok) {
            view.innerHTML = renderError(wrapper.descripcion);
            return;
        }
        
        state.tables = wrapper.data || [];
        renderTables();
    } catch (e) {
        view.innerHTML = renderError(e.message);
    }
}

async function loadTableData(table, schema, page = 0) {
    state.selectedTable = table;
    state.selectedSchema = schema;
    state.currentPage = page;

    const view = document.getElementById('viewArea');
    view.innerHTML = '<div class="loader"><div class="spinner"></div></div>';

    try {
        const [colsWrapper, dataWrapper] = await Promise.all([
            fetch(`/api/v1/explorer/columns?database=${state.selectedDb}&schema=${schema}&table=${table}`).then(r => r.json()),
            fetch(`/api/v1/explorer/data?database=${state.selectedDb}&schema=${schema}&table=${table}&page=${page}&size=${state.pageSize}`).then(r => r.json())
        ]);

        if (!colsWrapper.ok || !dataWrapper.ok) {
            view.innerHTML = renderError(colsWrapper.descripcion || dataWrapper.descripcion);
            return;
        }

        state.currentColumns = colsWrapper.data || [];
        const pageData = dataWrapper.data || { content: [], totalPages: 0, totalElements: 0 };
        
        state.totalPages = pageData.totalPages;
        state.totalElements = pageData.totalElements;

        renderData(state.currentColumns, pageData.content);
    } catch (e) {
        view.innerHTML = renderError(e.message);
    }
}

async function changePage(newPage) {
    if (newPage >= 0 && newPage < state.totalPages) {
        await loadTableData(state.selectedTable, state.selectedSchema, newPage);
    }
}

/* ===== Render ===== */

function renderDatabases() {
    const container = document.getElementById('dbList');
    container.innerHTML = '';

    state.databases.forEach(db => {
        const el = document.createElement('div');
        el.className = 'db-item';
        el.innerText = db.name;
        el.onclick = () => loadTables(db.name);
        container.appendChild(el);
    });
}

function renderTables() {
    const view = document.getElementById('viewArea');
    view.innerHTML = '<div class="grid"></div>';

    const grid = view.firstChild;

    state.tables.forEach(t => {
        const card = document.createElement('div');
        card.className = 'table-card';
        card.innerHTML = `<b>${t.name}</b><br><small>${t.schema}</small>`;
        card.onclick = () => loadTableData(t.name, t.schema);
        grid.appendChild(card);
    });
}

function renderData(columns, data) {
    const view = document.getElementById('viewArea');
    const startIdx = (state.currentPage * state.pageSize) + 1;

    let html = `
        <div style="display:flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <button class="btn" onclick="loadTables('${state.selectedDb}')">← Volver a Tablas</button>
            <div class="pagination-info">
                Mostrando <b>${data.length}</b> de <b>${state.totalElements.toLocaleString()}</b> registros
            </div>
        </div>

        <div class="data-container">
            <table>
                <thead>
                    <tr> 
                        <th style="width: 50px;">#</th>
                        ${columns.map(c => `<th>${escapeHTML(c.name)}</th>`).join('')}
                    </tr>
                </thead>
                <tbody>
                    ${data.length === 0 ? '<tr><td colspan="100%" style="text-align:center; padding: 40px;">No hay datos en esta página</td></tr>' : ''}
                    ${data.map((row, i) => `
                        <tr>
                            <td>${startIdx + i}</td>
                            ${columns.map(c => `
                                <td onclick="navigator.clipboard.writeText('${row[c.name.toLowerCase()] ?? ''}')">
                                    ${escapeHTML(row[c.name.toLowerCase()] ?? '')}
                                </td>
                            `).join('')}
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
        ${renderPagination()}
    `;

    view.innerHTML = html;
}

function renderPagination() {
    if (state.totalPages <= 1) return '';

    return `
        <div class="pagination">
            <button class="pagination-btn" ${state.currentPage === 0 ? 'disabled' : ''} onclick="changePage(${state.currentPage - 1})">
                ← Anterior
            </button>
            
            <span class="pagination-info">
                Página <b>${state.currentPage + 1}</b> de <b>${state.totalPages}</b>
            </span>

            <button class="pagination-btn" ${state.currentPage >= state.totalPages - 1 ? 'disabled' : ''} onclick="changePage(${state.currentPage + 1})">
                Siguiente →
            </button>
        </div>
    `;
}

/* ===== Actions ===== */

function refresh() {
    if (state.selectedTable) {
        loadTableData(state.selectedTable);
    } else if (state.selectedDb) {
        loadTables(state.selectedDb);
    }
}