#!/usr/bin/env bash
# 本地最低验证：前端构建 + 后端单元测试
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "==> frontend: npm run build"
npm --prefix frontend run build

echo "==> backend: mvn test"
if [[ -f "$ROOT/.env" ]]; then
  # shellcheck disable=SC1091
  source "$ROOT/.env"
fi
mvn -f "$ROOT/backend/pom.xml" \
  -Dmaven.repo.local="${MAVEN_REPO_LOCAL:-$ROOT/.m2/repository}" \
  test

echo "==> 验证通过"
