package com.zifang.util.http;

import com.zifang.util.http.base.define.*;

@RestController("http://localhost:8011/test")
/**
 * MockRequest接口。
 */
public interface MockRequest {

    @RequestMapping(value = "/test1", method = RequestMethod.GET)
    String test1(@RequestParam("name") String name, @RequestParam("password") String password);

    @RequestMapping(value = "/test2", method = RequestMethod.POST)
    String test2(@RequestBody Data data);

    @RequestLine(url = "/test3", requestMethod = RequestMethod.GET)
    @RequestHeaders({
            @RequestHeader(key = "", value = ""),
            @RequestHeader(key = "", value = ""),
            @RequestHeader(key = "", value = "")
    })
    @CookieValue()
    @BasicAuth(userName = "aa", password = "cc")
    String test3(@RequestBody Data data);

}

