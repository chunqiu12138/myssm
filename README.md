 ### 简易的SSM框架


- 实现了SSM框架的部分基本功能
  - 基于Java反射机制和xml文件，通过对xml文件bean标签和property标签的扫描，实现Java对象的实例化、IOC容器的创建、依赖注入
  - 通过创建自定义的DispatcherServlet类继承HttpServlet并重写Service方法，实现获取对应的Controller调用目标方法，完成职责的分派
