import { inject } from "vue";

export const dashboardShellKey = Symbol("dashboardShell");

const fallbackShell = {
  openSidebar: () => {},
  closeSidebar: () => {},
};

export function useDashboardShell() {
  return inject(dashboardShellKey, fallbackShell);
}
