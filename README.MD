# Pibrary

[English](README_EN.md)

`Pibrary` 是 PickAID 系列的根基础模组。它放最常用、最稳定、下游项目会反复需要的能力：Facet 状态挂载、注册辅助、实体与投掷物工具、数学工具、配置入口、诊断入口、recipe-viewer 中立契约和少量渲染/tint 辅助。

当前主线版本文档：

- [1.20.1 中文 Wiki](docs/wiki/1.20.1/zhCN/README.md)
- [1.20.1 English Wiki](docs/wiki/1.20.1/enUS/README.md)

## 当前定位

- 作为 Pi 系列公共依赖的稳定根。
- 为 Pibrary 下游提供短、稳、迁移友好的 API。
- 把 PiSerializeKit 和 PiNet 的底层能力包装成更好用的上层入口。
- 把 Minecraft/Forge 容易变动的实现细节尽量收进兼容层。

README 只保留入口。Facet、Registrate、配置、JEI、Math、Text、recipe、projectile、tint 等具体用法都放进 versioned wiki。

## 致谢

Pibrary 的配置与 Registrate 相关设计参考并学习了 lcy 在 L2 系列项目中的代码，尤其是 L2Core 和 L2Hostility。这里的实现是 Pibrary 自己写的，但“注册项和默认 datapack 配置放在一条链上”“用 config type 管理一类 JSON”“通过 datagen collector 统一写文件”等方向，来自对 L2 代码的学习。

## Maven

如果项目使用 Pi 系列模板，在 `project.toml` 里加依赖即可：

```toml
[dependencies.deobf_implementation]
pibrary = { notation = "com.mihono.pickaid:pibrary:<version>", transitive = false }

[dependencies.jarjar]
pibrary = { notation = "com.mihono.pickaid:pibrary:<version>", range = "[<version>,0.1.0)", transitive = false }
```
