# github-api → okhttp 迁移路线图

> z-util-devops-git 切换 GitHub REST 入口的工程级迁接表。
>
> **状态**：v0 切片已落地（commit 编号待补），3 个端点真跑 okhttp，其余端点 throw
> `UnsupportedOperationException` + TODO 备注。未来会话按本表逐项填实。

---

## 一、动机

`org.kohsuke:github-api`（hub4j）1.330 拖入的传递依赖树里，`commons-lang3 / okio / commons-codec / commons-io / jcabi-github / commons-compress` … 等 20+ 个 jar 长期位于 Dependabot HIGH 区。仓库**实测全 workspace 零代码消费方**（2026-10-01 扫 `com.zifang.util.devops` import 0 命中）。GitHub REST 本质只是「OAuth 头 + HTTP 调用 + JSON 解析」，本仓已有 okhttp + z-util-parser-json，自己实现一层更可控。

---

## 二、v0 已落地的端点（2026-10-01 commit）

| Wrapper | 方法 | HTTP 路径 | 响应字段映射 |
|---------|------|-----------|--------------|
| `RepositoryApiWrapper` | `get(owner, repo)` / `get()` | `GET /repos/{owner}/{repo}` | `Repository.fromJson(o)` |
| `RepositoryApiWrapper` | `listBranches()` | `GET /repos/{owner}/{repo}/branches` | `Branch.fromJson(o).getName()` |
| `UserApiWrapper` | `getCurrentUser()` | `GET /user` | `User.fromJson(o)` |

地基：`com.zifang.util.devops.git.github.http.GithubHttpClient`
- 构造：`(baseUrl, token)` —— Enterprise 走 `config.getApiUrl()`
- 鉴权：`Authorization: token {token}` 拦截器；公共头 `Accept: application/vnd.github+json` + `X-GitHub-Api-Version: 2022-11-28`
- 三个高层 method：`getJsonObject(path)` / `getJsonArray(path)` / `postJson(path, body)`
- 非 2xx 一律抛 `GithubHttpException`（继承 `IOException`，与原 github-api 语义对齐）
- 超时：连接 10s / 读取 30s

POJO（`com.zifang.util.devops.git.github.model`）：`Repository` / `User` / `Branch`
—— 仅覆盖已实跑端点字段；其它字段按下面表格逐步补全。

---

## 三、续接清单（按 wrapper 分组）

状态符号：✅ v0 已实跑  | 🟡 待实现（throw + TODO） | ⚪ 不计划（功能不在本仓库）

### §repo — `RepositoryApiWrapper`

| 方法 | HTTP | 请求体 | 响应 | 状态 | 备注 |
|------|------|--------|------|------|------|
| `get(owner, repo)` / `get()` | `GET /repos/{owner}/{repo}` | — | `Repository` | ✅ | v0 |
| `listBranches()` | `GET /repos/{owner}/{repo}/branches` | — | `List<String>` (name) | ✅ | v0 |
| `create(name, desc, isPrivate)` | `POST /user/repos` | `{name,description,private}` | `Repository` | 🟡 | |
| `createOrgRepo(org, name, desc, isPrivate)` | `POST /orgs/{org}/repos` | 同上 | `Repository` | 🟡 | |
| `delete(owner, repo)` / `delete()` | `DELETE /repos/{owner}/{repo}` | — | — | 🟡 | |
| `listUserRepos(username)` | `GET /users/{username}/repos?per_page=100` | — | `List<Repository>` | 🟡 | |
| `listMyRepos()` | `GET /user/repos?per_page=100` | — | `List<Repository>` | 🟡 | |
| `fork()` | `POST /repos/{owner}/{repo}/forks` | — | `Repository` | 🟡 | |
| `listForks()` | `GET /repos/{owner}/{repo}/forks` | — | `List<Repository>` | 🟡 | |
| `listStargazers()` | `GET /repos/{owner}/{repo}/stargazers` | — | `List<User>` | 🟡 | |
| `search(keyword)` / `search(keyword, lang)` | `GET /search/repositories?q=...+language:...` | — | `List<Repository>` | 🟡 | |

### §user — `UserApiWrapper`

| 方法 | HTTP | 状态 |
|------|------|------|
| `getCurrentUser()` | `GET /user` | ✅ |
| `get(login)` | `GET /users/{login}` | 🟡 |
| `listEmails()` | `GET /user/emails` | 🟡 |
| `listFollowers(login)` | `GET /users/{login}/followers` | 🟡 |
| `listFollowing(login)` | `GET /users/{login}/following` | 🟡 |

### §issue — `IssueApiWrapper`

| 方法 | HTTP | 状态 |
|------|------|------|
| `list(state)` | `GET /repos/{owner}/{repo}/issues?state=...` | 🟡 |
| `get(number)` | `GET /repos/{owner}/{repo}/issues/{number}` | 🟡 |
| `create(title, body)` | `POST /repos/{owner}/{repo}/issues` | 🟡 |
| `update(number, title, body)` | `PATCH /repos/{owner}/{repo}/issues/{number}` | 🟡 |
| `close(number)` / `reopen(number)` | `PATCH` with `state: closed/open` | 🟡 |
| `listComments(number)` | `GET /repos/{owner}/{repo}/issues/{number}/comments` | 🟡 |
| `addComment(number, body)` | `POST /repos/{owner}/{repo}/issues/{number}/comments` | 🟡 |

### §pr — `PullRequestApiWrapper`

| 方法 | HTTP | 状态 |
|------|------|------|
| `list(state)` | `GET /repos/{owner}/{repo}/pulls?state=...` | 🟡 |
| `get(number)` | `GET /repos/{owner}/{repo}/pulls/{number}` | 🟡 |
| `create(title, head, base)` | `POST /repos/{owner}/{repo}/pulls` | 🟡 |
| `merge(number, msg)` | `PUT /repos/{owner}/{repo}/pulls/{number}/merge` | 🟡 |
| `close(number)` | `PATCH` with `state: closed` | 🟡 |
| `listReviews(number)` | `GET /repos/{owner}/{repo}/pulls/{number}/reviews` | 🟡 |
| `submitReview(number, body, state)` | `POST .../reviews` | 🟡 |
| `listFiles(number)` | `GET /repos/{owner}/{repo}/pulls/{number}/files` | 🟡 |

### §release — `ReleaseApiWrapper`

| 方法 | HTTP | 状态 |
|------|------|------|
| `list()` | `GET /repos/{owner}/{repo}/releases` | 🟡 |
| `getLatest()` | `GET /repos/{owner}/{repo}/releases/latest` | 🟡 |
| `getByTag(tag)` | `GET /repos/{owner}/{repo}/releases/tags/{tag}` | 🟡 |
| `create(tag, name, body)` | `POST /repos/{owner}/{repo}/releases` | 🟡 |
| `delete(id)` | `DELETE /repos/{owner}/{repo}/releases/{id}` | 🟡 |

### §org — `OrganizationApiWrapper`

| 方法 | HTTP | 状态 |
|------|------|------|
| `get(org)` / `get()` | `GET /orgs/{org}` | 🟡 |
| `listMembers()` | `GET /orgs/{org}/members` | 🟡 |
| `listRepos()` | `GET /orgs/{org}/repos` | 🟡 |
| `listTeams()` | `GET /orgs/{org}/teams` | 🟡 |

### §action — `ActionApiWrapper`

| 方法 | HTTP | 状态 |
|------|------|------|
| `listWorkflows()` | `GET /repos/{owner}/{repo}/actions/workflows` | 🟡 |
| `listWorkflowRuns(workflowId)` | `GET /repos/{owner}/{repo}/actions/workflows/{id}/runs` | 🟡 |
| `listArtifacts(runId)` | `GET /repos/{owner}/{repo}/actions/runs/{id}/artifacts` | 🟡 |
| `listJobs(runId)` | `GET /repos/{owner}/{repo}/actions/runs/{id}/jobs` | 🟡 |

---

## 四、如何续接一个新端点

例：在 `RepositoryApiWrapper` 加 `listIssues(state)` 端点。

1. 打开 `RepositoryApiWrapper.java`，删掉对应的 `throw notImpl(...)` 方法
2. 写新方法体——按 v0 切片的方式调 `client.getJsonArray(path)`，再循环把 `JsonObject` 转 POJO（必要时先在 `model/` 包加字段）
3. 若响应里有新的领域字段，先扩 `model/Repository.java`（或加新 POJO），再 `fromJson` 反序列化
4. 给方法加个 `@Test` 用例（优先用 `MockWebServer` 或手写一个最小 JSON fixture；不要真打 GitHub）
5. `mvn -pl z-util-devops/z-util-devops-git test` 闸
6. 在本表状态列把 🟡 改成 ✅

---

## 五、未来可能升级的非功能项

- 分页：GitHub 默认 30/页，最大 100。`GithubHttpClient` 现 v0 **不做自动翻页**，单页结果够大多数场景；要全量时 caller 自己循环 `?page=N`
- 速率限制：`X-RateLimit-Remaining` 头未拦截。生产场景需要：(1) 监控 + 自定义异常 / (2) 429 时按 `Retry-After` 退避
- Enterprise 端点：`config.getApiUrl()` 已经支持，但各 wrapper 内的路径拼接硬编码 `/repos/...` —— 切 Enterprise 时若路径不同需另写
- Webhook 验签 / 推送事件接收：v0 不做

---

## 六、与 z-util-devops-git 其它面的关系

- 复用 `z-util-parser-json` 的 `JsonObject` / `JsonArray`，不引新 JSON 库
- 复用 `okhttp3` —— 本仓 `z-util-devops-nexus` 已经在用同一版本（根 DM 管），零版本分叉
- 与 `git/operations/GitClient`（jgit 层）**职责分明**：jgit 管本地仓库 + 协议层 Git 操作；`github/` 包管 GitHub 平台 REST
