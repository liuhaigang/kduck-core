# K-Duck

# 框架介绍

​    K-Duck是一款基于Spring MVC、Spring、Spring JdbcTemplate为底层技术的开源、免费的开发框架，在此框架上你可以快速的构建出自己想要的功能模块。与以往的框架不同的是，框架将数据表以对象的方式进行了封装，自身完全接管了数据访问层的逻辑，开发者无需编写DAO层的逻辑代码，并要求以SQL装配的形式构造SQL查询语句，降低SQL拼写的能力要求，减少由于SQL拼写导致的问题。在业务层也提供了一套较为灵活的默认Service实现，尽可能的避免开发者编写重复的逻辑代码，并将具体的业务对象进行了更高一层的抽象，形成统一、扩展能力良好的业务对象供所有业务场景使用。由于进行了抽象设计，为一些切面性的管理、控制的需求场景提供了可行性。

# 框架底层技术

1.  JDK 8

2.  Spring-Boot 2.1.5

# 框架组件

框架目前由以下几模块组成

-   **core模块：** 核心模块，包含了框架使用的核心逻辑，会强制绑定数据源，因此需要在application.yml中配置数据源信息。

-   **security模块：** 安全模块，一般对于单体应用需要安全控制时需要进行依赖，负责系统的认证、授权。

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


```java
package cn.kduck;
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
这个和常规的SpringBoot项目启动类没有什么区别

然后需要为工程创建一个配置文件application.yml，注意这个名字默认配置文件名称，不要随意修改命名。然后为其配置数据源信息（因为框架默认会对数据表进行扫描来获取实体对象信息），配置文件如下所示：
```
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/kduck_demo? useSSL=false&nullCatalogMeansCurrent=true&serverTimezone=Asia/Shanghai
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: liuhg
    password: gang317
```
当然你也可以禁用启动时扫描数据表的功能，但在此时我们先进行启动扫描为例接下来的步骤。

如果是Mysql数据库，为了能使用扫描功能，需要增加连接参数： nullCatalogMeansCurrent=true&useInformationSchema=true。（前者确保可以获取到准确的用户下的数据表，后者参数为了获取数据表定义的备注信息，如果启站过程发生卡顿，可尝试去掉后者参数。）

上面配置为SpringBoot的标准配置，更多配置请参考Spring官方手册。

然后我们启动程序，运行Application，看到如下界面表示启动成功：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0106/214758_3441f283_403814.png "启动成功.png")

由于当前数据库中没有任何数据表，因此启动信息中没有任何数据表扫描的信息输出。

## 模块开发

接下来会以一个示例来演示如何使用框架来开发。假设需要开发一个班级学员的模块，模块含有班级和学员两张表，数据表设计如下：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0106/214833_356c4698_403814.png "演示ER.png")

按照下面的步骤创建模块：
1.	创建为该模块创建一个包，例如：cn.kduck.demo
2.	在该包下创建web包及一个控制器类：DemoController
3.	在该包下创建query包及一个查询器类：DemoQuery
创建后如图：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0106/214850_c50ab2cb_403814.png "演示代码结构.png")

先为查询器对象编写查询逻辑（查询器主要负责拼装查询语句），DemoQuery实现cn.kduck.core.dao.query.QueryCreator接口，并实现接口方法，代码如下：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0106/215104_763540d7_403814.png "屏幕截图.png")
- queryCode：返回查询器的编码，要求全局唯一，用于获取该查询器时，作为标识使用。此方法可以不实现，框架会以当前类名为编码，因此需要注意重名情况。
- createQuery：构造一个QuerySupport对象返回，该对象可以得到真正执行用的Query。一般使用SelectBuilder来构造，后面的章节会对SelectBuilder的使用进行详细说明。示例代码中的含义是要进行CLASS_INFO的实体进行查询，并且支持按照className（班级名称）进行模糊查询。
- 最后需要声明为一个Spring的Bean。在类头打上注解：@Component

然后开始编写DemoController代码，使用框架自带的默认业务逻辑对象DefaultService，直接使用最终代码类似：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0106/215231_8d99e342_403814.png "最终代码.png")

> 重要说明：此处仅为示例，实际开发中不建议直接在Controller中使用DefaultService类，强烈推荐通过创建一个业务Service接口及对应实现类的方式，实现类继承DefaultService类的方式使用。

这是一个标准的Controller的写法（未加入Swagger相关注解），从代码层面几乎没有什么特别的，对于添加修改的传入参数和返回对象都是ValueMap或ValueMapList对象，同时只是这里使用的是默认的DefaultService，我们并未定义一个单独的业务接口，DefaultService的大部分接口都是需要传入实体定义对象的编码（默认是表名全大写），这个编码用于定位唯一的实体定义对象，这样框架才知道具体操作的数据表，这个编码在开发时推荐定义到一个单独的常量类中进行统一管理。

这段示例代码中，可以看到在条件查询时，我们是先构造了一个查询条件的Map来保存查询条件值，ParamMap是一个便于构造参数Map的工具类，然后调用DefaultService.getQuery方法通过编码值得到我们上面创建的DemoQuery对象，同时将查询参数Map传递进getQuery方法便于条件的拼装。之后调用list方法进行条件查询返回查询结果，page对象是分页对象，只包含分页信息，不包含结果集，用于进行分页处理，list方法执行后，会对page中的页码相关属性进行更新，最后构造一个JsonPageObject将分页及查询结果返回。

然后我们按照同样的步骤再创建班级学员（STUDENT_INFO）的代码，这里不再赘述。

如果在添加班级的同时也保存学员，这个时候需要扩展单独的Service接口进行实现了，在demo包下创建service子包及DemoService接口及相关的实现类，并创建一个addClassAndStudents(ValueMap classInfo, List<ValueMap> studentList)方法，编写实现类代码如图：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0106/215306_434646c6_403814.png "实现类代码.png")

- 接口实现类需要继承DefaultService类，这样才能使用封装的数据操作方法。
- 调用add方法保存班级信息，并返回保存后的主键。
- 调用batchAdd方法将学员信息批量保存，此处每个学员信息里需要设置班级的Id与新建立的班级进行关联，batchAdd的最后一个参数用于此种情况，会为List中的每个Map对象设置额外的扩展属性，具体使用方法请参考相关API文档。
- 最后不要忘记标记@Transactional事务注解，因为这个方法进行了多次写操作，要保持操作的原子性。

## 使用Bean对象

可以看到在框架中默认都是使用ValueMap和ValueMapList对象代表统一的业务对象来贯穿所有模块，负责承载业务数据的封装。但对于业务逻辑稍微复杂的模块，会出现满处皆ValueMap的窘状，开发者无法快速识别每个ValueMap到底是哪个业务对象。为此，框架也用一种对象结构的形式支持了Bean对象的形式但也保留着ValueMap的特性。这种结构以一个示例说明，如图所示：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0106/215736_238a7165_403814.png "Bean形式ValueMap.png")

以这种结构兼顾着JavaBean和ValueMap的特性，在编写业务逻辑中使用这种结构对象就可以避免出现上述混乱的问题。但接下来的问题就是一般IDE无法支持这种代码结构的自动生成，手动生成这种异结构的JavaBean还是会造成一定的工作量，为此框架专门定制了一种基于Eclipse和IntelliJ IDEA编辑器的插件，可以像生成传统getter和setter方法那样，方便快速的生成此结构的代码。

具体插件请到这里获取：https://gitee.com/platform_team/kduck-codeplugin-idea/releases

## 查询构造器

SelectBuilder是构造查询语句的构造器对象，可以将拼装SQL的部分封装到构造器中进行，可以支撑统一对SQL拼装逻辑的优化。SelectBuilder经常被用在QueryCreator接口实现中，该接口方法参数中提供了实体仓库对象，便于获取实体定义对象，因为构造器要求构造时提供要查询表对应的实体对象，然后调用where()开始条件的拼写，最后调用build()方法返回QuerySupport对象。

下面是SelectBuilder调用方法调用链图，包含了主要的SQL装配方法：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0106/215809_343d78fc_403814.png "SelectBuilder结构.png")

（上图已经是旧版本，待更新。目前已支持更为丰富的函数）

以下是一个SelectBuilder的基本用法：

```java
  //准备查询参数值Map
  Map<String, Object> paramMap = ParamMap.create("userName", "刚").set("age","20").toMap();

  //创建一个查询构造器
  SelectBuilder sqlBuiler = new SelectBuilder(paramMap);

  //绑定两表需要返回的查询字段（a别名表下的所有字段及b别名表下除userId属性对应字段外的所有字段）
  sqlBuiler.bindFields("a",userEntityDef.getFieldList())
           .bindFields("b", BeanDefUtils.excludeField(orgUserEntityDef.getFieldList(),"userId"));

  //首先构造查询的表及关系（前一个参数为别名，第二个参数为表对应的实体对象），两表之间为INNER JOIN关系
  //然后构造查询条件
  sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef)
   .where()
  .and("a.USER_NAME", ConditionType.BEGIN_WITH,"userName")
  .or("a.AGE", ConditionType.IS_NOT_EMPTY);

  //该查询需要以字段进行COUNT统计数量
  sqlBuiler.bindAggregate("a.USER_NAME", AggregateType.COUNT);
  QuerySupport querySupport = sqlBuiler.build();
```
最终构造出的SQL语句为：

```sql
SELECT a.USER_ID,a.USER_NAME,a.GENDER,a.BIRTHDAY,COUNT(a.AGE) AS AGE,a.ENABLE,b.ORG_USER_ID,b.ORG_ID  FROM DEMO_USER a INNER JOIN DEMO_ORG_USER b ON a.USER_ID=b.USER_ID WHERE a.USER_NAME LIKE ? OR  (a.AGE IS NOT NULL AND a.AGE !='') 
```
参数为："刚%"

一个简单的完整示例，请参考：[kduck-core示例项目](https://gitee.com/lhg317/kduck-example)。

我们会陆续完善该项目，有任何关于K-Duck框架问题可邮件至：lhg_0317@163.com