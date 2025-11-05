# EE219Lab3SpinalTemplate

EE219Lab3SpinalTemplate针对EE219课程的Lab3，重新设计了一个完全基于SpinalHDL的代码框架。

需要实现的模块如下（模块接口和参数已提供）
- `Image2Col`
- `SystolicArray`
- `ProcessElement`

顶层模块`Conv2D`和符合课程要求的测试也均已实现，若要进行完整测试，请运行
```
make sim
```