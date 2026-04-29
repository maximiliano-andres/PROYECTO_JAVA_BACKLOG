const state = {
    selectedDb: null,
    selectedTable: null,
    databases: [],
    tables: []
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
        state.databases = await res.json();
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
        state.tables = await res.json();
        renderTables();
    } catch (e) {
        view.innerHTML = renderError(e.message);
    }
}

async function loadTableData(table, schema) {
    state.selectedTable = table;

    const view = document.getElementById('viewArea');
    view.innerHTML = '<div class="loader"><div class="spinner"></div></div>';

    try {
        const [cols, data] = await Promise.all([
            fetch(`/api/v1/explorer/columns?database=${state.selectedDb}&schema=${schema}&table=${table}`).then(r => r.json()),
            fetch(`/api/v1/explorer/data?database=${state.selectedDb}&schema=${schema}&table=${table}&limit=50`).then(r => r.json())
        ]);

        renderData(cols, data);
    } catch (e) {
        view.innerHTML = renderError(e.message);
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

    let html = `
        <div>
            <button class="btn" onclick="loadTables('${state.selectedDb}')">← Volver</button>
        </div>
        <div class="data-container">
            <table>
                <thead>
                    <tr>
                        <th>#</th>
                        ${columns.map(c => `<th>${escapeHTML(c.name)}</th>`).join('')}
                    </tr>
                </thead>
                <tbody>
                    ${data.map((row, i) => `
                        <tr>
                            <td>${i + 1}</td>
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
    `;

    view.innerHTML = html;
}

/* ===== Actions ===== */

function refresh() {
    if (state.selectedTable) {
        loadTableData(state.selectedTable);
    } else if (state.selectedDb) {
        loadTables(state.selectedDb);
    }
}