package com.zifang.util.devops.git.github.release;

import org.kohsuke.github.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub Release API 封装
 * <p>
 * 提供GitHub Release相关操作的封装，
 * 包括Release的创建、更新、删除、查询，以及Asset的上传和管理。
 *
 * @author zifang
 * @version 1.0.0
 */
public class ReleaseApiWrapper {

    private final GitHub github;
    private String owner;
    private String repo;

    /**
     * ReleaseApiWrapper方法。
     * * @param github GitHub类型参数
     */
    public ReleaseApiWrapper(GitHub github) {
        this.github = github;
    }

    /**
     * ReleaseApiWrapper方法。
     * * @param github GitHub类型参数
     *
     * @param owner String类型参数
     * @param repo  String类型参数
     */
    public ReleaseApiWrapper(GitHub github, String owner, String repo) {
        this.github = github;
        this.owner = owner;
        this.repo = repo;
    }

    /**
     * withRepo方法。
     * * @param owner String类型参数
     *
     * @param repo String类型参数
     * @return ReleaseApiWrapper类型返回值
     */
    public ReleaseApiWrapper withRepo(String owner, String repo) {
        this.owner = owner;
        this.repo = repo;
        return this;
    }

    private String fullName() {
        return owner + "/" + repo;
    }

    private GHRepository getRepo() throws IOException {
        return github.getRepository(fullName());
    }

    // ==================== CRUD ====================

    /**
     * 创建 Release
     */
    public GHRelease create(String tagName, String name, String body) throws IOException {
        return getRepo().createRelease(tagName)
                .name(name)
                .body(body)
                .create();
    }

    /**
     * 创建 Draft Release
     */
    public GHRelease createDraft(String tagName, String name, String body) throws IOException {
        return getRepo().createRelease(tagName)
                .name(name)
                .body(body)
                .draft(true)
                .create();
    }

    /**
     * 获取 Release
     */
    public GHRelease get(long releaseId) throws IOException {
        return getRepo().getRelease(releaseId);
    }

    /**
     * 通过标签名获取 Release
     */
    public GHRelease getByTag(String tag) throws IOException {
        return getRepo().getReleaseByTagName(tag);
    }

    /**
     * 更新 Release
     */
    public GHRelease update(long releaseId, String name, String body, boolean draft, boolean prerelease) throws IOException {
        GHReleaseUpdater updater = get(releaseId).update();
        if (name != null) updater.name(name);
        if (body != null) updater.body(body);
        updater.draft(draft);
        updater.prerelease(prerelease);
        return updater.update();
    }

    /**
     * 删除 Release
     */
    public void delete(long releaseId) throws IOException {
        get(releaseId).delete();
    }

    // ==================== List ====================

    /**
     * 列出所有 Release
     */
    public List<GHRelease> list() throws IOException {
        List<GHRelease> releases = new ArrayList<>();
        PagedIterator<GHRelease> it = getRepo().listReleases().iterator();
        while (it.hasNext()) {
            releases.add(it.next());
        }
        return releases;
    }

    // ==================== Asset ====================

    /**
     * 上传 Release Asset（通过 File）
     */
    public GHAsset uploadAsset(long releaseId, File file, String mimeType) throws IOException {
        return get(releaseId).uploadAsset(file, mimeType);
    }

    /**
     * 上传 Release Asset（通过 InputStream）
     */
    public GHAsset uploadAsset(long releaseId, String fileName, InputStream data, String mimeType) throws IOException {
        return get(releaseId).uploadAsset(fileName, data, mimeType);
    }

    /**
     * 列出 Release 的所有 Asset
     */
    public List<GHAsset> listAssets(long releaseId) throws IOException {
        return get(releaseId).getAssets();
    }

    // ==================== Latest ====================

    /**
     * 获取最新的 Release
     */
    public GHRelease getLatest() throws IOException {
        return getRepo().getLatestRelease();
    }

    // ==================== DTO ====================

    /**
     * Release 信息 DTO
     * <p>
     * 用于封装 Release 的基本信息，包括标签名、标题、正文、是否草稿等
     */
    public static class ReleaseInfo {
        public long id;
        public String tagName;
        public String name;
        public String body;
        public boolean draft;
        public boolean prerelease;
        public String htmlUrl;
        public String createdAt;

        /**
         * 从 GHRelease 对象构建 ReleaseInfo
         *
         * @param r Release 对象
         * @return ReleaseInfo 实例
         */
        public static ReleaseInfo from(GHRelease r) {
            ReleaseInfo info = new ReleaseInfo();
            info.id = r.getId();
            info.tagName = r.getTagName();
            info.name = r.getName();
            info.body = r.getBody();
            info.draft = r.isDraft();
            info.prerelease = r.isPrerelease();
            info.htmlUrl = r.getHtmlUrl().toString();
            info.createdAt = r.getPublished_at() != null ? r.getPublished_at().toString() : null;
            return info;
        }
    }
}
