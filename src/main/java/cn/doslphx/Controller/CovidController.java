package cn.doslphx.Controller;

import cn.doslphx.utils.NettyClient;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/covid")
public class CovidController {

    @RequestMapping("/demo1")
    public String demo1(String date, Model model) {

        if (date == null || "".equals(date)) {
            date = "2020-1-21";
        }

        //动态加载数据中台数据
        String json = "";
        try {
            json = NettyClient.rmiCall("127.0.0.1", 50000, "/?type=days&filter=" + date);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //转换数据格式(Json反序列化==》JSONObject)
        String numbs = "";
        String coviddate = "";
        JSONObject json1 = new JSONObject();
        for (int i = 0; i < json1.size(); i++) {
            Object o = json1.get(i);
            JSONObject jsonObject = JSONObject.fromObject(o);

            //["确诊","新增确诊","疑似","新增疑似","治愈","新增治愈","死亡","新增死亡","重症","新增重症"]
            //[5, 20, 36, 10, 10, 20, 36, 10, 10, 20]
            numbs = String.format("[%s, %s, %s, %s, %s, %s, %s, %s, %s, %s]",
                    jsonObject.get("confirmed"),
                    jsonObject.get("newConfirmed"),
                    jsonObject.get("suspected"),
                    jsonObject.get("newSuspected"),
                    jsonObject.get("recovered"),
                    jsonObject.get("newRecovered"),
                    jsonObject.get("deaths"),
                    jsonObject.get("newDeaths"),
                    jsonObject.get("criticalConditions"),
                    jsonObject.get("newCriticalConditions")
            );

            coviddate = jsonObject.get("coviddate").toString();
        }


        //向View传递数据(thymeleaf视图的使用问题)
        model.addAttribute("coviddate", coviddate);
        model.addAttribute("numbs", numbs);

        return "covid/demo1";
    }


    @RequestMapping("/demo2")
    public String demo2(Model model) {
        //动态加载数据中台数据
        //http://127.0.0.1:50000/?type=days&filter=2020-1-21,2020-1-22,2020-1-23,2020-1-24,2020-1-25,2020-1-26&order=desc

        String json = "";
        try {
            json = NettyClient.rmiCall("127.0.0.1", 50000,
                    "/?type=days&filter=2020-1-23,2020-1-24,2020-1-25,2020-1-26&order=desc");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //转换数据格式(Json反序列化)
        //[
        //  {"confirmed":2744,"coviddate":"2020-1-26","criticalConditions":461,"daysId":6,"deaths":80,"newConfirmed":769,"newCriticalConditions":137,"newDeaths":24,"newRecovered":2,"newSuspected":3806,"recovered":51,"suspected":5794},
        //  {"confirmed":1975,"coviddate":"2020-1-25","criticalConditions":324,"daysId":5,"deaths":56,"newConfirmed":688,"newCriticalConditions":87,"newDeaths":15,"newRecovered":11,"newSuspected":1309,"recovered":49,"suspected":2684},
        //  {"confirmed":1287,"coviddate":"2020-1-24","criticalConditions":237,"daysId":4,"deaths":41,"newConfirmed":444,"newCriticalConditions":0,"newDeaths":16,"newRecovered":3,"newSuspected":1118,"recovered":38,"suspected":1965},
        //  {"confirmed":830,"coviddate":"2020-1-23","criticalConditions":0,"daysId":3,"deaths":0,"newConfirmed":259,"newCriticalConditions":177,"newDeaths":25,"newRecovered":34,"newSuspected":680,"recovered":0,"suspected":1072},
        //]
        //['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        //新增确诊 [320, 302, 301, 334, 390, 330, 320]
        //新增疑似 [120, 132, 101, 134, 90, 230, 210]
        //新增治愈 ...
        //新增死亡 ...
        //新增重症 ...
        String dateList = "[";
        String newConfirmed = "[";
        String newSuspected = "[";
        String newRecovered = "[";
        String newDeaths = "[";
        String newCriticalConditions = "[";
        JSONObject json2 = new JSONObject();
        for (int i = 0; i < json2.size(); i++) {
            Object o = json2.get(i);
            JSONObject jsonObject = JSONObject.fromObject(o);
            dateList += "'" + jsonObject.get("coviddate") + "',";
            newConfirmed += jsonObject.get("newConfirmed") + ",";
            newSuspected += jsonObject.get("newSuspected") + ",";
            newRecovered += jsonObject.get("newRecovered") + ",";
            newDeaths += jsonObject.get("newDeaths") + ",";
            newCriticalConditions += jsonObject.get("newCriticalConditions") + ",";
        }
        dateList += "]";
        newConfirmed += "]";
        newSuspected += "]";
        newRecovered += "]";
        newDeaths += "]";
        newCriticalConditions += "]";


        //向View传递数据
        model.addAttribute("dateList", dateList);
        model.addAttribute("newConfirmed", newConfirmed);
        model.addAttribute("newSuspected", newSuspected);
        model.addAttribute("newRecovered", newRecovered);
        model.addAttribute("newDeaths", newDeaths);
        model.addAttribute("newCriticalConditions", newCriticalConditions);

        return "covid/demo2";
    }
}
