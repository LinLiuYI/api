## 项目介绍 🙋



**作为用户可以通过注册登录账户，获取接口调用权限，并根据自己的需求浏览和选择适合的接口。您可以在线进行接口调试，快速验证接口的功能和效果。** 

**作为开发者 我们提供了[客户端SDK: API-SDK]，实现更高效的开发和调用。** 

**您可以将自己的接口接入到API 接口开放平台平台上，并发布给其他用户使用。 您可以管理和各个接口，以便更好地分析和优化接口性能。** 

## 目录结构 📑


| 目录                                                     | 描述               |
|--------------------------------------------------------| ------------------ |
| **🏘️ [backend](./api-backend)**             | API后端服务模块 |
| **🏘️ [common](./api-common)**               | 公共服务模块       |
| **🕸️ [gateway](./api-gateway)**             | 网关模块           |
| **🔗 [interface](./api-interface)**          | 接口模块           |

## 快速启动 🚀

### 后端

执行sql目录下ddl.sql

## 项目选型 🎯

### **后端**

- Spring Boot 2.7.0
- Spring MVC
- MySQL 数据库
- 腾讯云COS存储
- Dubbo 分布式（RPC、Nacos）
- Spring Cloud Gateway 微服务网关
- API 签名认证（Http 调用）
- IJPay-AliPay  支付宝支付
- WeiXin-Java-Pay  微信支付
- Swagger + Knife4j 接口文档
- Spring Boot Starter（SDK 开发）
- Jakarta.Mail 邮箱通知、验证码
- Spring Session Redis 分布式登录
- Apache Commons Lang3 工具类
- MyBatis-Plus 及 MyBatis X 自动生成
- Hutool、Apache Common Utils、Gson 等工具库
