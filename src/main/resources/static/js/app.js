"use strict";

/* ===== STATE ===== */

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
  currentColumns: [],
};

/* ===== INIT ===== */

document.addEventListener("DOMContentLoaded", () => {
  checkConnectionStatus();

  document.getElementById("menuToggle").addEventListener("click", () => {
    document.getElementById("sidebar").classList.toggle("open");
  });

  document.getElementById("sidebarToggle").addEventListener("click", () => {
    const collapsed = document.body.classList.toggle("sidebar-collapsed");
    const btn = document.getElementById("sidebarToggle");
    btn.setAttribute(
      "aria-label",
      collapsed ? "Mostrar panel lateral" : "Ocultar panel lateral",
    );
    btn.setAttribute(
      "title",
      collapsed ? "Mostrar panel lateral" : "Ocultar panel lateral",
    );
  });

  // Close sidebar when clicking outside on mobile
  document.getElementById("viewArea").addEventListener("click", () => {
    if (window.innerWidth <= 768) {
      document.getElementById("sidebar").classList.remove("open");
    }
  });
});

/* ===== UTILS ===== */

function escapeHTML(str) {
  const map = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;",
  };
  return String(str ?? "").replace(/[&<>"']/g, (m) => map[m]);
}

function renderError(msg) {
  const view = document.getElementById("viewArea");
  view.innerHTML = `
    <div class="error-state">
      <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
        <circle cx="8" cy="8" r="6.5" stroke="currentColor" stroke-width="1.3"/>
        <path d="M8 5v3.5M8 10.5v.5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
      </svg>
      ${escapeHTML(msg)}
    </div>`;
}

function setLoader(targetId) {
  document.getElementById(targetId).innerHTML =
    '<div class="loader"><div class="spinner"></div></div>';
}

function updateBreadcrumb(db, table) {
  document.getElementById("currentDb").textContent =
    db || "Seleccionar base de datos";

  const sep = document.getElementById("breadcrumbSep");
  const tElem = document.getElementById("breadcrumbTable");
  const tText = document.getElementById("currentTable");

  if (table) {
    sep.style.display = "";
    tElem.style.display = "";
    tText.textContent = table;
  } else {
    sep.style.display = "none";
    tElem.style.display = "none";
  }
}

/* ===== CONNECTION LOGIC ===== */

async function checkConnectionStatus() {
  try {
    const wrapper = await apiFetch("/api/connection/status");
    const status = wrapper.data;
    
    if (status && status.connected) {
      hideConnectionScreen();
      updateStatusBadge(status);
      
      // Si no es modo admin, cargamos directamente las tablas de la base de datos conectada
      if (!status.adminMode && status.databaseName) {
        loadTables(status.databaseName);
      } else {
        loadDatabases();
      }
    } else {
      showConnectionScreen();
    }
  } catch (e) {
    console.error("Error al verificar estado de conexión:", e);
    showConnectionScreen();
  }
}

async function testConnection() {
  const config = getConnectionFormData();
  showNotification("Probando conexión...", "info");
  
  try {
    const res = await fetch("/api/connection/test", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(config)
    });
    const wrapper = await res.json();
    
    if (wrapper.ok) {
        showNotification(wrapper.descripcion || "Conexión exitosa", "success");
    } else {
        showNotification(wrapper.descripcion || "Error de conexión", "error");
    }
  } catch (e) {
    showNotification("Error al intentar probar la conexión.", "error");
  }
}

async function connect(event) {
  if (event) event.preventDefault();
  const config = getConnectionFormData();
  const btn = document.getElementById("btnSubmitConnect");
  
  showNotification("Conectando...", "info");
  btn.disabled = true;
  
  try {
    const res = await fetch("/api/connection/connect", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(config)
    });
    const wrapper = await res.json();
    
    if (wrapper.ok) {
      const status = wrapper.data;
      showNotification("¡Conexión establecida!", "success");
      setTimeout(() => {
        hideConnectionScreen();
        updateStatusBadge(status);
        
        // Salto directo si no es modo admin
        if (!status.adminMode) {
            loadTables(status.databaseName);
        } else {
            loadDatabases();
        }
      }, 800);
    } else {
      showNotification(wrapper.descripcion || "Error al conectar", "error");
      btn.disabled = false;
    }
  } catch (e) {
    showNotification("Error inesperado al conectar.", "error");
    btn.disabled = false;
  }
}

async function disconnect() {
  try {
    await fetch("/api/connection/disconnect", { method: "POST" });
    showConnectionScreen();
    document.getElementById("dbList").innerHTML = "";
    renderWelcomeState();
  } catch (e) {
    console.error("Error al desconectar:", e);
  }
}

function getConnectionFormData() {
  return {
    engine: document.querySelector('input[name="engine"]:checked').value,
    host: document.getElementById("host").value,
    port: parseInt(document.getElementById("port").value),
    databaseName: document.getElementById("databaseName").value,
    username: document.getElementById("username").value,
    password: document.getElementById("password").value,
    adminMode: document.getElementById("adminMode").checked
  };
}

function showNotification(message, type = "info") {
    const container = document.getElementById("notificationContainer");
    const notification = document.createElement("div");
    notification.className = `notification ${type}`;
    
    const icon = document.createElement("div");
    icon.className = "notification-icon";
    // Podríamos añadir iconos SVG aquí según el tipo
    
    const text = document.createElement("div");
    text.className = "notification-text";
    text.textContent = message;
    
    notification.appendChild(icon);
    notification.appendChild(text);
    container.appendChild(notification);
    
    setTimeout(() => {
        notification.classList.add("fade-out");
        setTimeout(() => notification.remove(), 300);
    }, 4000);
}

function showConnectionScreen() {
  document.getElementById("connectionScreen").classList.remove("hidden");
  document.getElementById("btnDisconnect").style.display = "none";
  document.getElementById("btnSubmitConnect").disabled = false;
  document.getElementById("connectionStatusMsg").textContent = "";
}

function hideConnectionScreen() {
  document.getElementById("connectionScreen").classList.add("hidden");
  document.getElementById("btnDisconnect").style.display = "";
}

function updateStatusBadge(status) {
  const badge = document.getElementById("statusBadge");
  
  if (status.connected) {
    badge.className = "status-badge";
    badge.innerHTML = `
      <span class="status-dot"></span>
      <div class="badge-info">
        <span class="badge-label">Usuario:</span>
        <span class="badge-value">${escapeHTML(status.username)}</span>
      </div>
      <div class="badge-separator"></div>
      <div class="badge-info">
        <span class="badge-label">Servidor:</span>
        <span class="badge-value">${status.engine.toUpperCase()} - ${status.host}</span>
      </div>
    `;
  } else {
    badge.className = "status-badge disconnected";
    badge.innerHTML = `
      <span class="status-dot"></span>
      <span id="connectionStatusText">Desconectado</span>
    `;
  }
}

function renderWelcomeState() {
  const view = document.getElementById("viewArea");
  view.innerHTML = `
    <div class="welcome-state">
        <div class="welcome-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <rect x="2" y="2" width="7" height="7" rx="2" fill="currentColor" opacity="0.9"/>
            <rect x="11" y="2" width="7" height="7" rx="2" fill="currentColor" opacity="0.6"/>
            <rect x="2" y="11" width="7" height="7" rx="2" fill="currentColor" opacity="0.6"/>
            <rect x="11" y="11" width="7" height="7" rx="2" fill="currentColor" opacity="0.3"/>
        </svg>
        </div>
        <h2 class="welcome-title">Bienvenido al Explorador de datos</h2>
        <p class="welcome-desc">Seleccione una base de datos del panel izquierdo para ver sus tablas y contenido.</p>
    </div>`;
  updateBreadcrumb(null, null);
}

/* ===== API ===== */

async function apiFetch(url) {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

/* ===== DATA LOADERS ===== */

async function loadDatabases() {
  setLoader("dbList");
  try {
    const wrapper = await apiFetch("/api/v1/explorer/databases");
    if (!wrapper.ok) {
      renderDbError(wrapper.descripcion);
      return;
    }

    state.databases = wrapper.data ?? [];
    renderDatabases();
  } catch (e) {
    renderDbError(e.message);
  }
}

async function loadTables(db) {
  state.selectedDb = db;
  updateBreadcrumb(db, null);
  setActiveDb(db);
  setLoader("viewArea");

  try {
    const wrapper = await apiFetch(
      `/api/v1/explorer/tables?database=${encodeURIComponent(db)}`,
    );
    if (!wrapper.ok) {
      renderError(wrapper.descripcion);
      return;
    }

    state.tables = wrapper.data ?? [];
    renderTables();
  } catch (e) {
    renderError(e.message);
  }
}

async function loadTableData(table, schema, page = 0) {
  state.selectedTable = table;
  state.selectedSchema = schema;
  state.currentPage = page;
  updateBreadcrumb(state.selectedDb, table);
  setLoader("viewArea");

  try {
    const db = encodeURIComponent(state.selectedDb);
    const sc = encodeURIComponent(schema);
    const tb = encodeURIComponent(table);

    const [colsWrapper, dataWrapper] = await Promise.all([
      apiFetch(
        `/api/v1/explorer/columns?database=${db}&schema=${sc}&table=${tb}`,
      ),
      apiFetch(
        `/api/v1/explorer/data?database=${db}&schema=${sc}&table=${tb}&page=${page}&size=${state.pageSize}`,
      ),
    ]);

    if (!colsWrapper.ok || !dataWrapper.ok) {
      renderError(colsWrapper.descripcion || dataWrapper.descripcion);
      return;
    }

    state.currentColumns = colsWrapper.data ?? [];
    const pageData = dataWrapper.data ?? {
      content: [],
      totalPages: 0,
      totalElements: 0,
    };

    state.totalPages = pageData.totalPages;
    state.totalElements = pageData.totalElements;

    renderData(state.currentColumns, pageData.content);
  } catch (e) {
    renderError(e.message);
  }
}

/* ===== RENDERERS ===== */

function renderDbError(msg) {
  document.getElementById("dbList").innerHTML =
    `<p style="color:#C0392B;font-size:12px;padding:10px 6px;">${escapeHTML(msg)}</p>`;
}

function setActiveDb(name) {
  document.querySelectorAll(".db-item").forEach((el) => {
    el.classList.toggle("active", el.dataset.db === name);
  });
}

function renderDatabases() {
  const container = document.getElementById("dbList");
  container.innerHTML = "";

  if (!state.databases.length) {
    container.innerHTML =
      '<p style="font-size:12px;color:#7A7977;padding:10px 6px;">Sin bases de datos disponibles.</p>';
    return;
  }

  const frag = document.createDocumentFragment();
  state.databases.forEach((db) => {
    const el = document.createElement("div");
    el.className = "db-item";
    el.dataset.db = db.name;
    el.textContent = db.name;
    el.addEventListener("click", () => loadTables(db.name));
    frag.appendChild(el);
  });
  container.appendChild(frag);
}

function renderTables() {
  const view = document.getElementById("viewArea");

  const wrap = document.createElement("div");
  wrap.innerHTML = `
    <div class="section-header">
      <span class="section-title">Tablas de ${state.selectedDb}</span>
      <span class="section-count">${state.tables.length}</span>
    </div>
    <div class="grid" id="tableGrid"></div>`;

  view.innerHTML = "";
  view.appendChild(wrap);

  if (!state.tables.length) {
    document.getElementById("tableGrid").innerHTML =
      '<p style="font-size:13px;color:var(--text-tertiary)">Sin tablas disponibles.</p>';
    return;
  }

  const frag = document.createDocumentFragment();
  state.tables.forEach((t) => {
    const card = document.createElement("div");
    card.className = "table-card";
    card.innerHTML = `
      <div class="table-card-icon">
        <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
          <rect x="1.5" y="3.5" width="15" height="11" rx="2" stroke="currentColor" stroke-width="1.3"/>
          <path d="M1.5 7.5h15M6.5 3.5v11" stroke="currentColor" stroke-width="1.3"/>
        </svg>
      </div>
      <div class="table-card-name" title="${escapeHTML(t.name)}">${escapeHTML(t.name)}</div>
      <div class="table-card-schema">${escapeHTML(t.schema)}</div>`;
    card.addEventListener("click", () => loadTableData(t.name, t.schema));
    frag.appendChild(card);
  });

  document.getElementById("tableGrid").appendChild(frag);
}

function renderData(columns, data) {
  const view = document.getElementById("viewArea");
  const startIdx = state.currentPage * state.pageSize + 1;
  const shown = data.length;
  const total = state.totalElements.toLocaleString("es");

  const hasNext = state.currentPage < state.totalPages - 1;
  const hasPrev = state.currentPage > 0;
  const showPagination = state.totalPages > 1;

  // Build HTML as template literal — fast, single DOM insertion
  const thead = `<tr><th>#</th>${columns.map((c) => `<th>${escapeHTML(c.name)}</th>`).join("")}</tr>`;

  const tbody = data
    .map((row, i) => {
      const cells = columns
        .map((c) => {
          const val = row[c.name.toLowerCase()] ?? "";
          return `<td title="${escapeHTML(String(val))}">${escapeHTML(String(val))}</td>`;
        })
        .join("");
      return `<tr><td>${startIdx + i}</td>${cells}</tr>`;
    })
    .join("");

  view.innerHTML = `
    <div class="data-toolbar">
      <div class="toolbar-left">
        <button class="btn btn-ghost btn-sm" onclick="loadTables('${escapeHTML(state.selectedDb)}')">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M9 11L4 7l5-4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          Volver
        </button>
      </div>
      <div class="toolbar-center">
        <span class="pagination-info">
          Mostrando <b>${shown}</b> de <b>${total}</b> registros
        </span>
      </div>
      <div class="toolbar-right">
        ${
          showPagination
            ? `
          <button class="btn btn-ghost btn-sm" onclick="prevPage()" ${hasPrev ? "" : "disabled"}>
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M9 11L4 7l5-4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Anterior
          </button>
          <button class="btn btn-ghost btn-sm" onclick="nextPage()" ${hasNext ? "" : "disabled"}>
            Siguiente
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M5 3l5 4-5 4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        `
            : ""
        }
        <button class="btn btn-primary btn-sm" onclick="openExportModal()">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M7 2v8M7 10l-3-3M7 10l3-3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12h10" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
          </svg>
          Exportar
        </button>
      </div>
    </div>
    <div class="table-info-card">
      <div class="table-info-left">
        <div class="brand-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <rect x="2" y="2" width="7" height="7" rx="2" fill="currentColor" opacity="0.9"/>
                <rect x="11" y="2" width="7" height="7" rx="2" fill="currentColor" opacity="0.6"/>
                <rect x="2" y="11" width="7" height="7" rx="2" fill="currentColor" opacity="0.6"/>
                <rect x="11" y="11" width="7" height="7" rx="2" fill="currentColor" opacity="0.3"/>
            </svg>
        </div>
        <div class="table-info-text">
          <h2 class="table-info-name">Datos de la Tabla: ${escapeHTML(state.selectedTable)}</h2>
          <div class="table-info-meta">
            <span class="meta-tag">DB: ${escapeHTML(state.selectedDb)}</span>
            
          </div>
        </div>
      </div>
      <div class="table-info-right">
        <div class="table-stat">
          <span class="table-stat-label">Registros</span>
          <span class="table-stat-value">${state.totalElements.toLocaleString("es")}</span>
        </div>
      </div>
    </div>
    <div class="data-wrapper">
      <div class="data-container">
        <table>
          <thead>${thead}</thead>
          <tbody>${tbody}</tbody>
        </table>
      </div>
      <div class="table-footer">
        <span class="table-info">Página ${state.currentPage + 1} de ${state.totalPages || 1}</span>
      </div>
    </div>`;
}

/* ===== PAGINATION ===== */

function nextPage() {
  if (state.currentPage < state.totalPages - 1) {
    loadTableData(
      state.selectedTable,
      state.selectedSchema,
      state.currentPage + 1,
    );
  }
}

function prevPage() {
  if (state.currentPage > 0) {
    loadTableData(
      state.selectedTable,
      state.selectedSchema,
      state.currentPage - 1,
    );
  }
}

/* ===== MODAL ===== */

function openExportModal() {
  document.getElementById("exportModal").classList.remove("hidden");
  renderColumnSelector();
  initScopeDefaults();
}

function closeExportModal() {
  document.getElementById("exportModal").classList.add("hidden");
}

// Close on backdrop click
document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("exportModal").addEventListener("click", (e) => {
    if (e.target === e.currentTarget) closeExportModal();
  });
});

/* ===== SCOPE LOGIC ===== */

const SCOPE_CTRL_MAP = {
  all: null,
  page: null,
  pages: "ctrlPages",
  rows: "ctrlRows",
  range: "ctrlRange",
};

function initScopeDefaults() {
  // Reset to "all" each time modal opens
  const allRadio = document.querySelector(
    'input[name="exportScope"][value="all"]',
  );
  if (allRadio) {
    allRadio.checked = true;
  }
  hideScopeControls();

  // Update dynamic descriptions
  const el = document.getElementById("scopePageDesc");
  if (el) {
    const start = state.currentPage * state.pageSize + 1;
    const end = start + state.pageSize - 1;
    el.textContent = `Filas ${start.toLocaleString("es")} – ${Math.min(end, state.totalElements).toLocaleString("es")}`;
  }
  const allDesc = document.getElementById("scopeAllDesc");
  if (allDesc && state.totalElements) {
    allDesc.textContent = `${state.totalElements.toLocaleString("es")} registros en total`;
  }

  // Defaults for sub-controls
  const pageTo = document.getElementById("pageTo");
  const rowsMax = document.getElementById("rowsMax");
  const rowTo = document.getElementById("rowTo");

  if (pageTo) pageTo.max = state.totalPages || 1;
  if (rowsMax)
    rowsMax.textContent = state.totalElements
      ? state.totalElements.toLocaleString("es") + " filas"
      : "—";
  if (rowTo) {
    rowTo.max = state.totalElements || 1;
    rowTo.value = Math.min(500, state.totalElements || 500);
  }

  // Wire up live estimates
  wirePageEstimate();
  wireRangeTotal();
}

function hideScopeControls() {
  document.getElementById("scopeControls").classList.add("hidden");
  document
    .querySelectorAll(".scope-ctrl")
    .forEach((el) => el.classList.remove("visible"));
}

function onScopeChange(value) {
  const wrapper = document.getElementById("scopeControls");
  const ctrlId = SCOPE_CTRL_MAP[value];

  document
    .querySelectorAll(".scope-ctrl")
    .forEach((el) => el.classList.remove("visible"));

  if (ctrlId) {
    wrapper.classList.remove("hidden");
    document.getElementById(ctrlId).classList.add("visible");
  } else {
    wrapper.classList.add("hidden");
  }
}

function wirePageEstimate() {
  const from = document.getElementById("pageFrom");
  const to = document.getElementById("pageTo");
  const est = document.getElementById("pagesEstimate");
  if (!from || !to || !est) return;

  const update = () => {
    const f = Math.max(1, parseInt(from.value) || 1);
    const t = Math.max(f, parseInt(to.value) || f);
    const pages = t - f + 1;
    est.textContent =
      (pages * state.pageSize).toLocaleString("es") + " filas (aprox.)";
  };

  from.addEventListener("input", update);
  to.addEventListener("input", update);
  update();
}

function wireRangeTotal() {
  const from = document.getElementById("rowFrom");
  const to = document.getElementById("rowTo");
  const total = document.getElementById("rangeTotal");
  if (!from || !to || !total) return;

  const update = () => {
    const f = Math.max(1, parseInt(from.value) || 1);
    const t = Math.max(f, parseInt(to.value) || f);
    total.textContent = (t - f + 1).toLocaleString("es") + " filas";
  };

  from.addEventListener("input", update);
  to.addEventListener("input", update);
  update();
}

function getExportScope() {
  const scope =
    document.querySelector('input[name="exportScope"]:checked')?.value ?? "all";
  const result = { mode: scope };

  switch (scope) {
    case "all":
      break;
    case "page":
      result.page = state.currentPage;
      result.size = state.pageSize;
      break;
    case "pages":
      result.pageFrom = parseInt(document.getElementById("pageFrom").value) - 1; // 0-based
      result.pageTo = parseInt(document.getElementById("pageTo").value) - 1;
      result.size = state.pageSize;
      break;
    case "rows":
      result.limit =
        parseInt(document.getElementById("rowCount").value) || 1000;
      break;
    case "range":
      result.rowFrom = parseInt(document.getElementById("rowFrom").value) || 1;
      result.rowTo = parseInt(document.getElementById("rowTo").value) || 500;
      break;
  }

  return result;
}

function renderColumnSelector() {
  const container = document.getElementById("columnsContainer");
  container.innerHTML = "";

  const frag = document.createDocumentFragment();
  state.currentColumns.forEach((col) => {
    const label = document.createElement("label");
    label.className = "col-checkbox";
    label.innerHTML = `<input type="checkbox" checked value="${escapeHTML(col.name)}"> ${escapeHTML(col.name)}`;
    frag.appendChild(label);
  });
  container.appendChild(frag);
}

function selectAllColumns(checked) {
  document
    .querySelectorAll('#columnsContainer input[type="checkbox"]')
    .forEach((cb) => {
      cb.checked = checked;
    });
}

function addFilter() {
  const container = document.getElementById("filtersContainer");
  const emptyNotice = document.getElementById("filtersEmpty");
  if (emptyNotice) emptyNotice.remove();

  const row = document.createElement("div");
  row.className = "filter-row";

  const colOptions = state.currentColumns
    .map(
      (c) =>
        `<option value="${escapeHTML(c.name)}">${escapeHTML(c.name)}</option>`,
    )
    .join("");

  row.innerHTML = `
    <select>${colOptions}</select>
    <select>
      <option value="=">=</option>
      <option value="LIKE">LIKE</option>
      <option value=">">&gt;</option>
      <option value="<">&lt;</option>
      <option value="!=">≠</option>
    </select>
    <input type="text" placeholder="Valor…">
    <button class="filter-remove" title="Eliminar filtro" onclick="removeFilter(this)">
      <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
        <path d="M2 2l8 8M10 2L2 10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
    </button>`;

  container.appendChild(row);
}

function removeFilter(btn) {
  const row = btn.closest(".filter-row");
  const container = document.getElementById("filtersContainer");
  row.remove();

  if (!container.querySelector(".filter-row")) {
    const empty = document.createElement("div");
    empty.id = "filtersEmpty";
    empty.className = "filters-empty";
    empty.innerHTML = "<span>Sin filtros aplicados</span>";
    container.appendChild(empty);
  }
}

function getSelectedColumns() {
  return [...document.querySelectorAll("#columnsContainer input:checked")].map(
    (cb) => cb.value,
  );
}

function getFilters() {
  return [...document.querySelectorAll("#filtersContainer .filter-row")].map(
    (row) => {
      const [colSel, opSel, input] = row.querySelectorAll("select, input");
      return {
        column: colSel.value,
        operator: opSel.value,
        value: input.value,
      };
    },
  );
}

function getSelectedFormat() {
  return (
    document.querySelector('input[name="exportFormat"]:checked')?.value ?? "csv"
  );
}

/* ===== EXPORT PROGRESS ===== */

let currentEventSource = null;

function showProgress(jobId) {
    const container = document.getElementById('exportProgress');
    const messages = document.getElementById('progressMessages');
    
    container.classList.remove('hidden');
    messages.innerHTML = '<div class="progress-message">Iniciando conexión con el servidor...</div>';
    
    if (currentEventSource) {
        currentEventSource.close();
    }
    
    currentEventSource = new EventSource(`/api/export/progress/${jobId}`);
    
    currentEventSource.addEventListener('progress', (e) => {
        const msg = document.createElement('div');
        msg.className = 'progress-message';
        msg.textContent = e.data;
        messages.appendChild(msg);
        messages.scrollTop = messages.scrollHeight;
    });
    
    currentEventSource.addEventListener('complete', (e) => {
        const msg = document.createElement('div');
        msg.className = 'progress-message complete';
        msg.textContent = '¡Proceso finalizado con éxito!';
        messages.appendChild(msg);
        messages.scrollTop = messages.scrollHeight;
        
        // Deshabilitar spinner del header
        const icon = document.querySelector('.export-progress-icon');
        if (icon) icon.innerHTML = '<svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 7l3 3 5-5" stroke="var(--success)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>';
        
        setTimeout(() => {
            closeProgress();
        }, 5000); // Auto-cerrar después de 5s
        
        currentEventSource.close();
        currentEventSource = null;
    });
    
    currentEventSource.onerror = () => {
        console.warn('SSE connection closed or error occurred.');
        if (currentEventSource) {
            currentEventSource.close();
            currentEventSource = null;
        }
    };
}

function closeProgress() {
    const container = document.getElementById('exportProgress');
    container.classList.add('hidden');
    if (currentEventSource) {
        currentEventSource.close();
        currentEventSource = null;
    }
}

async function submitExport() {
  const scopeData = getExportScope();
  const format = getSelectedFormat().toUpperCase();
  const jobId = crypto.randomUUID();
  
  const payload = {
    database: state.selectedDb,
    schema: state.selectedSchema,
    table: state.selectedTable,
    format: format,
    scope: scopeData.mode.toUpperCase(),
    columns: getSelectedColumns(),
    filters: getFilters(),
    page: scopeData.page,
    pageSize: scopeData.size,
    pageFrom: (scopeData.pageFrom !== undefined) ? scopeData.pageFrom + 1 : undefined,
    pageTo: (scopeData.pageTo !== undefined) ? scopeData.pageTo + 1 : undefined,
    rowCount: scopeData.limit,
    rowFrom: scopeData.rowFrom,
    rowTo: scopeData.rowTo,
    jobId: jobId
  };

  const btn = document.querySelector('.modal-footer .btn-primary');
  const originalHtml = btn.innerHTML;
  
  try {
    btn.disabled = true;
    btn.innerHTML = '<div class="spinner-sm"></div> Generando...';

    // Mostrar ventana de progreso
    showProgress(jobId);

    const response = await fetch('/api/export', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!response.ok) throw new Error('Error en la exportación');

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    
    // Extraer nombre de archivo del header
    const disposition = response.headers.get('Content-Disposition');
    let fileName = `export_${state.selectedTable}.${format.toLowerCase()}`;
    if (disposition && disposition.indexOf('attachment') !== -1) {
        const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
        const matches = filenameRegex.exec(disposition);
        if (matches != null && matches[1]) { 
          fileName = matches[1].replace(/['"]/g, '');
        }
    }

    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove();
    
    closeExportModal();
  } catch (e) {
    console.error(e);
    alert('Error al exportar: ' + e.message);
    closeProgress();
  } finally {
    btn.disabled = false;
    btn.innerHTML = originalHtml;
  }
}
