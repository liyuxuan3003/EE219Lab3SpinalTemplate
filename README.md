# SpinalSulfurTemplate

SpinalSulfurTemplate是一个适用于SpinalHDL项目的模板，集成了从SpinalHDL到Verilog的编译和调用Vivado综合上板的功能。

## 仓库克隆
SpinalSulfurTemplate包含了若干子模块，因此克隆时需加相应选项
```
git clone --recursive git@Lithium:Hardware/SpinalSulfurTemplate.git
```

若未加该选项
```
git clone git@Lithium:Hardware/SpinalSulfurTemplate.git
```

你需要执行以下命令进行子模块初始化
```
cd SpinalSulfurTemplate
git submodule init
git submodule update
```

## 仓库结构
SpinalSulfurTemplate仓库结构如下
```
SpinalSulfurTemplate
|- .git
|- .gitignore
|- .scalafmt.conf
|- README.md
|- SpinalSulfur
    |- makefile-spinal [Submodule]
    |- magicleda [Submodule]
    |- TopLevel.scala
    |- Config.scala
    |- build.sbt
    |- Makefile
```

如果你希望修改`SpinalSulfur`的目录名称为`MyProject`，你需要进行下列操作
1. 重命名文件夹`SpinalSulfur`为`MyProject`，由于存在子模块，请使用`git mv SpinalSulfur MyProject`。
2. 修改`TopLevel.scala`和`Config.scala`中的`package SpinalSulfur`。
3. 修改`build.sbt`中的`name := "SpinalSulfur"`。

## 使用方法
SpinalSulfurTemplate提供Makefile实现了从SpinalHDL编译仿真到FPGA上板的所有工作的自动化，有关可用的Make命令，参见子模块makefile-spinal的README。有关上板相关的额外配置，参见子模块magcleda的README。

由于本项目的目录结构原因，在仓库根目录处，使用VSCode的Scala插件提供的run功能可能会出现与无法创建`build`目录相关的错误，对于该问题，鼓励使用Makefile而不是图形化界面上的点击。然而，已知有以下方法可能可以部分解决这个问题
- 在`SpinalSulfurTemplate/SpinalSulfur`处而不是仓库根目录`SpinalSulfurTemplate`处打开VSCode。
- 在`SpinalSulfurTemplate`下创建一个`build`的软链接`ln -s SpinalSulfur/build build`，然而，如果Scala代码中涉及对文件的读写，这种方法仍然可能出现路径问题。