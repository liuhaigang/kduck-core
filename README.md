# kduck-core

# 框架介绍

​    K-Duck是一款基于Spring MVC、Spring、Spring JdbcTemplate为底层技术的开发框架，在此框架上你可以快速的构建出自己想要的功能模块。与以往的框架不同的是，框架将数据表以对象的方式进行了封装，自身完全接管了数据访问层的逻辑，开发者无需编写DAO层的逻辑代码，并要求以SQL装配的形式构造SQL查询语句，降低SQL拼写的能力要求，减少由于SQL拼写导致的问题。在业务层也提供了一套较为灵活的默认Service实现，尽可能的避免开发者编写重复的逻辑代码，并将具体的业务对象进行了更高一层的抽象，形成统一、扩展能力良好的业务对象供所有业务场景使用。由于进行了抽象设计，为一些切面性的管理、控制的需求场景提供了可行性。

# 框架底层技术

1.  JDK 8

2.  Spring-Boot 2.1.5

# 框架组件

框架目前由以下几模块组成

-   **core模块：**核心模块，包含了框架使用的核心逻辑，会强制绑定数据源，因此需要在application.yml中配置数据源信息。

-   **security模块：**安全模块，一般对于单体应用需要安全控制时需要进行依赖，负责系统的认证、授权。

# 功能架构

    框架虽然在开发时也是传统的分层结构，即用户接口层，业务逻辑层和数据访问层，但框架将一些常见、重复的逻辑进行了封装，开发者只需要关注具体的业务逻辑代码的编写。

![输入图片说明](https://images.gitee.com/uploads/images/2021/0106/000120_90c0a16e_403814.png "功能架构.png")
    
（图左半部分）框架将数据表的信息也进行了对象化设计，形成了具体的类对象：数据实体定义，目前是由框架进行扫描创建，未来可扩展成其他形式，如JSON数据和单独的维护功能等。由于表的对象化设计，对字段级的功能实现提供了良好的支撑，并为平台统一构造数据访问对象提供了可能。

（图中间部分）结构的改变并不会导致分层的变化，只是在接口参数平台也进行了统一的设计，将所有业务对象更高一层的抽象，所有提交的参数和返回的对象均是ValueMap或ValueMapList对象（数据层对象除外，是ValueBean，开发者一般不会直接调用数据层接口），如没有特殊需要不会为业务单独定制业务对象，如同对象名所示，对象是一个Map形式的对象，因此并不会约束具体的属性，这样才能成为所有场景的统一业务对象。但这也会导致一些在开发控制上的难度（需要事先清楚业务拥有哪些业务属性），因此框架在使用上尽可能的保证了数据的正确性，支持属性的存在性检查，同时支持一种Bean形式的Map对象让开发者像是在使用传统的业务Bean而又保留了Map的所有特性（后续介绍）。

（图右半部分）数据访问层是由框架进行统一管理的（JdbcDao），开发者一般情况下不会直接使用该对象，而是间接的使用默认业务实现类（DefaultService）来访问数据，该对象包含了常用的数据访问操作接口，如单数据添加、批量添加、主键删除、外键删除、主键更新、外键更新、单数据条件查询、集合条件查询等等，在条件查询时，由于逻辑相对灵活，因此框架专门为条件查询提供了QueryCreator接口进行单独的处理。在默认业务实现类无法满足业务要求时，需继承该接口进行方法扩展，但无论是那种方式，开发者只允许调用默认业务实现类中的方法。

因此，如果没有复杂的业务场景，开发者无需编写数据访问层及业务逻辑层代码，减少了代码编写量，专注于逻辑编写。

# 框架使用

## 环境搭建

首先使用IDE新建立maven项目，在maven的pom文件中加入kduck的依赖，如图所示：


```
   <dependency>
    <groupId>cn.kduck</groupId>
    <artifactId>kduck-core</artifactId>
    <version>1.1.0</version>
   </dependency>
```

首次建站也许会花费一些时间来下载相关的依赖jar文件。

然后创建代码包com.goldgov，在该包下创建一个java文件用于启动主程序，比如我们创建一个Applicaton.java文件（主类如果放在其他的包中，需要Spring扫描cn.kduck.simple包代码），为其编写启动代码如下：


```
package cn.kduck.simple;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```


//TODO 