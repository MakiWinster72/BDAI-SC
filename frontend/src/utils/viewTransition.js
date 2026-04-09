export function navigateWithViewTransition(router, to) {
  const currentPath = router.currentRoute.value.path;
  const targetPath = router.resolve(to).path;
  if (targetPath !== currentPath) {
    window.scrollTo({ top: 0, left: 0, behavior: "auto" });
  }
  return router.push(to);
}
