// Screenly Web Presentation & Android Telemetry Sync Engine
const STORAGE_KEY = 'screenly_web_v3_state';

const INSTALLED_APPS = [
  { id: 'com.instagram.android', name: 'Instagram', category: 'SOCIAL', catLabel: 'Redes sociales', color: '#E1306C', icon: 'camera' },
  { id: 'com.google.android.youtube', name: 'YouTube', category: 'ENTERTAINMENT', catLabel: 'Entretenimiento', color: '#FF0000', icon: 'play' },
  { id: 'com.zhiliaoapp.musically', name: 'TikTok', category: 'ENTERTAINMENT', catLabel: 'Entretenimiento', color: '#06B6D4', icon: 'music' },
  { id: 'com.whatsapp', name: 'WhatsApp', category: 'MESSAGING', catLabel: 'Mensajería', color: '#25D366', icon: 'chat' },
  { id: 'com.android.chrome', name: 'Google Chrome', category: 'PRODUCTIVITY', catLabel: 'Productividad', color: '#2563EB', icon: 'globe' },
  { id: 'com.spotify.music', name: 'Spotify', category: 'ENTERTAINMENT', catLabel: 'Entretenimiento', color: '#1DB954', icon: 'headphones' },
  { id: 'com.google.android.gm', name: 'Gmail', category: 'PRODUCTIVITY', catLabel: 'Productividad', color: '#EA4335', icon: 'mail' },
  { id: 'com.Slack', name: 'Slack', category: 'PRODUCTIVITY', catLabel: 'Productividad', color: '#4A154B', icon: 'briefcase' },
  { id: 'com.google.android.calendar', name: 'Calendar', category: 'PRODUCTIVITY', catLabel: 'Productividad', color: '#2563EB', icon: 'calendar' },
  { id: 'com.google.android.apps.maps', name: 'Maps', category: 'SYSTEM', catLabel: 'Sistema', color: '#16A34A', icon: 'map' },
  { id: 'com.google.android.apps.messaging', name: 'Messages', category: 'SYSTEM', catLabel: 'Sistema', color: '#2563EB', icon: 'chat' },
  { id: 'com.android.mtaas.tools', name: 'MTaaS Tools', category: 'TOOLS', catLabel: 'Herramientas', color: '#0284C7', icon: 'cloud' },
  { id: 'com.google.android.dialer', name: 'Phone', category: 'SYSTEM', catLabel: 'Sistema', color: '#2563EB', icon: 'phone' },
  { id: 'com.google.android.apps.photos', name: 'Photos', category: 'PHOTOGRAPHY', catLabel: 'Fotografía', color: '#F59E0B', icon: 'photo' },
  { id: 'com.netflix.mediaclient', name: 'Netflix', category: 'ENTERTAINMENT', catLabel: 'Entretenimiento', color: '#E50914', icon: 'tv' },
  { id: 'com.twitter.android', name: 'X (Twitter)', category: 'SOCIAL', catLabel: 'Redes sociales', color: '#0F172A', icon: 'hash' },
  { id: 'org.telegram.messenger', name: 'Telegram', category: 'MESSAGING', catLabel: 'Mensajería', color: '#0284C7', icon: 'send' },
  { id: 'com.linkedin.android', name: 'LinkedIn', category: 'SOCIAL', catLabel: 'Redes sociales', color: '#0A66C2', icon: 'briefcase' },
  { id: 'com.duolingo', name: 'Duolingo', category: 'EDUCATION', catLabel: 'Educación', color: '#16A34A', icon: 'book' },
  { id: 'notion.id', name: 'Notion', category: 'PRODUCTIVITY', catLabel: 'Productividad', color: '#334155', icon: 'doc' },
  { id: 'com.discord', name: 'Discord', category: 'MESSAGING', catLabel: 'Mensajería', color: '#5865F2', icon: 'gamepad' },
  { id: 'com.amazon.mShop.android.shopping', name: 'Amazon', category: 'TOOLS', catLabel: 'Herramientas', color: '#D97706', icon: 'cart' },
  { id: 'com.android.settings', name: 'Ajustes del sistema', category: 'SYSTEM', catLabel: 'Sistema', color: '#475569', icon: 'gear' }
];

function getTodayIso() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function addDaysIso(iso, delta) {
  const [y, m, d] = iso.split('-').map(Number);
  const dt = new Date(y, m - 1, d + delta);
  return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
}

function buildInitialUsageData(todayIso) {
  const usageByDate = {};
  const profileOffsets = [
    { offset: 0, values: { 'com.instagram.android': [105, 21], 'com.google.android.youtube': [70, 14], 'com.zhiliaoapp.musically': [52, 19], 'com.whatsapp': [48, 32], 'com.android.chrome': [38, 11], 'com.spotify.music': [35, 6], 'com.google.android.gm': [25, 9], 'com.Slack': [22, 8], 'com.google.android.calendar': [15, 5], 'com.google.android.apps.maps': [14, 3], 'com.google.android.apps.messaging': [12, 7], 'com.android.mtaas.tools': [10, 2], 'com.google.android.dialer': [9, 4], 'com.google.android.apps.photos': [8, 3], 'com.netflix.mediaclient': [7, 2], 'com.twitter.android': [5, 4], 'org.telegram.messenger': [4, 3], 'com.linkedin.android': [3, 2], 'com.duolingo': [3, 1] } },
    { offset: -1, values: { 'com.instagram.android': [88, 17], 'com.google.android.youtube': [92, 15], 'com.zhiliaoapp.musically': [44, 14], 'com.whatsapp': [54, 28], 'com.android.chrome': [31, 9], 'com.spotify.music': [40, 7], 'com.google.android.gm': [19, 8], 'com.Slack': [28, 11] } },
    { offset: -2, values: { 'com.instagram.android': [74, 15], 'com.google.android.youtube': [64, 11], 'com.zhiliaoapp.musically': [61, 20], 'com.whatsapp': [42, 24], 'com.android.chrome': [27, 8], 'com.spotify.music': [30, 5], 'com.Slack': [33, 12] } },
    { offset: -3, values: { 'com.instagram.android': [95, 19], 'com.google.android.youtube': [80, 12], 'com.zhiliaoapp.musically': [38, 11], 'com.whatsapp': [49, 25], 'com.android.chrome': [35, 10], 'com.google.android.gm': [22, 7] } },
    { offset: -4, values: { 'com.instagram.android': [62, 13], 'com.google.android.youtube': [58, 9], 'com.zhiliaoapp.musically': [47, 16], 'com.whatsapp': [39, 21], 'com.spotify.music': [45, 8], 'com.Slack': [18, 6] } },
    { offset: -5, values: { 'com.instagram.android': [112, 24], 'com.google.android.youtube': [96, 16], 'com.zhiliaoapp.musically': [68, 22], 'com.whatsapp': [58, 30], 'com.netflix.mediaclient': [45, 3] } },
    { offset: -6, values: { 'com.instagram.android': [79, 16], 'com.google.android.youtube': [72, 13], 'com.zhiliaoapp.musically': [49, 15], 'com.whatsapp': [44, 22], 'com.android.chrome': [29, 7] } },
    { offset: -8, values: { 'com.instagram.android': [68, 14], 'com.google.android.youtube': [54, 10], 'com.whatsapp': [41, 19], 'com.Slack': [25, 8] } },
    { offset: -11, values: { 'com.instagram.android': [83, 18], 'com.zhiliaoapp.musically': [51, 16], 'com.google.android.youtube': [63, 11], 'com.android.chrome': [32, 9] } },
    { offset: -14, values: { 'com.instagram.android': [91, 20], 'com.google.android.youtube': [77, 14], 'com.whatsapp': [50, 26], 'com.spotify.music': [36, 6] } }
  ];

  profileOffsets.forEach(item => {
    const dateKey = addDaysIso(todayIso, item.offset);
    usageByDate[dateKey] = item.values;
  });
  return usageByDate;
}

function getDefaultState() {
  const today = getTodayIso();
  const [y, m] = today.split('-').map(Number);
  return {
    screen: 'home', // 'onboarding' | 'home' | 'usage' | 'limits' | 'stats' | 'limit-editor'
    usagePermissionGranted: true,
    notificationPermissionGranted: true,
    selectedDate: today,
    displayedYear: y,
    displayedMonth: m, // 1..12
    usageScope: 'DAY', // 'DAY' | 'MONTH'
    calendarExpanded: false,
    usageTab: 0, // 0 = Todas las aplicaciones, 1 = Top 5 más utilizadas
    statsPeriod: 'WEEK', // 'TODAY' | 'WEEK'
    statsAppFilter: null,
    syncSource: 'LOCAL', // 'LOCAL' | 'ANDROID_LIVE'
    lastSyncDevice: null,
    lastSyncTime: null,
    usageByDate: buildInitialUsageData(today),
    limits: [
      { id: 'lim_ig', packageName: 'com.instagram.android', limitMinutes: 90, isEnabled: true, startDate: null, endDate: null },
      { id: 'lim_tt', packageName: 'com.zhiliaoapp.musically', limitMinutes: 60, isEnabled: true, startDate: null, endDate: null },
      { id: 'lim_yt', packageName: 'com.google.android.youtube', limitMinutes: 120, isEnabled: true, startDate: null, endDate: addDaysIso(today, 10) },
      { id: 'lim_sl', packageName: 'com.Slack', limitMinutes: 30, isEnabled: true, startDate: addDaysIso(today, 3), endDate: addDaysIso(today, 14) },
      { id: 'lim_cal', packageName: 'com.google.android.calendar', limitMinutes: 1439, isEnabled: true, startDate: null, endDate: null },
      { id: 'lim_wa', packageName: 'com.whatsapp', limitMinutes: 90, isEnabled: false, startDate: null, endDate: null }
    ],
    alerts: [
      { id: 'al_1', packageName: 'com.instagram.android', appName: 'Instagram', thresholdPercent: 100, usedMinutes: 105, limitMinutes: 90, timestampLabel: '17:42', isRead: false },
      { id: 'al_2', packageName: 'com.zhiliaoapp.musically', appName: 'TikTok', thresholdPercent: 80, usedMinutes: 52, limitMinutes: 60, timestampLabel: '18:15', isRead: false }
    ],
    editor: {
      editingId: null,
      packageName: 'com.google.android.calendar',
      hours: 1,
      minutes: 0,
      hasSchedule: false,
      startDate: today,
      endDate: addDaysIso(today, 7),
      isEnabled: true,
      error: null
    }
  };
}

let state = loadState();

function loadState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return getDefaultState();
    const parsed = JSON.parse(raw);
    return { ...getDefaultState(), ...parsed };
  } catch (e) {
    return getDefaultState();
  }
}

function saveState() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch (e) {
    // Ignore storage quota errors
  }
}

function formatDuration(mins) {
  const m = Math.max(0, Math.round(mins || 0));
  if (m === 0) return '0 min';
  const h = Math.floor(m / 60);
  const rem = m % 60;
  if (h > 0 && rem > 0) return `${h}h ${rem}min`;
  if (h > 0) return `${h}h`;
  return `${rem}min`;
}

const MONTHS_ES = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];
const MONTHS_SHORT_ES = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
const DAYS_SHORT_ES = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];

function formatDateLongEs(iso) {
  const [y, m, d] = iso.split('-').map(Number);
  return `${d} de ${MONTHS_ES[m - 1]} de ${y}`;
}

function formatDateShortDayMonthEs(iso) {
  const [, m, d] = iso.split('-').map(Number);
  return `${d} de ${MONTHS_ES[m - 1]}`;
}

function formatDateChipEs(iso) {
  const [, m, d] = iso.split('-').map(Number);
  return `${d} ${MONTHS_SHORT_ES[m - 1]}`;
}

function formatDateSlash(iso) {
  if (!iso) return '';
  const [y, m, d] = iso.split('-');
  return `${d}/${m}/${y}`;
}

function getAppMeta(pkg) {
  const found = INSTALLED_APPS.find(a => a.id === pkg);
  if (found) return found;
  const shortName = pkg.split('.').pop() || pkg;
  return {
    id: pkg,
    name: shortName.charAt(0).toUpperCase() + shortName.slice(1),
    category: 'SYSTEM',
    catLabel: 'Aplicación Android',
    color: '#2563EB',
    icon: 'app'
  };
}

function getAppSvgIcon(iconType) {
  switch (iconType) {
    case 'camera':
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="3" y="5" width="18" height="14" rx="4"/><circle cx="12" cy="12" r="3.2"/><circle cx="17.5" cy="8.5" r="1"/></svg>`;
    case 'play':
      return `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M8 6.5v11l9-5.5-9-5.5z"/></svg>`;
    case 'music':
      return `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M9 17a3 3 0 1 0 2 2.83V7h6V4h-8v10.17A3 3 0 0 0 9 14z"/></svg>`;
    case 'chat':
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>`;
    case 'globe':
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="9"/><path d="M3.6 9h16.8M3.6 15h16.8M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18"/></svg>`;
    case 'headphones':
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M3 18v-6a9 9 0 0 1 18 0v6"/><path d="M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3zM3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z"/></svg>`;
    case 'mail':
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="m3 7 9 6 9-6"/></svg>`;
    case 'briefcase':
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="3" y="7" width="18" height="13" rx="2"/><path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>`;
    case 'calendar':
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="3" y="4" width="18" height="17" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>`;
    case 'map':
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M12 21s-7-5.5-7-11a7 7 0 1 1 14 0c0 5.5-7 11-7 11z"/><circle cx="12" cy="10" r="2.5"/></svg>`;
    case 'cloud':
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M17.5 19H7a5 5 0 0 1-.5-9.97A6.5 6.5 0 0 1 19 10.5a4.5 4.5 0 0 1-1.5 8.5z"/></svg>`;
    case 'phone':
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.8 19.8 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.12 4.18 2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.13.96.36 1.9.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.91.34 1.85.57 2.81.7A2 2 0 0 1 22 16.92z"/></svg>`;
    case 'photo':
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="3" y="3" width="18" height="18" rx="3"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="m21 15-5-5L5 21"/></svg>`;
    default:
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="4" y="4" width="16" height="16" rx="4"/><circle cx="12" cy="12" r="3"/></svg>`;
  }
}

function getUsageForDate(dateIso) {
  const dayData = state.usageByDate[dateIso] || {};
  return INSTALLED_APPS.map(app => {
    const entry = dayData[app.id] || [0, 0];
    return {
      ...app,
      minutes: entry[0] || 0,
      launches: entry[1] || 0
    };
  });
}

function getUsageForMonth(year, month) {
  const prefix = `${year}-${String(month).padStart(2, '0')}-`;
  const totals = {};
  Object.keys(state.usageByDate).forEach(dateKey => {
    if (dateKey.startsWith(prefix)) {
      const dayMap = state.usageByDate[dateKey] || {};
      Object.keys(dayMap).forEach(pkg => {
        if (!totals[pkg]) totals[pkg] = [0, 0];
        totals[pkg][0] += dayMap[pkg][0] || 0;
        totals[pkg][1] += dayMap[pkg][1] || 0;
      });
    }
  });
  return INSTALLED_APPS.map(app => {
    const entry = totals[app.id] || [0, 0];
    return {
      ...app,
      minutes: entry[0] || 0,
      launches: entry[1] || 0
    };
  });
}

function evaluateLimitStatus(limit, usedMinutes, refDateIso) {
  if (!limit.isEnabled) return { code: 'INACTIVE', label: 'Inactivo', cls: 'status-inactive' };
  if (limit.startDate && refDateIso < limit.startDate) return { code: 'SCHEDULED', label: 'Programado', cls: 'status-scheduled' };
  if (limit.endDate && refDateIso > limit.endDate) return { code: 'EXPIRED', label: 'Finalizado', cls: 'status-inactive' };
  if (usedMinutes >= limit.limitMinutes) return { code: 'REACHED', label: 'Alcanzado', cls: 'status-reached' };
  if (usedMinutes >= Math.ceil(limit.limitMinutes * 0.8)) return { code: 'APPROACHING', label: 'Próximo (80%)', cls: 'status-approaching' };
  return { code: 'ACTIVE', label: 'Vigente', cls: 'status-active' };
}

// Live device clock updater
function updateClocks() {
  const now = new Date();
  let hours = now.getHours();
  const mins = String(now.getMinutes()).padStart(2, '0');
  const ampm = hours >= 12 ? 'p. m.' : 'a. m.';
  const h12 = hours % 12 || 12;
  const timeLabel = `${h12}:${mins} ${ampm}`;
  const statusBarLabel = `${String(hours).padStart(2, '0')}:${mins}`;

  const sbTime = document.getElementById('status-bar-time');
  if (sbTime) sbTime.textContent = statusBarLabel;

  const pillClock = document.getElementById('live-clock-text');
  if (pillClock) pillClock.textContent = timeLabel;

  const pillDate = document.getElementById('live-date-text');
  if (pillDate) pillDate.textContent = formatDateLongEs(getTodayIso());
}

// Render controller
function renderAll() {
  updateClocks();
  renderNavigationState();
  renderAlertBadge();
  renderOnboarding();
  renderHome();
  renderUsage();
  renderLimits();
  renderLimitEditor();
  renderStats();
  renderAlertsSheet();
  renderSimulatorSheet();
  renderSyncBanner();
}

function renderSyncBanner() {
  const badge = document.getElementById('sync-status-indicator');
  if (!badge) return;
  if (state.syncSource === 'ANDROID_LIVE') {
    badge.innerHTML = `<span class="dot-live"></span> Conectado con Android (${state.lastSyncDevice || 'Dispositivo real'})`;
    badge.className = 'sync-pill live';
  } else {
    badge.innerHTML = `<span class="dot-local"></span> Estado interactivo persistente (Sincronizable con APK Android)`;
    badge.className = 'sync-pill';
  }
}

function renderNavigationState() {
  const screens = ['onboarding', 'home', 'usage', 'limits', 'stats', 'limit-editor'];
  screens.forEach(s => {
    const el = document.getElementById(`screen-${s}`);
    if (el) el.classList.toggle('active', state.screen === s);
  });

  const topbar = document.getElementById('phone-topbar');
  const bottomNav = document.getElementById('phone-bottom-nav');
  const hideChrome = state.screen === 'onboarding' || state.screen === 'limit-editor';
  if (topbar) topbar.style.display = hideChrome ? 'none' : 'flex';
  if (bottomNav) bottomNav.style.display = hideChrome ? 'none' : 'flex';

  ['home', 'usage', 'limits', 'stats'].forEach(tab => {
    const btn = document.getElementById(`nav-btn-${tab}`);
    if (btn) btn.classList.toggle('active', state.screen === tab);
  });

  document.querySelectorAll('[data-jump-screen]').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-jump-screen') === state.screen);
  });
}

function renderAlertBadge() {
  const unreadCount = state.alerts.filter(a => !a.isRead).length;
  const badge = document.getElementById('alerts-badge-count');
  if (badge) {
    badge.textContent = unreadCount;
    badge.style.display = unreadCount > 0 ? 'flex' : 'none';
  }
}

function renderOnboarding() {
  const usageStatus = document.getElementById('onb-usage-status');
  const usageAction = document.getElementById('onb-usage-action');
  if (usageStatus && usageAction) {
    usageStatus.textContent = state.usagePermissionGranted ? 'Concedido' : 'Pendiente de activación';
    usageStatus.style.color = state.usagePermissionGranted ? '#16A34A' : '#D97706';
    usageAction.textContent = state.usagePermissionGranted ? 'Activo' : 'Conceder';
    usageAction.className = state.usagePermissionGranted ? 'perm-status-btn granted' : 'perm-status-btn pending';
  }

  const notifStatus = document.getElementById('onb-notif-status');
  const notifAction = document.getElementById('onb-notif-action');
  if (notifStatus && notifAction) {
    notifStatus.textContent = state.notificationPermissionGranted ? 'Concedido' : 'Opcional / Desactivado';
    notifStatus.style.color = state.notificationPermissionGranted ? '#16A34A' : '#64748B';
    notifAction.textContent = state.notificationPermissionGranted ? 'Activo' : 'Permitir';
    notifAction.className = state.notificationPermissionGranted ? 'perm-status-btn granted' : 'perm-status-btn pending';
  }
}

function renderHome() {
  const todayIso = getTodayIso();
  const appsToday = getUsageForDate(todayIso);
  const totalMinutes = state.usagePermissionGranted
    ? appsToday.reduce((acc, a) => acc + a.minutes, 0)
    : 0;

  const permBanner = document.getElementById('home-perm-warning');
  if (permBanner) {
    permBanner.style.display = state.usagePermissionGranted ? 'none' : 'flex';
  }

  const totalEl = document.getElementById('home-total-time');
  if (totalEl) totalEl.textContent = formatDuration(totalMinutes);

  const barFill = document.getElementById('home-goal-bar-fill');
  if (barFill) {
    const pct = Math.min(100, Math.round((totalMinutes / 240) * 100));
    barFill.style.width = `${pct}%`;
    barFill.style.background = totalMinutes > 240 ? '#DC2626' : '#2563EB';
  }

  const monitoredEl = document.getElementById('home-kpi-apps');
  if (monitoredEl) monitoredEl.textContent = `${INSTALLED_APPS.length} apps`;

  const activeLimitsCount = state.limits.filter(l => {
    const u = appsToday.find(a => a.id === l.packageName)?.minutes || 0;
    const st = evaluateLimitStatus(l, u, todayIso).code;
    return st === 'ACTIVE' || st === 'APPROACHING' || st === 'REACHED';
  }).length;

  const reachedCount = state.limits.filter(l => {
    const u = appsToday.find(a => a.id === l.packageName)?.minutes || 0;
    return evaluateLimitStatus(l, u, todayIso).code === 'REACHED';
  }).length;

  const activeEl = document.getElementById('home-kpi-active');
  if (activeEl) activeEl.textContent = `${activeLimitsCount} activos`;

  const reachedEl = document.getElementById('home-kpi-reached');
  if (reachedEl) reachedEl.textContent = `${reachedCount} hoy`;

  const listContainer = document.getElementById('home-top-apps-list');
  if (!listContainer) return;

  if (!state.usagePermissionGranted) {
    listContainer.innerHTML = `
      <div class="empty-card">
        <p class="empty-title">Sin acceso a estadísticas de uso</p>
        <p class="empty-sub">Concede el permiso PACKAGE_USAGE_STATS para visualizar las aplicaciones utilizadas hoy.</p>
        <button class="btn-primary-sm" onclick="toggleUsagePermission(true)">Activar permiso ahora</button>
      </div>`;
    return;
  }

  const sorted = [...appsToday].filter(a => a.minutes > 0).sort((a, b) => b.minutes - a.minutes).slice(0, 5);
  listContainer.innerHTML = sorted.map(app => {
    const pct = totalMinutes > 0 ? Math.round((app.minutes / totalMinutes) * 100) : 0;
    const lim = state.limits.find(l => l.packageName === app.id);
    const limStatus = lim ? evaluateLimitStatus(lim, app.minutes, todayIso) : null;
    const isReached = limStatus && limStatus.code === 'REACHED';
    const timeColor = isReached ? '#DC2626' : '#0F172A';
    const barColor = isReached ? '#DC2626' : '#2563EB';
    return `
      <div class="app-row-card" onclick="openLimitEditorForPackage('${app.id}')">
        <div class="app-row-main">
          <div class="app-icon-box" style="background:${app.color}">${getAppSvgIcon(app.icon)}</div>
          <div class="app-row-info">
            <div class="app-row-name">${app.name}</div>
            <div class="app-row-meta">${app.catLabel} · ${app.launches} aperturas</div>
            <div class="app-mini-bar-track">
              <div class="app-mini-bar-fill" style="width:${Math.max(8, pct)}%;background:${barColor}"></div>
            </div>
          </div>
          <div class="app-row-right">
            <div class="app-row-time" style="color:${timeColor}">${formatDuration(app.minutes)}</div>
            <div class="app-row-pct">${pct}% del uso total</div>
          </div>
        </div>
      </div>`;
  }).join('');
}

function renderUsage() {
  const todayIso = getTodayIso();
  const yesterdayIso = addDaysIso(todayIso, -1);

  // Header title & subtitle in collapsible card
  const titleEl = document.getElementById('usage-date-title');
  if (titleEl) {
    if (state.usageScope === 'MONTH') {
      const mName = MONTHS_ES[state.displayedMonth - 1];
      titleEl.textContent = `Mes de ${mName.charAt(0).toUpperCase() + mName.slice(1)} ${state.displayedYear}`;
    } else if (state.selectedDate === todayIso) {
      titleEl.textContent = `Hoy, ${formatDateShortDayMonthEs(state.selectedDate)}`;
    } else if (state.selectedDate === yesterdayIso) {
      titleEl.textContent = `Ayer, ${formatDateShortDayMonthEs(state.selectedDate)}`;
    } else {
      titleEl.textContent = formatDateShortDayMonthEs(state.selectedDate);
    }
  }

  const subEl = document.getElementById('usage-date-subtitle');
  if (subEl) {
    subEl.textContent = state.calendarExpanded
      ? 'Toca para ocultar el calendario'
      : 'Toca para abrir el calendario y seleccionar otra fecha o mes';
  }

  const chevronEl = document.getElementById('usage-calendar-chevron');
  if (chevronEl) {
    chevronEl.style.transform = state.calendarExpanded ? 'rotate(180deg)' : 'rotate(0deg)';
  }

  // Quick chips
  const chipHoy = document.getElementById('chip-usage-today');
  const chipAyer = document.getElementById('chip-usage-yesterday');
  const chipCustom = document.getElementById('chip-usage-custom');
  const chipMonth = document.getElementById('chip-usage-month');

  const isToday = state.usageScope === 'DAY' && state.selectedDate === todayIso;
  const isYesterday = state.usageScope === 'DAY' && state.selectedDate === yesterdayIso;
  const isCustomDay = state.usageScope === 'DAY' && !isToday && !isYesterday;
  const isMonth = state.usageScope === 'MONTH';

  if (chipHoy) chipHoy.classList.toggle('active', isToday);
  if (chipAyer) chipAyer.classList.toggle('active', isYesterday);
  if (chipCustom) {
    chipCustom.style.display = isCustomDay ? 'inline-flex' : 'none';
    chipCustom.classList.toggle('active', isCustomDay);
    chipCustom.innerHTML = `✓ ${formatDateChipEs(state.selectedDate)}`;
  }
  if (chipMonth) chipMonth.classList.toggle('active', isMonth);

  // Calendar body
  const calContainer = document.getElementById('usage-calendar-collapsible');
  if (calContainer) {
    calContainer.style.display = state.calendarExpanded ? 'block' : 'none';
  }
  renderCalendarGrid();

  // Compute apps usage for selected scope
  const appsList = !state.usagePermissionGranted
    ? []
    : (state.usageScope === 'MONTH'
        ? getUsageForMonth(state.displayedYear, state.displayedMonth)
        : getUsageForDate(state.selectedDate));

  const totalMinutes = appsList.reduce((acc, a) => acc + a.minutes, 0);
  const totalLabelEl = document.getElementById('usage-scope-total-label');
  const totalValEl = document.getElementById('usage-scope-total-value');
  if (totalLabelEl) {
    totalLabelEl.textContent = state.usageScope === 'MONTH' ? 'Total acumulado del mes' : 'Total del día';
  }
  if (totalValEl) {
    totalValEl.textContent = formatDuration(totalMinutes);
  }

  // Tabs
  const tabAll = document.getElementById('usage-tab-all');
  const tabTop5 = document.getElementById('usage-tab-top5');
  if (tabAll) tabAll.classList.toggle('active', state.usageTab === 0);
  if (tabTop5) tabTop5.classList.toggle('active', state.usageTab === 1);

  const listEl = document.getElementById('usage-apps-list');
  if (!listEl) return;

  const sortedApps = [...appsList].sort((a, b) => {
    if (b.minutes !== a.minutes) return b.minutes - a.minutes;
    return a.name.localeCompare(b.name);
  });

  const displayApps = state.usageTab === 1
    ? sortedApps.filter(a => a.minutes > 0).slice(0, 5)
    : sortedApps;

  if (displayApps.length === 0) {
    listEl.innerHTML = `
      <div class="empty-card">
        <p class="empty-title">Sin actividad registrada en esta selección</p>
        <p class="empty-sub">Selecciona otro día en el calendario o cambia a Todas las aplicaciones (${INSTALLED_APPS.length} instaladas).</p>
      </div>`;
    return;
  }

  listEl.innerHTML = displayApps.map(app => {
    const lim = state.limits.find(l => l.packageName === app.id);
    const limStatus = lim ? evaluateLimitStatus(lim, app.minutes, state.selectedDate) : null;
    return `
      <div class="usage-app-card" onclick="openLimitEditorForPackage('${app.id}')">
        <div class="app-icon-box" style="background:${app.color}">${getAppSvgIcon(app.icon)}</div>
        <div class="usage-app-info">
          <div class="usage-app-name">${app.name}</div>
          <div class="usage-app-meta">${app.catLabel} · ${app.launches} aperturas</div>
          ${lim ? `<div class="usage-app-limit-tag">Límite: ${formatDuration(lim.limitMinutes)} (${limStatus.label})</div>` : ''}
        </div>
        <div class="usage-app-right">
          <span class="usage-app-duration">${formatDuration(app.minutes)}</span>
          <span class="usage-chevron">›</span>
        </div>
      </div>`;
  }).join('');
}

function renderCalendarGrid() {
  const monthTitle = document.getElementById('cal-month-title');
  const mName = MONTHS_ES[state.displayedMonth - 1];
  if (monthTitle) {
    monthTitle.textContent = `${mName.charAt(0).toUpperCase() + mName.slice(1)} ${state.displayedYear}`;
  }

  const gridEl = document.getElementById('cal-days-grid');
  if (!gridEl) return;

  const todayIso = getTodayIso();
  const firstDay = new Date(state.displayedYear, state.displayedMonth - 1, 1);
  // Monday-based index (0 = Monday ... 6 = Sunday)
  const firstDayOffset = (firstDay.getDay() + 6) % 7;
  const daysInMonth = new Date(state.displayedYear, state.displayedMonth, 0).getDate();

  let cellsHtml = '';
  for (let i = 0; i < firstDayOffset; i++) {
    cellsHtml += `<div class="cal-day-cell empty"></div>`;
  }

  for (let day = 1; day <= daysInMonth; day++) {
    const iso = `${state.displayedYear}-${String(state.displayedMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    const isSelected = state.usageScope === 'DAY' && state.selectedDate === iso;
    const isToday = todayIso === iso;
    const isFuture = iso > todayIso;
    const dayMap = state.usageByDate[iso] || {};
    const hasData = Object.values(dayMap).some(arr => (arr[0] || 0) > 0);

    const classes = [
      'cal-day-cell',
      isSelected ? 'selected' : '',
      !isSelected && isToday ? 'today' : '',
      isFuture ? 'future' : ''
    ].filter(Boolean).join(' ');

    cellsHtml += `
      <button type="button" class="${classes}" ${isFuture ? 'disabled' : `onclick="selectCalendarDate('${iso}')"`}>
        <span class="cal-day-num">${day}</span>
        ${hasData && !isFuture ? `<span class="cal-day-dot"></span>` : `<span class="cal-day-dot-spacer"></span>`}
      </button>`;
  }

  gridEl.innerHTML = cellsHtml;

  const scopeBadge = document.getElementById('cal-scope-badge');
  if (scopeBadge) {
    scopeBadge.textContent = state.usageScope === 'MONTH' ? 'Mes' : 'Día';
  }

  const footerText = document.getElementById('cal-footer-selected-text');
  if (footerText) {
    footerText.textContent = state.usageScope === 'MONTH'
      ? `Consultando mes completo: ${mName} ${state.displayedYear}`
      : `Fecha seleccionada: ${formatDateLongEs(state.selectedDate)}`;
  }
}

function renderLimits() {
  const todayIso = getTodayIso();
  const appsToday = getUsageForDate(todayIso);
  const activeCount = state.limits.filter(l => l.isEnabled).length;

  const subtitleEl = document.getElementById('limits-header-subtitle');
  if (subtitleEl) {
    subtitleEl.textContent = `${activeCount} activos de ${state.limits.length} configurados`;
  }

  const container = document.getElementById('limits-cards-list');
  if (!container) return;

  if (state.limits.length === 0) {
    container.innerHTML = `
      <div class="empty-card">
        <p class="empty-title">No tienes límites configurados</p>
        <p class="empty-sub">Pulsa el botón azul "+" para crear un límite diario para cualquier aplicación instalada.</p>
        <button class="btn-primary-sm" onclick="openLimitEditorNew()">+ Crear primer límite</button>
      </div>`;
    return;
  }

  container.innerHTML = state.limits.map(lim => {
    const app = getAppMeta(lim.packageName);
    const usedToday = appsToday.find(a => a.id === lim.packageName)?.minutes || 0;
    const status = evaluateLimitStatus(lim, usedToday, todayIso);
    const pct = lim.limitMinutes > 0 ? Math.min(100, Math.round((usedToday / lim.limitMinutes) * 100)) : 0;
    const barColor = status.code === 'REACHED' ? '#DC2626' : (status.code === 'APPROACHING' ? '#D97706' : '#2563EB');

    let validityHtml = '';
    if (lim.startDate || lim.endDate) {
      const rangeText = lim.startDate && lim.endDate
        ? `del ${formatDateSlash(lim.startDate)} al ${formatDateSlash(lim.endDate)}`
        : (lim.startDate ? `desde ${formatDateSlash(lim.startDate)}` : `hasta ${formatDateSlash(lim.endDate)}`);
      validityHtml = `<div class="limit-validity-row">📅 Vigencia: ${rangeText}</div>`;
    }

    return `
      <div class="limit-item-card">
        <div class="limit-card-top">
          <div class="app-icon-box" style="background:${app.color}">${getAppSvgIcon(app.icon)}</div>
          <div class="limit-card-title-box">
            <div class="limit-card-app-name">${app.name}</div>
            <span class="status-pill ${status.cls}">${status.label}</span>
          </div>
          <button type="button" class="m3-switch ${lim.isEnabled ? 'on' : ''}" onclick="toggleLimitEnabled('${lim.id}')" aria-label="Activar o desactivar límite">
            <span class="m3-switch-thumb"></span>
          </button>
        </div>

        <div class="limit-metrics-row">
          <div>
            <div class="limit-metric-label">Límite diario</div>
            <div class="limit-metric-val">${formatDuration(lim.limitMinutes)}</div>
          </div>
          <div style="text-align:right">
            <div class="limit-metric-label">Tiempo registrado hoy</div>
            <div class="limit-metric-val" style="color:${status.code === 'REACHED' ? '#DC2626' : '#2563EB'}">${formatDuration(usedToday)}</div>
          </div>
        </div>

        <div class="limit-progress-track">
          <div class="limit-progress-fill" style="width:${pct}%;background:${barColor}"></div>
        </div>

        ${validityHtml}

        <div class="limit-actions-row">
          <button type="button" class="icon-action-btn edit" onclick="openLimitEditorById('${lim.id}')" title="Editar límite">✏️</button>
          <button type="button" class="icon-action-btn delete" onclick="deleteLimitById('${lim.id}')" title="Eliminar límite">🗑️</button>
        </div>
      </div>`;
  }).join('');
}

function renderLimitEditor() {
  const ed = state.editor;
  const app = getAppMeta(ed.packageName);

  const titleEl = document.getElementById('editor-header-title');
  if (titleEl) titleEl.textContent = ed.editingId ? 'Editar límite diario' : 'Nuevo límite diario';

  const iconEl = document.getElementById('editor-app-icon');
  if (iconEl) {
    iconEl.style.background = app.color;
    iconEl.innerHTML = getAppSvgIcon(app.icon);
  }
  const nameEl = document.getElementById('editor-app-name');
  if (nameEl) nameEl.textContent = app.name;

  const pkgEl = document.getElementById('editor-app-meta');
  if (pkgEl) pkgEl.textContent = `${app.catLabel} · ${app.id}`;

  const hInput = document.getElementById('editor-hours-input');
  if (hInput && document.activeElement !== hInput) hInput.value = ed.hours;

  const mInput = document.getElementById('editor-minutes-input');
  if (mInput && document.activeElement !== mInput) mInput.value = ed.minutes;

  const schedSwitch = document.getElementById('editor-schedule-switch');
  if (schedSwitch) schedSwitch.classList.toggle('on', ed.hasSchedule);

  const schedDatesBox = document.getElementById('editor-schedule-dates');
  if (schedDatesBox) schedDatesBox.style.display = ed.hasSchedule ? 'grid' : 'none';

  const startInput = document.getElementById('editor-start-date');
  if (startInput) startInput.value = ed.startDate || getTodayIso();

  const endInput = document.getElementById('editor-end-date');
  if (endInput) endInput.value = ed.endDate || addDaysIso(getTodayIso(), 7);

  const activeSwitch = document.getElementById('editor-active-switch');
  if (activeSwitch) activeSwitch.classList.toggle('on', ed.isEnabled);

  const totalMins = (Number(ed.hours) || 0) * 60 + (Number(ed.minutes) || 0);
  const summaryEl = document.getElementById('editor-summary-list');
  if (summaryEl) {
    const periodText = ed.hasSchedule
      ? `Del ${formatDateSlash(ed.startDate)} al ${formatDateSlash(ed.endDate)}`
      : 'Permanente (sin fecha de caducidad)';
    summaryEl.innerHTML = `
      <li>• App: <strong>${app.name}</strong></li>
      <li>• Tiempo diario permitido: <strong>${formatDuration(totalMins)}</strong></li>
      <li>• Estado: <strong>${ed.isEnabled ? 'Activado' : 'Desactivado'}</strong></li>
      <li>• Período: <strong>${periodText}</strong></li>`;
  }

  const errEl = document.getElementById('editor-error-banner');
  if (errEl) {
    errEl.style.display = ed.error ? 'block' : 'none';
    errEl.textContent = ed.error || '';
  }
}

function renderStats() {
  const todayIso = getTodayIso();
  const filterChipsEl = document.getElementById('stats-app-filter-chips');
  if (filterChipsEl) {
    const topApps = INSTALLED_APPS.slice(0, 8);
    filterChipsEl.innerHTML = `
      <button type="button" class="quick-chip ${state.statsAppFilter === null ? 'active' : ''}" onclick="setStatsAppFilter(null)">Todas las apps</button>
      ${topApps.map(a => `
        <button type="button" class="quick-chip ${state.statsAppFilter === a.id ? 'active' : ''}" onclick="setStatsAppFilter('${a.id}')">${a.name}</button>
      `).join('')}`;
  }

  const tabToday = document.getElementById('stats-tab-today');
  const tabWeek = document.getElementById('stats-tab-week');
  if (tabToday) tabToday.classList.toggle('active', state.statsPeriod === 'TODAY');
  if (tabWeek) tabWeek.classList.toggle('active', state.statsPeriod === 'WEEK');

  // Build 7-day series
  const series = [];
  for (let offset = -6; offset <= 0; offset++) {
    const iso = addDaysIso(todayIso, offset);
    const [y, m, d] = iso.split('-').map(Number);
    const dt = new Date(y, m - 1, d);
    const dayApps = getUsageForDate(iso);
    const mins = state.statsAppFilter
      ? (dayApps.find(a => a.id === state.statsAppFilter)?.minutes || 0)
      : dayApps.reduce((acc, a) => acc + a.minutes, 0);
    series.push({
      iso,
      label: offset === 0 ? 'Hoy' : DAYS_SHORT_ES[dt.getDay()],
      minutes: state.usagePermissionGranted ? mins : 0
    });
  }

  const todayMins = series[series.length - 1].minutes;
  const weekTotal = series.reduce((s, d) => s + d.minutes, 0);
  const weekAvg = Math.round(weekTotal / 7);

  const kpiTotalEl = document.getElementById('stats-kpi-total');
  const kpiAvgEl = document.getElementById('stats-kpi-avg');
  if (kpiTotalEl) kpiTotalEl.textContent = formatDuration(state.statsPeriod === 'TODAY' ? todayMins : weekTotal);
  if (kpiAvgEl) kpiAvgEl.textContent = formatDuration(state.statsPeriod === 'TODAY' ? todayMins : weekAvg);

  const maxDayMins = Math.max(60, ...series.map(s => s.minutes));
  const chartEl = document.getElementById('stats-bars-container');
  if (chartEl) {
    chartEl.innerHTML = series.map((item, idx) => {
      const isLast = idx === series.length - 1;
      const heightPct = Math.max(6, Math.round((item.minutes / maxDayMins) * 100));
      return `
        <div class="bar-col">
          <div class="bar-val-top">${item.minutes > 0 ? formatDuration(item.minutes) : '0m'}</div>
          <div class="bar-track-vert">
            <div class="bar-fill-vert ${isLast ? 'today' : ''}" style="height:${heightPct}%"></div>
          </div>
          <div class="bar-day-lbl ${isLast ? 'today' : ''}">${item.label}</div>
        </div>`;
    }).join('');
  }

  // Category breakdown
  const catContainer = document.getElementById('stats-categories-list');
  if (catContainer) {
    const catTotals = {};
    const daysToAggregate = state.statsPeriod === 'TODAY' ? [todayIso] : series.map(s => s.iso);
    daysToAggregate.forEach(iso => {
      getUsageForDate(iso).forEach(app => {
        if (state.statsAppFilter && app.id !== state.statsAppFilter) return;
        if (!catTotals[app.catLabel]) catTotals[app.catLabel] = 0;
        catTotals[app.catLabel] += app.minutes;
      });
    });

    const entries = Object.entries(catTotals).filter(([, m]) => m > 0).sort((a, b) => b[1] - a[1]);
    const grandTotal = entries.reduce((s, [, m]) => s + m, 0) || 1;

    catContainer.innerHTML = entries.map(([catName, mins]) => {
      const pct = Math.round((mins / grandTotal) * 100);
      return `
        <div class="cat-stat-row">
          <div class="cat-stat-head">
            <span class="cat-stat-name">${catName}</span>
            <span class="cat-stat-val">${formatDuration(mins)} (${pct}%)</span>
          </div>
          <div class="limit-progress-track">
            <div class="limit-progress-fill" style="width:${pct}%;background:#2563EB"></div>
          </div>
        </div>`;
    }).join('');
  }
}

function renderAlertsSheet() {
  const listEl = document.getElementById('alerts-sheet-list');
  if (!listEl) return;

  if (state.alerts.length === 0) {
    listEl.innerHTML = `
      <div class="empty-card">
        <p class="empty-title">No hay alertas pendientes</p>
        <p class="empty-sub">Screenly te avisará automáticamente cuando alcances el 80% o el 100% de un límite activo.</p>
      </div>`;
    return;
  }

  listEl.innerHTML = state.alerts.map(alert => {
    const is100 = alert.thresholdPercent >= 100;
    const title = is100 ? 'Límite diario alcanzado' : 'Límite próximo a alcanzarse';
    const titleColor = is100 ? '#DC2626' : '#0F172A';
    const dotColor = is100 ? '#DC2626' : '#F59E0B';
    const iconHtml = is100 ? '⏳' : '⚠️';
    const desc = is100
      ? `Has alcanzado tu límite de uso para ${alert.appName}.`
      : `Has utilizado el 80% del tiempo permitido en ${alert.appName}.`;

    return `
      <div class="alert-card-item">
        <div class="alert-icon-col ${is100 ? 'danger' : 'warning'}">${iconHtml}</div>
        <div class="alert-body-col">
          <div class="alert-top-line">
            <span class="alert-title" style="color:${titleColor}">${title}</span>
            <span class="alert-time">${alert.timestampLabel}</span>
          </div>
          <div class="alert-sub-strong">${alert.appName} · ${formatDuration(alert.usedMinutes)} de ${formatDuration(alert.limitMinutes)}</div>
          <div class="alert-desc">${desc}</div>
        </div>
        <span class="alert-unread-dot" style="background:${dotColor}"></span>
      </div>`;
  }).join('');
}

function renderSimulatorSheet() {
  const uSwitch = document.getElementById('sim-usage-perm-switch');
  if (uSwitch) uSwitch.classList.toggle('on', state.usagePermissionGranted);

  const nSwitch = document.getElementById('sim-notif-perm-switch');
  if (nSwitch) nSwitch.classList.toggle('on', state.notificationPermissionGranted);
}

// Interactive Handlers
window.navigateScreen = function(target) {
  state.screen = target;
  saveState();
  renderAll();
};

window.toggleUsagePermission = function(forceVal) {
  state.usagePermissionGranted = typeof forceVal === 'boolean' ? forceVal : !state.usagePermissionGranted;
  saveState();
  renderAll();
};

window.toggleNotificationPermission = function(forceVal) {
  state.notificationPermissionGranted = typeof forceVal === 'boolean' ? forceVal : !state.notificationPermissionGranted;
  saveState();
  renderAll();
};

window.completeOnboarding = function() {
  state.screen = 'home';
  saveState();
  renderAll();
};

window.toggleUsageCalendar = function() {
  state.calendarExpanded = !state.calendarExpanded;
  saveState();
  renderAll();
};

window.selectQuickUsageFilter = function(mode) {
  const todayIso = getTodayIso();
  if (mode === 'TODAY') {
    state.usageScope = 'DAY';
    state.selectedDate = todayIso;
    const [y, m] = todayIso.split('-').map(Number);
    state.displayedYear = y;
    state.displayedMonth = m;
  } else if (mode === 'YESTERDAY') {
    const yest = addDaysIso(todayIso, -1);
    state.usageScope = 'DAY';
    state.selectedDate = yest;
    const [y, m] = yest.split('-').map(Number);
    state.displayedYear = y;
    state.displayedMonth = m;
  } else if (mode === 'MONTH') {
    state.usageScope = 'MONTH';
  }
  saveState();
  renderAll();
};

window.changeCalendarMonth = function(delta) {
  let m = state.displayedMonth + delta;
  let y = state.displayedYear;
  if (m < 1) { m = 12; y -= 1; }
  if (m > 12) { m = 1; y += 1; }
  state.displayedMonth = m;
  state.displayedYear = y;
  saveState();
  renderAll();
};

window.selectCalendarDate = function(iso) {
  state.selectedDate = iso;
  state.usageScope = 'DAY';
  const [y, m] = iso.split('-').map(Number);
  state.displayedYear = y;
  state.displayedMonth = m;
  saveState();
  renderAll();
};

window.setUsageTab = function(tabIdx) {
  state.usageTab = tabIdx;
  saveState();
  renderAll();
};

window.toggleLimitEnabled = function(limitId) {
  const lim = state.limits.find(l => l.id === limitId);
  if (lim) {
    lim.isEnabled = !lim.isEnabled;
    saveState();
    renderAll();
  }
};

window.deleteLimitById = function(limitId) {
  state.limits = state.limits.filter(l => l.id !== limitId);
  saveState();
  renderAll();
};

window.openLimitEditorNew = function() {
  const todayIso = getTodayIso();
  const usedPkgs = new Set(state.limits.map(l => l.packageName));
  const firstFree = INSTALLED_APPS.find(a => !usedPkgs.has(a.id)) || INSTALLED_APPS[0];
  state.editor = {
    editingId: null,
    packageName: firstFree.id,
    hours: 1,
    minutes: 0,
    hasSchedule: false,
    startDate: todayIso,
    endDate: addDaysIso(todayIso, 7),
    isEnabled: true,
    error: null
  };
  state.screen = 'limit-editor';
  saveState();
  renderAll();
};

window.openLimitEditorById = function(limitId) {
  const lim = state.limits.find(l => l.id === limitId);
  if (!lim) return;
  const todayIso = getTodayIso();
  state.editor = {
    editingId: lim.id,
    packageName: lim.packageName,
    hours: Math.floor(lim.limitMinutes / 60),
    minutes: lim.limitMinutes % 60,
    hasSchedule: Boolean(lim.startDate || lim.endDate),
    startDate: lim.startDate || todayIso,
    endDate: lim.endDate || addDaysIso(todayIso, 7),
    isEnabled: lim.isEnabled,
    error: null
  };
  state.screen = 'limit-editor';
  saveState();
  renderAll();
};

window.openLimitEditorForPackage = function(pkg) {
  const existing = state.limits.find(l => l.packageName === pkg);
  if (existing) {
    window.openLimitEditorById(existing.id);
    return;
  }
  const todayIso = getTodayIso();
  state.editor = {
    editingId: null,
    packageName: pkg,
    hours: 1,
    minutes: 0,
    hasSchedule: false,
    startDate: todayIso,
    endDate: addDaysIso(todayIso, 7),
    isEnabled: true,
    error: null
  };
  state.screen = 'limit-editor';
  saveState();
  renderAll();
};

window.updateEditorDurationInputs = function() {
  const h = Math.max(0, Math.min(23, parseInt(document.getElementById('editor-hours-input')?.value || '0', 10) || 0));
  const m = Math.max(0, Math.min(59, parseInt(document.getElementById('editor-minutes-input')?.value || '0', 10) || 0));
  state.editor.hours = h;
  state.editor.minutes = m;
  state.editor.error = null;
  renderLimitEditor();
};

window.addQuickMinutes = function(minsToAdd) {
  let total = (Number(state.editor.hours) || 0) * 60 + (Number(state.editor.minutes) || 0) + minsToAdd;
  total = Math.min(1439, Math.max(1, total));
  state.editor.hours = Math.floor(total / 60);
  state.editor.minutes = total % 60;
  state.editor.error = null;
  renderLimitEditor();
};

window.toggleEditorSchedule = function() {
  state.editor.hasSchedule = !state.editor.hasSchedule;
  state.editor.error = null;
  renderLimitEditor();
};

window.updateEditorDates = function() {
  state.editor.startDate = document.getElementById('editor-start-date')?.value || getTodayIso();
  state.editor.endDate = document.getElementById('editor-end-date')?.value || addDaysIso(getTodayIso(), 7);
  state.editor.error = null;
  renderLimitEditor();
};

window.toggleEditorActive = function() {
  state.editor.isEnabled = !state.editor.isEnabled;
  renderLimitEditor();
};

window.saveEditorLimit = function() {
  const ed = state.editor;
  const totalMins = (Number(ed.hours) || 0) * 60 + (Number(ed.minutes) || 0);
  if (totalMins < 1 || totalMins > 1439) {
    ed.error = 'El límite diario debe estar entre 1 minuto y 23 horas 59 minutos.';
    renderLimitEditor();
    return;
  }
  if (ed.hasSchedule && ed.startDate && ed.endDate && ed.endDate < ed.startDate) {
    ed.error = 'La fecha de fin no puede ser anterior a la fecha de inicio.';
    renderLimitEditor();
    return;
  }

  const payload = {
    id: ed.editingId || `lim_${Date.now()}`,
    packageName: ed.packageName,
    limitMinutes: totalMins,
    isEnabled: ed.isEnabled,
    startDate: ed.hasSchedule ? ed.startDate : null,
    endDate: ed.hasSchedule ? ed.endDate : null
  };

  const existingIdx = state.limits.findIndex(l => l.id === ed.editingId || l.packageName === ed.packageName);
  if (existingIdx >= 0) {
    state.limits[existingIdx] = payload;
  } else {
    state.limits.unshift(payload);
  }

  // Evaluate thresholds for today and emit alert if 80% or 100% reached
  const todayIso = getTodayIso();
  const usedToday = getUsageForDate(todayIso).find(a => a.id === payload.packageName)?.minutes || 0;
  const appMeta = getAppMeta(payload.packageName);
  if (payload.isEnabled && usedToday >= Math.ceil(payload.limitMinutes * 0.8)) {
    const threshold = usedToday >= payload.limitMinutes ? 100 : 80;
    const now = new Date();
    const tLabel = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    state.alerts.unshift({
      id: `al_${Date.now()}`,
      packageName: payload.packageName,
      appName: appMeta.name,
      thresholdPercent: threshold,
      usedMinutes: usedToday,
      limitMinutes: payload.limitMinutes,
      timestampLabel: tLabel,
      isRead: false
    });
  }

  state.screen = 'limits';
  saveState();
  renderAll();
};

// App Picker Modal for Limit Editor
window.openAppPickerModal = function() {
  const modal = document.getElementById('app-picker-sheet');
  if (modal) {
    modal.classList.add('open');
    renderAppPickerList('');
  }
};

window.closeAppPickerModal = function() {
  const modal = document.getElementById('app-picker-sheet');
  if (modal) modal.classList.remove('open');
};

window.renderAppPickerList = function(query) {
  const q = (query || '').trim().toLowerCase();
  const listEl = document.getElementById('app-picker-list');
  if (!listEl) return;
  const filtered = INSTALLED_APPS.filter(a => a.name.toLowerCase().includes(q) || a.catLabel.toLowerCase().includes(q));
  listEl.innerHTML = filtered.map(app => {
    const isSelected = state.editor.packageName === app.id;
    return `
      <button type="button" class="picker-app-item ${isSelected ? 'selected' : ''}" onclick="selectAppInPicker('${app.id}')">
        <div class="app-icon-box" style="background:${app.color}">${getAppSvgIcon(app.icon)}</div>
        <div class="picker-app-info">
          <div class="picker-app-name">${app.name}</div>
          <div class="picker-app-sub">${app.catLabel} · ${app.id}</div>
        </div>
        ${isSelected ? '<span class="picker-check">✓</span>' : ''}
      </button>`;
  }).join('');
};

window.selectAppInPicker = function(pkg) {
  state.editor.packageName = pkg;
  closeAppPickerModal();
  renderLimitEditor();
};

// Stats filters
window.setStatsPeriod = function(period) {
  state.statsPeriod = period;
  saveState();
  renderAll();
};

window.setStatsAppFilter = function(pkg) {
  state.statsAppFilter = pkg;
  saveState();
  renderAll();
};

// Bottom sheets & modals
window.openAlertsSheet = function() {
  state.alerts.forEach(a => { a.isRead = true; });
  saveState();
  renderAll();
  document.getElementById('alerts-bottom-sheet')?.classList.add('open');
};

window.closeAlertsSheet = function() {
  document.getElementById('alerts-bottom-sheet')?.classList.remove('open');
};

window.clearAllAlerts = function() {
  state.alerts = [];
  saveState();
  renderAll();
};

window.openSimulatorSheet = function() {
  renderSimulatorSheet();
  document.getElementById('simulator-bottom-sheet')?.classList.add('open');
};

window.closeSimulatorSheet = function() {
  document.getElementById('simulator-bottom-sheet')?.classList.remove('open');
};

window.openLimitReachedModal = function() {
  closeSimulatorSheet();
  document.getElementById('limit-reached-modal')?.classList.add('open');
};

window.closeLimitReachedModal = function() {
  document.getElementById('limit-reached-modal')?.classList.remove('open');
};

window.openSyncModal = function() {
  closeSimulatorSheet();
  document.getElementById('sync-android-modal')?.classList.add('open');
};

window.closeSyncModal = function() {
  document.getElementById('sync-android-modal')?.classList.remove('open');
};

// Real Android Telemetry Synchronization (/api/sync & Base64 Token)
function applyAndroidTelemetrySnapshot(snapshot) {
  if (!snapshot || typeof snapshot !== 'object') return false;
  const todayIso = getTodayIso();

  if (Array.isArray(snapshot.appsUsageToday) && snapshot.appsUsageToday.length > 0) {
    const dayMap = {};
    snapshot.appsUsageToday.forEach(item => {
      if (item.packageName) {
        dayMap[item.packageName] = [Number(item.usedMinutes) || 0, Number(item.launches) || 0];
        if (!INSTALLED_APPS.some(a => a.id === item.packageName)) {
          INSTALLED_APPS.push({
            id: item.packageName,
            name: item.appName || item.packageName.split('.').pop(),
            category: item.category || 'SYSTEM',
            catLabel: 'App del dispositivo',
            color: '#2563EB',
            icon: 'app'
          });
        }
      }
    });
    state.usageByDate[todayIso] = dayMap;
  }

  if (Array.isArray(snapshot.limits)) {
    state.limits = snapshot.limits.map((l, idx) => ({
      id: l.id || `lim_sync_${idx}`,
      packageName: l.packageName,
      limitMinutes: Number(l.limitMinutes) || 60,
      isEnabled: Boolean(l.isEnabled),
      startDate: l.startDate || null,
      endDate: l.endDate || null
    }));
  }

  if (Array.isArray(snapshot.alerts)) {
    state.alerts = snapshot.alerts.map((a, idx) => ({
      id: a.id || `al_sync_${idx}`,
      packageName: a.packageName,
      appName: a.appName || 'App',
      thresholdPercent: Number(a.thresholdPercent) || 80,
      usedMinutes: Number(a.usedMinutes) || 0,
      limitMinutes: Number(a.limitMinutes) || 60,
      timestampLabel: a.timestampLabel || 'Ahora',
      isRead: Boolean(a.isRead)
    }));
  }

  state.syncSource = 'ANDROID_LIVE';
  state.lastSyncDevice = snapshot.deviceModel || 'Android Screenly APK';
  state.lastSyncTime = new Date().toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
  saveState();
  renderAll();
  return true;
}

window.importSyncTokenFromAndroid = function() {
  const inputEl = document.getElementById('sync-token-textarea');
  const feedbackEl = document.getElementById('sync-modal-feedback');
  if (!inputEl || !feedbackEl) return;

  const raw = inputEl.value.trim();
  if (!raw) {
    feedbackEl.style.color = '#DC2626';
    feedbackEl.textContent = 'Pega el código Base64 o JSON generado desde la app Android.';
    return;
  }

  try {
    let jsonStr = raw;
    if (!raw.startsWith('{')) {
      jsonStr = decodeURIComponent(escape(atob(raw)));
    }
    const parsed = JSON.parse(jsonStr);
    applyAndroidTelemetrySnapshot(parsed);
    feedbackEl.style.color = '#16A34A';
    feedbackEl.textContent = `✓ Sincronizado correctamente con ${state.lastSyncDevice} (${state.lastSyncTime}).`;
  } catch (err) {
    feedbackEl.style.color = '#DC2626';
    feedbackEl.textContent = 'El formato del token de sincronización no es válido.';
  }
};

window.fetchLiveVercelSync = async function(showFeedback) {
  const feedbackEl = document.getElementById('sync-modal-feedback');
  try {
    const res = await fetch('/api/sync');
    if (!res.ok) return;
    const data = await res.json();
    if (data && data.hasLiveSnapshot && data.snapshot) {
      applyAndroidTelemetrySnapshot(data.snapshot);
      if (showFeedback && feedbackEl) {
        feedbackEl.style.color = '#16A34A';
        feedbackEl.textContent = `✓ Datos en vivo recibidos desde el endpoint /api/sync (${state.lastSyncDevice}).`;
      }
    } else if (showFeedback && feedbackEl) {
      feedbackEl.style.color = '#2563EB';
      feedbackEl.textContent = 'El endpoint /api/sync está operativo. Envía datos desde el Panel de Pruebas en Android o pega el token Base64.';
    }
  } catch (e) {
    // Offline or local file mode
  }
};

window.resetDemoData = function() {
  localStorage.removeItem(STORAGE_KEY);
  state = getDefaultState();
  closeSimulatorSheet();
  renderAll();
};

document.addEventListener('DOMContentLoaded', () => {
  renderAll();
  setInterval(updateClocks, 15000);
  window.fetchLiveVercelSync(false);
});
