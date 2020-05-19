package cn.doslphx.Controller;

import cn.doslphx.utils.NettyClient;
import cn.doslphx.utils.NettyClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@EnableAutoConfiguration
public class IndexController {

    //第一个springboot网页（url）
    @RequestMapping("/index")
    public String index(HttpSession session){
        //默认Controller类方法的返回值为：视图名

        //判断是否是合法用户
        if(session.getAttribute("username")==null){
            return "redirect:login";
        }

        return "index";
    }

    @RequestMapping(path="/login", method = RequestMethod.GET)
    public String login(){
        return "login";
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String loginPost(String username, String userpwd, HttpSession session){
        //数据采集
        //System.out.println(username);
        //System.out.println(userpwd);

        //数据验证、身份验证
        String result = "";
        try {
            String dictate = String.format("/?type=authorize&username=%s&userpwd=%s", username, userpwd);
            result = NettyClient.rmiCall("127.0.0.1", 50000, dictate);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //if("admin".equals(username) && "admin888".equals(userpwd)){
        //调用数据中台 身份认证
        if(result.equals("true")){
            //成功        跳转至 管理中心页
            //应记录身份认证后的状态（cookiess、session）
            session.setAttribute("username", username);
            session.setAttribute("loginDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            //session.setAttribute("loginIP", "");
            return "redirect:index";
        }else{
            //失败        再次加次login视图，提示其再次输入
            return "login";
        }
    }

    @RequestMapping("/logout")
    //@ResponseBody
    public String logout(HttpSession session){
        //如该请求，需要访问数据库，调用Model去完成数据操作
        //如该请求，需要提供界面，则调用View，去完成界面呈现。

        //清理工作要完成
        session.removeAttribute("username");
        session.removeAttribute("loginDate");

        //logout 无界面，立即跳转至登录页即可
        return "redirect:login";
    }

}
