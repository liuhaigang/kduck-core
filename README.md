# K-Duck

# 框架介绍

​    K-Duck是一款基于Spring MVC、Spring、Spring JdbcTemplate为底层技术的开源、免费的开发框架，在此框架上你可以快速的构建出自己想要的功能模块。与以往的框架不同的是，框架将数据表以对象的方式进行了封装，自身完全接管了数据访问层的逻辑，开发者无需编写DAO层的逻辑代码，并要求以SQL装配的形式构造SQL查询语句，降低SQL拼写的能力要求，减少由于SQL拼写导致的问题。在业务层也提供了一套较为灵活的默认Service实现，尽可能的避免开发者编写重复的逻辑代码，并将具体的业务对象进行了更高一层的抽象，形成统一、扩展能力良好的业务对象供所有业务场景使用。由于进行了抽象设计，为一些切面性的管理、控制的需求场景提供了可行性。

# 框架底层技术

1.  JDK 8

2.  Spring-Boot 2.4.0

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

然后创建代码包com.goldgov，在该包下创建一个java文件用于启动主程序，比如我们创建一个Applicaton.java文件（主类如果放在其他的包中，需要Spring扫描cn.kduck包代码），为其编写启动代码如下：


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
    url: jdbc:mysql://127.0.0.1:3306/kduck_demo?useSSL=false&nullCatalogMeansCurrent=true&serverTimezone=Asia/Shanghai
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: liuhg
    password: liuhg317
```
当然你也可以禁用启动时扫描数据表的功能，但在此时我们先进行启动扫描为例接下来的步骤。

如果是Mysql数据库，为了能使用扫描功能，需要增加连接参数： nullCatalogMeansCurrent=true&useInformationSchema=true。（前者确保可以获取到准确的用户下的数据表，后者参数为了获取数据表定义的备注信息，如果启站过程发生卡顿，可尝试去掉后者参数。）

上面配置为SpringBoot的标准配置，更多配置请参考Spring官方手册。

然后我们启动程序，运行Application，看到如下界面表示启动成功：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0106/214758_3441f283_403814.png "启动成功.png")

由于当前数据库中没有任何数据表，因此启动信息中没有任何数据表扫描的信息输出。下面的章节我们基于这个空项目创建一个演示用的模块。

## 模块开发

接下来会以一个示例来演示如何使用框架来开发。假设需要开发一个班级学员的模块，模块含有班级和学员两张表，数据表设计如下：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0106/214833_356c4698_403814.png "演示ER.png")

按照下面的步骤创建模块：
1. 创建为该模块创建一个包，例如：cn.kduck.example
2. 在包下创建service包，创建班级实体ClassInfo和学生实体StudentInfo对象
3. 创建业务接口及对应的实现类，在service包中创建一个DemoService接口，并再创建一个service.impl包，在其下创建一个DemoService接口的实现类DemoServiceImpl。
4. 在该包下创建query包及一个查询器类：DemoQuery，主要用于构造数据查询对象。
5. 在该包下创建web包及一个控制器类：DemoController
创建后的代码结构如图：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0107/232910_44cc8449_403814.png "演示代码结构.png")

然后我们依次完善每个类的代码逻辑，首先我们完善两个实体的属性，框架为了扩展性，在设计初期使用的是Map对象（框架中为ValueMap对象）代替了传统Bean对象，
负责承载业务数据的封装。但对于业务逻辑稍微复杂的模块，会出现满处皆ValueMap的窘状，开发者无法快速识别每个ValueMap到底是哪个业务对象。
为此，框架也用一种对象结构的形式支持了Bean对象的形式但也保留着ValueMap的特性。所以我们的班级和学生实体看起来像是这样的结构：

ClassInfo:
```java
public class ClassInfo extends ValueMap {

    /**班级ID*/
    public static final String CLASS_ID = "classId";
    /**班级名称*/
    public static final String CLASS_NAME = "className";
    /**班号*/
    public static final String CLASS_NO = "classNo";

    public ClassInfo() {}

    public ClassInfo(Map<String, Object> map) {
        super(map);
    }

    public void setClassId(Long classId) {
        super.setValue(CLASS_ID, classId);
    }

    public Long getClassId() {
        return super.getValueAsLong(CLASS_ID);
    }

    public void setClassName(String className) {
        super.setValue(CLASS_NAME, className);
    }

    public String getClassName() {
        return super.getValueAsString(CLASS_NAME);
    }

    public void setClassNo(String classNo) {
        super.setValue(CLASS_NO, classNo);
    }

    public String getClassNo() {
        return super.getValueAsString(CLASS_NO);
    }
}
```

StudentInfo:
```java
public class StudentInfo extends ValueMap {

    /**学生ID*/
    public static final String STUDENT_ID = "studentId";
    /**班级ID*/
    public static final String CLASS_ID = "classId";
    /**学生姓名*/
    public static final String NAME = "name";
    /**学生性别*/
    public static final String GENDER = "gender";
    /**学号*/
    public static final String STUDENT_NO = "studentNo";

    public StudentInfo() {}

    public StudentInfo(Map<String, Object> map) {
        super(map);
    }

    public void setStudentId(long studentId) {
        super.setValue(STUDENT_ID, studentId);
    }

    public Long getStudentId() {
        return super.getValueAsLong(STUDENT_ID);
    }

    public void setClassId(Long classId) {
        super.setValue(CLASS_ID, classId);
    }

    public long getClassId() {
        return super.getValueAsLong(CLASS_ID);
    }

    public void setName(String name) {
        super.setValue(NAME, name);
    }

    public String getName() {
        return super.getValueAsString(NAME);
    }

    public void setGender(Integer gender) {
        super.setValue(GENDER, gender);
    }

    public Integer getGender() {
        return super.getValueAsInteger(GENDER);
    }

    public void setStudentNo(Integer studentNo) {
        super.setValue(STUDENT_NO, studentNo);
    }

    public Integer getStudentNo() {
        return super.getValueAsInteger(STUDENT_NO);
    }
}
```

以这种结构兼顾着JavaBean和ValueMap的特性，在编写业务逻辑中使用这种结构对象就可以避免出现上述混乱的问题。但接下来的问题就是一般IDE无法支持这种代码结构的自动生成，手动生成这种异结构的JavaBean还是会造成一定的工作量，为此框架专门定制了一种基于Eclipse和IntelliJ IDEA编辑器的插件，可以像生成传统getter和setter方法那样，方便快速的生成此结构的代码。
具体插件请到这里获取：https://gitee.com/platform_team/kduck-codeplugin-idea/releases

接下来我们定义业务接口及相关方法，我们仅为演示，暂时不考虑接口的合理性及严谨性，接口方法定义如下所示：

```java
public interface DemoService {

    String CODE_CLASS = "CLASS_INFO"; //CLASS_INFO表的编码常量，框架默认以表名全大写为实体编码，该编码会使用在很多地方
    String CODE_STUDENT = "STUDENT_INFO";//STUDENT_INFO表的编码常量，框架默认以表名全大写为实体编码，该编码会使用在很多地方

    void addClass(ClassInfo classInfo);

    void addStudent(Long classId,StudentInfo studentInfo);

    void updateStudent(StudentInfo studentInfo);

    void deleteStudent(String[] studentId);

    StudentInfo getStudent(String studentId);

    List<StudentInfo> listStudent(String studentName, Page page);
}
```

这个接口没有什么不同，其中包含了与业务相关的两个表的编码常量，这个编码会被多处使用，因此我们习惯在业务接口中定义常量的方式使用，注意这两个常量的值虽然看上去和表名一致，但本质上是框架默认使用表名作为了编码值，编码值可以通过框架提供的扩展方式调整生成策略，编码值需全局唯一。

然后创建查询器对象编写查询逻辑，查询器主要负责拼装比较复杂的查询语句，DemoQuery实现cn.kduck.core.dao.query.QueryCreator接口，并实现接口方法，代码如下：

```java
@Component
public class DemoQuery implements QueryCreator {

    @Override
    public QuerySupport createQuery(Map<String, Object> paramMap, BeanDefDepository depository) {
        BeanEntityDef classEntityDef = depository.getEntityDef(CODE_CLASS);//获取班级表的对象的实体表示对象，CODE_CLASS为接口中定义的实体编码
        BeanEntityDef studentEntityDef = depository.getEntityDef(CODE_STUDENT);//获取学生表的对象的实体表示对象，CODE_STUDENT为接口中定义的实体编码

        SelectBuilder selectBuilder = new SelectBuilder(paramMap);
        selectBuilder.bindFields("c", BeanDefUtils.includeField(classEntityDef.getFieldList(), "className"));
        selectBuilder.bindFields("s", studentEntityDef.getFieldList());

        selectBuilder.from("s", studentEntityDef).innerJoin("c", classEntityDef)
                .where()
                .and("s.NAME", ConditionType.CONTAINS,"studentName");//设置一个按照学生姓名模糊匹配的查询条件，其中"studentName"为与方法参数paramMap中的key对应，如果paramMap中没有key为"studentName"的元素，则不拼写该条件。
        return selectBuilder.build();
    }
}
```

- createQuery：构造一个QuerySupport对象返回，该对象可以得到真正执行用的Query。一般使用SelectBuilder来构造，后面的章节会对SelectBuilder的使用进行详细说明。示例代码中的含义是要进行CLASS_INFO的实体进行查询，并且支持按照className（班级名称）进行模糊查询。
- 最后需要声明为一个Spring的Bean。在类头标注注解：@Component

> 框架之所以将查询对象单独提取出来，是因此类查询逻辑是有一定复用价值，将其独立出来可以在多处需要的地方使用。

然后编写接口的实现类：

```java
@Service
public class DemoServiceImpl extends DefaultService implements DemoService {

    @Override
    public void addClass(ClassInfo classInfo) {
        super.add(CODE_CLASS,classInfo);
    }

    @Override
    public void addStudent(Long classId, StudentInfo studentInfo) {
        Assert.notNull(classId,"班级ID不能为null");

        studentInfo.setClassId(classId);
        super.add(CODE_STUDENT,studentInfo);
    }

    @Override
    public void updateStudent(StudentInfo studentInfo) {
        super.update(CODE_STUDENT,studentInfo);
    }

    @Override
    public void deleteStudent(String[] studentId) {
        super.delete(CODE_STUDENT,studentId);
    }

    @Override
    public StudentInfo getStudent(String studentId) {
        return super.getForBean(CODE_STUDENT,studentId,StudentInfo::new);
    }

    @Override
    public List<StudentInfo> listStudent(String studentName, Page page) {
        Map<String, Object> paramMap = ParamMap.create("studentName", studentName).toMap();
        QuerySupport query = super.getQuery(DemoQuery.class, paramMap);
        return super.listForBean(query,page,StudentInfo::new);
    }
}
```
这里和我们平常编写的Service实现类有些不同，首先就是继承了DefaultService类，该类提供类对Dao操作的封装，涵盖类大多数常用的数据访问操作方法。因此我们可以看到在进行增删改查操作时，我们并不需要太多的代码逻辑以及特定Dao的注入实现。
在接口实现中，所有的数据操作均使用DefaultService中提供的方法，在接口中定义的编码值几乎在所有方法中均需要被使用，用来表示预操作的数据表对象。
对于查询方法，由于使用了Bean形式的ValueMap对象，因此可以通过xxxForBean方法来对结果集进行转换，关于其他接口及使用方法，您可以参看DefaultService中其他方法的接口说明。

最后编写DemoController代码，在Controller中注入DemoService接口，直接使用最终代码类似：

```java
@RestController
@RequestMapping("/example")
@Api(tags="示例模块")
public class DemoController {

    private DemoService demoService;

    @Autowired
    public DemoController(DemoService demoService){
        this.demoService = demoService;
    }

    @PostMapping("/class/add")
    @ApiOperation("添加班级")
    @ApiParamRequest({
            @ApiField(name="className",value="班级名称"),
            @ApiField(name="classNo",value="班号")
    })
    public JsonObject addClass(ClassInfo classInfo) {
        demoService.addClass(classInfo);
        return JsonObject.SUCCESS;
    }

    @PostMapping("/student/add")
    @ApiOperation("添加学生信息")
    @ApiParamRequest({
            @ApiField(name="classId",value="班级ID"),
            @ApiField(name="name",value="学生姓名"),
            @ApiField(name="gender",value="学生性别（1男，2女）",allowableValues = "1,2"),
            @ApiField(name="studentNo",value="学号")

    })
    public JsonObject addStudent(Long classId,StudentInfo studentInfo) {
        demoService.addStudent(classId,studentInfo);
        return JsonObject.SUCCESS;
    }

    @PutMapping("/student/update")
    @ApiOperation("更新学生信息")
    @ApiParamRequest({
            @ApiField(name="studentId",value="学生ID"),
            @ApiField(name="name",value="学生姓名"),
            @ApiField(name="gender",value="学生性别（1男，2女）",allowableValues = "1,2"),
            @ApiField(name="studentNo",value="学号")

    })
    public JsonObject updateStudent(StudentInfo studentInfo) {
        demoService.updateStudent(studentInfo);
        return JsonObject.SUCCESS;
    }

    @PutMapping("/student/delete")
    @ApiOperation("删除学生信息")
    @ApiParamRequest({
            @ApiField(name="ids",value="学生ID",allowMultiple = true)
    })
    public JsonObject deleteStudent(@RequestParam("ids") String[] ids) {
        demoService.deleteStudent(ids);
        return JsonObject.SUCCESS;
    }

    @PutMapping("/student/get")
    @ApiOperation("查看学生信息")
    @ApiParamRequest({
            @ApiField(name="studentId",value="学生ID")
    })
    @ApiJsonResponse({
            @ApiField(name="studentId",value="学生ID"),
            @ApiField(name="name",value="学生姓名"),
            @ApiField(name="gender",value="学生性别（1男，2女）",allowableValues = "1,2"),
            @ApiField(name="studentNo",value="学号")
    })
    public JsonObject getStudent(@RequestParam("studentId") String studentId) {
        StudentInfo student = demoService.getStudent(studentId);
        return new JsonObject(student);
    }

    @PutMapping("/student/list")
    @ApiOperation("分页查询学生信息")
    @ApiParamRequest({
            @ApiField(name="studentName",value="学生姓名")
    })
    @ApiJsonResponse(isArray = true,value={
            @ApiField(name="studentId",value="学生ID"),
            @ApiField(name="name",value="学生姓名"),
            @ApiField(name="gender",value="学生性别（1男，2女）",allowableValues = "1,2"),
            @ApiField(name="studentNo",value="学号")
    })
    public JsonObject listStudent(String studentName, @ApiIgnore Page page) {
        List<StudentInfo> studentInfos = demoService.listStudent(studentName, page);
        return new JsonPageObject(page,studentInfos);
    }
}
```

这是一个标准的Controller的写法并加入了Swagger相关注解，从代码层面几乎没有什么特别的。其中@ApiJsonResponse和@ApiField注解是框架扩展的，由于JsonObject对象中仅为标准的属性结构，无法正确真实的反应返回的json数据结构，可以通过这两个注解以json结构展现在swagger界面中。

随后将数据表创建好，并再次启动应用，在启动的日志输出中可以看到扫描数据表的信息：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0107/221720_aebad687_403814.png "日志输出.png")

由于仅包含后端接口服务，没有集成页面，你可以通过swagger来测试接口（http://localhost:8080/swagger-ui.html）：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0107/234946_a00dbcb8_403814.png "swagger截图.png")

> 一个简单的完整示例，请参考：[kduck-core示例项目](https://gitee.com/lhg317/kduck-example)。

## 关于查询构造器

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


 **我们会陆续完善该项目，有任何关于K-Duck框架问题可邮件至：lhg_0317@163.com** 