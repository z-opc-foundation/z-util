package com.zifang.util.devops.git.github.user;

import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub User API 封装
 * <p>
 * 提供GitHub用户相关操作的封装，
 * 包括获取用户信息、仓库列表、Star列表、关注者管理等功能。
 *
 * @author zifang
 * @version 1.0.0
 */
public class UserApiWrapper {

    private final GitHub github;

    /**
     * UserApiWrapper方法。
     * * @param github GitHub类型参数
     */
    public UserApiWrapper(GitHub github) {
        this.github = github;
    }

    /**
     * 获取当前认证用户
     */
    public GHUser me() throws IOException {
        return github.getMyself();
    }

    /**
     * 获取当前用户名
     */
    public String myLogin() throws IOException {
        return github.getMyself().getLogin();
    }

    /**
     * 获取用户信息
     */
    public GHUser get(String username) throws IOException {
        return github.getUser(username);
    }

    /**
     * 列出用户的仓库
     */
    public List<org.kohsuke.github.GHRepository> listRepos(String username) throws IOException {
        List<org.kohsuke.github.GHRepository> repos = new ArrayList<>();
        for (org.kohsuke.github.GHRepository r : github.getUser(username).listRepositories(100)) {
            repos.add(r);
        }
        return repos;
    }

    /**
     * 列出用户关注的仓库（Starred）
     */
    public List<org.kohsuke.github.GHRepository> listStarred(String username) throws IOException {
        List<org.kohsuke.github.GHRepository> repos = new ArrayList<>();
        for (org.kohsuke.github.GHRepository r : github.getUser(username).listStarredRepositories()) {
            repos.add(r);
        }
        return repos;
    }

    /**
     * 列出用户的 followers
     */
    public List<GHUser> listFollowers(String username) throws IOException {
        List<GHUser> users = new ArrayList<>();
        PagedIterator<GHUser> it = github.getUser(username).listFollowers().iterator();
        while (it.hasNext()) {
            users.add(it.next());
        }
        return users;
    }

    /**
     * 列出用户 following
     */
    public List<GHUser> listFollowing(String username) throws IOException {
        List<GHUser> users = new ArrayList<>();
        for (GHUser u : github.getUser(username).listFollows()) {
            users.add(u);
        }
        return users;
    }

    /**
     * 关注用户
     */
    public void follow(String username) throws IOException {
        github.getUser(username).follow();
    }

    /**
     * 取消关注用户
     */
    public void unfollow(String username) throws IOException {
        github.getUser(username).unfollow();
    }

    /**
     * GitHub 用户信息 DTO
     * <p>
     * 用于封装用户的基本信息，包括登录名、姓名、邮箱、简介等
     */
    public static class UserInfo {
        public String login;
        public String name;
        public String email;
        public String bio;
        public String location;
        public String company;
        public int followers;
        public int following;
        public int publicRepos;

        /**
         * 从 GHUser 对象构建 UserInfo
         *
         * @param user GitHub 用户对象
         * @return UserInfo 实例
         * @throws IOException IO异常
         */
        public static UserInfo from(GHUser user) throws IOException {
            UserInfo info = new UserInfo();
            info.login = user.getLogin();
            info.name = user.getName();
            info.email = user.getEmail();
            info.bio = user.getBio();
            info.location = user.getLocation();
            info.company = user.getCompany();
            info.followers = user.getFollowersCount();
            info.following = user.getFollowingCount();
            info.publicRepos = user.getPublicRepoCount();
            return info;
        }
    }
}
