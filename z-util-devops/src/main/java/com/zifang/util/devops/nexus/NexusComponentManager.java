package com.zifang.util.devops.nexus;

import com.zifang.util.json.JsonUtil;
import com.zifang.util.json.define.TypeReference;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nexus组件管理器
 * <p>
 * 提供Nexus仓库组件的搜索、查询、删除等管理功能，
 * 支持GAV坐标查询和组件存在性检查。
 *
 * @author zifang
 * @version 1.0.0
 */
public class NexusComponentManager {

    public static final String respository = "maven-releases";
    /**
     * nexus访问地址
     */
    private static final String NEXUS_URL = "http://nexus.cfuture.cc";
    /**
     * nexus账号
     */
    private static final String NEXUS_USERNAME = "admin";
    /**
     * nexus密码
     */
    private static final String NEXUS_PASSWORD = "admin@123";
    /**
     * 成功删除的组件数量
     */
    private int successDeleteTotal;

    /**
     * 复用的 OkHttp 客户端，OkHttp 推荐全局共用同一实例（内部已做连接池与线程池管理）。
     */
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    /**
     * 构造带 Basic Auth 头的 Request.Builder。
     */
    private static Request.Builder authRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("Authorization", Credentials.basic(NEXUS_USERNAME, NEXUS_PASSWORD));
    }

    /**
     * 搜索组件
     *
     * @param repository 仓库名称
     * @return
     */
    public static List<Component> search(String repository) {
        final List<Component> list = new ArrayList<>();
        search(repository, list, null);
        return list;
    }

    /**
     * 搜索组件
     * <p>
     * 搜索组件时API会对匹配结果进行分页，所以这里采用递归搜索
     *
     * @param repository 仓库名称
     * @param list       存放查询结果的集合
     * @param token      token，由上一页查询结果中获取，第一次查询传null
     */
    private static void search(String repository, List<Component> list, String token) {
        String url = String.format("%s/service/rest/v1/search?repository=%s", NEXUS_URL, repository);
        if (token != null) {
            url += "&continuationToken=" + token;
        }
        try (Response response = HTTP_CLIENT.newCall(authRequest(url).get().build()).execute()) {
            int status = response.code();
            String body = response.body() != null ? response.body().string() : "";
            if (status == 200) {
                JsonObject jsonObject = JsonUtil.parseObject(body);
                JsonArray itemsArray = jsonObject.getJsonArray("items");
                List<Component> temp = JsonUtil.fromJson(itemsArray.toString(), new TypeReference<List<Component>>() {});
                list.addAll(temp);
                if (jsonObject.get("continuationToken") != null) {
                    token = String.valueOf(jsonObject.get("continuationToken"));
                    //search(repository, groupId, artifactId, list, token);
                }
            } else {
                System.out.println(String.format("组件搜索出错，http响应代码：%d，错误信息：%s", status, response.message()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("组件搜索出错，IO 异常：" + e.getMessage());
        }
    }

    /**
     * 删除组件
     *
     * @param component 组件对象
     */
    public void delete(Component component) {
        String url = String.format("%s/service/rest/v1/components/%s", NEXUS_URL, component.getId());
        try (Response response = HTTP_CLIENT.newCall(authRequest(url).delete().build()).execute()) {
            int status = response.code();
            if (status == 204) {
                successDeleteTotal++;
            } else {
                System.out.println(String.format("组件【%s-%s-%s】删除失败，http响应代码：%d",
                        component.getRepository(), component.getGroup(), component.getName(), component.getVersion(), status));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(String.format("组件【%s-%s-%s】删除失败，IO 异常：%s",
                    component.getRepository(), component.getGroup(), component.getName(), component.getVersion(), e.getMessage()));
        }
    }

    /**
     * 根据 GAV 坐标查找组件
     *
     * @param groupId    组ID
     * @param artifactId 构件ID
     * @param version    版本号
     * @return 匹配的组件对象，未找到返回 null
     */
    public Component findByGav(String groupId, String artifactId, String version) {

        List<Component> list = search(respository);

        List<Component> filted = list.stream()
                .filter(e -> e.getGroup().equals(groupId) && e.getName().equals(artifactId) && e.getVersion().equals(version))
                .collect(Collectors.toList());

        if (filted.size() == 1) {
            return filted.get(0);
        }
        return null;
    }

    /**
     * 检查指定 GAV 坐标的组件是否存在
     *
     * @param groupId    组ID
     * @param artifactId 构件ID
     * @param version    版本号
     * @return 存在返回 true，不存在返回 false
     */
    public Boolean checkExistGav(String groupId, String artifactId, String version) {

        List<Component> list = search(respository);

        System.out.print(list);

        List<Component> filted = list.stream()
                .filter(e -> e.getGroup().equals(groupId) && e.getName().equals(artifactId) && e.getVersion().equals(version))
                .collect(Collectors.toList());
        if (filted.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

}