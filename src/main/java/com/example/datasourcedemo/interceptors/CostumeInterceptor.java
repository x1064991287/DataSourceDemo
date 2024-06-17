package com.example.datasourcedemo.interceptors;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义HTTP请求拦截器
 *
 * preHandle()方法通常用于身份验证和授权。如果返回false，请求将不会继续传递给控制器。
 * postHandle()方法可以在处理完请求后，修改ModelAndView对象。
 * afterCompletion()方法适用于资源清理工作，例如关闭资源或记录日志。
 * @author bxy
 * @date 2024/6/14 15:11:17
 */
public class CostumeInterceptor implements HandlerInterceptor {

    /**
     * 预处理回调方法，实现处理器的预处理（如检查登陆），第三个参数为响应的处理器，自定义Controller
     * 返回值：true表示继续流程（如调用下一个拦截器或处理器）；false表示流程中断（如登录检查失败），不会继续调用其他的拦截器或处理器，此时我们需要通过response来产生响应
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("preHandle");
        return true;
    }

    /**
     * 后处理回调方法，实现处理器（Controller）的后处理，但在渲染视图之前，此时我们可以通过modelAndView对模型数据进行处理或对视图进行处理
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("afterCompletion");
    }

    /**
     * 整个请求处理完毕回调方法，即在视图渲染完毕时回调，如性能监控中我们可以在此记录结束时间并输出消耗时间，还可以进行一些资源清理，类似于try-catch-finally中的finally，但仅调用处理器执行链中
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

        System.out.println("postHandle");
    }

}
