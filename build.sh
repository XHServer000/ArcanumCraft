#!/usr/bin/env bash
# =============================================================================
# ArcanumCraft 编译脚本
# 依赖: Maven (mvn), JDK 21
# 用法: chmod +x build.sh && ./build.sh
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_NAME="ArcanumCraft"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

log_info()  { echo -e "${CYAN}[INFO]${NC}  $*"; }
log_success() { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error() { echo -e "${RED}[FAIL]${NC}  $*"; }

# 检查 Maven
if ! command -v mvn &>/dev/null; then
    log_error "未找到 Maven，请先安装 Maven"
    exit 1
fi

# 检查 JDK 21
JAVA_HOME_CHECK=$(java -version 2>&1 | grep -i "version" | head -1 || true)
log_info "Java 版本: $JAVA_HOME_CHECK"

if ! java -version 2>&1 | grep -qE "21|22|23"; then
    log_warn "检测到 JDK 非 21，建议升级至 JDK 21 以获得最佳兼容性"
fi

log_info "开始编译 ${PROJECT_NAME} ..."
cd "$SCRIPT_DIR"

# 清理旧构建产物
log_info "清理旧构建产物..."
mvn clean -q 2>/dev/null || true

# 执行编译
log_info "执行 Maven 编译..."
if mvn package -q --no-transfer-progress; then
    log_success "编译成功！"
else
    log_error "编译失败，请检查上方错误信息"
    exit 1
fi

# 查找生成的 JAR
JAR_PATH=$(find "$SCRIPT_DIR/target" -name "${PROJECT_NAME}.jar" ! -name "*-sources*" ! -name "*-javadoc*" 2>/dev/null | head -1)
if [[ -z "$JAR_PATH" ]]; then
    # 尝试 shade 后的 jar
    JAR_PATH=$(find "$SCRIPT_DIR/target" -name "*.jar" ! -name "*-sources*" ! -name "*-javadoc*" 2>/dev/null | head -1)
fi

if [[ -z "$JAR_PATH" ]]; then
    log_error "未找到生成的 JAR 文件"
    exit 1
fi

JAR_SIZE=$(du -h "$JAR_PATH" | cut -f1)
log_success "JAR 文件: $JAR_PATH ($JAR_SIZE)"

# 生成源码压缩包
SOURCE_ZIP="$SCRIPT_DIR/${PROJECT_NAME}-source.zip"
log_info "生成源码压缩包: $SOURCE_ZIP"
(
    cd "$SCRIPT_DIR/.."
    zip -r "$SOURCE_ZIP" "$PROJECT_NAME" \
        -x "${PROJECT_NAME}/build/*" \
        -x "${PROJECT_NAME}/.git/*" \
        -x "${PROJECT_NAME}/.gradle/*" \
        -x "${PROJECT_NAME}/target/*" \
        -x "${PROJECT_NAME}/*.iml" \
        -x "*node_modules/*"
)
log_success "源码包: $SOURCE_ZIP ($(du -h "$SOURCE_ZIP" | cut -f1))"

# 输出安装说明
echo ""
echo -e "${CYAN}══════════════════════════════════════════════════${NC}"
echo -e "${GREEN}  ${PROJECT_NAME} 编译完成！${NC}"
echo -e "${CYAN}══════════════════════════════════════════════════${NC}"
echo ""
echo -e "  📦 JAR 文件:  ${GREEN}$JAR_PATH${NC}"
echo -e "  📦 源码包:    ${GREEN}$SOURCE_ZIP${NC}"
echo ""
echo -e "  ${YELLOW}安装方式:${NC}"
echo -e "    将 JAR 文件复制到 Paper/Spigot 服务器的 plugins/ 目录："
echo -e "    cp \"$JAR_PATH\" /path/to/server/plugins/"
echo ""
echo -e "  ${YELLOW}首次运行配置:${NC}"
echo -e "    1. 启动服务器，插件会自动生成 config.yml"
echo -e "    2. 编辑 config.yml 调整各项参数"
echo -e "    3. 使用 /arcanumcraft reload 重载配置"
echo ""
echo -e "  ${YELLOW}常用命令:${NC}"
echo -e "    /crystal          - 查看魔晶余额"
echo -e "    /crystal pay <玩家> <数量> - 转账魔晶"
echo -e "    /portal set <名称>   - 建立传送门"
echo -e "    /portal goto <名称>  - 传送至目标"
echo -e "    /forge status     - 查看熔炉状态"
echo -e "    /enchant info     - 查看可附魔列表"
echo ""
