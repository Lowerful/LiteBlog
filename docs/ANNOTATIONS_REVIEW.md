# 注解复盘：截至第 3 步项目里出现过的所有注解

> 阶段性复盘文档。核心结论先放在最前面：
>
> **注解本身没有任何行为。它只是一个贴在代码上的标签，所有"魔法"都来自"读取标签的人"。**
> 按读取时机分，本项目用到的注解只属于两个世界：**编译期**（读完就消失）和**运行时**（框架反射读取，决定框架行为）。

---

## 一、编译期注解：字节码生成后，注解本身消失

这一类注解由**注解处理器（Annotation Processor）**在编译阶段读取，产出物是生成好的代码或编译检查。运行时你去反射搜它们，是搜不到的。

### Lombok 全家（编译期生成代码）

| 注解 | 使用位置 | 干什么 |
|---|---|---|
| `@Data` | Result、User、各 DTO（6 处） | 组合注解 = `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` + `@RequiredArgsConstructor`。一行顶五行的样板代码 |
| `@Builder` | UserVO、LoginResponse | 生成建造者模式：`UserVO.builder().id(1L).nickname("a").build()`。字段多时比一长串构造参数可读 |
| `@RequiredArgsConstructor` | 7 个类 | 为所有 `final` 字段生成构造器。**我们构造器注入的全部秘密在这**：字段声明成 `private final AuthService authService`，Lombok 生成唯一构造器，Spring 自动用它注入——所以整个项目看不到一个 `@Autowired` |
| `@Slf4j` | GlobalExceptionHandler、AuthServiceImpl | 生成 `private static final Logger log` 字段，直接 `log.error(...)` |

### 编译器检查（不算框架注解，顺带收录）

| 注解 | 作用 |
|---|---|
| `@Override` | 编译器检查：标了它就必须真的重写父类/接口方法，拼错方法名直接编译失败。纯安全网，无运行时行为 |

**为什么这一类"看起来像简化"**：因为它们确实只是简化——删掉它们，手写等价代码，程序行为分毫不变。这正是你说的"更像对某个模块的简化"的那部分。

---

## 二、运行时注解：框架用反射读取，决定框架行为

这一类注解是**框架的输入**：删掉任何一个，行为都会真的改变。它们由框架在启动或请求处理时通过反射读取。按框架分组：

### 1. Spring 装配（决定"谁被创建、谁注入给谁"）

| 注解 | 使用位置 | 干什么 |
|---|---|---|
| `@SpringBootApplication` | BlogApplication | 组合注解 = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`。三个字面意思：我是配置类 / 开启按依赖自动装配 / 扫描本包及子包下的组件 |
| `@Configuration` | PasswordConfig、WebMvcConfig | 声明"这个类是配置类，里面的 `@Bean` 方法产出交给容器管理" |
| `@Bean` | PasswordConfig#passwordEncoder | 方法级注册。用在**第三方库的类**上（BCrypt 的实现类我们改不了源码，没法贴 `@Component`），只能自己写个方法 new 出来上交 |
| `@Component` | JwtUtil、LoginInterceptor | 通用组件：启动时被组件扫描发现，注册为容器 Bean |
| `@Service` | AuthServiceImpl、UserServiceImpl | **语义化的 `@Component`**，行为完全一样。价值在于读代码的人一眼知道这是业务层，也为将来按层做 AOP 留了钩子 |
| `@Value` | JwtUtil 构造器参数 | 注入配置文件的值，支持默认值写法 `${jwt.expire-hours:72}` |
| `@Autowired` | 仅 UserMapperTest | 按类型注入。主代码用构造器注入替代了它（可测性好、能声明 final）；测试类里写它无妨 |

### 2. Spring MVC（决定"HTTP 请求怎么路由、参数怎么绑"）

| 注解 | 使用位置 | 干什么 |
|---|---|---|
| `@RestController` | 3 个 Controller | 组合注解 = `@Controller` + `@ResponseBody`：每个方法返回值直接序列化成 JSON 写进响应体（而不是跳转页面） |
| `@RequestMapping` | 类级 + 方法级 | 定义 URL 映射。类上定前缀 `/api/auth`，方法上定具体路径 |
| `@GetMapping` / `@PostMapping` | 各接口 | `@RequestMapping(method=GET)` 的语义化缩写，把"方法写错成 POST"这类错误提前到注解层面 |
| `@RequestBody` | register、login | 把请求体的 JSON 反序列化成 DTO 对象 |
| `@RestControllerAdvice` | GlobalExceptionHandler | "所有 `@RestController` 的附加增强"：让里面的 `@ExceptionHandler` 对全局生效 |
| `@ExceptionHandler` | 5 个方法 | 声明"这类异常归我处理"，返回值同样按 JSON 写出 |

（`@RequestParam` / `@PathVariable`：讲解中出现过，第 4 步文章列表/详情接口将首次实战。）

### 3. Jakarta Validation（声明规则，框架执行）

| 注解 | 使用位置 | 干什么 |
|---|---|---|
| `@Valid` | Controller 参数前 | **开关**：触发对这个 DTO 的级联校验。没有它，字段上的规则注解全是摆设 |
| `@NotBlank` | RegisterRequest.username | 非空且不止空格 |
| `@Pattern` | RegisterRequest.username | 正则：`^[a-zA-Z0-9_]{4,20}$` |
| `@Size` | RegisterRequest 字段 | 长度区间 |

注意分工：**规则注解是"声明"，`@Valid` 是"开关"，Hibernate Validator 是"执行者"，`GlobalExceptionHandler` 是"翻译官"**。四者缺一不可，但注解本身只负责把规则写在离字段最近的地方。

### 4. MyBatis（把接口方法绑定到 SQL）

| 注解 | 使用位置 | 干什么 |
|---|---|---|
| `@Mapper` | UserMapper | 告诉 MyBatis：为这个接口生成动态代理、注册为 Bean、去 XML 里找同名 statement |
| `@Param` | UserMapper 方法参数 | 给 SQL 占位符命名。单参数可省，多参数必须——否则 XML 里只能用 `param1`/`param2` 这种无名氏 |

### 5. 测试框架（JUnit 5 + Mockito + Spring Test）

| 注解 | 使用位置 | 干什么 |
|---|---|---|
| `@Test` | 所有测试方法 | JUnit 引擎通过反射找到它，才知道"这是要执行的用例" |
| `@DisplayName` | 测试方法 | 用人类可读的中文描述用例，测试报告里显示它而不是方法名 |
| `@SpringBootTest` | UserMapperTest | 起**完整容器**（连数据库、初始化全部 Bean）——集成测试专用，慢（3.6 秒） |
| `@ExtendWith(MockitoExtension.class)` | AuthServiceTest | 挂载 Mockito 扩展，启用 `@Mock` |
| `@Mock` | AuthServiceTest | 生成假对象注入被测类——**不起容器**，单测 0.7 秒 |
| `@BeforeEach` | 两个测试类 | 每个用例执行前跑一次（重置状态） |

---

## 三、两个值得记住的元规律

**1. 组合注解（meta-annotation）是"注解上贴注解"。**
`@SpringBootApplication`、`@RestController`、`@Data` 都是组合注解——一个注解的定义上贴着别的注解，框架读到外层时等于读到全部内层。这也是你自己做"项目内自定义注解"的方式：比如将来想要 `@RequireLogin`，就是组合 `@Component` + 拦截器逻辑。看到陌生的组合注解，先问"它等于哪几个注解的和"。

**2. 判断一个注解"值不值得理解到原理层"，看删掉它会发生什么。**
- 删掉 `@Data` → 编译报错（方法没了）——但可以手写补回，行为不变 → **糖**
- 删掉 `@RestController` → 接口 404，行为真的变了 → **框架输入**
- 删掉 `@Valid` → 校验静默失效，脏数据进库 → **框架输入，且失效方式隐蔽**（最危险的一类）

---

## 四、下一阶段预告

第 4 步（文章模块）将首次出现：`@RequestParam`（分页参数）、`@PathVariable`（路径变量）、以及自定义组合注解的讨论。到第 6 步会出现 `@Transactional`（事务）和 `@Cacheable`（缓存）——它们属于运行时注解里最值得深挖的一类，因为涉及动态代理。
