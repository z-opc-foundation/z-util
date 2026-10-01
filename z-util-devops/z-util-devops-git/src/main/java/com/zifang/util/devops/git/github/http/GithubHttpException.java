package com.zifang.util.devops.git.github.http;

import java.io.IOException;

/**
 * GitHub REST 调用层面的 IO 异常。语义与原 github-api 的 {@code IOException} 对齐。
 */
public class GithubHttpException extends IOException {

    public GithubHttpException(String message) {
        super(message);
    }

    public GithubHttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
