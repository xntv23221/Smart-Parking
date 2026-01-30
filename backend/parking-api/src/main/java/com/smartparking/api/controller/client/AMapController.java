package com.smartparking.api.controller.client;

import com.smartparking.common.api.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 高德地图服务代理控制器
 * 提供IP定位、天气查询、地理编码等基础服务
 */
@RestController
@RequestMapping("/api/client/v1/map")
public class AMapController {

    private final RestTemplate restTemplate;
    // 用户提供的 Key
    private static final String AMAP_KEY = "***********"; // 高德地图web服务API密钥
    private static final String AMAP_BASE_URL = "https://restapi.amap.com/v3";

    public AMapController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * IP定位
     * @param ip 可选，不传则由高德自动识别请求IP
     */
    @GetMapping("/ip")
    public Result<Object> ipLocate(@RequestParam(name = "ip", required = false) String ip) {
        String url = AMAP_BASE_URL + "/ip?key=" + AMAP_KEY;
        if (ip != null && !ip.isEmpty()) {
            url += "&ip=" + ip;
        }
        Object response = restTemplate.getForObject(url, Object.class);
        return Result.ok(response);
    }

    /**
     * 天气查询
     * @param city 城市编码 (adcode)
     */
    @GetMapping("/weather")
    public Result<Object> weather(@RequestParam("city") String city) {
        String url = AMAP_BASE_URL + "/weather/weatherInfo?key=" + AMAP_KEY + "&city=" + city;
        Object response = restTemplate.getForObject(url, Object.class);
        return Result.ok(response);
    }

    /**
     * 地理编码
     * @param address 地址
     */
    @GetMapping("/geocode")
    public Result<Object> geocode(@RequestParam("address") String address, @RequestParam(name = "city", defaultValue = "北京") String city) {
        String url = AMAP_BASE_URL + "/geocode/geo?key=" + AMAP_KEY + "&address=" + address + "&city=" + city;
        Object response = restTemplate.getForObject(url, Object.class);
        return Result.ok(response);
    }

    /**
     * 逆地理编码
     * @param location 经纬度 "lon,lat"
     */
    @GetMapping("/regeocode")
    public Result<Object> regeocode(@RequestParam("location") String location) {
        String url = AMAP_BASE_URL + "/geocode/regeo?key=" + AMAP_KEY + "&location=" + location;
        Object response = restTemplate.getForObject(url, Object.class);
        return Result.ok(response);
    }

    /**
     * 驾车路径规划
     * @param origin 起点 "lon,lat"
     * @param destination 终点 "lon,lat"
     * @param strategy 策略 0~9
     */
    @GetMapping("/direction/driving")
    public Result<Object> driving(
            @RequestParam("origin") String origin,
            @RequestParam("destination") String destination,
            @RequestParam(name = "strategy", defaultValue = "0") Integer strategy
    ) {
        String url = AMAP_BASE_URL + "/direction/driving?key=" + AMAP_KEY + 
                     "&origin=" + origin + 
                     "&destination=" + destination + 
                     "&strategy=" + strategy + 
                     "&extensions=all";
        Object response = restTemplate.getForObject(url, Object.class);
        return Result.ok(response);
    }

    /**
     * 关键字搜索
     * @param keywords 关键字
     * @param city 城市
     */
    @GetMapping("/place/text")
    public Result<Object> placeText(@RequestParam("keywords") String keywords, @RequestParam(name = "city", required = false) String city) {
        String url = AMAP_BASE_URL + "/place/text?key=" + AMAP_KEY + "&keywords=" + keywords;
        if (city != null) {
            url += "&city=" + city;
        }
        Object response = restTemplate.getForObject(url, Object.class);
        return Result.ok(response);
    }

    /**
     * 周边搜索
     * @param location 中心点 "lon,lat"
     * @param keywords 关键字
     * @param radius 半径（米）
     */
    @GetMapping("/place/around")
    public Result<Object> placeAround(@RequestParam("location") String location, @RequestParam("keywords") String keywords, @RequestParam(name = "radius", defaultValue = "2000") Integer radius) {
        String url = AMAP_BASE_URL + "/place/around?key=" + AMAP_KEY + "&location=" + location + "&keywords=" + keywords + "&radius=" + radius;
        Object response = restTemplate.getForObject(url, Object.class);
        return Result.ok(response);
    }

    /**
     * 输入提示
     * @param keywords 关键字
     * @param city 城市
     */
    @GetMapping("/assistant/inputtips")
    public Result<Object> inputTips(@RequestParam("keywords") String keywords, @RequestParam(name = "city", required = false) String city) {
        String url = AMAP_BASE_URL + "/assistant/inputtips?key=" + AMAP_KEY + "&keywords=" + keywords;
        if (city != null) {
            url += "&city=" + city;
        }
        Object response = restTemplate.getForObject(url, Object.class);
        return Result.ok(response);
    }
}
