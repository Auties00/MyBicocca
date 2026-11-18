const sidebarKey = 'mybicocca:sidebar-collapsed';

function readCollapsed(): boolean {
	try {
		return localStorage.getItem(sidebarKey) === '1';
	} catch {
		return false;
	}
}

/** UI state of the app chrome that several shell parts read and the shortcuts change. */
class ShellState {
	paletteOpen = $state(false);
	shortcutsOpen = $state(false);
	/** The account sheet (compact) or popover (wider). */
	accountOpen = $state(false);
	/** Expanded size class only: the sidebar folded to the rail (Ctrl/⌘+B), remembered. */
	sidebarCollapsed = $state(readCollapsed());

	toggleSidebar() {
		this.sidebarCollapsed = !this.sidebarCollapsed;
		try {
			localStorage.setItem(sidebarKey, this.sidebarCollapsed ? '1' : '0');
		} catch {
			/* Not remembered in private mode; still toggles. */
		}
	}
}

export const shell = new ShellState();
