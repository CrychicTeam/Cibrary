# Pibrary 1.20.1 Wiki

Pibrary 的 1.20.1 文档按基础能力拆分：

- [Facet 状态系统](facets.md)
- [PiRegistrate 和创造栏](registrate.md)
- [配置与 datapack 数据](../../../zhCN/config.md)
- [Recipe / JEI 中立契约](../../../zhCN/jei-integration.md)
- [Recipe runtime](recipe.md)
- [Entity damage helper](entity-damage.md)
- [Projectile trace](projectile.md)
- [Tint 染色](tint.md)
- [Math 教程](../../../zhCN/math.md)
- [富文本与图片 Component](../../../zhCN/text-markup.md)

## 代码分层

- `api/**`：下游直接使用的稳定入口。
- `runtime/**`：Forge 1.20.1 落地实现，例如 capability、sync、creative tab、recipe cache、projectile trace。
- `mixin/**`：只放确实需要访问原版内部结构的点。
- `src/devExample/java/**`：参与编译测试，但不会进入生产 jar 的示例代码。

## 重点 API

- `api/facet`：living / level / chunk facet 注解、typed handle、descriptor bootstrap 和状态基类。
- `api/registry`：PiRegistrate 扩展、创造栏 section、NBT variant、tint 声明。
- `api/config`：配置条目、分组 spec、作用域、注释与默认值校验。
- `api/recipe` 和 `api/jei`：机器逻辑和 recipe-viewer compat 共用的中立契约。
- `api/math`：基于原版 `Vec3` / `AABB` 的数学、曲线、权重、视口投影和几何 helper。
- `api/entity`：实体生命周期、空间查询、hurt 事件辅助和原版兼容伤害计算。
- `api/projectile`：面向性能优化的 projectile trace / impact 契约。
